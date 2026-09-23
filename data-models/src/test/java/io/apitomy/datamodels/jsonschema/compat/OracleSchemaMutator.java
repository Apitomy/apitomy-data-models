package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Derives schema pairs from a base schema for {@link CompatibilityOracleTest}: every single edit
 * from a fixed set, applied at every subschema location of the base.
 * <p>
 * Enumerating edit × location is the point. A defect in the checker is typically one kind of edit
 * handled wrongly in one kind of place, and a hand-written catalogue covers only the combinations
 * someone thought of.
 */
final class OracleSchemaMutator {

    private static final JsonNodeFactory F = JsonNodeFactory.instance;

    /** Keywords whose value is a map of subschemas. */
    private static final List<String> SCHEMA_MAPS = List.of("properties", "patternProperties", "dependentSchemas",
            "definitions", "$defs");
    /** Keywords whose value is a single subschema. */
    private static final List<String> SCHEMA_SINGLES = List.of("additionalProperties", "additionalItems",
            "unevaluatedItems", "unevaluatedProperties", "contains", "propertyNames", "not", "if", "then", "else");
    /** Keywords whose value is a list of subschemas. */
    private static final List<String> SCHEMA_LISTS = List.of("allOf", "anyOf", "oneOf", "prefixItems");

    private static final List<String> NON_NEGATIVE = List.of("minLength", "maxLength", "minItems", "maxItems",
            "minProperties", "maxProperties");
    private static final List<String> SIGNED = List.of("minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum");

    private static final Map<String, List<String>> TYPE_ALTERNATIVES = Map.of(
            "string", List.of("integer", "object"),
            "integer", List.of("number", "string"),
            "number", List.of("integer", "string"),
            "object", List.of("array", "string"),
            "array", List.of("object", "string"),
            "boolean", List.of("string"),
            "null", List.of("string"));

    record Mutation(String location, String edit, JsonNode original, JsonNode updated) {
    }

    private record Edit(String name, UnaryOperator<JsonNode> apply) {
    }

    private OracleSchemaMutator() {
    }

    static List<Mutation> mutations(JsonNode base) {
        var out = new ArrayList<Mutation>();
        for (var pointer : locations(base)) {
            for (var edit : edits(base.at(pointer), pointer.isEmpty())) {
                var replacement = edit.apply().apply(base.at(pointer).deepCopy());
                out.add(new Mutation(pointer.isEmpty() ? "/" : pointer, edit.name(),
                        base, replaceAt(base, pointer, replacement)));
            }
        }
        return out;
    }

    // -----------------------------------------------------------------------
    // Locations
    // -----------------------------------------------------------------------

    static List<String> locations(JsonNode root) {
        var out = new ArrayList<String>();
        walk(root, "", out);
        return out;
    }

    private static void walk(JsonNode schema, String pointer, List<String> out) {
        out.add(pointer);
        if (!schema.isObject()) {
            return;
        }
        for (var keyword : SCHEMA_MAPS) {
            var map = schema.path(keyword);
            map.fieldNames().forEachRemaining(key ->
                    walk(map.get(key), pointer + "/" + escape(keyword) + "/" + escape(key), out));
        }
        var dependencies = schema.path("dependencies");
        dependencies.fieldNames().forEachRemaining(key -> {
            if (!dependencies.get(key).isArray()) {
                walk(dependencies.get(key), pointer + "/dependencies/" + escape(key), out);
            }
        });
        for (var keyword : SCHEMA_SINGLES) {
            if (schema.has(keyword)) {
                walk(schema.get(keyword), pointer + "/" + escape(keyword), out);
            }
        }
        var items = schema.path("items");
        if (items.isObject() || items.isBoolean()) {
            walk(items, pointer + "/items", out);
        }
        for (var i = 0; items.isArray() && i < items.size(); i++) {
            walk(items.get(i), pointer + "/items/" + i, out);
        }
        for (var keyword : SCHEMA_LISTS) {
            var list = schema.path(keyword);
            for (var i = 0; list.isArray() && i < list.size(); i++) {
                walk(list.get(i), pointer + "/" + keyword + "/" + i, out);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Edits
    // -----------------------------------------------------------------------

    private static List<Edit> edits(JsonNode schema, boolean root) {
        var out = new ArrayList<Edit>();
        if (!schema.isBoolean() || !schema.asBoolean()) {
            out.add(new Edit("replace with true", s -> BooleanNode.TRUE));
        }
        if (!schema.isBoolean() || schema.asBoolean()) {
            out.add(new Edit("replace with false", s -> BooleanNode.FALSE));
        }
        if (!schema.isObject()) {
            return out;
        }
        if (schema.size() > (schema.has("$schema") ? 1 : 0)) {
            out.add(new Edit("replace with {}", s -> F.objectNode()));
        }
        if (!root) {
            // At the root, wrapping would move $schema and the definitions away from the top level.
            out.add(new Edit("wrap in anyOf with null", s -> {
                var wrapper = F.objectNode();
                wrapper.putArray("anyOf").add(s).add(F.objectNode().put("type", "null"));
                return wrapper;
            }));
        }

        boundEdits(schema, out);
        typeEdits(schema, out);
        objectEdits(schema, out);
        arrayEdits(schema, out);
        valueEdits(schema, out);
        return out;
    }

    private static void boundEdits(JsonNode schema, List<Edit> out) {
        for (var keyword : NON_NEGATIVE) {
            if (schema.path(keyword).isInt()) {
                var value = schema.get(keyword).asInt();
                out.add(set(keyword + " + 1", keyword, F.numberNode(value + 1)));
                if (value > 0) {
                    out.add(set(keyword + " - 1", keyword, F.numberNode(value - 1)));
                }
                out.add(remove(keyword));
            }
        }
        for (var keyword : SIGNED) {
            var value = schema.path(keyword);
            if (value.isNumber()) {
                out.add(set(keyword + " + 1", keyword, F.numberNode(value.decimalValue().add(BigDecimal.ONE))));
                out.add(set(keyword + " - 1", keyword,
                        F.numberNode(value.decimalValue().subtract(BigDecimal.ONE))));
                out.add(remove(keyword));
            } else if (value.isBoolean()) {
                out.add(set("toggle " + keyword, keyword, F.booleanNode(!value.asBoolean())));
            }
        }
        if (schema.path("multipleOf").isNumber()) {
            var value = schema.get("multipleOf").decimalValue();
            out.add(set("multipleOf * 2", "multipleOf", F.numberNode(value.multiply(BigDecimal.valueOf(2)))));
            out.add(remove("multipleOf"));
        }
        if (hasType(schema, "string") && !schema.has("maxLength")) {
            out.add(set("add maxLength", "maxLength", F.numberNode(3)));
        }
        if ((hasType(schema, "number") || hasType(schema, "integer")) && !schema.has("minimum")) {
            out.add(set("add minimum", "minimum", F.numberNode(1)));
        }
        if (hasType(schema, "array") && !schema.has("maxItems")) {
            out.add(set("add maxItems", "maxItems", F.numberNode(1)));
        }
        if (hasType(schema, "object") && !schema.has("maxProperties")) {
            out.add(set("add maxProperties", "maxProperties", F.numberNode(1)));
        }
    }

    private static void typeEdits(JsonNode schema, List<Edit> out) {
        var type = schema.path("type");
        if (type.isTextual()) {
            for (var alternative : TYPE_ALTERNATIVES.getOrDefault(type.asText(), List.of())) {
                out.add(set("type -> " + alternative, "type", F.textNode(alternative)));
            }
            if (!"null".equals(type.asText())) {
                out.add(set("type -> [" + type.asText() + ", null]", "type",
                        F.arrayNode().add(type.asText()).add("null")));
            }
            out.add(remove("type"));
        } else if (type.isArray() && type.size() > 1) {
            var dropped = (ArrayNode) type.deepCopy();
            dropped.remove(type.size() - 1);
            out.add(set("type drops " + type.get(type.size() - 1).asText(), "type", dropped));
            out.add(remove("type"));
        }
    }

    private static void objectEdits(JsonNode schema, List<Edit> out) {
        var properties = schema.path("properties");
        var required = schema.path("required");
        properties.fieldNames().forEachRemaining(name -> {
            out.add(new Edit("remove property " + name, s -> {
                ((ObjectNode) s.get("properties")).remove(name);
                return s;
            }));
            if (!contains(required, name)) {
                out.add(new Edit("require " + name, s -> {
                    var list = s.has("required") ? (ArrayNode) s.get("required") : ((ObjectNode) s).putArray("required");
                    list.add(name);
                    return s;
                }));
            }
        });
        for (var i = 0; required.isArray() && i < required.size(); i++) {
            var index = i;
            out.add(new Edit("unrequire " + required.get(i).asText(), s -> {
                ((ArrayNode) s.get("required")).remove(index);
                return s;
            }));
        }
        if (properties.isObject() || hasType(schema, "object")) {
            out.add(new Edit("add property", s -> {
                var props = s.has("properties") ? (ObjectNode) s.get("properties") : ((ObjectNode) s).putObject("properties");
                props.putObject("added").put("type", "string");
                return s;
            }));
            if (!schema.has("additionalProperties")) {
                out.add(set("add additionalProperties false", "additionalProperties", BooleanNode.FALSE));
            }
        }
        for (var keyword : List.of("dependencies", "dependentRequired")) {
            var dependencies = schema.path(keyword);
            dependencies.fieldNames().forEachRemaining(key -> {
                var members = dependencies.get(key);
                if (!members.isArray()) {
                    return;
                }
                out.add(new Edit(keyword + "." + key + " adds a member", s -> {
                    ((ArrayNode) s.get(keyword).get(key)).add("added");
                    return s;
                }));
                if (!members.isEmpty()) {
                    out.add(new Edit(keyword + "." + key + " drops a member", s -> {
                        ((ArrayNode) s.get(keyword).get(key)).remove(members.size() - 1);
                        return s;
                    }));
                }
            });
        }
    }

    private static void arrayEdits(JsonNode schema, List<Edit> out) {
        var draftTuple = schema.path("items").isArray();
        var tupleKeyword = draftTuple ? "items" : schema.path("prefixItems").isArray() ? "prefixItems" : null;
        if (tupleKeyword != null) {
            out.add(new Edit(tupleKeyword + " appends an element", s -> {
                ((ArrayNode) s.get(tupleKeyword)).addObject().put("type", "string");
                return s;
            }));
            if (!schema.get(tupleKeyword).isEmpty()) {
                out.add(new Edit(tupleKeyword + " drops the last element", s -> {
                    var tuple = (ArrayNode) s.get(tupleKeyword);
                    tuple.remove(tuple.size() - 1);
                    return s;
                }));
            }
            var rest = draftTuple ? "additionalItems" : "items";
            if (!schema.has(rest)) {
                out.add(set("add " + rest + " false", rest, BooleanNode.FALSE));
            }
        }
        if (schema.path("uniqueItems").isBoolean()) {
            out.add(set("toggle uniqueItems", "uniqueItems", F.booleanNode(!schema.get("uniqueItems").asBoolean())));
        }
    }

    private static void valueEdits(JsonNode schema, List<Edit> out) {
        var values = schema.path("enum");
        if (values.isArray() && !values.isEmpty()) {
            out.add(new Edit("enum adds a value", s -> {
                var first = values.get(0);
                ((ArrayNode) s.get("enum")).add(first.isNumber() ? F.numberNode(first.asLong() + 100) : F.textNode("added"));
                return s;
            }));
            if (values.size() > 1) {
                out.add(new Edit("enum drops a value", s -> {
                    ((ArrayNode) s.get("enum")).remove(values.size() - 1);
                    return s;
                }));
            }
        }
        var constant = schema.path("const");
        if (constant.isTextual() || constant.isNumber()) {
            out.add(set("const changes", "const",
                    constant.isNumber() ? F.numberNode(constant.asLong() + 1) : F.textNode(constant.asText() + "z")));
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Edit set(String name, String keyword, JsonNode value) {
        return new Edit(name, s -> {
            ((ObjectNode) s).set(keyword, value);
            return s;
        });
    }

    private static Edit remove(String keyword) {
        return new Edit("remove " + keyword, s -> {
            ((ObjectNode) s).remove(keyword);
            return s;
        });
    }

    private static boolean hasType(JsonNode schema, String type) {
        var declared = schema.path("type");
        return declared.isTextual() ? declared.asText().equals(type) : contains(declared, type);
    }

    private static boolean contains(JsonNode array, String value) {
        for (var element : array) {
            if (element.asText().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a copy of {@code root} with the node at {@code pointer} replaced. A replaced root keeps
     * the base's {@code $schema} when the replacement is an object, so the draft does not change
     * with it.
     */
    private static JsonNode replaceAt(JsonNode root, String pointer, JsonNode replacement) {
        if (pointer.isEmpty()) {
            if (replacement.isObject() && root.has("$schema") && !replacement.has("$schema")) {
                var withDraft = F.objectNode();
                withDraft.set("$schema", root.get("$schema"));
                withDraft.setAll((ObjectNode) replacement);
                return withDraft;
            }
            return replacement;
        }
        var copy = root.deepCopy();
        var slash = pointer.lastIndexOf('/');
        var parent = copy.at(pointer.substring(0, slash));
        var key = unescape(pointer.substring(slash + 1));
        if (parent.isArray()) {
            ((ArrayNode) parent).set(Integer.parseInt(key), replacement);
        } else {
            ((ObjectNode) parent).set(key, replacement);
        }
        return copy;
    }

    private static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    private static String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~");
    }
}
