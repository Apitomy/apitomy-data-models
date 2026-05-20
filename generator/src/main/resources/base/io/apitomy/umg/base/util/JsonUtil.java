package io.apitomy.umg.base.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
            Iterator<String> fieldNames = json.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                rval.add(fieldName);
            }
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

    /* Get/Consume a JSON (Object) property. */
    public static ObjectNode getObjectProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            JsonNode objectNode = json.get(propertyName);
            if (objectNode.isObject()) {
                return (ObjectNode) objectNode;
            }
        }
        return null;
    }
    public static ObjectNode consumeObjectProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            ObjectNode rval = getObjectProperty(json, propertyName);
            if (rval != null) {
                json.remove(propertyName);
                return rval;
            }
        }
        return null;
    }

    /* Get/Consume a JSON (Any) property. */
    public static JsonNode getAnyProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            JsonNode jsonNode = json.get(propertyName);
            if (!jsonNode.isNull()) {
                return jsonNode;
            }
        }
        return null;
    }
    public static JsonNode consumeAnyProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            JsonNode rval = getAnyProperty(json, propertyName);
            json.remove(propertyName);
            return rval;
        }
        return null;
    }

    /* Get/consume an array of anys property. */
    public static List<JsonNode> getAnyArrayProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            List<JsonNode> rval = new ArrayList<>();
            for (JsonNode item : array) {
                rval.add(item);
            }
            return rval;
        } else {
            return null;
        }
    }
    public static List<JsonNode> consumeAnyArrayProperty(ObjectNode json, String propertyName) {
        List<JsonNode> rval = getAnyArrayProperty(json, propertyName);
        if (rval != null) {
            json.remove(propertyName);
        }
        return rval;
    }

    /* Get/consume an array of objects property. */
    public static List<ObjectNode> getObjectArrayProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            List<ObjectNode> rval = new ArrayList<>();
            for (JsonNode item : array) {
                if (item.isObject()) {
                    rval.add((ObjectNode) item);
                }
            }
            return rval;
        } else {
            return null;
        }
    }
    public static List<ObjectNode> consumeObjectArrayProperty(ObjectNode json, String propertyName) {
        List<ObjectNode> rval = getObjectArrayProperty(json, propertyName);
        if (rval != null) {
            json.remove(propertyName);
        }
        return rval;
    }

    /* Get/consume an array of strings property. */
    public static List<String> getStringArrayProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            List<String> rval = new ArrayList<>();
            for (JsonNode item : array) {
                rval.add(item.isNull() ? null : item.asText());
            }
            return rval;
        } else {
            return null;
        }
    }
    public static List<String> consumeStringArrayProperty(ObjectNode json, String propertyName) {
        ObjectNode node = json;
        if (node.has(propertyName)) {
            List<String> rval = getStringArrayProperty(json, propertyName);
            node.remove(propertyName);
            return rval;
        }
        return null;
    }

    /* Get/consume an array of integers property. */
    public static List<Integer> getIntegerArrayProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            List<Integer> rval = new ArrayList<>();
            for (JsonNode item : array) {
                if (item.isInt()) {
                    rval.add(item.asInt());
                }
            }
            return rval;
        } else {
            return null;
        }
    }
    public static List<Integer> consumeIntegerArrayProperty(ObjectNode json, String propertyName) {
        ObjectNode node = json;
        if (node.has(propertyName)) {
            List<Integer> rval = getIntegerArrayProperty(json, propertyName);
            node.remove(propertyName);
            return rval;
        }
        return null;
    }

    /* Get/consume an array of numbers property. */
    public static List<Number> getNumberArrayProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            List<Number> rval = new ArrayList<>();
            for (JsonNode item : array) {
                if (item.isInt()) {
                    rval.add(item.asInt());
                }
                if (item.isLong()) {
                    rval.add(item.asLong());
                }
                if (item.isFloat() || item.isDouble() || item.isBigDecimal() || item.isBigInteger()) {
                    rval.add(item.asDouble());
                }
            }
            return rval;
        } else {
            return null;
        }
    }
    public static List<Number> consumeNumberArrayProperty(ObjectNode json, String propertyName) {
        ObjectNode node = json;
        if (node.has(propertyName)) {
            List<Number> rval = getNumberArrayProperty(json, propertyName);
            node.remove(propertyName);
            return rval;
        }
        return null;
    }

    /* Get/consume an array of booleans property. */
    public static List<Boolean> getBooleanArrayProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            List<Boolean> rval = new ArrayList<>();
            for (JsonNode item : array) {
                if (item.isBoolean()) {
                    rval.add(item.asBoolean());
                }
            }
            return rval;
        } else {
            return null;
        }
    }
    public static List<Boolean> consumeBooleanArrayProperty(ObjectNode json, String propertyName) {
        ObjectNode node = json;
        if (node.has(propertyName)) {
            List<Boolean> rval = getBooleanArrayProperty(json, propertyName);
            node.remove(propertyName);
            return rval;
        }
        return null;
    }

    /* Get/Consume a string property. */
    public static String getStringProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            JsonNode textNode = json.get(propertyName);
            if (textNode.isTextual()) {
                return textNode.asText();
            }
        }
        return null;
    }
    public static String consumeStringProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            String rval = getStringProperty(json, propertyName);
            if (rval != null) {
                json.remove(propertyName);
                return rval;
            }
        }
        return null;
    }

    /* Get/Consume an Integer property. */
    public static Integer getIntegerProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            JsonNode textNode = json.get(propertyName);
            if (textNode.isInt()) {
                return textNode.asInt();
            }
        }
        return null;
    }
    public static Integer consumeIntegerProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            Integer rval = getIntegerProperty(json, propertyName);
            if (rval != null) {
                json.remove(propertyName);
                return rval;
            }
        }
        return null;
    }

    /* Get/Consume a Number property. */
    public static Number getNumberProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            JsonNode node = json.get(propertyName);
            if (node.isInt()) {
                return node.asInt();
            }
            if (node.isLong()) {
                return node.asLong();
            }
            if (node.isFloat() || node.isDouble()) {
                return node.asDouble();
            }
        }
        return null;
    }
    public static Number consumeNumberProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            Number rval = getNumberProperty(json, propertyName);
            if (rval != null) {
                json.remove(propertyName);
                return rval;
            }
        }
        return null;
    }

    /* Get/Consume a Boolean property. */
    public static Boolean getBooleanProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            JsonNode textNode = json.get(propertyName);
            if (textNode.isBoolean()) {
                return textNode.asBoolean();
            }
        }
        return null;
    }
    public static Boolean consumeBooleanProperty(ObjectNode json, String propertyName) {
        if (json.has(propertyName)) {
            Boolean rval = getBooleanProperty(json, propertyName);
            if (rval != null) {
                json.remove(propertyName);
                return rval;
            }
        }
        return null;
    }

    /* Get/consume a map of anys property. */
    public static Map<String, JsonNode> getAnyMapProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            Map<String, JsonNode> rval = new LinkedHashMap<>();
            List<String> keys = keys(object);
            keys.forEach(key -> {
                rval.put(key, getAnyProperty(object, key));
            });
            return rval;
        } else {
            return null;
        }
    }
    public static Map<String, JsonNode> consumeAnyMapProperty(ObjectNode json, String propertyName) {
        Map<String, JsonNode> rval = getAnyMapProperty(json, propertyName);
        if (rval != null) {
            json.remove(propertyName);
        }
        return rval;
    }

    /* Get/consume a map of objects property. */
    public static Map<String, ObjectNode> getObjectMapProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            Map<String, ObjectNode> rval = new LinkedHashMap<>();
            List<String> keys = keys(object);
            keys.forEach(key -> {
                rval.put(key, getObjectProperty(object, key));
            });
            return rval;
        } else {
            return null;
        }
    }
    public static Map<String, ObjectNode> consumeObjectMapProperty(ObjectNode json, String propertyName) {
        Map<String, ObjectNode> rval = getObjectMapProperty(json, propertyName);
        if (rval != null) {
            json.remove(propertyName);
        }
        return rval;
    }

    /* Get/consume a map of strings property. */
    public static Map<String, String> getStringMapProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            Map<String, String> rval = new LinkedHashMap<>();
            List<String> keys = keys(object);
            keys.forEach(key -> {
                rval.put(key, getStringProperty(object, key));
            });
            return rval;
        } else {
            return null;
        }
    }
    public static Map<String, String> consumeStringMapProperty(ObjectNode json, String propertyName) {
        Map<String, String> rval = getStringMapProperty(json, propertyName);
        if (rval != null) {
            json.remove(propertyName);
        }
        return rval;
    }

    /* Get/consume a map of integers property. */
    public static Map<String, Integer> getIntegerMapProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            Map<String, Integer> rval = new LinkedHashMap<>();
            List<String> keys = keys(object);
            keys.forEach(key -> {
                rval.put(key, getIntegerProperty(object, key));
            });
            return rval;
        } else {
            return null;
        }
    }
    public static Map<String, Integer> consumeIntegerMapProperty(ObjectNode json, String propertyName) {
        Map<String, Integer> rval = getIntegerMapProperty(json, propertyName);
        if (rval != null) {
            json.remove(propertyName);
        }
        return rval;
    }

    /* Get/consume a map of numbers property. */
    public static Map<String, Number> getNumberMapProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            Map<String, Number> rval = new LinkedHashMap<>();
            List<String> keys = keys(object);
            keys.forEach(key -> {
                rval.put(key, getNumberProperty(object, key));
            });
            return rval;
        } else {
            return null;
        }
    }
    public static Map<String, Number> consumeNumberMapProperty(ObjectNode json, String propertyName) {
        Map<String, Number> rval = getNumberMapProperty(json, propertyName);
        if (rval != null) {
            json.remove(propertyName);
        }
        return rval;
    }

    /* Get/consume a map of numbers property. */
    public static Map<String, Boolean> getBooleanMapProperty(ObjectNode json, String propertyName) {
        if (!json.has(propertyName)) {
            return null;
        }
        JsonNode node = json.get(propertyName);
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            Map<String, Boolean> rval = new LinkedHashMap<>();
            List<String> keys = keys(object);
            keys.forEach(key -> {
                rval.put(key, getBooleanProperty(object, key));
            });
            return rval;
        } else {
            return null;
        }
    }
    public static Map<String, Boolean> consumeBooleanMapProperty(ObjectNode json, String propertyName) {
        Map<String, Boolean> rval = getBooleanMapProperty(json, propertyName);
        if (rval != null) {
            json.remove(propertyName);
        }
        return rval;
    }

    /* Set a JSON (Object) property. */
    public static void setObjectProperty(ObjectNode json, String propertyName, ObjectNode value) {
        if (value != null) {
            json.set(propertyName, value);
        }
    }
    /* Set a JSON (Any) property. */
    public static void setAnyProperty(ObjectNode json, String propertyName, JsonNode value) {
        if (value != null) {
            json.set(propertyName, value);
        }
    }
    /* Set an array of anys property. */
    public static void setAnyArrayProperty(ObjectNode json, String propertyName, List<JsonNode> value) {
        if (value != null) {
            ArrayNode array = json.arrayNode(value.size());
            value.forEach(v -> array.add(v));
            json.set(propertyName, array);
        }
    }
    /* Set an array of objects property. */
    public static void setObjectArrayProperty(ObjectNode json, String propertyName, List<ObjectNode> value) {
        if (value != null) {
            ArrayNode array = json.arrayNode(value.size());
            value.forEach(v -> array.add(v));
            json.set(propertyName, array);
        }
    }
    /* Set an array of strings property. */
    public static void setStringArrayProperty(ObjectNode json, String propertyName, List<String> value) {
        if (value != null) {
            ArrayNode array = json.arrayNode(value.size());
            value.forEach(v -> array.add(v));
            json.set(propertyName, array);
        }
    }
    /* Set an array of integers property. */
    public static void setIntegerArrayProperty(ObjectNode json, String propertyName, List<Integer> value) {
        if (value != null) {
            ArrayNode array = json.arrayNode(value.size());
            value.forEach(v -> array.add(v));
            json.set(propertyName, array);
        }
    }
    /* Set an array of numbers property. */
    public static void setNumberArrayProperty(ObjectNode json, String propertyName, List<Number> value) {
        if (value != null) {
            ArrayNode array = json.arrayNode(value.size());
            value.forEach(v -> array.add(v != null ? v.doubleValue() : null));
            json.set(propertyName, array);
        }
    }
    /* Set an array of booleans property. */
    public static void setBooleanArrayProperty(ObjectNode json, String propertyName, List<Boolean> value) {
        if (value != null) {
            ArrayNode array = json.arrayNode(value.size());
            value.forEach(v -> array.add(v));
            json.set(propertyName, array);
        }
    }
    /* Set a string property. */
    public static void setStringProperty(ObjectNode json, String propertyName, String value) {
        if (value != null) {
            json.set(propertyName, factory.textNode(value));
        }
    }
    /* Set an Integer property. */
    public static void setIntegerProperty(ObjectNode json, String propertyName, Integer value) {
        if (value != null) {
            json.set(propertyName, factory.numberNode(value));
        }
    }
    /* Set a Number property. */
    public static void setNumberProperty(ObjectNode json, String propertyName, Number value) {
        if (value != null) {
            json.set(propertyName, factory.numberNode(value.doubleValue()));
        }
    }
    /* Set a Boolean property. */
    public static void setBooleanProperty(ObjectNode json, String propertyName, Boolean value) {
        if (value != null) {
            json.set(propertyName, factory.booleanNode(value));
        }
    }
    /* Set a map of anys property. */
    public static void setAnyMapProperty(ObjectNode json, String propertyName, Map<String, JsonNode> value) {
        if (value != null) {
            ObjectNode object = objectNode();
            value.entrySet().forEach(entry -> {
                object.set(entry.getKey(), entry.getValue());
            });
            setProperty(json, propertyName, object);
        }
    }
    /* Set a map of objects property. */
    public static void setObjectMapProperty(ObjectNode json, String propertyName, Map<String, ObjectNode> value) {
        if (value != null) {
            ObjectNode object = objectNode();
            value.entrySet().forEach(entry -> {
                object.set(entry.getKey(), entry.getValue());
            });
            setProperty(json, propertyName, object);
        }
    }
    /* Set a map of strings property. */
    public static void setStringMapProperty(ObjectNode json, String propertyName, Map<String, String> value) {
        if (value != null) {
            ObjectNode object = objectNode();
            value.entrySet().forEach(entry -> {
                setStringProperty(object, entry.getKey(), entry.getValue());
            });
            setProperty(json, propertyName, object);
        }
    }
    /* Set a map of integers property. */
    public static void setIntegerMapProperty(ObjectNode json, String propertyName, Map<String, Integer> value) {
        if (value != null) {
            ObjectNode object = objectNode();
            value.entrySet().forEach(entry -> {
                setIntegerProperty(object, entry.getKey(), entry.getValue());
            });
            setProperty(json, propertyName, object);
        }
    }
    /* Set a map of numbers property. */
    public static void setNumberMapProperty(ObjectNode json, String propertyName, Map<String, Number> value) {
        if (value != null) {
            ObjectNode object = objectNode();
            value.entrySet().forEach(entry -> {
                setNumberProperty(object, entry.getKey(), entry.getValue());
            });
            setProperty(json, propertyName, object);
        }
    }
    /* Set a map of numbers property. */
    public static void setBooleanMapProperty(ObjectNode json, String propertyName, Map<String, Boolean> value) {
        if (value != null) {
            ObjectNode object = objectNode();
            value.entrySet().forEach(entry -> {
                setBooleanProperty(object, entry.getKey(), entry.getValue());
            });
            setProperty(json, propertyName, object);
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

    public static Collection<?> cloneCollection(Collection<?> collection) {
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

    public static void addToArray(ArrayNode array, JsonNode value) {
        array.add(value);
    }

    public static boolean isPropertyDefined(JsonNode json, String propertyName) {
        if (json.isObject()) {
            ObjectNode node = (ObjectNode) json;
            return node.has(propertyName) && !node.get(propertyName).isNull();
        } else {
            return false;
        }
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

    public static ObjectNode toObjectNode(JsonNode value) {
        return (ObjectNode) value;
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
