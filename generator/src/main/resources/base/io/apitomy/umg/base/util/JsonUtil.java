package io.apitomy.umg.base.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class JsonUtil {

    private static final JsonNodeFactory factory = JsonNodeFactory.instance;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<String> keys(ObjectNode json) {
        List<String> rval = new ArrayList<>();
        if (json != null) {
            json.fieldNames().forEachRemaining(rval::add);
        }
        return rval;
    }

    public static List<String> matchingKeys(String regex, ObjectNode json) {
        return keys(json).stream().filter(key -> Pattern.matches(regex, key)).collect(Collectors.toList());
    }

    public static JsonNode getProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            return json.get(propertyName);
        }
        return null;
    }

    public static void setProperty(ObjectNode json, String propertyName, JsonNode value) {
        if (value != null) {
            json.set(propertyName, value);
        }
    }


    public static String stringify(JsonNode json) {
        try {
            PrettyPrinter pp = new PrettyPrinter();
            return mapper.writer(pp).writeValueAsString(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseJSON(String jsonString) {
        try {
            return mapper.readTree(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode clone(JsonNode json) {
        try {
            TokenBuffer tb = new TokenBuffer(mapper, false);
            mapper.writeTree(tb, json);
            return mapper.readTree(tb.asParser());
        } catch (IOException e) {
            throw new RuntimeException("Error cloning JSON node.", e);
        }
    }

    public static <T> List<T> collectionToList(Collection<T> collection) {
        if (collection == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(collection);
    }

    public static ObjectNode objectNode() {
        return factory.objectNode();
    }

    public static ArrayNode arrayNode() {
        return factory.arrayNode();
    }

    public static TextNode textNode(String value) {
        return factory.textNode(value);
    }

    public static JsonNode toJsonNode(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            return (JsonNode) value;
        }
        if (value instanceof String) {
            return factory.textNode((String) value);
        }
        if (value instanceof Boolean) {
            return factory.booleanNode((Boolean) value);
        }
        if (value instanceof Integer) {
            return factory.numberNode((Integer) value); // IntNode — preserves 42 (not 42.0)
        }
        if (value instanceof Number) {
            return factory.numberNode(((Number) value).doubleValue()); // DoubleNode
        }
        return null;
    }

    public static ArrayNode toArrayNode(List<?> list) {
        if (list == null) {
            return null;
        }
        ArrayNode array = factory.arrayNode(list.size());
        for (int i = 0; i < list.size(); i++) {
            JsonNode node = toJsonNode(list.get(i));
            if (node != null) {
                array.add(node);
            }
        }
        return array;
    }

    public static ObjectNode toObjectNode(Map<String, ?> map) {
        if (map == null) {
            return null;
        }
        ObjectNode object = factory.objectNode();
        List<String> mapKeys = new ArrayList<>(map.keySet());
        for (int i = 0; i < mapKeys.size(); i++) {
            String key = mapKeys.get(i);
            JsonNode node = toJsonNode(map.get(key));
            if (node != null) {
                object.set(key, node);
            }
        }
        return object;
    }

    public static boolean allMatch(JsonNode array, String expectedType) {
        if (array == null || !array.isArray()) {
            return false;
        }
        ArrayNode arrayNode = (ArrayNode) array;
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode item = arrayNode.get(i);
            if ("string".equals(expectedType)) {
                if (!item.isTextual()) return false;
            } else if ("boolean".equals(expectedType)) {
                if (!item.isBoolean()) return false;
            } else if ("number".equals(expectedType)) {
                if (!item.isNumber()) return false;
            } else if ("integer".equals(expectedType)) {
                if (!item.isInt()) return false;
            } else if ("object".equals(expectedType)) {
                if (!item.isObject()) return false;
            } else if ("any".equals(expectedType)) {
                if (item.isNull()) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean allValuesMatch(ObjectNode obj, String expectedType) {
        if (obj == null) {
            return false;
        }
        for (String fieldName : keys(obj)) {
            JsonNode item = obj.get(fieldName);
            if ("string".equals(expectedType)) {
                if (!item.isTextual()) return false;
            } else if ("boolean".equals(expectedType)) {
                if (!item.isBoolean()) return false;
            } else if ("number".equals(expectedType)) {
                if (!item.isNumber()) return false;
            } else if ("integer".equals(expectedType)) {
                if (!item.isInt()) return false;
            } else if ("object".equals(expectedType)) {
                if (!item.isObject()) return false;
            } else if ("any".equals(expectedType)) {
                if (item.isNull()) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    public static void addToArray(ArrayNode array, JsonNode value) {
        array.add(value);
    }

    public static boolean isString(JsonNode value) {
        if (value == null) {
            return false;
        }
        return value.isTextual();
    }

    public static boolean isJsonNode(JsonNode value) {
        if (value == null) {
            return false;
        }
        return true;
    }

    public static boolean isObjectNode(JsonNode value) {
        if (value == null) {
            return false;
        }
        return value.isObject();
    }

    public static String toString(JsonNode value) {
        return value.asText();
    }

    public static JsonNode toJsonNode(JsonNode value) {
        return value;
    }

    public static boolean isBoolean(JsonNode value) {
        if (value == null) {
            return false;
        }
        return value.isBoolean();
    }

    public static Boolean toBoolean(JsonNode value) {
        return value.asBoolean();
    }

    public static boolean isNumber(JsonNode value) {
        if (value == null) {
            return false;
        }
        return value.isNumber();
    }

    public static Number toNumber(JsonNode value) {
        if (value.isInt()) {
            return value.asInt();
        }
        if (value.isLong()) {
            return value.asLong();
        }
        return value.asDouble();
    }

    public static Integer toInteger(JsonNode value) {
        return value.asInt();
    }

    public static boolean isObject(JsonNode value) {
        if (value == null) {
            return false;
        }
        return value.isObject();
    }

    public static boolean isObjectWithProperty(JsonNode value, String propertyName) {
        if (value == null) {
            return false;
        }
        if (value.isObject()) {
            ObjectNode object = (ObjectNode) value;
            return object.has(propertyName);
        }
        return false;
    }

    public static boolean isObjectWithPropertyValue(JsonNode value, String propertyName, String propertyValue) {
        if (value == null) {
            return false;
        }
        if (value.isObject()) {
            ObjectNode object = (ObjectNode) value;
            if (object.has(propertyName)) {
                JsonNode pvalue = object.get(propertyName);
                if (!pvalue.isNull() && pvalue.isTextual()) {
                    String val = pvalue.asText();
                    return propertyValue.equals(val);
                }
            }
        }
        return false;
    }

    public static ObjectNode toObject(JsonNode value) {
        return (ObjectNode) value;
    }

    public static boolean isArray(JsonNode value) {
        if (value == null) {
            return false;
        }
        return value.isArray();
    }

    public static ArrayNode toArray(JsonNode value) {
        return (ArrayNode) value;
    }

    public static List<JsonNode> toList(JsonNode value) {
        List<JsonNode> rval = new LinkedList<>();
        ArrayNode array = (ArrayNode) value;
        for (int idx = 0; idx < array.size(); idx++) {
            JsonNode node = array.get(idx);
            rval.add(node);
        }
        return rval;
    }

    private static class PrettyPrinter extends MinimalPrettyPrinter {
        private static final long serialVersionUID = -4446121026177697380L;

        private int indentLevel = 0;

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeStartObject(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeStartObject(JsonGenerator g) throws IOException {
            super.writeStartObject(g);
            indentLevel++;
            g.writeRaw("\n");
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeEndObject(com.fasterxml.jackson.core.JsonGenerator,
         *      int)
         */
        @Override
        public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
            indentLevel--;
            g.writeRaw("\n");
            writeIndent(g);
            super.writeEndObject(g, nrOfEntries);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeStartArray(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeStartArray(JsonGenerator g) throws IOException {
            super.writeStartArray(g);
            indentLevel++;
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeEndArray(com.fasterxml.jackson.core.JsonGenerator,
         *      int)
         */
        @Override
        public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
            g.writeRaw("\n");
            indentLevel--;
            writeIndent(g);
            super.writeEndArray(g, nrOfValues);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#beforeObjectEntries(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void beforeObjectEntries(JsonGenerator g) throws IOException {
            writeIndent(g);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#beforeArrayValues(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void beforeArrayValues(JsonGenerator g) throws IOException {
            g.writeRaw("\n");
            writeIndent(g);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeArrayValueSeparator(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
            super.writeArrayValueSeparator(g);
            g.writeRaw("\n");
            writeIndent(g);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeObjectEntrySeparator(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeObjectEntrySeparator(JsonGenerator g) throws IOException {
            super.writeObjectEntrySeparator(g);
            g.writeRaw("\n");
            writeIndent(g);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeObjectFieldValueSeparator(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
            super.writeObjectFieldValueSeparator(g);
            g.writeRaw(" ");
        }

        private void writeIndent(JsonGenerator g) throws IOException {
            for (int idx = 0; idx < this.indentLevel; idx++) {
                g.writeRaw("    ");
            }
        }
    }

}
