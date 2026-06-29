package io.apitomy.umg.pipe.java.method.reader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.ListType;
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
 * Generates code to read a regex-patterned property from JSON.
 * Handles entity, primitive, primitive-list, and primitive-map subcases.
 */
public class ReadRegexPropertyBlock extends CodeBlock {

    private final PropertyCodeGen prop;
    private final JavaClassSource readerClassSource;

    public ReadRegexPropertyBlock(PropertyCodeGen prop, JavaClassSource readerClassSource) {
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
            prop.getCtx().warn("REGEX Entity property '" + property.getName()
                    + "' not read (unsupported) for entity: " + prop.getOwningEntity().fullyQualifiedName());
            prop.getCtx().warn("       property type: " + resolvedType);
        }
    }

    private void appendEntity(BodyBuilder body, PropertyModel property) {
        var resolved = EntityResolver.resolveEntityInterface(property, property.getResolvedType().getName(),
                prop.getOwningEntity(), prop.getCtx(), "REGEX");
        if (resolved == null) {
            return;
        }
        readerClassSource.addImport(resolved.javaInterface());
        readerClassSource.addImport(List.class);
        readerClassSource.addImport(JsonNode.class);

        body.addContext(Map.of(
                "propertyRegex", encodeRegex(extractRegex(property.getName())),
                "entityJavaType", resolved.javaInterface().getName(),
                "createMethodName", new FactoryMethod(resolved.entityModel().getName()).getName(),
                "readMethodName", new ReaderMethod(resolved.entityModel().getName()).getName(),
                "addMethodName", new AddMethod(prop.getCtx().singularize(property.getCollection())).getName()
        ));

        body.appendBlock("""
{
    List<String> propertyNames = JsonUtil.matchingKeys("${propertyRegex}", json);
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

        body.addContext(Map.of(
                "propertyRegex", encodeRegex(extractRegex(property.getName())),
                "isCheckMethod", PrimitiveTypeUtil.determineIsCheckMethod(property.getResolvedType(), prop.getCtx(), readerClassSource),
                "toConversionMethod", PrimitiveTypeUtil.determineToConversionMethod(property.getResolvedType(), prop.getCtx(), readerClassSource),
                "addMethodName", new AddMethod(prop.getCtx().singularize(property.getCollection())).getName()
        ));

        body.appendBlock("""
{
    List<String> propertyNames = JsonUtil.matchingKeys("${propertyRegex}", json);
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String name = propertyNames.get(_i);
        JsonNode _val = JsonUtil.getProperty(json, name);
        if (JsonUtil.${isCheckMethod}(_val)) {
            node.${addMethodName}(name, JsonUtil.${toConversionMethod}(_val));
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

        Type listValueType = ((ListType) property.getResolvedType()).getValueType();
        body.addContext(Map.of(
                "expectedType", PrimitiveTypeUtil.determineExpectedTypeString(listValueType, prop.getCtx()),
                "toConversionMethod", PrimitiveTypeUtil.determineToConversionMethod(listValueType, prop.getCtx(), readerClassSource),
                "elementValueType", PrimitiveTypeUtil.determineValueType(listValueType, prop.getCtx(), readerClassSource),
                "propertyRegex", encodeRegex(extractRegex(property.getName())),
                "addMethodName", new AddMethod(prop.getCtx().singularize(property.getCollection())).getName()
        ));

        body.appendBlock("""
{
    List<String> propertyNames = JsonUtil.matchingKeys("${propertyRegex}", json);
    for (int _i = 0; _i < propertyNames.size(); _i++) {
        String name = propertyNames.get(_i);
        JsonNode _val = JsonUtil.getProperty(json, name);
        if (JsonUtil.isArray(_val) && JsonUtil.allMatch(_val, "${expectedType}")) {
            List<${elementValueType}> items = new ArrayList<>();
            List<JsonNode> _nodes = JsonUtil.toList(_val);
            for (int _j = 0; _j < _nodes.size(); _j++) {
                items.add(JsonUtil.${toConversionMethod}(_nodes.get(_j)));
            }
            node.${addMethodName}(name, items);
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

        Type mapValueType = ((MapType) property.getResolvedType()).getValueType();
        body.addContext(Map.of(
                "expectedType", PrimitiveTypeUtil.determineExpectedTypeString(mapValueType, prop.getCtx()),
                "toConversionMethod", PrimitiveTypeUtil.determineToConversionMethod(mapValueType, prop.getCtx(), readerClassSource),
                "elementValueType", PrimitiveTypeUtil.determineValueType(mapValueType, prop.getCtx(), readerClassSource),
                "propertyRegex", encodeRegex(extractRegex(property.getName())),
                "addMethodName", new AddMethod(prop.getCtx().singularize(property.getCollection())).getName()
        ));

        body.appendBlock("""
{
    List<String> propertyNames = JsonUtil.matchingKeys("${propertyRegex}", json);
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
            node.${addMethodName}(name, items);
            JsonUtil.removeProperty(json, name);
        }
    }
}
""");
    }

    /**
     * Encodes backslashes in a regex pattern for embedding in generated Java source.
     */
    public static String encodeRegex(String regex) {
        return regex.replace("\\", "\\\\");
    }

    /**
     * Extracts the regex pattern from a property name (strips the leading and trailing slashes).
     */
    static String extractRegex(String propertyName) {
        return propertyName.substring(1, propertyName.length() - 1);
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Imports are added directly to readerClassSource during appendTo
    }
}
