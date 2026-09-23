package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Generates JSON documents that are likely to tell two schemas apart, for
 * {@link CompatibilityOracleTest}.
 * <p>
 * This is not random fuzzing, which almost never lands on a boundary. The generator walks both
 * schemas together, collects the constants that decide validity at each location (length and
 * number bounds, {@code enum}/{@code const} values, property names, tuple lengths, types) and
 * builds candidates at and around each one. Constants present on only one side are the likeliest
 * to discriminate, so their candidates come first. Output is deterministic, so a failure
 * reproduces exactly.
 * <p>
 * The generator reads every keyword regardless of draft, and never decides validity itself: that
 * is left to the validator.
 */
final class OracleInstanceGenerator {

    private static final JsonNodeFactory F = JsonNodeFactory.instance;

    private static final int MAX_DEPTH = 4;
    private static final int MAX_ARRAY_LENGTH = 6;
    private static final int MAX_STRING_LENGTH = 40;
    /** Candidates taken from a nested location when building a container around it. */
    private static final int NESTED_CANDIDATES = 10;
    private static final int MAX_TOTAL = 400;

    /** One value of every JSON type, so that type changes are always exercised. */
    private static final List<JsonNode> ONE_OF_EACH_TYPE = List.of(F.textNode("s"), F.numberNode(1),
            F.numberNode(1.5), F.booleanNode(true), F.nullNode(), F.objectNode(), F.arrayNode());

    private static final List<String> STRING_KEYWORDS = List.of("minLength", "maxLength", "pattern", "format");
    private static final List<String> NUMBER_KEYWORDS = List.of("minimum", "maximum", "exclusiveMinimum",
            "exclusiveMaximum", "multipleOf");
    private static final List<String> ARRAY_KEYWORDS = List.of("items", "prefixItems", "additionalItems",
            "minItems", "maxItems", "uniqueItems", "contains", "unevaluatedItems");
    private static final List<String> OBJECT_KEYWORDS = List.of("properties", "required", "additionalProperties",
            "patternProperties", "minProperties", "maxProperties", "dependencies", "dependentSchemas",
            "dependentRequired", "propertyNames", "unevaluatedProperties");

    /** A schema node, the document its {@code $ref}s resolve against, and which side it came from. */
    private record Located(JsonNode schema, JsonNode root, int side) {

        Located with(JsonNode other) {
            return new Located(other, root, side);
        }
    }

    private OracleInstanceGenerator() {
    }

    static List<JsonNode> generate(JsonNode original, JsonNode updated) {
        var generator = new OracleInstanceGenerator();
        var candidates = generator.candidates(
                List.of(new Located(original, original, 0), new Located(updated, updated, 1)), MAX_DEPTH);
        return candidates.size() > MAX_TOTAL ? candidates.subList(0, MAX_TOTAL) : candidates;
    }

    private List<JsonNode> candidates(List<Located> schemas, int depth) {
        var facets = facets(schemas);
        var out = new LinkedHashSet<JsonNode>();

        for (var facet : facets) {
            var node = facet.schema();
            node.path("enum").forEach(out::add);
            if (node.has("const")) {
                out.add(node.get("const"));
            }
        }

        var types = types(facets);
        for (var type : types) {
            switch (type) {
                case "string" -> strings(facets, out);
                case "integer", "number" -> numbers(facets, "integer".equals(type) && !types.contains("number"), out);
                case "boolean" -> {
                    out.add(F.booleanNode(true));
                    out.add(F.booleanNode(false));
                }
                case "null" -> out.add(F.nullNode());
                case "array" -> {
                    if (depth > 0) {
                        arrays(facets, depth, out);
                    }
                }
                case "object" -> {
                    if (depth > 0) {
                        objects(facets, depth, out);
                    }
                }
                default -> {
                }
            }
        }

        out.addAll(ONE_OF_EACH_TYPE);
        return new ArrayList<>(out);
    }

    /**
     * The candidates used for a nested location: the first few, which are the likeliest to
     * discriminate, plus one value of every type, so that a type change is exercised at any depth.
     */
    private static List<JsonNode> nested(List<JsonNode> candidates) {
        var out = new LinkedHashSet<>(candidates.subList(0, Math.min(NESTED_CANDIDATES, candidates.size())));
        out.addAll(ONE_OF_EACH_TYPE);
        return new ArrayList<>(out);
    }

    /**
     * The schemas that apply to the same instance location: each schema, what its local
     * {@code $ref} points at, and its applicator subschemas. Boolean schemas carry no constants,
     * so they are dropped.
     */
    private static List<Located> facets(List<Located> schemas) {
        var out = new ArrayList<Located>();
        Set<JsonNode> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var schema : schemas) {
            expand(schema, out, seen);
        }
        return out;
    }

    private static void expand(Located located, List<Located> out, Set<JsonNode> seen) {
        var node = located.schema();
        if (node == null || !node.isObject() || !seen.add(node)) {
            return;
        }
        out.add(located);
        var ref = node.path("$ref");
        // Only local JSON Pointer references; anchors such as "#Address" are not followed.
        if (ref.isTextual() && (ref.asText().equals("#") || ref.asText().startsWith("#/"))) {
            var target = located.root().at(ref.asText().substring(1));
            if (!target.isMissingNode()) {
                expand(located.with(target), out, seen);
            }
        }
        for (var keyword : List.of("allOf", "anyOf", "oneOf")) {
            node.path(keyword).forEach(sub -> expand(located.with(sub), out, seen));
        }
        for (var keyword : List.of("not", "if", "then", "else")) {
            expand(located.with(node.get(keyword)), out, seen);
        }
        for (var keyword : List.of("dependencies", "dependentSchemas")) {
            node.path(keyword).forEach(sub -> expand(located.with(sub), out, seen));
        }
    }

    private static Set<String> types(List<Located> facets) {
        var types = new LinkedHashSet<String>();
        for (var facet : facets) {
            var type = facet.schema().path("type");
            if (type.isTextual()) {
                types.add(type.asText());
            }
            type.forEach(t -> types.add(t.asText()));
        }
        for (var facet : facets) {
            var node = facet.schema();
            if (STRING_KEYWORDS.stream().anyMatch(node::has)) {
                types.add("string");
            }
            if (NUMBER_KEYWORDS.stream().anyMatch(node::has)) {
                types.add("number");
            }
            if (ARRAY_KEYWORDS.stream().anyMatch(node::has)) {
                types.add("array");
            }
            if (OBJECT_KEYWORDS.stream().anyMatch(node::has)) {
                types.add("object");
            }
        }
        return types;
    }

    // -----------------------------------------------------------------------
    // Scalars
    // -----------------------------------------------------------------------

    private static void strings(List<Located> facets, Set<JsonNode> out) {
        var min = firstInt(facets, "minLength", 0);
        var max = firstInt(facets, "maxLength", Integer.MAX_VALUE);
        out.add(text(Math.min(Math.max(1, min), max)));
        for (var length : probes(facets, List.of("minLength", "maxLength"), List.of(0, 1))) {
            var n = length.intValue();
            if (n >= 0 && n <= MAX_STRING_LENGTH) {
                out.add(text(n));
            }
        }
        for (var facet : facets) {
            var pattern = facet.schema().path("pattern");
            if (pattern.isTextual()) {
                var literal = literalPrefix(pattern.asText());
                if (!literal.isEmpty()) {
                    out.add(F.textNode(literal));
                }
            }
        }
    }

    private static void numbers(List<Located> facets, boolean integerOnly, Set<JsonNode> out) {
        var preferred = BigDecimal.ZERO;
        var first = facets.isEmpty() ? null : facets.get(0).schema();
        if (first != null) {
            var min = first.path("minimum");
            var exclusiveMin = first.path("exclusiveMinimum");
            var max = first.path("maximum");
            if (min.isNumber() && preferred.compareTo(min.decimalValue()) < 0) {
                preferred = min.decimalValue();
            }
            if (exclusiveMin.isNumber() && preferred.compareTo(exclusiveMin.decimalValue()) <= 0) {
                preferred = exclusiveMin.decimalValue().add(BigDecimal.ONE);
            }
            if (max.isNumber() && preferred.compareTo(max.decimalValue()) > 0) {
                preferred = max.decimalValue();
            }
        }
        out.add(number(preferred));

        var bounds = probes(facets, List.of("minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum"), List.of(0));
        var deltas = integerOnly ? List.of("0", "-1", "1") : List.of("0", "-1", "1", "-0.5", "0.5");
        for (var bound : bounds) {
            for (var delta : deltas) {
                out.add(number(bound.add(new BigDecimal(delta))));
            }
        }
        for (var facet : facets) {
            var multipleOf = facet.schema().path("multipleOf");
            if (multipleOf.isNumber()) {
                var m = multipleOf.decimalValue();
                out.add(number(m));
                out.add(number(m.multiply(BigDecimal.valueOf(2))));
                out.add(number(m.multiply(new BigDecimal("1.5"))));
            }
        }
    }

    /**
     * Values at and around each numeric bound of the given keywords, those present on only one
     * side first.
     */
    private static List<BigDecimal> probes(List<Located> facets, List<String> keywords, List<Integer> extra) {
        var perSide = List.<Set<BigDecimal>>of(new TreeSet<>(), new TreeSet<>());
        for (var facet : facets) {
            for (var keyword : keywords) {
                var value = facet.schema().path(keyword);
                if (value.isNumber()) {
                    perSide.get(facet.side()).add(value.decimalValue().stripTrailingZeros());
                }
            }
        }
        var ordered = new LinkedHashSet<BigDecimal>();
        var discriminating = new TreeSet<BigDecimal>();
        for (var side = 0; side < 2; side++) {
            for (var value : perSide.get(side)) {
                if (!perSide.get(1 - side).contains(value)) {
                    discriminating.add(value);
                }
            }
        }
        var lengthLike = keywords.contains("minLength");
        for (var value : discriminating) {
            addAround(ordered, value, lengthLike);
        }
        for (var side = 0; side < 2; side++) {
            for (var value : perSide.get(side)) {
                addAround(ordered, value, lengthLike);
            }
        }
        for (var value : extra) {
            ordered.add(BigDecimal.valueOf(value));
        }
        return new ArrayList<>(ordered);
    }

    private static void addAround(Set<BigDecimal> out, BigDecimal value, boolean lengthLike) {
        if (lengthLike) {
            out.add(value.subtract(BigDecimal.ONE));
            out.add(value);
            out.add(value.add(BigDecimal.ONE));
        } else {
            out.add(value);
        }
    }

    // -----------------------------------------------------------------------
    // Arrays
    // -----------------------------------------------------------------------

    private void arrays(List<Located> facets, int depth, Set<JsonNode> out) {
        var lengths = new LinkedHashSet<Integer>();
        var first = facets.isEmpty() ? null : facets.get(0).schema();
        lengths.add(first != null && first.path("minItems").isInt() ? first.get("minItems").asInt() : 1);
        for (var facet : facets) {
            var node = facet.schema();
            for (var keyword : List.of("minItems", "maxItems")) {
                if (node.path(keyword).isInt()) {
                    var n = node.get(keyword).asInt();
                    lengths.add(n - 1);
                    lengths.add(n);
                    lengths.add(n + 1);
                }
            }
            var tuple = tupleLength(node);
            if (tuple >= 0) {
                lengths.add(tuple - 1);
                lengths.add(tuple);
                lengths.add(tuple + 1);
            }
        }
        lengths.add(0);
        lengths.add(1);
        lengths.add(2);

        var elementCandidates = new ArrayList<List<JsonNode>>();
        for (var i = 0; i < MAX_ARRAY_LENGTH; i++) {
            var elements = candidates(elementSchemas(facets, i), depth - 1);
            elementCandidates.add(nested(elements));
        }

        for (var length : lengths) {
            if (length < 0 || length > MAX_ARRAY_LENGTH) {
                continue;
            }
            var preferred = F.arrayNode();
            for (var i = 0; i < length; i++) {
                preferred.add(elementCandidates.get(i).get(0));
            }
            out.add(preferred);
            for (var i = 0; i < length; i++) {
                for (var candidate : elementCandidates.get(i)) {
                    var variant = preferred.deepCopy();
                    variant.set(i, candidate);
                    out.add(variant);
                }
            }
        }
    }

    private static int tupleLength(JsonNode node) {
        if (node.path("prefixItems").isArray()) {
            return node.get("prefixItems").size();
        }
        if (node.path("items").isArray()) {
            return node.get("items").size();
        }
        return -1;
    }

    /** Every schema that may apply to the element at {@code index}. */
    private static List<Located> elementSchemas(List<Located> facets, int index) {
        var out = new ArrayList<Located>();
        for (var facet : facets) {
            var node = facet.schema();
            var prefixItems = node.path("prefixItems");
            var items = node.path("items");
            if (prefixItems.isArray() && index < prefixItems.size()) {
                out.add(facet.with(prefixItems.get(index)));
            }
            if (items.isArray() && index < items.size()) {
                out.add(facet.with(items.get(index)));
            }
            if (items.isObject()) {
                out.add(facet.with(items));
            }
            var tuple = tupleLength(node);
            if (node.path("additionalItems").isObject() && tuple >= 0 && index >= tuple) {
                out.add(facet.with(node.get("additionalItems")));
            }
            for (var keyword : List.of("contains", "unevaluatedItems")) {
                if (node.path(keyword).isObject()) {
                    out.add(facet.with(node.get(keyword)));
                }
            }
        }
        return out;
    }

    // -----------------------------------------------------------------------
    // Objects
    // -----------------------------------------------------------------------

    private void objects(List<Located> facets, int depth, Set<JsonNode> out) {
        var names = propertyNames(facets);
        var required = new LinkedHashSet<String>();
        for (var facet : facets) {
            facet.schema().path("required").forEach(r -> required.add(r.asText()));
        }

        var valueCandidates = new LinkedHashMap<String, List<JsonNode>>();
        for (var name : names) {
            var values = candidates(valueSchemas(facets, name), depth - 1);
            valueCandidates.put(name, nested(values));
        }

        var preferred = F.objectNode();
        for (var name : required) {
            preferred.set(name, preferredValue(valueCandidates, name));
        }
        var all = preferred.deepCopy();
        for (var name : names) {
            if (!all.has(name)) {
                all.set(name, preferredValue(valueCandidates, name));
            }
        }

        out.add(F.objectNode());
        out.add(preferred);
        out.add(all);
        for (var base : List.of(preferred, all)) {
            for (var name : names) {
                for (var value : valueCandidates.get(name)) {
                    var variant = base.deepCopy();
                    variant.set(name, value);
                    out.add(variant);
                }
                if (base.has(name)) {
                    var variant = base.deepCopy();
                    variant.remove(name);
                    out.add(variant);
                }
            }
        }
    }

    private static JsonNode preferredValue(Map<String, List<JsonNode>> candidates, String name) {
        var values = candidates.get(name);
        return values == null || values.isEmpty() ? F.textNode("s") : values.get(0);
    }

    /**
     * Every property name either schema mentions, those mentioned on only one side first, plus a
     * short and a long name neither mentions.
     */
    private static List<String> propertyNames(List<Located> facets) {
        var perSide = List.<Set<String>>of(new LinkedHashSet<>(), new LinkedHashSet<>());
        for (var facet : facets) {
            var names = perSide.get(facet.side());
            var node = facet.schema();
            node.path("properties").fieldNames().forEachRemaining(names::add);
            node.path("required").forEach(r -> names.add(r.asText()));
            for (var keyword : List.of("dependencies", "dependentRequired", "dependentSchemas")) {
                var dependencies = node.path(keyword);
                dependencies.fieldNames().forEachRemaining(names::add);
                dependencies.forEach(value -> {
                    if (value.isArray()) {
                        value.forEach(v -> names.add(v.asText()));
                    }
                });
            }
            node.path("patternProperties").fieldNames().forEachRemaining(pattern -> {
                var literal = literalPrefix(pattern);
                names.add(literal.isEmpty() ? "p1" : literal + "1");
            });
        }
        var ordered = new LinkedHashSet<String>();
        for (var side = 0; side < 2; side++) {
            for (var name : perSide.get(side)) {
                if (!perSide.get(1 - side).contains(name)) {
                    ordered.add(name);
                }
            }
        }
        ordered.addAll(perSide.get(0));
        ordered.addAll(perSide.get(1));
        ordered.add("u");
        ordered.add("unknown");
        return new ArrayList<>(ordered);
    }

    /** Every schema that may apply to the value of property {@code name}. */
    private static List<Located> valueSchemas(List<Located> facets, String name) {
        var out = new ArrayList<Located>();
        for (var facet : facets) {
            var node = facet.schema();
            var declared = node.path("properties").has(name);
            if (declared) {
                out.add(facet.with(node.get("properties").get(name)));
            }
            var matchedPattern = false;
            var patterns = node.path("patternProperties");
            for (var it = patterns.fieldNames(); it.hasNext(); ) {
                var pattern = it.next();
                if (matches(pattern, name)) {
                    matchedPattern = true;
                    out.add(facet.with(patterns.get(pattern)));
                }
            }
            if (!declared && !matchedPattern) {
                for (var keyword : List.of("additionalProperties", "unevaluatedProperties")) {
                    if (node.path(keyword).isObject()) {
                        out.add(facet.with(node.get(keyword)));
                    }
                }
            }
        }
        return out;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static int firstInt(List<Located> facets, String keyword, int fallback) {
        return facets.isEmpty() || !facets.get(0).schema().path(keyword).isInt()
                ? fallback : facets.get(0).schema().get(keyword).asInt();
    }

    private static JsonNode text(int length) {
        return F.textNode("s".repeat(length));
    }

    private static JsonNode number(BigDecimal value) {
        var stripped = value.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            // Same node type as the literals in ONE_OF_EACH_TYPE, so equal values deduplicate.
            var asLong = stripped.longValueExact();
            return asLong == (int) asLong ? F.numberNode((int) asLong) : F.numberNode(asLong);
        }
        return F.numberNode(stripped.doubleValue());
    }

    /** The literal characters a simple regex starts with, e.g. {@code "x"} for {@code "^x"}. */
    private static String literalPrefix(String pattern) {
        var literal = new StringBuilder();
        var start = pattern.startsWith("^") ? 1 : 0;
        for (var i = start; i < pattern.length(); i++) {
            var c = pattern.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                break;
            }
            literal.append(c);
        }
        return literal.toString();
    }

    private static boolean matches(String pattern, String name) {
        try {
            return Pattern.compile(pattern).matcher(name).find();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
