package io.apitomy.umg.pipe.java.method;

import java.util.Map;

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
    private final SpecificationVersion specVersion;
    private final UnionType unionType;
    private final CodeGenContext ctx;

    public WriterMethod(String entityName) {
        this.entityName = entityName;
        this.specVersion = null;
        this.unionType = null;
        this.ctx = null;
    }

    public WriterMethod(SpecificationVersion specVersion, UnionType unionType, CodeGenContext ctx) {
        NamespaceModel nsModel = ctx.getConceptIndex().lookupNamespace(specVersion.getNamespace());
        JavaType jt = ctx.getJavaTypeFactory().createJavaType(unionType, nsModel);
        this.entityName = jt.getSimpleName();
        this.specVersion = specVersion;
        this.unionType = unionType;
        this.ctx = ctx;
    }



    @Override
    public String getName() {
        return "write" + StringUtils.capitalize(entityName);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // No imports needed for the naming-only use case
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        if (specVersion == null || unionType == null || ctx == null) {
            throw new UnsupportedOperationException(
                    "WriterMethod requires full context constructor for writeTo");
        }
        writeUnionWriterMethod((JavaClassSource) target);
    }

    private void writeUnionWriterMethod(JavaClassSource writerClassSource) {
        String namespace = specVersion.getNamespace();
        NamespaceModel nsModel = ctx.getConceptIndex().lookupNamespace(namespace);
        JavaType jt = ctx.getJavaTypeFactory().createJavaType(unionType, nsModel);
        String unionTypeName = jt.getSimpleName();
        String methodName = new WriterMethod(unionTypeName).getName();

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

                body.addContext(Map.of(
                        "isMethod", new UnionIsMethod(typeName).getName(),
                        "asMethod", new UnionAsMethod(typeName).getName(),
                        "entityType", entitySource.getName(),
                        "writeMethodName", new WriterMethod(entity.getName()).getName()
                ));

                body.append("if (union.${isMethod}()) {");
                body.append("    ObjectNode jsonValue = JsonUtil.objectNode();");
                body.append("    this.${writeMethodName}((${entityType}) union.${asMethod}(), jsonValue);");
                body.append("    return jsonValue;");
                body.append("}");
            } else if (variantType instanceof PrimitiveUnionVariantType puv) {
                String typeName = JavaTypeFactory.getUnionComponentName(variantType);
                Class<?> javaClass = PrimitiveTypeUtil.PRIMITIVE_TYPE_MAP.get(puv.getType().name().toLowerCase());
                if (javaClass == null) continue;

                body.addContext("isMethod", new UnionIsMethod(typeName).getName());
                body.addContext("asMethod", new UnionAsMethod(typeName).getName());

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

                body.addContext("isMethod", new UnionIsMethod(typeName).getName());
                body.addContext("asMethod", new UnionAsMethod(typeName).getName());

                writerClassSource.addImport(ArrayNode.class);

                if (listType.getValueType() instanceof EntityType listEntityType) {
                    var entity = listEntityType.getEntity();
                    if (entity == null) entity = ctx.getConceptIndex().lookupEntity(namespace, listEntityType.getName());
                    if (entity == null) continue;

                    JavaInterfaceSource entitySource = ctx.lookupJavaEntity(entity);
                    writerClassSource.addImport(entitySource);
                    body.addContext("entityType", entitySource.getName());
                    body.addContext("writeMethodName", new WriterMethod(entity.getName()).getName());

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
                    Class<?> javaClass = PrimitiveTypeUtil.PRIMITIVE_TYPE_MAP.get(primType.name().toLowerCase());
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
