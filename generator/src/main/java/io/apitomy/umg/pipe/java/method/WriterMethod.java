package io.apitomy.umg.pipe.java.method;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.PrimitiveType;
import io.apitomy.umg.models.concept.type.PrimitiveUnionVariantType;
import io.apitomy.umg.models.concept.type.UnionType;
import io.apitomy.umg.models.java.type.JavaType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;


/**
 * Naming class for writer methods: {@code write${EntityName}}.
 * Also generates full union writer dispatch methods via {@link #writeTo}.
 */
public class WriterMethod implements Method {

    private final String entityName;

    public WriterMethod(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Returns the writer method name for the given entity name.
     */
    public static String methodName(String entityName) {
        return "write" + StringUtils.capitalize(entityName);
    }

    @Override
    public String getName() {
        return methodName(entityName);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed for the naming-only use case
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        throw new UnsupportedOperationException(
                "WriterMethod requires additional context; use writeTo(JavaClassSource, SpecificationVersion, UnionType, CodeGenContext) instead");
    }

    /**
     * Generates the full union writer dispatch method on the given writer class.
     * Creates a method {@code writeUnionName(UnionType union)} that dispatches
     * to the correct variant writer based on is/as methods.
     */
    public void writeTo(JavaClassSource writerClassSource, SpecificationVersion specVersion,
                        UnionType unionType, CodeGenContext ctx) {
        String namespace = specVersion.getNamespace();
        NamespaceModel nsModel = ctx.getConceptIndex().lookupNamespace(namespace);
        JavaType jt = ctx.getJavaTypeFactory().createJavaType(unionType, nsModel);
        String unionTypeName = jt.getSimpleName();
        String methodName = methodName(unionTypeName);

        // Skip if already created
        if (writerClassSource.getMethods().stream().anyMatch(m -> m.getName().equals(methodName))) {
            return;
        }

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
            if (variantType instanceof EntityType entityType) {
                var entity = entityType.getEntity();
                if (entity == null) entity = ctx.getConceptIndex().lookupEntity(namespace, entityType.getName());
                if (entity == null) continue;

                String typeName = JavaTypeFactory.getUnionComponentName(variantType);
                JavaInterfaceSource entitySource = ctx.lookupJavaEntity(entity);
                writerClassSource.addImport(entitySource);

                body.addContext("isMethod", UnionIsMethod.methodName(typeName));
                body.addContext("asMethod", UnionAsMethod.methodName(typeName));
                body.addContext("entityType", entitySource.getName());
                body.addContext("writeMethodName", methodName(entity.getName()));

                body.append("if (union.${isMethod}()) {");
                body.append("    ObjectNode jsonValue = JsonUtil.objectNode();");
                body.append("    this.${writeMethodName}((${entityType}) union.${asMethod}(), jsonValue);");
                body.append("    return jsonValue;");
                body.append("}");
            } else if (variantType instanceof PrimitiveUnionVariantType puv) {
                String typeName = JavaTypeFactory.getUnionComponentName(variantType);
                Class<?> javaClass = PrimitiveTypeHelper.PRIMITIVE_TYPE_MAP.get(puv.getType().name().toLowerCase());
                if (javaClass == null) continue;

                body.addContext("isMethod", UnionIsMethod.methodName(typeName));
                body.addContext("asMethod", UnionAsMethod.methodName(typeName));

                if (JsonNode.class.isAssignableFrom(javaClass)) {
                    body.append("if (union.${isMethod}()) {");
                    body.append("    return union.${asMethod}();");
                    body.append("}");
                } else {
                    body.append("if (union.${isMethod}()) {");
                    body.append("    return JsonUtil.toJsonNode(union.${asMethod}());");
                    body.append("}");
                }
            } else if (variantType instanceof ListType listType) {
                String typeName = JavaTypeFactory.getUnionComponentName(variantType);

                body.addContext("isMethod", UnionIsMethod.methodName(typeName));
                body.addContext("asMethod", UnionAsMethod.methodName(typeName));

                writerClassSource.addImport(ArrayNode.class);

                if (listType.getValueType() instanceof EntityType listEntityType) {
                    var entity = listEntityType.getEntity();
                    if (entity == null) entity = ctx.getConceptIndex().lookupEntity(namespace, listEntityType.getName());
                    if (entity == null) continue;

                    JavaInterfaceSource entitySource = ctx.lookupJavaEntity(entity);
                    writerClassSource.addImport(entitySource);
                    body.addContext("entityType", entitySource.getName());
                    body.addContext("writeMethodName", methodName(entity.getName()));

                    body.append("if (union.${isMethod}()) {");
                    body.append("    ArrayNode array = JsonUtil.arrayNode();");
                    body.append("    for (Object item : (java.util.List<?>) union.${asMethod}()) {");
                    body.append("        ObjectNode itemNode = JsonUtil.objectNode();");
                    body.append("        this.${writeMethodName}((${entityType}) item, itemNode);");
                    body.append("        array.add(itemNode);");
                    body.append("    }");
                    body.append("    return array;");
                    body.append("}");
                } else if (listType.getValueType() instanceof PrimitiveType primType) {
                    Class<?> javaClass = PrimitiveTypeHelper.PRIMITIVE_TYPE_MAP.get(primType.name().toLowerCase());
                    if (javaClass == null) continue;

                    writerClassSource.addImport(javaClass);
                    body.addContext("primType", javaClass.getSimpleName());

                    body.addContext("addExpr", "array.add(JsonUtil.toJsonNode(item))");
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
}
