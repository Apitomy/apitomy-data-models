package io.apitomy.umg.pipe.java.method.reader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.AddMethod;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeUtil;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;

/**
 * Generates code to read a map property from JSON (primitive map or entity map).
 */
public class ReadMapPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadMapPropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
        this.prop = prop;
        this.readerClassSource = readerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type mapValueType = ((MapType) property.getResolvedType()).getValueType();
        if (mapValueType.isPrimitiveType()) {
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(ObjectNode.class);
            readerClassSource.addImport(Map.class);
            readerClassSource.addImport(LinkedHashMap.class);
            readerClassSource.addImport(List.class);

            String expectedType = PrimitiveTypeUtil.determineExpectedTypeString(mapValueType, prop.getCtx());
            String toConversionMethod = PrimitiveTypeUtil.determineToConversionMethod(mapValueType, prop.getCtx(), readerClassSource);
            String elementValueType = PrimitiveTypeUtil.determineValueType(mapValueType, prop.getCtx(), readerClassSource);
            body.addContext(Map.of(
                    "propertyName", property.getName(),
                    "setterMethodName", prop.getSetterName(),
                    "expectedType", expectedType,
                    "toConversionMethod", toConversionMethod,
                    "elementValueType", elementValueType,
                    "varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_")
            ));

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
            var resolved = EntityResolver.resolveEntityInterface(property, entityTypeName, prop.getOwningEntity(), prop.getCtx(), "MAP");
            if (resolved == null) {
                return;
            }
            readerClassSource.addImport(resolved.javaInterface());
            readerClassSource.addImport(JsonNode.class);
            readerClassSource.addImport(ObjectNode.class);
            readerClassSource.addImport(List.class);

            body.addContext(Map.of(
                    "propertyName", property.getName(),
                    "setterMethodName", prop.getSetterName(),
                    "mapValueJavaType", resolved.javaInterface().getName(),
                    "createMethodName", new FactoryMethod(entityTypeName).getName(),
                    "readMethodName", new ReaderMethod(entityTypeName).getName(),
                    "addMethodName", new AddMethod(prop.getCtx().singularize(property.getName())).getName(),
                    "varName", "_" + property.getName().replaceAll("[^a-zA-Z0-9]", "_")
            ));

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
            prop.getCtx().warn("MAP Entity property '" + property.getName() + "' not read (unsupported) for entity: " + prop.getOwningEntity().fullyQualifiedName());
            prop.getCtx().warn("       property type: " + property.getResolvedType());
        }
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
