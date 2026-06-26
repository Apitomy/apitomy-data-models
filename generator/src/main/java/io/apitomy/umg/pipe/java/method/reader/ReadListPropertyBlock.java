package io.apitomy.umg.pipe.java.method.reader;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;

/**
 * Generates code to read a list property from JSON (primitive list or entity list).
 */
public class ReadListPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadListPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
            JavaClassSource readerClassSource, CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.entityModel = entityModel;
        this.readerClassSource = readerClassSource;
        this.ctx = ctx;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = propertyWithOrigin.getProperty();
        body.addContext("propertyName", property.getName());
        body.addContext("setterMethodName", ctx.setterMethodName(property));

        Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();
        if (listValueType.isPrimitiveType()) {
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(List.class);
            readerClassSource.addImport(ArrayList.class);

            String expectedType = PrimitiveTypeHelper.determineExpectedTypeString(listValueType, ctx);
            String toConversionMethod = PrimitiveTypeHelper.determineToConversionMethod(listValueType, ctx, readerClassSource);
            String elementValueType = PrimitiveTypeHelper.determineValueType(listValueType, ctx, readerClassSource);
            body.addContext("expectedType", expectedType);
            body.addContext("toConversionMethod", toConversionMethod);
            body.addContext("elementValueType", elementValueType);
            body.addContext("varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"));

            body.append("{");
            body.append("    JsonNode ${varName} = JsonUtil.getProperty(json, \"${propertyName}\");");
            body.append("    if (JsonUtil.isArray(${varName}) && JsonUtil.allMatch(${varName}, \"${expectedType}\")) {");
            body.append("        List<${elementValueType}> items = new ArrayList<>();");
            body.append("        List<JsonNode> _nodes = JsonUtil.toList(${varName});");
            body.append("        for (int _i = 0; _i < _nodes.size(); _i++) {");
            body.append("            items.add(JsonUtil.${toConversionMethod}(_nodes.get(_i)));");
            body.append("        }");
            body.append("        node.${setterMethodName}(items);");
            body.append("        json.remove(\"${propertyName}\");");
            body.append("    }");
            body.append("}");
        } else if (listValueType.isEntityType()) {
            var resolved = EntityResolver.resolveEntityInterface(property, listValueType.getName(), entityModel, ctx, "LIST");
            if (resolved == null) {
                return;
            }
            readerClassSource.addImport(resolved.javaInterface());
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(List.class);

            body.addContext("listValueJavaType", resolved.javaInterface().getName());
            body.addContext("createMethodName", ctx.createMethodName(resolved.entityModel()));
            body.addContext("readMethodName", ctx.readMethodName(resolved.entityModel()));
            body.addContext("addMethodName", ctx.addMethodName(ctx.singularize(property.getName())));
            body.addContext("varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"));

            body.append("{");
            body.append("    JsonNode ${varName} = JsonUtil.getProperty(json, \"${propertyName}\");");
            body.append("    if (JsonUtil.isArray(${varName}) && JsonUtil.allMatch(${varName}, \"object\")) {");
            body.append("        List<JsonNode> _nodes = JsonUtil.toList(${varName});");
            body.append("        for (int _i = 0; _i < _nodes.size(); _i++) {");
            body.append("            ObjectNode object = JsonUtil.toObject(_nodes.get(_i));");
            body.append("            ${listValueJavaType} model = (${listValueJavaType}) node.${createMethodName}();");
            body.append("            node.${addMethodName}(model);");
            body.append("            this.${readMethodName}(object, model);");
            body.append("        }");
            body.append("        json.remove(\"${propertyName}\");");
            body.append("    }");
            body.append("}");
        } else {
            ctx.warn("LIST Entity property '" + property.getName() + "' not read (unsupported) for entity: " + entityModel.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
