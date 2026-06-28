package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.beans.UnionRuleType;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.PrimitiveType;
import io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.java.type.JavaType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import io.apitomy.umg.pipe.java.Util;

/**
 * Naming class for reader methods: {@code read${EntityName}}.
 * Also generates full union reader dispatch methods via {@link #writeTo}.
 */
public class ReaderMethod implements Method {

    private final String entityName;

    public ReaderMethod(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Returns the reader method name for the given entity name.
     */
    public static String methodName(String entityName) {
        return "read" + StringUtils.capitalize(entityName);
    }

    @Override
    public String getName() {
        return methodName(entityName);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed for the naming-only use case
    }

    /**
     * Generates the full union reader dispatch method on the given reader class.
     * Creates a method {@code readUnionName(JsonNode json, ModelType modelType)} that
     * dispatches to the correct variant based on JSON shape.
     */
    public void writeTo(JavaClassSource readerClassSource, SpecificationVersion specVersion,
                        UnionType unionType, CodeGenContext ctx) {
        String namespace = specVersion.getNamespace();
        NamespaceModel nsModel = ctx.getConceptIndex().lookupNamespace(namespace);
        JavaType jt = ctx.getJavaTypeFactory().createJavaType(unionType, nsModel);
        String unionTypeName = jt.getSimpleName();
        String methodName = methodName(unionTypeName);

        // Skip if already created
        if (readerClassSource.getMethod(methodName, JsonNode.class.getSimpleName(), "ModelType") != null) {
            return;
        }

        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(ObjectNode.class);
        jt.addImportsTo(readerClassSource);

        JavaEnumSource modelTypeEnum = ctx.getJavaIndex().lookupEnum(ctx.getModelTypeEnumFQN());
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
            if (variantType instanceof EntityType entityType) {
                var entity = entityType.getEntity();
                if (entity == null) entity = ctx.getConceptIndex().lookupEntity(namespace, entityType.getName());
                if (entity == null) continue;

                JavaInterfaceSource entitySource = ctx.lookupJavaEntity(entity);
                JavaClassSource entityImplSource = ctx.lookupJavaEntityImpl(entity);
                readerClassSource.addImport(entitySource);
                readerClassSource.addImport(entityImplSource);

                body.addContext("entityType", entitySource.getName());
                body.addContext("entityImplType", entityImplSource.getName());
                body.addContext("readMethodName", methodName(entity.getName()));

                var rule = unionType.getRuleFor(entityType.getName());
                String condition;
                if (rule != null && rule.getRuleType() == UnionRuleType.PROPERTYEXISTS) {
                    body.addContext("rulePropName", rule.getPropertyName());
                    condition = "JsonUtil.isObjectWithProperty(json, \"${rulePropName}\")";
                } else if (rule != null && rule.getRuleType() == UnionRuleType.PROPERTYVALUE) {
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
            } else if (variantType instanceof PrimitiveUnionVariantType puv) {
                Class<?> javaClass = Util.PRIMITIVE_TYPE_MAP.get(puv.getType().name().toLowerCase());
                if (javaClass == null) continue;

                String typeName = JavaTypeFactory.getUnionComponentName(variantType);
                String unionValueClassFQN = ctx.getUnionTypeFQN(typeName + "UnionValueImpl");
                JavaClassSource unionValueClass = ctx.getJavaIndex().lookupClass(unionValueClassFQN);
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
            } else if (variantType instanceof ListType listType
                    && listType.getValueType() instanceof EntityType listEntityType) {
                var entity = listEntityType.getEntity();
                if (entity == null) entity = ctx.getConceptIndex().lookupEntity(namespace, listEntityType.getName());
                if (entity == null) continue;

                String typeName = JavaTypeFactory.getUnionComponentName(variantType);
                String unionValueClassFQN = ctx.getUnionTypeFQN(typeName + "UnionValueImpl");
                JavaClassSource unionValueClass = ctx.getJavaIndex().lookupClass(unionValueClassFQN);
                if (unionValueClass == null) {
                    unionValueClass = ctx.getJavaIndex().lookupClass(
                            ctx.resolveUnionPackage(unionType) + "." + typeName + "UnionValueImpl");
                }
                if (unionValueClass == null) continue;

                JavaInterfaceSource entitySource = ctx.lookupJavaEntity(entity);
                readerClassSource.addImport(entitySource);
                readerClassSource.addImport(unionValueClass);
                readerClassSource.addImport(java.util.List.class);
                readerClassSource.addImport(java.util.ArrayList.class);

                body.addContext("listValueType", entitySource.getName());
                body.addContext("readMethodName", methodName(entity.getName()));
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
            } else if (variantType instanceof ListType listType
                    && listType.getValueType() instanceof PrimitiveType primType) {
                String typeName = JavaTypeFactory.getUnionComponentName(variantType);
                String unionValueClassFQN = ctx.getUnionTypeFQN(typeName + "UnionValueImpl");
                JavaClassSource unionValueClass = ctx.getJavaIndex().lookupClass(unionValueClassFQN);
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
}
