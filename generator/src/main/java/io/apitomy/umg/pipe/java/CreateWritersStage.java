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
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.writer.WriteEntityPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteListPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WritePrimitivePropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionListPropertyBlock;
import io.apitomy.umg.pipe.java.method.writer.WriteUnionMapPropertyBlock;
import io.apitomy.umg.index.concept.ConceptIndex;
import io.apitomy.umg.index.java.JavaIndex;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Creates the i/o writer classes.  There is a bespoke writer for each specification
 * version.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateWritersStage extends AbstractJavaStage implements CodeGenContext {

    @Override
    protected void doProcess() {
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
                .forEach(unionType -> createUnionWriterMethod(specVersion, writerClassSource, unionType));
    }

    private void createUnionWriterMethod(SpecificationVersion specVersion, JavaClassSource writerClassSource,
                                          io.apitomy.umg.models.concept.type.UnionType unionType) {
        var namespace = specVersion.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
        String unionTypeName = jt.getSimpleName();
        String methodName = "write" + unionTypeName;

        if (hasMethod(writerClassSource, methodName)) {
            return;
        }

        debug("Creating union writer method: %s", methodName);

        writerClassSource.addImport(ObjectNode.class);
        writerClassSource.addImport(JsonNode.class);
        jt.addImportsTo(writerClassSource);

        MethodSource<JavaClassSource> method = writerClassSource.addMethod()
                .setName(methodName)
                .setReturnType("JsonNode")
                .setPrivate();
        method.addParameter(jt.toJavaTypeString(), "union");

        BodyBuilder body = new BodyBuilder();
        body.append("if (union == null) return null;");

        for (var variantType : unionType.getTypes()) {
            if (variantType instanceof io.apitomy.umg.models.concept.type.EntityType entityType) {
                var entity = entityType.getEntity();
                if (entity == null) entity = getState().getConceptIndex().lookupEntity(namespace, entityType.getName());
                if (entity == null) continue;

                String typeName = io.apitomy.umg.models.java.type.JavaTypeFactory.getUnionComponentName(variantType);
                JavaInterfaceSource entitySource = lookupJavaEntity(entity);
                writerClassSource.addImport(entitySource);

                body.addContext("isMethod", "is" + typeName);
                body.addContext("asMethod", "as" + typeName);
                body.addContext("entityType", entitySource.getName());
                body.addContext("writeMethodName", writeMethodName(entity));

                body.append("if (union.${isMethod}()) {");
                body.append("    ObjectNode jsonValue = JsonUtil.objectNode();");
                body.append("    this.${writeMethodName}((${entityType}) union.${asMethod}(), jsonValue);");
                body.append("    return jsonValue;");
                body.append("}");
            } else if (variantType instanceof io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType puv) {
                String typeName = io.apitomy.umg.models.java.type.JavaTypeFactory.getUnionComponentName(variantType);
                Class<?> javaClass = Util.PRIMITIVE_TYPE_MAP.get(puv.getType().name().toLowerCase());
                if (javaClass == null) continue;

                body.addContext("isMethod", "is" + typeName);
                body.addContext("asMethod", "as" + typeName);

                if (JsonNode.class.isAssignableFrom(javaClass)) {
                    body.append("if (union.${isMethod}()) {");
                    body.append("    return union.${asMethod}();");
                    body.append("}");
                } else {
                    String toJsonMethod;
                    if (Boolean.class.equals(javaClass)) toJsonMethod = "booleanToJsonNode";
                    else if (String.class.equals(javaClass)) toJsonMethod = "stringToJsonNode";
                    else toJsonMethod = "numberToJsonNode";

                    body.addContext("toJsonMethod", toJsonMethod);
                    body.append("if (union.${isMethod}()) {");
                    body.append("    return JsonUtil.${toJsonMethod}(union.${asMethod}());");
                    body.append("}");
                }
            } else if (variantType instanceof io.apitomy.umg.models.concept.type.ListType listType) {
                String typeName = io.apitomy.umg.models.java.type.JavaTypeFactory.getUnionComponentName(variantType);

                body.addContext("isMethod", "is" + typeName);
                body.addContext("asMethod", "as" + typeName);

                writerClassSource.addImport(ArrayNode.class);

                if (listType.getValueType() instanceof io.apitomy.umg.models.concept.type.EntityType listEntityType) {
                    var entity = listEntityType.getEntity();
                    if (entity == null) entity = getState().getConceptIndex().lookupEntity(namespace, listEntityType.getName());
                    if (entity == null) continue;

                    JavaInterfaceSource entitySource = lookupJavaEntity(entity);
                    writerClassSource.addImport(entitySource);
                    body.addContext("entityType", entitySource.getName());
                    body.addContext("writeMethodName", writeMethodName(entity));

                    body.append("if (union.${isMethod}()) {");
                    body.append("    ArrayNode array = JsonUtil.arrayNode();");
                    body.append("    for (Object item : (java.util.List<?>) union.${asMethod}()) {");
                    body.append("        ObjectNode itemNode = JsonUtil.objectNode();");
                    body.append("        this.${writeMethodName}((${entityType}) item, itemNode);");
                    body.append("        array.add(itemNode);");
                    body.append("    }");
                    body.append("    return array;");
                    body.append("}");
                } else if (listType.getValueType() instanceof io.apitomy.umg.models.concept.type.PrimitiveType primType) {
                    Class<?> javaClass = Util.PRIMITIVE_TYPE_MAP.get(primType.name().toLowerCase());
                    if (javaClass == null) continue;

                    writerClassSource.addImport(javaClass);
                    body.addContext("primType", javaClass.getSimpleName());

                    String addExpr;
                    if (Boolean.class.equals(javaClass)) addExpr = "array.add((Boolean) item)";
                    else if (String.class.equals(javaClass)) addExpr = "array.add((String) item)";
                    else addExpr = "array.add(JsonUtil.numberToJsonNode((Number) item))";

                    body.addContext("addExpr", addExpr);
                    body.append("if (union.${isMethod}()) {");
                    body.append("    ArrayNode array = JsonUtil.arrayNode();");
                    body.append("    for (Object item : (java.util.List<?>) union.${asMethod}()) {");
                    body.append("        ${addExpr};");
                    body.append("    }");
                    body.append("    return array;");
                    body.append("}");
                }
            }
        }
        body.append("return null;");
        method.setBody(body.toString());
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
        String writeMethodName = "write" + jt.getSimpleName();
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
        return "write" + StringUtils.capitalize(entityName);
    }

    // --- CodeGenContext implementation (only methods not inherited from AbstractJavaStage) ---

    @Override
    public ConceptIndex getConceptIndex() {
        return getState().getConceptIndex();
    }

    @Override
    public JavaIndex getJavaIndex() {
        return getState().getJavaIndex();
    }

    @Override
    public void warn(String message) {
        super.warn(message);
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
            new WriteUnionPropertyBlock(propertyWithOrigin, writerClassSource, CreateWritersStage.this).appendTo(body);
        }

        private void handleResolvedUnionListProperty(BodyBuilder body,
                io.apitomy.umg.models.concept.type.ListType listType) {
            new WriteUnionListPropertyBlock(propertyWithOrigin, writerClassSource, CreateWritersStage.this).appendTo(body);
        }

        private void handleResolvedUnionMapProperty(BodyBuilder body,
                io.apitomy.umg.models.concept.type.MapType mapType) {
            new WriteUnionMapPropertyBlock(propertyWithOrigin, writerClassSource, CreateWritersStage.this).appendTo(body);
        }

        private void handleStarProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isEntity(property)) {
                var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                        entityModel, CreateWritersStage.this, "STAR");
                if (resolved == null) {
                    return;
                }

                writerClassSource.addImport(List.class);
                writerClassSource.addImport(resolved.javaInterface());

                body.addContext("writeMethodName", writeMethodName(resolved.entityModel()));
                body.addContext("entityJavaType", resolved.javaInterface().getName());

                body.append("{");
                body.append("    List<String> propertyNames = node.getItemNames();");
                body.append("    propertyNames.forEach(propertyName -> {");
                body.append("        ObjectNode object = JsonUtil.objectNode();");
                body.append("        this.${writeMethodName}((${entityJavaType}) node.getItem(propertyName), object);");
                body.append("        JsonUtil.setObjectProperty(json, propertyName, object);");
                body.append("    });");
                body.append("}");
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                writerClassSource.addImport(List.class);

                body.addContext("valueType", PrimitiveTypeHelper.determineValueType(property.getResolvedType(), CreateWritersStage.this, writerClassSource));
                body.addContext("setPropertyMethodName", PrimitiveTypeHelper.determineSetPropertyVariant(property.getResolvedType(), CreateWritersStage.this, writerClassSource));

                body.append("{");
                body.append("    List<String> propertyNames = node.getItemNames();");
                body.append("    propertyNames.forEach(propertyName -> {");
                body.append("        ${valueType} value = node.getItem(propertyName);");
                body.append("        JsonUtil.${setPropertyMethodName}(json, propertyName, value);");
                body.append("    });");
                body.append("}");
            } else {
                warn("STAR Entity property '" + property.getName() + "' not written (unhandled) for entity: " + entityModel.fullyQualifiedName());
                warn("       property type: " + property.getResolvedType());
            }
        }

        private void handleRegexProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isEntity(property)) {
                var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                        entityModel, CreateWritersStage.this, "REGEX");
                if (resolved == null) {
                    return;
                }
                JavaInterfaceSource commonEntityTypeJavaModel = resolveCommonJavaEntity(resolved.entityModel());

                writerClassSource.addImport(Map.class);
                writerClassSource.addImport(resolved.javaInterface());
                writerClassSource.addImport(commonEntityTypeJavaModel);

                body.addContext("mapValueJavaType", resolved.javaInterface().getName());
                body.addContext("getterMethodName", getterMethodName(property));
                body.addContext("writeMethodName", writeMethodName(resolved.entityModel()));
                body.addContext("mapValueCommonJavaType", commonEntityTypeJavaModel.getName());

                body.append("{");
                body.append("    Map<String, ? extends ${mapValueCommonJavaType}> models = node.${getterMethodName}();");
                body.append("    if (models != null && !models.isEmpty()) {");
                body.append("        models.keySet().forEach(propertyName -> {");
                body.append("            ObjectNode object = JsonUtil.objectNode();");
                body.append("            this.${writeMethodName}((${mapValueJavaType}) models.get(propertyName), object);");
                body.append("            JsonUtil.setObjectProperty(json, propertyName, object);");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                writerClassSource.addImport(List.class);

                body.addContext("valueType", PrimitiveTypeHelper.determineValueType(property.getResolvedType(), CreateWritersStage.this, writerClassSource));
                body.addContext("getterMethodName", getterMethodName(property));
                body.addContext("setPropertyMethodName", PrimitiveTypeHelper.determineSetPropertyVariant(property.getResolvedType(), CreateWritersStage.this, writerClassSource));

                body.append("{");
                body.append("    Map<String, ${valueType}> values = node.${getterMethodName}();");
                body.append("    if (values != null && !values.isEmpty()) {");
                body.append("        values.keySet().forEach(propertyName -> {");
                body.append("            ${valueType} value = values.get(propertyName);");
                body.append("            JsonUtil.${setPropertyMethodName}(json, propertyName, value);");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else {
                warn("REGEX Entity property '" + property.getName() + "' not written (unhandled) for entity: " + entityModel.fullyQualifiedName());
                warn("       property type: " + property.getResolvedType());
            }
        }

        private void handleEntityProperty(BodyBuilder body) {
            new WriteEntityPropertyBlock(propertyWithOrigin, entityModel, writerClassSource, CreateWritersStage.this).appendTo(body);
        }

        private void handlePrimitiveTypeProperty(BodyBuilder body) {
            new WritePrimitivePropertyBlock(propertyWithOrigin, writerClassSource, CreateWritersStage.this).appendTo(body);
        }

        private void handleListProperty(BodyBuilder body) {
            new WriteListPropertyBlock(propertyWithOrigin, entityModel, writerClassSource, CreateWritersStage.this).appendTo(body);
        }

        private void handleMapProperty(BodyBuilder body) {
            new WriteMapPropertyBlock(propertyWithOrigin, entityModel, writerClassSource, CreateWritersStage.this).appendTo(body);
        }




    }
}
