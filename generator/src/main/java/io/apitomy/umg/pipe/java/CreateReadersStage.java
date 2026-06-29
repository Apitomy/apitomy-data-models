package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;
import io.apitomy.umg.pipe.java.method.reader.ReadEntityPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadListPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadPrimitivePropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadRegexPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadStarPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionListPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionPropertyBlock;

/**
 * Creates the i/o reader classes.  There is a bespoke reader for each specification
 * version.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateReadersStage extends AbstractIOStage {

    @Override
    protected String getPackageName(SpecificationVersion specVersion) {
        return getReaderPackageName(specVersion);
    }

    @Override
    protected String getClassName(SpecificationVersion specVersion) {
        return getReaderClassName(specVersion);
    }

    @Override
    protected void addImports(JavaClassSource classSource) {
        classSource.addImport(getState().getConfig().getRootNamespace() + ".util.JsonUtil");
        classSource.addImport(getState().getConfig().getRootNamespace() + ".util.ReaderUtil");
    }

    @Override
    protected void addInterfaceImplementation(JavaClassSource classSource) {
        debug("Reader implements: " + getModelReaderInterfaceFQN());
        JavaInterfaceSource modelReaderInterfaceSource =
                getState().getJavaIndex().lookupInterface(getModelReaderInterfaceFQN());
        classSource.addImport(modelReaderInterfaceSource);
        classSource.addInterface(modelReaderInterfaceSource);
    }

    @Override
    protected void createEntityMethod(SpecificationVersion specVersion,
            JavaClassSource classSource, EntityModel entityModel) {
        String entityFQN = getJavaEntityInterfaceFQN(entityModel);
        String readMethodName = ReaderMethod.methodName(entityModel.getName());

        debug("Creating read method: " + readMethodName);

        JavaInterfaceSource javaEntity = getState().getJavaIndex().lookupInterface(entityFQN);
        if (javaEntity == null) {
            warn("Java interface for entity not found: " + entityFQN);
            return;
        }

        classSource.addImport(ObjectNode.class);
        classSource.addImport(javaEntity);
        MethodSource<JavaClassSource> methodSource = classSource.addMethod()
                .setName(readMethodName)
                .setReturnTypeVoid()
                .setPublic();
        methodSource.addParameter(ObjectNode.class.getSimpleName(), "json");
        methodSource.addParameter(javaEntity.getName(), "node");

        BodyBuilder body = new BodyBuilder();
        Collection<PropertyModelWithOrigin> allProperties =
                getState().getConceptIndex().getAllEntityProperties(entityModel);
        allProperties.forEach(property -> {
            body.clearContext();
            dispatchProperty(body, property, entityModel, classSource);
        });
        body.append("ReaderUtil.readExtraProperties(json, node);");

        methodSource.setBody(body.toString());
    }

    @Override
    protected void afterEntityMethods(SpecificationVersion specVersion, JavaClassSource classSource) {
        createTypeBasedReaderMethods(specVersion, classSource);
        createReadRootFromSpec(specVersion, classSource);
    }

    private void createTypeBasedReaderMethods(SpecificationVersion specVersion, JavaClassSource classSource) {
        var namespace = specVersion.getNamespace();

        getState().getConceptIndex().findTypes(namespace).stream()
                .filter(t -> t instanceof io.apitomy.umg.models.concept.type.UnionType)
                .map(t -> (io.apitomy.umg.models.concept.type.UnionType) t)
                .forEach(unionType -> {
                    new ReaderMethod(specVersion, unionType, ctx).writeTo(classSource);
                });
    }

    private void createReadRootFromSpec(SpecificationVersion specVersion, JavaClassSource classSource) {
        if (specVersion.getRoot() == null) return;

        var rootTypeName = specVersion.getRoot().getType();
        var namespace = specVersion.getNamespace();

        var rootType = getState().getConceptIndex().lookupType(namespace, rootTypeName);
        if (rootType instanceof io.apitomy.umg.models.concept.type.UnionType unionType) {
            createUnionReadRootMethod(specVersion, classSource, unionType);
            return;
        }

        var entity = getState().getConceptIndex().lookupEntity(namespace, rootTypeName);
        if (entity != null) {
            createReadRootMethod(specVersion, classSource, entity);
            return;
        }

        warn("Root type '%s' not found in namespace '%s'", rootTypeName, namespace);
    }

    private void createReadRootMethod(SpecificationVersion specVersion,
            JavaClassSource classSource, EntityModel entityModel) {
        JavaInterfaceSource rootNodeInterfaceSource =
                getState().getJavaIndex().lookupInterface(getRootNodeInterfaceFQN());
        classSource.addImport(rootNodeInterfaceSource);
        classSource.addImport(JsonNode.class);
        classSource.addImport(ObjectNode.class);

        MethodSource<JavaClassSource> readRootMethodSource = classSource.addMethod()
                .setName("readRoot")
                .setReturnType(rootNodeInterfaceSource.getName())
                .setPublic();
        readRootMethodSource.addParameter("JsonNode", "json");
        readRootMethodSource.addAnnotation(Override.class);

        String readMethodName = ReaderMethod.methodName(entityModel.getName());
        JavaInterfaceSource entitySource = lookupJavaEntity(entityModel);
        JavaClassSource entityImplSource = lookupJavaEntityImpl(entityModel);

        classSource.addImport(entitySource);
        classSource.addImport(entityImplSource);

        BodyBuilder body = new BodyBuilder();
        body.addContext("readMethodName", readMethodName);
        body.addContext("rootEntityType", entitySource.getName());
        body.addContext("rootEntityImplType", entityImplSource.getName());

        body.append("${rootEntityType} rootModel = new ${rootEntityImplType}();");
        body.append("this.${readMethodName}((ObjectNode) json, rootModel);");
        body.append("return rootModel;");
        readRootMethodSource.setBody(body.toString());
    }

    private void createUnionReadRootMethod(SpecificationVersion specVersion,
            JavaClassSource classSource, io.apitomy.umg.models.concept.type.UnionType unionType) {
        JavaInterfaceSource rootCapableSource =
                getState().getJavaIndex().lookupInterface(getRootNodeInterfaceFQN());
        classSource.addImport(rootCapableSource);
        classSource.addImport(JsonNode.class);

        JavaEnumSource modelTypeEnum = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
        classSource.addImport(modelTypeEnum);

        var namespace = specVersion.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
        String readMethodName = ReaderMethod.methodName(jt.getSimpleName());

        MethodSource<JavaClassSource> readRootMethodSource = classSource.addMethod()
                .setName("readRoot")
                .setReturnType(rootCapableSource.getName())
                .setPublic();
        readRootMethodSource.addParameter("JsonNode", "json");
        readRootMethodSource.addAnnotation(Override.class);

        String prefix = getPrefix(namespace);
        String modelType = prefixToModelType(prefix);

        BodyBuilder body = new BodyBuilder();
        body.addContext("readMethodName", readMethodName);
        body.addContext("modelType", modelType);

        body.append("return (RootCapable) this.${readMethodName}(json, ModelType.${modelType});");
        readRootMethodSource.setBody(body.toString());
    }

    @Override
    protected CodeBlock createPropertyBlock(PropertyBlockKind kind,
            PropertyCodeGen prop, JavaClassSource classSource) {
        switch (kind) {
            case STAR:       return new ReadStarPropertyBlock(prop, classSource);
            case REGEX:      return new ReadRegexPropertyBlock(prop, classSource);
            case UNION:      return new ReadUnionPropertyBlock(prop, classSource);
            case UNION_LIST: return new ReadUnionListPropertyBlock(prop, classSource);
            case UNION_MAP:  return new ReadUnionMapPropertyBlock(prop, classSource);
            case ENTITY:     return new ReadEntityPropertyBlock(prop, classSource);
            case PRIMITIVE:  return new ReadPrimitivePropertyBlock(prop, classSource);
            case LIST:       return new ReadListPropertyBlock(prop, classSource);
            case MAP:        return new ReadMapPropertyBlock(prop, classSource);
            default:         throw new IllegalArgumentException("Unknown block kind: " + kind);
        }
    }
}
