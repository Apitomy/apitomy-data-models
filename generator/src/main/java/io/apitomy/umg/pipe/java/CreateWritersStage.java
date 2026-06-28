package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.GetterMethod;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;

import io.apitomy.umg.pipe.java.method.WriterMethod;
import io.apitomy.umg.pipe.java.method.writer.WriteEntityPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteListPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WritePrimitivePropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteRegexPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteStarPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionListPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionMapPropertyBlock;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Creates the i/o writer classes.  There is a bespoke writer for each specification
 * version.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateWritersStage extends AbstractJavaStage {

    private CodeGenContext ctx;

    @Override
    protected void doProcess() {
        ctx = new CodeGenContext(
                getState().getConceptIndex(),
                getState().getJavaIndex(),
                getJavaTypeFactory(),
                getState().getConfig().getRootNamespace(),
                getState().getSpecIndex(),
                getClass().getSimpleName());
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            createWriter(specVersion);
        });
    }

    /**
     * Creates a writer for the given spec version.
     * @param specVersion
     */
    private void createWriter(SpecificationVersion specVersion) {
        String writerPackageName = getWriterPackageName(specVersion);
        String writerClassName = getWriterClassName(specVersion);

        // Create java source code for the writer
        JavaClassSource writerClassSource = Roaster.create(JavaClassSource.class)
                .setPackage(writerPackageName)
                .setName(writerClassName)
                .setPublic();
        writerClassSource.addImport(getState().getConfig().getRootNamespace() + ".util." + "JsonUtil");
        writerClassSource.addImport(getState().getConfig().getRootNamespace() + ".util." + "WriterUtil");

        // Implements the ModelWriter interface
        JavaInterfaceSource modelWriterInterfaceSource = getState().getJavaIndex().lookupInterface(getModelWriterInterfaceFQN());
        writerClassSource.addImport(modelWriterInterfaceSource);
        writerClassSource.addInterface(modelWriterInterfaceSource);

        // Create the writeXYZ methods - one for each entity
        specVersion.getEntities().forEach(entity -> {
            EntityModel entityModel = getState().getConceptIndex().lookupEntity(specVersion.getNamespace() + "." + entity.getName());
            if (entityModel == null) {
                warn("Entity model not found for entity: " + entity);
            } else {
                createWriteMethodFor(specVersion, writerClassSource, entityModel);

            }
        });

        // Create type-based writer methods for unions
        createTypeBasedWriterMethods(specVersion, writerClassSource);

        // Create writeRoot method based on the spec-level root type
        createWriteRootFromSpec(specVersion, writerClassSource);

        getState().getJavaIndex().index(writerClassSource);
    }

    private void createTypeBasedWriterMethods(SpecificationVersion specVersion, JavaClassSource writerClassSource) {
        var namespace = specVersion.getNamespace();

        getState().getConceptIndex().findTypes(namespace).stream()
                .filter(t -> t instanceof io.apitomy.umg.models.concept.type.UnionType)
                .map(t -> (io.apitomy.umg.models.concept.type.UnionType) t)
                .forEach(unionType -> {
                    var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
                    var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
                    new WriterMethod(jt.getSimpleName()).writeTo(writerClassSource, specVersion, unionType, ctx);
                });
    }



    private boolean hasMethod(JavaClassSource source, String name) {
        return source.getMethods().stream().anyMatch(m -> m.getName().equals(name));
    }

    private void createWriteRootFromSpec(SpecificationVersion specVersion, JavaClassSource writerClassSource) {
        if (specVersion.getRoot() == null) return;

        var rootTypeName = specVersion.getRoot().getType();
        var namespace = specVersion.getNamespace();

        var rootType = getState().getConceptIndex().lookupType(namespace, rootTypeName);
        if (rootType instanceof io.apitomy.umg.models.concept.type.UnionType unionType) {
            createUnionWriteRootMethod(specVersion, writerClassSource, unionType);
            return;
        }

        var entity = getState().getConceptIndex().lookupEntity(namespace, rootTypeName);
        if (entity != null) {
            createWriteRootMethod(specVersion, writerClassSource, entity);
        }
    }

    private void createUnionWriteRootMethod(SpecificationVersion specVersion, JavaClassSource writerClassSource,
                                             io.apitomy.umg.models.concept.type.UnionType unionType) {
        JavaInterfaceSource rootCapableSource = getState().getJavaIndex().lookupInterface(getRootNodeInterfaceFQN());
        writerClassSource.addImport(rootCapableSource);
        writerClassSource.addImport(JsonNode.class);
        writerClassSource.addImport(ObjectNode.class);

        var namespace = specVersion.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
        String writeMethodName = WriterMethod.methodName(jt.getSimpleName());
        jt.addImportsTo(writerClassSource);

        MethodSource<JavaClassSource> writeRootMethodSource = writerClassSource.addMethod()
                .setName("writeRoot")
                .setReturnType(ObjectNode.class.getName())
                .setPublic();
        writeRootMethodSource.addParameter(rootCapableSource.getName(), "node");
        writeRootMethodSource.addAnnotation(Override.class);

        BodyBuilder body = new BodyBuilder();
        body.addContext("writeMethodName", writeMethodName);
        body.addContext("unionType", jt.toJavaTypeString());

        body.append("JsonNode result = this.${writeMethodName}((${unionType}) node);");
        body.append("if (result != null && JsonUtil.isObjectNode(result)) {");
        body.append("    return (ObjectNode) result;");
        body.append("}");
        body.append("return null;");
        writeRootMethodSource.setBody(body.toString());
    }

    /**
     * Creates a "writeRoot(node)" method for this writer.
     * @param specVersion
     * @param writerClassSource
     * @param entityModel
     */
    private void createWriteRootMethod(SpecificationVersion specVersion, JavaClassSource writerClassSource, EntityModel entityModel) {
        JavaInterfaceSource rootNodeInterfaceSource = getState().getJavaIndex().lookupInterface(getRootNodeInterfaceFQN());
        writerClassSource.addImport(rootNodeInterfaceSource);
        writerClassSource.addImport(ObjectNode.class);

        MethodSource<JavaClassSource> writeRootMethodSource = writerClassSource.addMethod()
                .setName("writeRoot")
                .setReturnType(ObjectNode.class.getName())
                .setPublic();
        writeRootMethodSource.addParameter(rootNodeInterfaceSource.getName(), "node");
        writeRootMethodSource.addAnnotation(Override.class);

        String writeMethodName = writeMethodName(entityModel);
        JavaInterfaceSource entitySource = lookupJavaEntity(entityModel);

        writerClassSource.addImport(entitySource);

        BodyBuilder body = new BodyBuilder();
        body.addContext("writeMethodName", writeMethodName);
        body.addContext("rootEntityType", entitySource.getName());

        body.append("ObjectNode json = JsonUtil.objectNode();");
        body.append("this.${writeMethodName}((${rootEntityType}) node, json);");
        body.append("return json;");
        writeRootMethodSource.setBody(body.toString());
    }

    /**
     * Creates a single "writeXyx" method for the given entity.
     *
     * @param specVersion
     * @param writerClassSource
     * @param entityModel
     */
    private void createWriteMethodFor(SpecificationVersion specVersion, JavaClassSource writerClassSource, EntityModel entityModel) {
        String writeMethodName = writeMethodName(entityModel);

        JavaInterfaceSource javaEntityModel = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(entityModel));
        if (javaEntityModel == null) {
            warn("Java entity not found for: " + entityModel.fullyQualifiedName());
            return;
        }

        writerClassSource.addImport(javaEntityModel.getQualifiedName());
        writerClassSource.addImport(ObjectNode.class);
        MethodSource<JavaClassSource> methodSource = writerClassSource.addMethod()
                .setName(writeMethodName)
                .setReturnTypeVoid()
                .setPublic();
        methodSource.addParameter(javaEntityModel.getName(), "node");
        methodSource.addParameter(ObjectNode.class.getSimpleName(), "json");

        // Now create the body content for the writer.
        BodyBuilder body = new BodyBuilder();
        body.append("if (node == null) {");
        body.append("    return;");
        body.append("}");

        // Write each property of the entity
        Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entityModel);
        allProperties.forEach(property -> {
            createWritePropertyCode(body, property, entityModel, javaEntityModel, writerClassSource);
        });
        // Write "extra" properties
        createWriteExtraPropertiesCode(body);

        methodSource.setBody(body.toString());
    }

    /**
     * Generates the right java code for writing a single property of an entity.
     *
     * @param body
     * @param property
     * @param javaEntity
     * @param javaEntity
     * @param writerClassSource
     */
    private void createWritePropertyCode(BodyBuilder body, PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaInterfaceSource javaEntity, JavaClassSource writerClassSource) {
        CreateWriteProperty crp = new CreateWriteProperty(propertyWithOrigin, entityModel, javaEntity, writerClassSource);
        body.clearContext();
        crp.writeTo(body);
    }

    /**
     * Generates code that will write the extra properties from the model to the JSON output.
     *
     * @param body
     */
    private void createWriteExtraPropertiesCode(BodyBuilder body) {
        body.append("WriterUtil.writeExtraProperties(node, json);");
    }

    private static String writeMethodName(EntityModel entityModel) {
        return writeMethodName(entityModel.getName());
    }

    private static String writeMethodName(String entityName) {
        return WriterMethod.methodName(entityName);
    }

    @Data
    @AllArgsConstructor
    private class CreateWriteProperty {
        PropertyModelWithOrigin propertyWithOrigin;
        EntityModel entityModel;
        JavaInterfaceSource javaEntityModel;
        JavaClassSource writerClassSource;

        /**
         * Generates code to write a property from a JSON node into the data model.
         *
         * @param body
         */
        public void writeTo(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isStarProperty(property)) {
                handleStarProperty(body);
            } else if (isRegexProperty(property)) {
                handleRegexProperty(body);
            } else if (handleViaResolvedType(body)) {
                // Handled by resolved type dispatch
            } else {
                warn("Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
            }
        }

        private boolean handleViaResolvedType(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            var resolved = property.getResolvedType();
            if (resolved == null) return false;

            // Direct union property (both type aliases and inline unions)
            if (resolved instanceof io.apitomy.umg.models.concept.type.UnionType) {
                handleResolvedUnionProperty(body, resolved);
                return true;
            }

            if (resolved instanceof io.apitomy.umg.models.concept.type.ListType lt
                    && lt.getValueType() instanceof io.apitomy.umg.models.concept.type.UnionType) {
                handleResolvedUnionListProperty(body, lt);
                return true;
            }

            if (resolved instanceof io.apitomy.umg.models.concept.type.MapType mt
                    && mt.getValueType() instanceof io.apitomy.umg.models.concept.type.UnionType) {
                handleResolvedUnionMapProperty(body, mt);
                return true;
            }

            if (resolved.isEntityType()) {
                handleEntityProperty(body);
                return true;
            }
            if (resolved.isPrimitiveType()) {
                handlePrimitiveTypeProperty(body);
                return true;
            }
            if (resolved.isListType()) {
                handleListProperty(body);
                return true;
            }
            if (resolved.isMapType()) {
                handleMapProperty(body);
                return true;
            }

            return false;
        }

        private void handleResolvedUnionProperty(BodyBuilder body, io.apitomy.umg.models.concept.type.Type resolved) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WriteUnionPropertyBlock(prop, writerClassSource).appendTo(body);
        }

        private void handleResolvedUnionListProperty(BodyBuilder body,
                io.apitomy.umg.models.concept.type.ListType listType) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WriteUnionListPropertyBlock(prop, writerClassSource).appendTo(body);
        }

        private void handleResolvedUnionMapProperty(BodyBuilder body,
                io.apitomy.umg.models.concept.type.MapType mapType) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WriteUnionMapPropertyBlock(prop, writerClassSource).appendTo(body);
        }

        private void handleStarProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WriteStarPropertyBlock(prop, writerClassSource).appendTo(body);
        }

        private void handleRegexProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WriteRegexPropertyBlock(prop, writerClassSource).appendTo(body);
        }

        private void handleEntityProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WriteEntityPropertyBlock(prop, writerClassSource).appendTo(body);
        }

        private void handlePrimitiveTypeProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WritePrimitivePropertyBlock(prop, writerClassSource).appendTo(body);
        }

        private void handleListProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WriteListPropertyBlock(prop, writerClassSource).appendTo(body);
        }

        private void handleMapProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new WriteMapPropertyBlock(prop, writerClassSource).appendTo(body);
        }




    }
}
