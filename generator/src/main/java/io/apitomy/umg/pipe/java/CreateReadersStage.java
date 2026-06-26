package io.apitomy.umg.pipe.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import io.apitomy.umg.models.concept.type.UnionVariantComparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.pipe.java.Util;
import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.beans.UnionRule;
import io.apitomy.umg.beans.UnionRuleType;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.reader.ReadEntityPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadListPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadPrimitivePropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionListPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionMapPropertyBlock;
import io.apitomy.umg.index.concept.ConceptIndex;
import io.apitomy.umg.index.java.JavaIndex;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Creates the i/o reader classes.  There is a bespoke reader for each specification
 * version.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateReadersStage extends AbstractJavaStage implements CodeGenContext {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            createReader(specVersion);
        });
    }

    /**
     * Creates a reader for the given spec version.
     * @param specVersion
     */
    private void createReader(SpecificationVersion specVersion) {
        String readerPackageName = getReaderPackageName(specVersion);
        String readerClassName = getReaderClassName(specVersion);

        debug("Creating reader: " + readerPackageName + "." + readerClassName);

        // Create java source code for the reader
        JavaClassSource readerClassSource = Roaster.create(JavaClassSource.class)
                .setPackage(readerPackageName)
                .setName(readerClassName)
                .setPublic();
        readerClassSource.addImport(getState().getConfig().getRootNamespace() + ".util." + "JsonUtil");
        readerClassSource.addImport(getState().getConfig().getRootNamespace() + ".util." + "ReaderUtil");

        // Implements the ModelReader interface
        debug("Reader implements: " + getModelReaderInterfaceFQN());
        JavaInterfaceSource modelReaderInterfaceSource = getState().getJavaIndex().lookupInterface(getModelReaderInterfaceFQN());
        readerClassSource.addImport(modelReaderInterfaceSource);
        readerClassSource.addInterface(modelReaderInterfaceSource);

        // Create the readXYZ methods - one for each entity
        debug("Creating readXYZ methods...");
        specVersion.getEntities().forEach(entity -> {
            EntityModel entityModel = getState().getConceptIndex().lookupEntity(specVersion.getNamespace() + "." + entity.getName());
            if (entityModel == null) {
                warn("Entity model not found for entity: " + entity);
            } else {
                createReadMethodFor(specVersion, readerClassSource, entityModel);
            }
        });

        // Create type-based reader methods for unions and collections
        createTypeBasedReaderMethods(specVersion, readerClassSource);

        // Create readRoot method based on the spec-level root type
        createReadRootFromSpec(specVersion, readerClassSource);

        getState().getJavaIndex().index(readerClassSource);
    }

    private void createTypeBasedReaderMethods(SpecificationVersion specVersion, JavaClassSource readerClassSource) {
        var namespace = specVersion.getNamespace();

        getState().getConceptIndex().findTypes(namespace).stream()
                .filter(t -> t instanceof io.apitomy.umg.models.concept.type.UnionType)
                .map(t -> (io.apitomy.umg.models.concept.type.UnionType) t)
                .forEach(unionType -> createUnionReaderMethod(specVersion, readerClassSource, unionType));
    }

    private void createUnionReaderMethod(SpecificationVersion specVersion, JavaClassSource readerClassSource,
                                          io.apitomy.umg.models.concept.type.UnionType unionType) {
        var namespace = specVersion.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
        String unionTypeName = jt.getSimpleName();
        String methodName = "read" + unionTypeName;

        // Skip if already created
        if (readerClassSource.getMethod(methodName, JsonNode.class.getSimpleName(), "ModelType") != null) {
            return;
        }

        debug("Creating union reader method: %s", methodName);

        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(ObjectNode.class);
        jt.addImportsTo(readerClassSource);

        JavaEnumSource modelTypeEnum = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
        readerClassSource.addImport(modelTypeEnum);

        MethodSource<JavaClassSource> method = readerClassSource.addMethod()
                .setName(methodName)
                .setReturnType(jt.toJavaTypeString())
                .setPrivate();
        method.addParameter("JsonNode", "json");
        method.addParameter("ModelType", "modelType");

        BodyBuilder body = new BodyBuilder();
        body.append("if (json == null) return null;");

        // Generate dispatch for each variant
        boolean first = true;
        for (var variantType : unionType.getTypes()) {
            if (variantType instanceof io.apitomy.umg.models.concept.type.EntityType entityType) {
                var entity = entityType.getEntity();
                if (entity == null) entity = getState().getConceptIndex().lookupEntity(namespace, entityType.getName());
                if (entity == null) continue;

                JavaInterfaceSource entitySource = lookupJavaEntity(entity);
                JavaClassSource entityImplSource = lookupJavaEntityImpl(entity);
                readerClassSource.addImport(entitySource);
                readerClassSource.addImport(entityImplSource);

                body.addContext("entityType", entitySource.getName());
                body.addContext("entityImplType", entityImplSource.getName());
                body.addContext("readMethodName", readMethodName(entity));

                var rule = unionType.getRuleFor(entityType.getName());
                String condition;
                if (rule != null && rule.getRuleType() == io.apitomy.umg.beans.UnionRuleType.PROPERTYEXISTS) {
                    body.addContext("rulePropName", rule.getPropertyName());
                    condition = "JsonUtil.isObjectWithProperty(json, \"${rulePropName}\")";
                } else if (rule != null && rule.getRuleType() == io.apitomy.umg.beans.UnionRuleType.PROPERTYVALUE) {
                    body.addContext("rulePropName", rule.getPropertyName());
                    body.addContext("rulePropValue", rule.getPropertyValue());
                    condition = "JsonUtil.isObjectWithPropertyValue(json, \"${rulePropName}\", \"${rulePropValue}\")";
                } else {
                    condition = "JsonUtil.isObject(json)";
                }

                if (!first) body.append(" else ");
                first = false;

                body.append("if (" + condition + ") {");
                body.append("    ${entityType} node = new ${entityImplType}();");
                body.append("    this.${readMethodName}((ObjectNode) json, node);");
                body.append("    return node;");
                body.append("}");
            } else if (variantType instanceof io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType puv) {
                Class<?> javaClass = Util.PRIMITIVE_TYPE_MAP.get(puv.getType().name().toLowerCase());
                if (javaClass == null) continue;

                String typeName = io.apitomy.umg.models.java.type.JavaTypeFactory.getUnionComponentName(variantType);
                String unionValueClassFQN = getUnionTypeFQN(typeName + "UnionValueImpl");
                JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueClass == null) continue;

                readerClassSource.addImport(unionValueClass);

                body.addContext("isMethod", "is" + javaClass.getSimpleName());
                body.addContext("toMethod", "to" + javaClass.getSimpleName());
                body.addContext("unionValueClass", unionValueClass.getName());

                if (!first) body.append(" else ");
                first = false;

                body.append("if (JsonUtil.${isMethod}(json)) {");
                body.append("    return new ${unionValueClass}(JsonUtil.${toMethod}(json), modelType);");
                body.append("}");
            } else if (variantType instanceof io.apitomy.umg.models.concept.type.ListType listType
                    && listType.getValueType() instanceof io.apitomy.umg.models.concept.type.EntityType listEntityType) {
                var entity = listEntityType.getEntity();
                if (entity == null) entity = getState().getConceptIndex().lookupEntity(namespace, listEntityType.getName());
                if (entity == null) continue;

                String typeName = io.apitomy.umg.models.java.type.JavaTypeFactory.getUnionComponentName(variantType);
                String unionValueClassFQN = getUnionTypeFQN(typeName + "UnionValueImpl");
                JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueClass == null) {
                    unionValueClass = getState().getJavaIndex().lookupClass(
                            resolveUnionPackage(unionType) + "." + typeName + "UnionValueImpl");
                }
                if (unionValueClass == null) continue;

                JavaInterfaceSource entitySource = lookupJavaEntity(entity);
                readerClassSource.addImport(entitySource);
                readerClassSource.addImport(unionValueClass);
                readerClassSource.addImport(java.util.List.class);
                readerClassSource.addImport(java.util.ArrayList.class);

                body.addContext("listValueType", entitySource.getName());
                body.addContext("readMethodName", readMethodName(entity));
                body.addContext("unionValueClass", unionValueClass.getName());

                if (!first) body.append(" else ");
                first = false;

                body.append("if (JsonUtil.isArray(json)) {");
                body.append("    List<JsonNode> array = JsonUtil.toList(json);");
                body.append("    List<${listValueType}> models = new ArrayList<>();");
                body.append("    array.forEach(item -> {");
                body.append("        ObjectNode object = JsonUtil.toObject(item);");
                body.append("        ${listValueType} model = new ${listValueType}Impl();");
                body.append("        this.${readMethodName}(object, model);");
                body.append("        models.add(model);");
                body.append("    });");
                body.append("    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })");
                body.append("    ${unionValueClass} unionValue = new ${unionValueClass}((List) models);");
                body.append("    return unionValue;");
                body.append("}");
            } else if (variantType instanceof io.apitomy.umg.models.concept.type.ListType listType
                    && listType.getValueType() instanceof io.apitomy.umg.models.concept.type.PrimitiveType primType) {
                String typeName = io.apitomy.umg.models.java.type.JavaTypeFactory.getUnionComponentName(variantType);
                String unionValueClassFQN = getUnionTypeFQN(typeName + "UnionValueImpl");
                JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueClass == null) continue;

                Class<?> javaClass = Util.PRIMITIVE_TYPE_MAP.get(primType.name().toLowerCase());
                if (javaClass == null) continue;

                readerClassSource.addImport(unionValueClass);
                readerClassSource.addImport(java.util.List.class);
                readerClassSource.addImport(java.util.ArrayList.class);
                readerClassSource.addImport(javaClass);

                body.addContext("primType", javaClass.getSimpleName());
                body.addContext("toMethod", "to" + javaClass.getSimpleName());
                body.addContext("unionValueClass", unionValueClass.getName());

                if (!first) body.append(" else ");
                first = false;

                body.append("if (JsonUtil.isArray(json)) {");
                body.append("    List<JsonNode> array = JsonUtil.toList(json);");
                body.append("    List<${primType}> items = new ArrayList<>();");
                body.append("    array.forEach(item -> {");
                body.append("        items.add(JsonUtil.${toMethod}(item));");
                body.append("    });");
                body.append("    return new ${unionValueClass}(items);");
                body.append("}");
            }
        }
        body.append("return null;");
        method.setBody(body.toString());
    }

    private void createReadRootFromSpec(SpecificationVersion specVersion, JavaClassSource readerClassSource) {
        if (specVersion.getRoot() == null) return;

        var rootTypeName = specVersion.getRoot().getType();
        var namespace = specVersion.getNamespace();

        // Try type alias (union root)
        var rootType = getState().getConceptIndex().lookupType(namespace, rootTypeName);
        if (rootType instanceof io.apitomy.umg.models.concept.type.UnionType unionType) {
            createUnionReadRootMethod(specVersion, readerClassSource, unionType);
            return;
        }

        // Single entity root
        var entity = getState().getConceptIndex().lookupEntity(namespace, rootTypeName);
        if (entity != null) {
            createReadRootMethod(specVersion, readerClassSource, entity);
            return;
        }

        warn("Root type '%s' not found in namespace '%s'", rootTypeName, namespace);
    }

    /**
     * Creates a "readRoot(json)" method for this reader.
     * @param specVersion
     * @param readerClassSource
     * @param entityModel
     */
    private void createReadRootMethod(SpecificationVersion specVersion, JavaClassSource readerClassSource, EntityModel entityModel) {
        JavaInterfaceSource rootNodeInterfaceSource = getState().getJavaIndex().lookupInterface(getRootNodeInterfaceFQN());
        readerClassSource.addImport(rootNodeInterfaceSource);
        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(ObjectNode.class);

        MethodSource<JavaClassSource> readRootMethodSource = readerClassSource.addMethod()
                .setName("readRoot")
                .setReturnType(rootNodeInterfaceSource.getName())
                .setPublic();
        readRootMethodSource.addParameter("JsonNode", "json");
        readRootMethodSource.addAnnotation(Override.class);

        String readMethodName = readMethodName(entityModel);
        JavaInterfaceSource entitySource = lookupJavaEntity(entityModel);
        JavaClassSource entityImplSource = lookupJavaEntityImpl(entityModel);

        readerClassSource.addImport(entitySource);
        readerClassSource.addImport(entityImplSource);

        BodyBuilder body = new BodyBuilder();
        body.addContext("readMethodName", readMethodName);
        body.addContext("rootEntityType", entitySource.getName());
        body.addContext("rootEntityImplType", entityImplSource.getName());

        body.append("${rootEntityType} rootModel = new ${rootEntityImplType}();");
        body.append("this.${readMethodName}((ObjectNode) json, rootModel);");
        body.append("return rootModel;");
        readRootMethodSource.setBody(body.toString());
    }

    private void createUnionReadRootMethod(SpecificationVersion specVersion, JavaClassSource readerClassSource,
                                              io.apitomy.umg.models.concept.type.UnionType unionType) {
        JavaInterfaceSource rootCapableSource = getState().getJavaIndex().lookupInterface(getRootNodeInterfaceFQN());
        readerClassSource.addImport(rootCapableSource);
        readerClassSource.addImport(JsonNode.class);

        JavaEnumSource modelTypeEnum = getState().getJavaIndex().lookupEnum(getModelTypeEnumFQN());
        readerClassSource.addImport(modelTypeEnum);

        var namespace = specVersion.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
        String readMethodName = "read" + jt.getSimpleName();

        MethodSource<JavaClassSource> readRootMethodSource = readerClassSource.addMethod()
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

    /**
     * Creates a single "readXyz" method for the given entity.
     *
     * @param specVersion
     * @param readerClassSource
     * @param entityModel
     */
    private void createReadMethodFor(SpecificationVersion specVersion, JavaClassSource readerClassSource, EntityModel entityModel) {
        String entityFQN = getJavaEntityInterfaceFQN(entityModel);
        String readMethodName = readMethodName(entityModel);

        debug("Creating read method: " + readMethodName);

        JavaInterfaceSource javaEntity = getState().getJavaIndex().lookupInterface(entityFQN);
        if (javaEntity == null) {
            warn("Java interface for entity not found: " + entityFQN);
        }

        readerClassSource.addImport(ObjectNode.class);
        readerClassSource.addImport(javaEntity);
        MethodSource<JavaClassSource> methodSource = readerClassSource.addMethod()
                .setName(readMethodName)
                .setReturnTypeVoid()
                .setPublic();
        methodSource.addParameter(ObjectNode.class.getSimpleName(), "json");
        methodSource.addParameter(javaEntity.getName(), "node");

        // Now create the body content for the reader.
        BodyBuilder body = new BodyBuilder();
        // Read each property of the entity
        Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entityModel);
        allProperties.forEach(property -> {
            createReadPropertyCode(body, property, entityModel, javaEntity, readerClassSource);
        });
        // Read "extra" properties (whatever is left over)
        createReadExtraPropertiesCode(body);

        methodSource.setBody(body.toString());
    }

    /**
     * Generates the right java code for reading a single property of an entity.
     *
     * @param body
     * @param property
     * @param entityModel
     * @param javaEntity
     * @param readerClassSource
     */
    private void createReadPropertyCode(BodyBuilder body, PropertyModelWithOrigin property, EntityModel entityModel,
            JavaInterfaceSource javaEntity, JavaClassSource readerClassSource) {
        CreateReadPropertySnippet crp = new CreateReadPropertySnippet(property, entityModel, javaEntity, readerClassSource);
        body.clearContext();
        crp.writeTo(body);
    }

    /**
     * Creates code that will read any extra/remaining properties on a JSON object.
     *
     * @param body
     */
    private void createReadExtraPropertiesCode(BodyBuilder body) {
        body.append("ReaderUtil.readExtraProperties(json, node);");
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
    private class CreateReadPropertySnippet {
        PropertyModelWithOrigin propertyWithOrigin;
        EntityModel entityModel;
        JavaInterfaceSource javaEntity;
        JavaClassSource readerClassSource;

        /**
         * Generates code to read a property from a JSON node into the data model.
         *
         * @param body
         */
        public void writeTo(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if ("*".equals(property.getName())) {
                handleStarProperty(body);
            } else if (property.getName().startsWith("/")) {
                handleRegexProperty(body);
            } else if (handleViaResolvedType(body)) {
                // Handled by resolved type dispatch
            } else {
                warn("Entity property '" + property.getName() + "' not read (unsupported) for entity: " + entityModel.fullyQualifiedName());
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

            // List of union
            if (resolved instanceof io.apitomy.umg.models.concept.type.ListType lt
                    && lt.getValueType() instanceof io.apitomy.umg.models.concept.type.UnionType) {
                handleResolvedUnionListProperty(body, lt);
                return true;
            }

            // Map of union
            if (resolved instanceof io.apitomy.umg.models.concept.type.MapType mt
                    && mt.getValueType() instanceof io.apitomy.umg.models.concept.type.UnionType) {
                handleResolvedUnionMapProperty(body, mt);
                return true;
            }

            // Entity
            if (resolved.isEntityType()) {
                handleEntityProperty(body);
                return true;
            }

            // Primitive
            if (resolved.isPrimitiveType()) {
                handlePrimitiveTypeProperty(body);
                return true;
            }

            // List (entity or primitive)
            if (resolved.isListType()) {
                handleListProperty(body);
                return true;
            }

            // Map (entity or primitive)
            if (resolved.isMapType()) {
                handleMapProperty(body);
                return true;
            }

            return false;
        }

        private void handleResolvedUnionProperty(BodyBuilder body, io.apitomy.umg.models.concept.type.Type resolved) {
            new ReadUnionPropertyBlock(propertyWithOrigin, readerClassSource, CreateReadersStage.this).appendTo(body);
        }

        private void handleResolvedUnionListProperty(BodyBuilder body,
                io.apitomy.umg.models.concept.type.ListType listType) {
            new ReadUnionListPropertyBlock(propertyWithOrigin, readerClassSource, CreateReadersStage.this).appendTo(body);
        }

        private void handleResolvedUnionMapProperty(BodyBuilder body,
                io.apitomy.umg.models.concept.type.MapType mapType) {
            new ReadUnionMapPropertyBlock(propertyWithOrigin, readerClassSource, CreateReadersStage.this).appendTo(body);
        }

        private void handleStarProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isEntity(property)) {
                var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                        entityModel, CreateReadersStage.this, "STAR");
                if (resolved == null) {
                    return;
                }
                readerClassSource.addImport(resolved.javaInterface());
                readerClassSource.addImport(List.class);
                readerClassSource.addImport(JsonNode.class);

                body.addContext("entityJavaType", resolved.javaInterface().getName());
                body.addContext("createMethodName", createMethodName(resolved.entityModel()));
                body.addContext("readMethodName", readMethodName(resolved.entityModel()));
                body.addContext("addMethodName", "addItem");

                body.append("{");
                body.append("    List<String> propertyNames = JsonUtil.keys(json);");
                body.append("    for (int _i = 0; _i < propertyNames.size(); _i++) {");
                body.append("        String name = propertyNames.get(_i);");
                body.append("        JsonNode _val = JsonUtil.getProperty(json, name);");
                body.append("        if (JsonUtil.isObject(_val)) {");
                body.append("            ${entityJavaType} model = (${entityJavaType}) node.${createMethodName}();");
                body.append("            node.${addMethodName}(name, model);");
                body.append("            this.${readMethodName}((ObjectNode) _val, model);");
                body.append("            json.remove(name);");
                body.append("        }");
                body.append("    }");
                body.append("}");
            } else if (isPrimitive(property)) {
                readerClassSource.addImport(List.class);
                readerClassSource.addImport(JsonNode.class);

                body.addContext("isCheckMethod", PrimitiveTypeHelper.determineIsCheckMethod(property.getResolvedType(), CreateReadersStage.this, readerClassSource));
                body.addContext("toConversionMethod", PrimitiveTypeHelper.determineToConversionMethod(property.getResolvedType(), CreateReadersStage.this, readerClassSource));

                body.append("{");
                body.append("    List<String> propertyNames = JsonUtil.keys(json);");
                body.append("    for (int _i = 0; _i < propertyNames.size(); _i++) {");
                body.append("        String name = propertyNames.get(_i);");
                body.append("        JsonNode _val = JsonUtil.getProperty(json, name);");
                body.append("        if (JsonUtil.${isCheckMethod}(_val)) {");
                body.append("            node.addItem(name, JsonUtil.${toConversionMethod}(_val));");
                body.append("            json.remove(name);");
                body.append("        }");
                body.append("    }");
                body.append("}");
            } else if (isPrimitiveList(property)) {
                readerClassSource.addImport(List.class);
                readerClassSource.addImport(ArrayList.class);
                readerClassSource.addImport(JsonNode.class);

                Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();
                String expectedType = PrimitiveTypeHelper.determineExpectedTypeString(listValueType, CreateReadersStage.this);
                String toConversionMethod = PrimitiveTypeHelper.determineToConversionMethod(listValueType, CreateReadersStage.this, readerClassSource);
                String elementValueType = PrimitiveTypeHelper.determineValueType(listValueType, CreateReadersStage.this, readerClassSource);
                body.addContext("expectedType", expectedType);
                body.addContext("toConversionMethod", toConversionMethod);
                body.addContext("elementValueType", elementValueType);

                body.append("{");
                body.append("    List<String> propertyNames = JsonUtil.keys(json);");
                body.append("    for (int _i = 0; _i < propertyNames.size(); _i++) {");
                body.append("        String name = propertyNames.get(_i);");
                body.append("        JsonNode _val = JsonUtil.getProperty(json, name);");
                body.append("        if (JsonUtil.isArray(_val) && JsonUtil.allMatch(_val, \"${expectedType}\")) {");
                body.append("            List<${elementValueType}> items = new ArrayList<>();");
                body.append("            List<JsonNode> _nodes = JsonUtil.toList(_val);");
                body.append("            for (int _j = 0; _j < _nodes.size(); _j++) {");
                body.append("                items.add(JsonUtil.${toConversionMethod}(_nodes.get(_j)));");
                body.append("            }");
                body.append("            node.addItem(name, items);");
                body.append("            json.remove(name);");
                body.append("        }");
                body.append("    }");
                body.append("}");
            } else if (isPrimitiveMap(property)) {
                readerClassSource.addImport(List.class);
                readerClassSource.addImport(JsonNode.class);
                readerClassSource.addImport(Map.class);
                readerClassSource.addImport(java.util.LinkedHashMap.class);

                Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType();
                String expectedType = PrimitiveTypeHelper.determineExpectedTypeString(mapValueType, CreateReadersStage.this);
                String toConversionMethod = PrimitiveTypeHelper.determineToConversionMethod(mapValueType, CreateReadersStage.this, readerClassSource);
                String elementValueType = PrimitiveTypeHelper.determineValueType(mapValueType, CreateReadersStage.this, readerClassSource);
                body.addContext("expectedType", expectedType);
                body.addContext("toConversionMethod", toConversionMethod);
                body.addContext("elementValueType", elementValueType);

                body.append("{");
                body.append("    List<String> propertyNames = JsonUtil.keys(json);");
                body.append("    for (int _i = 0; _i < propertyNames.size(); _i++) {");
                body.append("        String name = propertyNames.get(_i);");
                body.append("        JsonNode _val = JsonUtil.getProperty(json, name);");
                body.append("        if (JsonUtil.isObject(_val) && JsonUtil.allValuesMatch((ObjectNode) _val, \"${expectedType}\")) {");
                body.append("            Map<String, ${elementValueType}> items = new LinkedHashMap<>();");
                body.append("            List<String> _keys = JsonUtil.keys((ObjectNode) _val);");
                body.append("            for (int _j = 0; _j < _keys.size(); _j++) {");
                body.append("                String _key = _keys.get(_j);");
                body.append("                items.put(_key, JsonUtil.${toConversionMethod}(JsonUtil.getProperty((ObjectNode) _val, _key)));");
                body.append("            }");
                body.append("            node.addItem(name, items);");
                body.append("            json.remove(name);");
                body.append("        }");
                body.append("    }");
                body.append("}");
            } else {
                warn("STAR Entity property '" + property.getName() + "' not read (unhandled) for entity: " + entityModel.fullyQualifiedName());
                warn("       property type: " + property.getResolvedType());
            }
        }

        private void handleRegexProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isEntity(property)) {
                var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                        entityModel, CreateReadersStage.this, "REGEX");
                if (resolved == null) {
                    return;
                }
                readerClassSource.addImport(resolved.javaInterface());
                readerClassSource.addImport(List.class);
                readerClassSource.addImport(JsonNode.class);

                body.addContext("propertyRegex", encodeRegex(extractRegex(property.getName())));
                body.addContext("entityJavaType", resolved.javaInterface().getName());
                body.addContext("createMethodName", createMethodName(resolved.entityModel()));
                body.addContext("readMethodName", readMethodName(resolved.entityModel()));
                body.addContext("addMethodName", addMethodName(singularize(property.getCollection())));

                body.append("{");
                body.append("    List<String> propertyNames = JsonUtil.matchingKeys(\"${propertyRegex}\", json);");
                body.append("    for (int _i = 0; _i < propertyNames.size(); _i++) {");
                body.append("        String name = propertyNames.get(_i);");
                body.append("        JsonNode _val = JsonUtil.getProperty(json, name);");
                body.append("        if (JsonUtil.isObject(_val)) {");
                body.append("            ${entityJavaType} model = (${entityJavaType}) node.${createMethodName}();");
                body.append("            node.${addMethodName}(name, model);");
                body.append("            this.${readMethodName}((ObjectNode) _val, model);");
                body.append("            json.remove(name);");
                body.append("        }");
                body.append("    }");
                body.append("}");
            } else if (isPrimitive(property)) {
                readerClassSource.addImport(List.class);
                readerClassSource.addImport(JsonNode.class);

                body.addContext("propertyRegex", encodeRegex(extractRegex(property.getName())));
                body.addContext("isCheckMethod", PrimitiveTypeHelper.determineIsCheckMethod(property.getResolvedType(), CreateReadersStage.this, readerClassSource));
                body.addContext("toConversionMethod", PrimitiveTypeHelper.determineToConversionMethod(property.getResolvedType(), CreateReadersStage.this, readerClassSource));
                body.addContext("addMethodName", addMethodName(singularize(property.getCollection())));

                body.append("{");
                body.append("    List<String> propertyNames = JsonUtil.matchingKeys(\"${propertyRegex}\", json);");
                body.append("    for (int _i = 0; _i < propertyNames.size(); _i++) {");
                body.append("        String name = propertyNames.get(_i);");
                body.append("        JsonNode _val = JsonUtil.getProperty(json, name);");
                body.append("        if (JsonUtil.${isCheckMethod}(_val)) {");
                body.append("            node.${addMethodName}(name, JsonUtil.${toConversionMethod}(_val));");
                body.append("            json.remove(name);");
                body.append("        }");
                body.append("    }");
                body.append("}");
            } else if (isPrimitiveList(property)) {
                readerClassSource.addImport(List.class);
                readerClassSource.addImport(ArrayList.class);
                readerClassSource.addImport(JsonNode.class);

                Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();
                String expectedType = PrimitiveTypeHelper.determineExpectedTypeString(listValueType, CreateReadersStage.this);
                String toConversionMethod = PrimitiveTypeHelper.determineToConversionMethod(listValueType, CreateReadersStage.this, readerClassSource);
                String elementValueType = PrimitiveTypeHelper.determineValueType(listValueType, CreateReadersStage.this, readerClassSource);
                body.addContext("expectedType", expectedType);
                body.addContext("toConversionMethod", toConversionMethod);
                body.addContext("elementValueType", elementValueType);
                body.addContext("propertyRegex", encodeRegex(extractRegex(property.getName())));
                body.addContext("addMethodName", addMethodName(singularize(property.getCollection())));

                body.append("{");
                body.append("    List<String> propertyNames = JsonUtil.matchingKeys(\"${propertyRegex}\", json);");
                body.append("    for (int _i = 0; _i < propertyNames.size(); _i++) {");
                body.append("        String name = propertyNames.get(_i);");
                body.append("        JsonNode _val = JsonUtil.getProperty(json, name);");
                body.append("        if (JsonUtil.isArray(_val) && JsonUtil.allMatch(_val, \"${expectedType}\")) {");
                body.append("            List<${elementValueType}> items = new ArrayList<>();");
                body.append("            List<JsonNode> _nodes = JsonUtil.toList(_val);");
                body.append("            for (int _j = 0; _j < _nodes.size(); _j++) {");
                body.append("                items.add(JsonUtil.${toConversionMethod}(_nodes.get(_j)));");
                body.append("            }");
                body.append("            node.${addMethodName}(name, items);");
                body.append("            json.remove(name);");
                body.append("        }");
                body.append("    }");
                body.append("}");
            } else if (isPrimitiveMap(property)) {
                readerClassSource.addImport(List.class);
                readerClassSource.addImport(JsonNode.class);
                readerClassSource.addImport(Map.class);
                readerClassSource.addImport(java.util.LinkedHashMap.class);

                Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType();
                String expectedType = PrimitiveTypeHelper.determineExpectedTypeString(mapValueType, CreateReadersStage.this);
                String toConversionMethod = PrimitiveTypeHelper.determineToConversionMethod(mapValueType, CreateReadersStage.this, readerClassSource);
                String elementValueType = PrimitiveTypeHelper.determineValueType(mapValueType, CreateReadersStage.this, readerClassSource);
                body.addContext("expectedType", expectedType);
                body.addContext("toConversionMethod", toConversionMethod);
                body.addContext("elementValueType", elementValueType);
                body.addContext("propertyRegex", encodeRegex(extractRegex(property.getName())));
                body.addContext("addMethodName", addMethodName(singularize(property.getCollection())));

                body.append("{");
                body.append("    List<String> propertyNames = JsonUtil.matchingKeys(\"${propertyRegex}\", json);");
                body.append("    for (int _i = 0; _i < propertyNames.size(); _i++) {");
                body.append("        String name = propertyNames.get(_i);");
                body.append("        JsonNode _val = JsonUtil.getProperty(json, name);");
                body.append("        if (JsonUtil.isObject(_val) && JsonUtil.allValuesMatch((ObjectNode) _val, \"${expectedType}\")) {");
                body.append("            Map<String, ${elementValueType}> items = new LinkedHashMap<>();");
                body.append("            List<String> _keys = JsonUtil.keys((ObjectNode) _val);");
                body.append("            for (int _j = 0; _j < _keys.size(); _j++) {");
                body.append("                String _key = _keys.get(_j);");
                body.append("                items.put(_key, JsonUtil.${toConversionMethod}(JsonUtil.getProperty((ObjectNode) _val, _key)));");
                body.append("            }");
                body.append("            node.${addMethodName}(name, items);");
                body.append("            json.remove(name);");
                body.append("        }");
                body.append("    }");
                body.append("}");
            } else {
                warn("REGEX Entity property '" + property.getName() + "' not read (unsupported) for entity: " + entityModel.fullyQualifiedName());
                warn("       property type: " + property.getResolvedType());
            }
        }

        private void handleEntityProperty(BodyBuilder body) {
            new ReadEntityPropertyBlock(propertyWithOrigin, entityModel, readerClassSource, CreateReadersStage.this).appendTo(body);
        }

        private void handlePrimitiveTypeProperty(BodyBuilder body) {
            new ReadPrimitivePropertyBlock(propertyWithOrigin, readerClassSource, CreateReadersStage.this).appendTo(body);
        }

        private void handleListProperty(BodyBuilder body) {
            new ReadListPropertyBlock(propertyWithOrigin, entityModel, readerClassSource, CreateReadersStage.this).appendTo(body);
        }

        private void handleMapProperty(BodyBuilder body) {
            new ReadMapPropertyBlock(propertyWithOrigin, entityModel, readerClassSource, CreateReadersStage.this).appendTo(body);
        }




        private String encodeRegex(String regex) {
            return regex.replace("\\", "\\\\");
        }
    }
}
