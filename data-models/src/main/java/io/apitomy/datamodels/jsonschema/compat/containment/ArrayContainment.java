package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Array containment: does every array satisfying {@code source} also satisfy
 * {@code target}? Tracks each position's effective schema -- the matching
 * {@code prefixItems} entry if the position is within the tuple prefix, else
 * {@code items} (the {@code true} schema if absent) -- and treats a tuple
 * position beyond {@code source}'s {@code maxItems} as unreachable rather
 * than a real counterexample, since no valid source instance can have an
 * item there at all.
 * <p>
 * The legacy JSON Schema style where {@code items} itself holds an array of
 * per-position tuple schemas (superseded by {@code prefixItems} in 2020-12) is
 * not supported: a position whose effective schema cannot be determined
 * contributes {@link ContainmentVerdict#UNKNOWN}, never a guess.
 */
public final class ArrayContainment {

    private ArrayContainment() {
    }

    /** True if {@code schema}'s own length constraints admit no array at all. */
    public static boolean isProvenUnsatisfiable(SchemaView schema) {
        Integer minItems = intKeyword(schema, "minItems");
        Integer maxItems = intKeyword(schema, "maxItems");
        return minItems != null && maxItems != null && minItems.intValue() > maxItems.intValue();
    }

    public static ContainmentResult compare(SchemaView source, SchemaView target, ContainmentContext context) {
        if (isProvenUnsatisfiable(source)) {
            return ContainmentResult.yes(evidence(source, target, "empty-source-array",
                    "The source schema's own `minItems`/`maxItems` admit no array"));
        }

        Integer sourceMinItems = intKeyword(source, "minItems");
        Integer targetMinItems = intKeyword(target, "minItems");
        if (targetMinItems != null) {
            int sourceValue = sourceMinItems != null ? sourceMinItems.intValue() : 0;
            if (sourceValue < targetMinItems.intValue()) {
                JsonNode witness = buildWitnessArray(source, sourceValue, -1, null, context);
                ContainmentResult failure = confirmOrUnknown(source, target, witness, context, "min-items-narrower",
                        "The target schema's `minItems` is not implied by the source schema's own `minItems`");
                return failure != null ? failure : ContainmentResult.unknown(evidence(source, target,
                        "min-items-narrower-unresolved", "Could not validate a witness for `minItems` containment"));
            }
        }

        Integer sourceMaxItems = intKeyword(source, "maxItems");
        Integer targetMaxItems = intKeyword(target, "maxItems");
        if (targetMaxItems != null && (sourceMaxItems == null || sourceMaxItems.intValue() > targetMaxItems.intValue())) {
            JsonNode witness = buildWitnessArray(source, targetMaxItems.intValue() + 1, -1, null, context);
            ContainmentResult failure = confirmOrUnknown(source, target, witness, context, "max-items-wider",
                    "The target schema's `maxItems` is not implied by the source schema's own `maxItems`");
            return failure != null ? failure : ContainmentResult.unknown(evidence(source, target,
                    "max-items-wider-unresolved", "Could not validate a witness for `maxItems` containment"));
        }

        Boolean sourceUnique = boolKeyword(source, "uniqueItems");
        Boolean targetUnique = boolKeyword(target, "uniqueItems");
        if (Boolean.TRUE.equals(targetUnique) && !Boolean.TRUE.equals(sourceUnique)) {
            JsonNode witness = buildDuplicateWitnessArray(source, context);
            ContainmentResult failure = confirmOrUnknown(source, target, witness, context, "unique-items-not-guaranteed",
                    "The target schema requires `uniqueItems`, and the source schema does not guarantee it");
            return failure != null ? failure : ContainmentResult.unknown(evidence(source, target,
                    "unique-items-not-guaranteed-unresolved", "Could not validate a duplicate-item witness"));
        }

        int prefixLength = Math.max(sizeOfPrefixItems(source), sizeOfPrefixItems(target));
        boolean unresolved = false;
        for (int index = 0; index <= prefixLength; index++) {
            if (sourceMaxItems != null && index >= sourceMaxItems.intValue()) {
                continue;
            }
            SchemaView sourceEffective = effectiveItemSchema(source, index);
            SchemaView targetEffective = effectiveItemSchema(target, index);
            if (sourceEffective == null || targetEffective == null) {
                unresolved = true;
                continue;
            }
            ContainmentResult itemResult = SchemaContainment.compare(sourceEffective, targetEffective, context);
            if (itemResult.getVerdict() == ContainmentVerdict.NO) {
                JsonNode witness = buildWitnessArray(source, -1, index, itemResult.getWitness(), context);
                ContainmentResult failure = confirmOrUnknown(source, target, witness, context, "item-not-contained",
                        "Position " + index + "'s effective schema on the source side is not contained by its "
                                + "effective schema on the target side");
                if (failure != null) {
                    return failure;
                }
                unresolved = true;
                continue;
            }
            if (itemResult.getVerdict() == ContainmentVerdict.UNKNOWN) {
                unresolved = true;
            }
        }

        ContainmentResult containsResult = checkContains(source, target, context);
        if (containsResult != null) {
            if (containsResult.getVerdict() != ContainmentVerdict.YES) {
                return containsResult;
            }
        } else if (target.hasKeyword("contains")) {
            unresolved = true;
        }

        if (unresolved) {
            return ContainmentResult.unknown(evidence(source, target, "array-position-unresolved",
                    "At least one array position or `contains` obligation could not be fully proven contained"));
        }
        return ContainmentResult.yes(evidence(source, target, "array-positions-contained",
                "Every reachable position's effective schema on the source side is contained by its effective "
                        + "schema on the target side"));
    }

    /**
     * The schema that governs the item at {@code index}: the matching
     * {@code prefixItems} entry if {@code index} is within the tuple prefix,
     * else {@code items} (the {@code true} schema if absent), or {@code null}
     * if {@code items} uses the unsupported legacy tuple-array style.
     */
    static SchemaView effectiveItemSchema(SchemaView schema, int index) {
        JsonNode prefixItemsNode = schema.getKeyword("prefixItems");
        if (prefixItemsNode != null && JsonUtil.isArray(prefixItemsNode)) {
            List<JsonNode> prefixSchemas = JsonUtil.toList(prefixItemsNode);
            if (index < prefixSchemas.size()) {
                return childOf(schema, prefixSchemas.get(index), "prefixItems/" + index);
            }
        }
        JsonNode itemsNode = schema.getKeyword("items");
        if (itemsNode != null) {
            if (JsonUtil.isArray(itemsNode)) {
                return null;
            }
            return childOf(schema, itemsNode, "items");
        }
        return trueView(schema);
    }

    private static int sizeOfPrefixItems(SchemaView schema) {
        JsonNode prefixItemsNode = schema.getKeyword("prefixItems");
        if (prefixItemsNode != null && JsonUtil.isArray(prefixItemsNode)) {
            return JsonUtil.toList(prefixItemsNode).size();
        }
        return 0;
    }

    /**
     * A sound special case for `contains`: if the source has no
     * `prefixItems`/tuple structure and its own homogeneous `items` schema is
     * itself contained by the target's `contains` schema, then every source
     * item satisfies `contains`, so it holds as long as the source guarantees
     * at least the target's `minContains` items overall. Returns {@code null}
     * (unresolved) for anything more complex than this.
     */
    private static ContainmentResult checkContains(SchemaView source, SchemaView target, ContainmentContext context) {
        JsonNode targetContainsNode = target.getKeyword("contains");
        if (targetContainsNode == null) {
            return null;
        }
        if (source.hasKeyword("prefixItems") || !source.hasKeyword("items") || JsonUtil.isArray(source.getKeyword("items"))) {
            return null;
        }
        SchemaView sourceHomogeneous = source.childView("items");
        SchemaView targetContains = target.childView("contains");
        if (sourceHomogeneous == null || targetContains == null) {
            return null;
        }
        ContainmentResult itemsVsContains = SchemaContainment.compare(sourceHomogeneous, targetContains, context);
        if (itemsVsContains.getVerdict() != ContainmentVerdict.YES) {
            return null;
        }
        Integer sourceMinItems = intKeyword(source, "minItems");
        Integer targetMinContains = intKeyword(target, "minContains");
        int requiredMinContains = targetMinContains != null ? targetMinContains.intValue() : 1;
        int guaranteedItems = sourceMinItems != null ? sourceMinItems.intValue() : 0;
        if (guaranteedItems >= requiredMinContains) {
            return ContainmentResult.yes(evidence(source, target, "contains-homogeneous-satisfied",
                    "Every source item satisfies the target's `contains` schema, and the source guarantees at "
                            + "least " + requiredMinContains + " item(s)"));
        }
        return null;
    }

    /** A minimal array satisfying {@code schema}'s own length constraints, or {@code null} if one could not be confidently constructed. */
    public static JsonNode buildSatisfyingWitness(SchemaView schema, ContainmentContext context) {
        return buildWitnessArray(schema, 0, -1, null, context);
    }

    private static JsonNode buildWitnessArray(SchemaView source, int forcedLength, int overrideIndex, JsonNode overrideValue,
            ContainmentContext context) {
        int length = forcedLength >= 0 ? forcedLength : 0;
        if (overrideIndex >= 0) {
            length = Math.max(length, overrideIndex + 1);
        }
        Integer sourceMinItems = intKeyword(source, "minItems");
        if (sourceMinItems != null) {
            length = Math.max(length, sourceMinItems.intValue());
        }
        ArrayNode array = JsonUtil.arrayNode();
        for (int i = 0; i < length; i++) {
            JsonNode value;
            if (i == overrideIndex) {
                value = overrideValue;
            } else {
                SchemaView effective = effectiveItemSchema(source, i);
                value = representativeValue(effective, context);
                if (value == null) {
                    return null;
                }
            }
            JsonUtil.addToArray(array, value);
        }
        return array;
    }

    private static JsonNode buildDuplicateWitnessArray(SchemaView source, ContainmentContext context) {
        SchemaView itemSchema = effectiveItemSchema(source, 0);
        JsonNode value = representativeValue(itemSchema, context);
        if (value == null) {
            return null;
        }
        int length = 2;
        Integer sourceMinItems = intKeyword(source, "minItems");
        if (sourceMinItems != null) {
            length = Math.max(length, sourceMinItems.intValue());
        }
        ArrayNode array = JsonUtil.arrayNode();
        for (int i = 0; i < length; i++) {
            JsonUtil.addToArray(array, value);
        }
        return array;
    }

    private static JsonNode representativeValue(SchemaView schema, ContainmentContext context) {
        if (schema == null) {
            return null;
        }
        JsonNode constNode = schema.getKeyword("const");
        if (constNode != null) {
            return constNode;
        }
        JsonNode enumNode = schema.getKeyword("enum");
        if (enumNode != null) {
            List<JsonNode> values = JsonUtil.toList(enumNode);
            for (int i = 0; i < values.size(); i++) {
                if (WitnessValidator.validate(values.get(i), schema, context) == ContainmentVerdict.YES) {
                    return values.get(i);
                }
            }
        }
        List<String> types = SchemaNormalizer.normalizeEffectiveTypes(schema);
        String type = types != null && !types.isEmpty() ? types.get(0) : ScalarTypes.STRING;
        JsonNode candidate = ScalarWitness.exampleOfType(type);
        if (candidate != null && WitnessValidator.validate(candidate, schema, context) == ContainmentVerdict.YES) {
            return candidate;
        }
        return null;
    }

    private static Integer intKeyword(SchemaView schema, String name) {
        JsonNode node = schema.getKeyword(name);
        if (node == null || !JsonUtil.isNumber(node)) {
            return null;
        }
        return Integer.valueOf(JsonUtil.toNumber(node).intValue());
    }

    private static Boolean boolKeyword(SchemaView schema, String name) {
        JsonNode node = schema.getKeyword(name);
        if (node == null || !JsonUtil.isBoolean(node)) {
            return null;
        }
        return JsonUtil.toBoolean(node);
    }

    private static ContainmentResult confirmOrUnknown(SchemaView source, SchemaView target, JsonNode witness,
            ContainmentContext context, String rule, String message) {
        if (witness != null && WitnessValidator.validate(witness, source, context) == ContainmentVerdict.YES
                && WitnessValidator.validate(witness, target, context) == ContainmentVerdict.NO) {
            return ContainmentResult.no(evidence(source, target, rule, message), witness);
        }
        return null;
    }

    private static SchemaView childOf(SchemaView parent, JsonNode node, String pointerSuffix) {
        String pointer = parent.getPointer() != null ? parent.getPointer() + "/" + pointerSuffix : null;
        return new SchemaView(node, parent.getDialect(), parent.getResourceUri(), pointer);
    }

    private static SchemaView trueView(SchemaView parent) {
        return new SchemaView(JsonUtil.toJsonNode(Boolean.TRUE), parent.getDialect(), parent.getResourceUri(), parent.getPointer());
    }

    private static SchemaEvidence evidence(SchemaView source, SchemaView target, String rule, String message) {
        return new SchemaEvidence(source.getPointer(), target.getPointer(), rule, message);
    }
}
