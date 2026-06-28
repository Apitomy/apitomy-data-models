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
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;
import io.apitomy.umg.pipe.java.method.UnionIsMethod;
import io.apitomy.umg.pipe.java.method.reader.ReadEntityPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadListPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadPrimitivePropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadRegexPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadStarPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionListPropertyBlock;
import io.apitomy.umg.pipe.java.method.reader.ReadUnionMapPropertyBlock;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Creates the i/o reader classes.  There is a bespoke reader for each specification
 * version.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateReadersStage extends AbstractJavaStage {

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
        String methodName = ReaderMethod.methodName(unionTypeName);

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
                body.addContext("readMethodName", ReaderMethod.methodName(entity.getName()));

                var rule = unionType.getRuleFor(entityType.getName());
                String condition;
                if (rule != null && rule.getRuleType() == io.apitomy.umg.beans.UnionRuleType.PROPERTYEXISTS) {
                    body.addContext("rulePropName", rule.getPropertyName());
                    condition = "JsonUtil.isObjectWithProperty(json, \"${rulePropName}\")";
                } else if (rule != null && rule.getRuleType() == io.apitomy.umg.beans.UnionRuleType.PROPERTYVALUE) {
                    body.addContext("rulePropName", rule.getPropertyName());
                    body.addContext("rulePropValue", rule.getPropertyValue());
                    condition = "JsonUtil.isObjectWithStringPropertyValue(json, \"${rulePropName}\", \"${rulePropValue}\")";
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

                body.addContext("isMethod", UnionIsMethod.methodName(javaClass.getSimpleName()));
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
                body.addContext("readMethodName", ReaderMethod.methodName(entity.getName()));
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

        String readMethodName = ReaderMethod.methodName(entityModel.getName());
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
        String readMethodName = ReaderMethod.methodName(jt.getSimpleName());

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
        String readMethodName = ReaderMethod.methodName(entityModel.getName());

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
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadUnionPropertyBlock(prop, readerClassSource).appendTo(body);
        }

        private void handleResolvedUnionListProperty(BodyBuilder body,
                io.apitomy.umg.models.concept.type.ListType listType) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadUnionListPropertyBlock(prop, readerClassSource).appendTo(body);
        }

        private void handleResolvedUnionMapProperty(BodyBuilder body,
                io.apitomy.umg.models.concept.type.MapType mapType) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadUnionMapPropertyBlock(prop, readerClassSource).appendTo(body);
        }

        private void handleStarProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadStarPropertyBlock(prop, readerClassSource).appendTo(body);
        }

        private void handleRegexProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadRegexPropertyBlock(prop, readerClassSource).appendTo(body);
        }

        private void handleEntityProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadEntityPropertyBlock(prop, readerClassSource).appendTo(body);
        }

        private void handlePrimitiveTypeProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadPrimitivePropertyBlock(prop, readerClassSource).appendTo(body);
        }

        private void handleListProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadListPropertyBlock(prop, readerClassSource).appendTo(body);
        }

        private void handleMapProperty(BodyBuilder body) {
            PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);
            new ReadMapPropertyBlock(prop, readerClassSource).appendTo(body);
        }

    }
}
