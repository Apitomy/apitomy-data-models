package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.WriterMethod;
import io.apitomy.umg.pipe.java.method.writer.WriteEntityPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteListPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WritePrimitivePropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteRegexPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteStarPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionListPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionPropertyBlock;

/**
 * Creates the i/o writer classes.  There is a bespoke writer for each specification
 * version.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateWritersStage extends AbstractIOStage {

    @Override
    protected String getPackageName(SpecificationVersion specVersion) {
        return getWriterPackageName(specVersion);
    }

    @Override
    protected String getClassName(SpecificationVersion specVersion) {
        return getWriterClassName(specVersion);
    }

    @Override
    protected void addImports(JavaClassSource classSource) {
        classSource.addImport(getState().getConfig().getRootNamespace() + ".util.JsonUtil");
        classSource.addImport(getState().getConfig().getRootNamespace() + ".util.WriterUtil");
    }

    @Override
    protected void addInterfaceImplementation(JavaClassSource classSource) {
        JavaInterfaceSource modelWriterInterfaceSource =
                getState().getJavaIndex().lookupInterface(getModelWriterInterfaceFQN());
        classSource.addImport(modelWriterInterfaceSource);
        classSource.addInterface(modelWriterInterfaceSource);
    }

    @Override
    protected void createEntityMethod(SpecificationVersion specVersion,
            JavaClassSource classSource, EntityModel entityModel) {
        String writeMethodName = new WriterMethod(entityModel.getName()).getName();

        JavaInterfaceSource javaEntity = getState().getJavaIndex()
                .lookupInterface(getJavaEntityInterfaceFQN(entityModel));
        if (javaEntity == null) {
            warn("Java entity not found for: " + entityModel.fullyQualifiedName());
            return;
        }

        classSource.addImport(javaEntity.getQualifiedName());
        classSource.addImport(ObjectNode.class);
        MethodSource<JavaClassSource> methodSource = classSource.addMethod()
                .setName(writeMethodName)
                .setReturnTypeVoid()
                .setPublic();
        methodSource.addParameter(javaEntity.getName(), "node");
        methodSource.addParameter(ObjectNode.class.getSimpleName(), "json");

        BodyBuilder body = new BodyBuilder();
        body.append("if (node == null) {");
        body.append("    return;");
        body.append("}");

        Collection<PropertyModelWithOrigin> allProperties =
                getState().getConceptIndex().getAllEntityProperties(entityModel);
        allProperties.forEach(property -> {
            body.clearContext();
            dispatchProperty(body, property, entityModel, classSource);
        });
        body.append("WriterUtil.writeExtraProperties(node, json);");

        methodSource.setBody(body.toString());
    }

    @Override
    protected void afterEntityMethods(SpecificationVersion specVersion, JavaClassSource classSource) {
        createTypeBasedWriterMethods(specVersion, classSource);
        createWriteRootFromSpec(specVersion, classSource);
    }

    private void createTypeBasedWriterMethods(SpecificationVersion specVersion, JavaClassSource classSource) {
        var namespace = specVersion.getNamespace();

        getState().getConceptIndex().findTypes(namespace).stream()
                .filter(t -> t instanceof UnionType)
                .map(t -> (UnionType) t)
                .forEach(unionType -> {
                    new WriterMethod(specVersion, unionType, ctx).writeTo(classSource);
                });
    }

    private void createWriteRootFromSpec(SpecificationVersion specVersion, JavaClassSource classSource) {
        if (specVersion.getRoot() == null) return;

        var rootTypeName = specVersion.getRoot().getType();
        var namespace = specVersion.getNamespace();

        var rootType = getState().getConceptIndex().lookupType(namespace, rootTypeName);
        if (rootType instanceof UnionType unionType) {
            createUnionWriteRootMethod(specVersion, classSource, unionType);
            return;
        }

        var entity = getState().getConceptIndex().lookupEntity(namespace, rootTypeName);
        if (entity != null) {
            createWriteRootMethod(specVersion, classSource, entity);
        }
    }

    private void createUnionWriteRootMethod(SpecificationVersion specVersion,
            JavaClassSource classSource, UnionType unionType) {
        JavaInterfaceSource rootCapableSource =
                getState().getJavaIndex().lookupInterface(getRootNodeInterfaceFQN());
        classSource.addImport(rootCapableSource);
        classSource.addImport(JsonNode.class);
        classSource.addImport(ObjectNode.class);

        var namespace = specVersion.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
        String writeMethodName = new WriterMethod(jt.getSimpleName()).getName();
        jt.addImportsTo(classSource);

        MethodSource<JavaClassSource> writeRootMethodSource = classSource.addMethod()
                .setName("writeRoot")
                .setReturnType(JsonNode.class.getName())
                .setPublic();
        writeRootMethodSource.addParameter(rootCapableSource.getName(), "node");
        writeRootMethodSource.addAnnotation(Override.class);

        BodyBuilder body = new BodyBuilder();
        body.addContext("writeMethodName", writeMethodName);
        body.addContext("unionType", jt.toJavaTypeString());

        body.append("return this.${writeMethodName}((${unionType}) node);");
        writeRootMethodSource.setBody(body.toString());
    }

    private void createWriteRootMethod(SpecificationVersion specVersion,
            JavaClassSource classSource, EntityModel entityModel) {
        JavaInterfaceSource rootNodeInterfaceSource =
                getState().getJavaIndex().lookupInterface(getRootNodeInterfaceFQN());
        classSource.addImport(rootNodeInterfaceSource);
        classSource.addImport(ObjectNode.class);

        classSource.addImport(JsonNode.class);

        MethodSource<JavaClassSource> writeRootMethodSource = classSource.addMethod()
                .setName("writeRoot")
                .setReturnType(JsonNode.class.getName())
                .setPublic();
        writeRootMethodSource.addParameter(rootNodeInterfaceSource.getName(), "node");
        writeRootMethodSource.addAnnotation(Override.class);

        String writeMethodName = new WriterMethod(entityModel.getName()).getName();
        JavaInterfaceSource entitySource = lookupJavaEntity(entityModel);

        classSource.addImport(entitySource);

        BodyBuilder body = new BodyBuilder();
        body.addContext("writeMethodName", writeMethodName);
        body.addContext("rootEntityType", entitySource.getName());

        body.append("ObjectNode json = JsonUtil.objectNode();");
        body.append("this.${writeMethodName}((${rootEntityType}) node, json);");
        body.append("return json;");
        writeRootMethodSource.setBody(body.toString());
    }

    @Override
    protected CodeBlock createPropertyBlock(PropertyBlockKind kind,
            PropertyCodeGen prop, JavaClassSource classSource) {
        switch (kind) {
            case STAR:       return new WriteStarPropertyBlock(prop, classSource);
            case REGEX:      return new WriteRegexPropertyBlock(prop, classSource);
            case UNION:      return new WriteUnionPropertyBlock(prop, classSource);
            case UNION_LIST: return new WriteUnionListPropertyBlock(prop, classSource);
            case UNION_MAP:  return new WriteUnionMapPropertyBlock(prop, classSource);
            case ENTITY:     return new WriteEntityPropertyBlock(prop, classSource);
            case PRIMITIVE:  return new WritePrimitivePropertyBlock(prop, classSource);
            case LIST:       return new WriteListPropertyBlock(prop, classSource);
            case MAP:        return new WriteMapPropertyBlock(prop, classSource);
            default:         throw new IllegalArgumentException("Unknown block kind: " + kind);
        }
    }
}
