package io.apitomy.umg.pipe.java.method.reader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.EntityResolver;
import io.apitomy.umg.pipe.java.method.FactoryMethod;
import io.apitomy.umg.pipe.java.method.PrimitiveTypeHelper;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.ReaderMethod;

/**
 * Generates code to read a star (*) property from JSON.
 * Handles entity, primitive, primitive-list, and primitive-map subcases.
 */
public class ReadStarPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadStarPropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
        this.prop = prop;
        this.readerClassSource = readerClassSource;
    }

    @Override
    public void appendTo(BodyBuilder body) {
        PropertyModel property = prop.getProperty();
        Type resolvedType = property.getResolvedType();

        if (resolvedType.isEntityType()) {
            appendEntity(body, property);
        } else if (resolvedType.isPrimitiveType()) {
            appendPrimitive(body, property);
        } else if (resolvedType.isPrimitiveListType()) {
            appendPrimitiveList(body, property);
        } else if (resolvedType.isPrimitiveMapType()) {
            appendPrimitiveMap(body, property);
        } else {
            prop.getCtx().warn("STAR Entity property '" + property.getName()
                    + "' not read (unhandled) for entity: " + prop.getOwningEntity().fullyQualifiedName());
            prop.getCtx().warn("       property type: " + resolvedType);
        }
    }

    private void appendEntity(BodyBuilder body, PropertyModel property) {
        var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                prop.getOwningEntity(), prop.getCtx(), "STAR");
        if (resolved == null) {
            return;
        }
        readerClassSource.addImport(resolved.javaInterface());
        readerClassSource.addImport(List.class);
        readerClassSource.addImport(JsonNode.class);

        body.addContext(Map.of(
                "entityJavaType", resolved.javaInterface().getName(),
                "createMethodName", new FactoryMethod(resolved.entityModel().getName()).getName(),
                "readMethodName", new ReaderMethod(resolved.entityModel().getName()).getName(),
                "addMethodName", "addItem"
        ));

        body.appendBlock("""
{
    List<String> propertyNames = JsonUtil.keys(json);
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String name = propertyNames.get(_i);
        JsonNode _val = JsonUtil.getProperty(json, name);
        if (JsonUtil.isObject(_val)) {
            ${entityJavaType} model = (${entityJavaType}) node.${createMethodName}();
            node.${addMethodName}(name, model);
            this.${readMethodName}((ObjectNode) _val, model);
            JsonUtil.removeProperty(json, name);
        }
    }
}
""");
    }

    private void appendPrimitive(BodyBuilder body, PropertyModel property) {
        readerClassSource.addImport(List.class);
        readerClassSource.addImport(JsonNode.class);

        body.addContext("isCheckMethod", PrimitiveTypeHelper.determineIsCheckMethod(property.getResolvedType(), prop.getCtx(), readerClassSource));
        body.addContext("toConversionMethod", PrimitiveTypeHelper.determineToConversionMethod(property.getResolvedType(), prop.getCtx(), readerClassSource));

        body.appendBlock("""
{
    List<String> propertyNames = JsonUtil.keys(json);
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String name = propertyNames.get(_i);
        JsonNode _val = JsonUtil.getProperty(json, name);
        if (JsonUtil.${isCheckMethod}(_val)) {
            node.addItem(name, JsonUtil.${toConversionMethod}(_val));
            JsonUtil.removeProperty(json, name);
        }
    }
}
""");
    }

    private void appendPrimitiveList(BodyBuilder body, PropertyModel property) {
        readerClassSource.addImport(List.class);
        readerClassSource.addImport(ArrayList.class);
        readerClassSource.addImport(JsonNode.class);

        Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType();
        body.addContext(Map.of(
                "expectedType", PrimitiveTypeHelper.determineExpectedTypeString(listValueType, prop.getCtx()),
                "toConversionMethod", PrimitiveTypeHelper.determineToConversionMethod(listValueType, prop.getCtx(), readerClassSource),
                "elementValueType", PrimitiveTypeHelper.determineValueType(listValueType, prop.getCtx(), readerClassSource)
        ));

        body.appendBlock("""
{
    List<String> propertyNames = JsonUtil.keys(json);
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String name = propertyNames.get(_i);
        JsonNode _val = JsonUtil.getProperty(json, name);
        if (JsonUtil.isArray(_val) && JsonUtil.allMatch(_val, "${expectedType}")) {
            List<${elementValueType}> items = new ArrayList<>();
            List<JsonNode> _nodes = JsonUtil.toList(_val);
            for (int _j = 0; _j < _nodes.size(); _j++) {
                items.add(JsonUtil.${toConversionMethod}(_nodes.get(_j)));
            }
            node.addItem(name, items);
            JsonUtil.removeProperty(json, name);
        }
    }
}
""");
    }

    private void appendPrimitiveMap(BodyBuilder body, PropertyModel property) {
        readerClassSource.addImport(List.class);
        readerClassSource.addImport(JsonNode.class);
        readerClassSource.addImport(Map.class);
        readerClassSource.addImport(LinkedHashMap.class);

        Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType();
        body.addContext(Map.of(
                "expectedType", PrimitiveTypeHelper.determineExpectedTypeString(mapValueType, prop.getCtx()),
                "toConversionMethod", PrimitiveTypeHelper.determineToConversionMethod(mapValueType, prop.getCtx(), readerClassSource),
                "elementValueType", PrimitiveTypeHelper.determineValueType(mapValueType, prop.getCtx(), readerClassSource)
        ));

        body.appendBlock("""
{
    List<String> propertyNames = JsonUtil.keys(json);
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String name = propertyNames.get(_i);
        JsonNode _val = JsonUtil.getProperty(json, name);
        if (JsonUtil.isObject(_val) && JsonUtil.allValuesMatch((ObjectNode) _val, "${expectedType}")) {
            Map<String, ${elementValueType}> items = new LinkedHashMap<>();
            List<String> _keys = JsonUtil.keys((ObjectNode) _val);
            for (int _j = 0; _j < _keys.size(); _j++) {
                String _key = _keys.get(_j);
                items.put(_key, JsonUtil.${toConversionMethod}(JsonUtil.getProperty((ObjectNode) _val, _key)));
            }
            node.addItem(name, items);
            JsonUtil.removeProperty(json, name);
        }
    }
}
""");
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
