package io.apitomy.umg.pipe.java.method.reader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
 * Generates code to read a map property from JSON (primitive map or entity map).
 */
public class ReadMapPropertyBlock extends CodeBlock {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel entityModel;
    private final JavaClassSource readerClassSource;
    private final CodeGenContext ctx;

    public ReadMapPropertyBlock(PropertyModelWithOrigin propertyWithOrigin, EntityModel entityModel,
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

        Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType();
        if (mapValueType.isPrimitiveType()) {
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(ObjectNode.class);
            readerClassSource.addImport(Map.class);
            readerClassSource.addImport(LinkedHashMap.class);
            readerClassSource.addImport(List.class);

            String expectedType = PrimitiveTypeHelper.determineExpectedTypeString(mapValueType, ctx);
            String toConversionMethod = PrimitiveTypeHelper.determineToConversionMethod(mapValueType, ctx, readerClassSource);
            String elementValueType = PrimitiveTypeHelper.determineValueType(mapValueType, ctx, readerClassSource);
            body.addContext("expectedType", expectedType);
            body.addContext("toConversionMethod", toConversionMethod);
            body.addContext("elementValueType", elementValueType);
            body.addContext("varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"));

            body.append("{");
            body.append("    JsonNode ${varName} = JsonUtil.getProperty(json, \"${propertyName}\");");
            body.append("    if (JsonUtil.isObject(${varName}) && JsonUtil.allValuesMatch(JsonUtil.toObject(${varName}), \"${expectedType}\")) {");
            body.append("        Map<String, ${elementValueType}> items = new LinkedHashMap<>();");
            body.append("        List<String> _keys = JsonUtil.keys(JsonUtil.toObject(${varName}));");
            body.append("        for (int _i = 0; _i < _keys.size(); _i++) {");
            body.append("            String _key = _keys.get(_i);");
            body.append("            items.put(_key, JsonUtil.${toConversionMethod}(JsonUtil.getProperty(JsonUtil.toObject(${varName}), _key)));");
            body.append("        }");
            body.append("        node.${setterMethodName}(items);");
            body.append("        JsonUtil.removeProperty(json, \"${propertyName}\");");
            body.append("    }");
            body.append("}");
        } else if (mapValueType.isEntityType()) {
            String entityTypeName = mapValueType.getName();
            var resolved = EntityResolver.resolveEntityInterface(property, entityTypeName, entityModel, ctx, "MAP");
            if (resolved == null) {
                return;
            }
            readerClassSource.addImport(resolved.javaInterface());
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(ObjectNode.class);
            readerClassSource.addImport(List.class);

            body.addContext("mapValueJavaType", resolved.javaInterface().getName());
            body.addContext("createMethodName", "create" + entityTypeName);
            body.addContext("readMethodName", "read" + entityTypeName);
            body.addContext("addMethodName", ctx.addMethodName(ctx.singularize(property.getName())));
            body.addContext("varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_"));

            body.append("{");
            body.append("    JsonNode ${varName} = JsonUtil.getProperty(json, \"${propertyName}\");");
            body.append("    if (JsonUtil.isObject(${varName})) {");
            body.append("        ObjectNode _obj = JsonUtil.toObject(${varName});");
            body.append("        List<String> _keys = JsonUtil.keys(_obj);");
            body.append("        for (int _i = 0; _i < _keys.size(); _i++) {");
            body.append("            String _key = _keys.get(_i);");
            body.append("            JsonNode _val = JsonUtil.getProperty(_obj, _key);");
            body.append("            if (JsonUtil.isObject(_val)) {");
            body.append("                ${mapValueJavaType} model = (${mapValueJavaType}) node.${createMethodName}();");
            body.append("                node.${addMethodName}(_key, model);");
            body.append("                this.${readMethodName}(JsonUtil.toObject(_val), model);");
            body.append("            }");
            body.append("        }");
            body.append("        JsonUtil.removeProperty(json, \"${propertyName}\");");
            body.append("    }");
            body.append("}");
        } else {
            ctx.warn("MAP Entity property '" + property.getName() + "' not read (unsupported) for entity: " + entityModel.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
