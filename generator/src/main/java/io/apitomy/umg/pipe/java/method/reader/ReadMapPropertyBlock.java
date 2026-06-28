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

            body.appendBlock("""
{
    JsonNode ${varName} = JsonUtil.getProperty(json, "${propertyName}");
    if (JsonUtil.isObject(${varName}) && JsonUtil.allValuesMatch(JsonUtil.toObject(${varName}), "${expectedType}")) {
        Map<String, ${elementValueType}> items = new LinkedHashMap<>();
        List<String> _keys = JsonUtil.keys(JsonUtil.toObject(${varName}));
        for (int _i = 0; _i < _keys.size(); _i++) {
            String _key = _keys.get(_i);
            items.put(_key, JsonUtil.${toConversionMethod}(JsonUtil.getProperty(JsonUtil.toObject(${varName}), _key)));
        }
        node.${setterMethodName}(items);
        JsonUtil.removeProperty(json, "${propertyName}");
    }
}
""");
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

            body.appendBlock("""
{
    JsonNode ${varName} = JsonUtil.getProperty(json, "${propertyName}");
    if (JsonUtil.isObject(${varName})) {
        ObjectNode _obj = JsonUtil.toObject(${varName});
        List<String> _keys = JsonUtil.keys(_obj);
        for (int _i = 0; _i < _keys.size(); _i++) {
            String _key = _keys.get(_i);
            JsonNode _val = JsonUtil.getProperty(_obj, _key);
            if (JsonUtil.isObject(_val)) {
                ${mapValueJavaType} model = (${mapValueJavaType}) node.${createMethodName}();
                node.${addMethodName}(_key, model);
                this.${readMethodName}(JsonUtil.toObject(_val), model);
            }
        }
        JsonUtil.removeProperty(json, "${propertyName}");
    }
}
""");
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
