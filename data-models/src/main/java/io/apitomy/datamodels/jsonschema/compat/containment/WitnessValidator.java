package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.util.RegexUtil;

/**
 * Validates a concrete JSON value against a {@link SchemaView} directly --
 * independent of any containment comparison -- for schema shapes this package
 * supports: booleans, scalar type/enum/const/numeric/string assertions
 * (T7), and object/array structural assertions (T8), recursing into property
 * values and array items.
 * <p>
 * Used two ways: to confirm a candidate counterexample actually satisfies the
 * source schema and actually fails the target schema before a proof rule
 * reports {@link ContainmentVerdict#NO}, and to check whether a finite
 * enumerated value survives a schema's other constraints.
 */
public final class WitnessValidator {

    private WitnessValidator() {
    }

    /**
     * Whether {@code candidate} satisfies {@code schema}.
     *
     * @return {@link ContainmentVerdict#YES} or {@link ContainmentVerdict#NO} if
     *         every keyword on {@code schema} (and, recursively, every nested
     *         property/item schema) is one this method supports;
     *         {@link ContainmentVerdict#UNKNOWN} if some keyword uses a
     *         construct this method cannot evaluate (an unsupported/
     *         unrecognized keyword, or a composition keyword -- T9)
     */
    public static ContainmentVerdict validate(JsonNode candidate, SchemaView schema, ContainmentContext context) {
        if (schema.isBoolean()) {
            return schema.booleanValue() ? ContainmentVerdict.YES : ContainmentVerdict.NO;
        }
        if (SchemaContainment.coverageGap(schema) != null) {
            return ContainmentVerdict.UNKNOWN;
        }
        if (schema.hasKeyword("if")) {
            return ContainmentVerdict.UNKNOWN;
        }

        String candidateType = ScalarTypes.nameOf(candidate);

        List<String> types = SchemaNormalizer.normalizeEffectiveTypes(schema);
        if (types != null && !ScalarTypes.matchesAny(candidateType, types)) {
            return ContainmentVerdict.NO;
        }

        JsonNode enumNode = schema.getKeyword("enum");
        if (enumNode != null) {
            boolean found = false;
            List<JsonNode> values = JsonUtil.toList(enumNode);
            for (int i = 0; i < values.size(); i++) {
                if (PortableSchemaUtil.jsonEquals(candidate, values.get(i))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return ContainmentVerdict.NO;
            }
        }

        JsonNode constNode = schema.getKeyword("const");
        if (constNode != null && !PortableSchemaUtil.jsonEquals(candidate, constNode)) {
            return ContainmentVerdict.NO;
        }

        if (ScalarTypes.NUMBER.equals(candidateType) || ScalarTypes.INTEGER.equals(candidateType)) {
            ContainmentVerdict verdict = validateNumeric(candidate, schema);
            if (verdict != ContainmentVerdict.YES) {
                return verdict;
            }
        }

        if (ScalarTypes.STRING.equals(candidateType)) {
            ContainmentVerdict verdict = validateString(candidate, schema);
            if (verdict != ContainmentVerdict.YES) {
                return verdict;
            }
        }

        if (ScalarTypes.OBJECT.equals(candidateType)) {
            ContainmentVerdict verdict = validateObject(candidate, schema, context);
            if (verdict != ContainmentVerdict.YES) {
                return verdict;
            }
        }

        if (ScalarTypes.ARRAY.equals(candidateType)) {
            ContainmentVerdict verdict = validateArray(candidate, schema, context);
            if (verdict != ContainmentVerdict.YES) {
                return verdict;
            }
        }

        ContainmentVerdict compositionVerdict = validateComposition(candidate, schema, context);
        if (compositionVerdict != ContainmentVerdict.YES) {
            return compositionVerdict;
        }

        return ContainmentVerdict.YES;
    }

    /** Validates {@code candidate} against any {@code allOf}/{@code anyOf}/{@code oneOf}/{@code not} on {@code schema}. */
    private static ContainmentVerdict validateComposition(JsonNode candidate, SchemaView schema, ContainmentContext context) {
        JsonNode allOfNode = schema.getKeyword("allOf");
        if (allOfNode != null && JsonUtil.isArray(allOfNode)) {
            List<JsonNode> branches = JsonUtil.toList(allOfNode);
            for (int i = 0; i < branches.size(); i++) {
                ContainmentVerdict branchVerdict = validate(candidate, wrap(schema, branches.get(i)), context);
                if (branchVerdict != ContainmentVerdict.YES) {
                    return branchVerdict;
                }
            }
        }

        JsonNode anyOfNode = schema.getKeyword("anyOf");
        if (anyOfNode != null && JsonUtil.isArray(anyOfNode)) {
            List<JsonNode> branches = JsonUtil.toList(anyOfNode);
            boolean matchedAny = false;
            boolean anyUnknown = false;
            for (int i = 0; i < branches.size(); i++) {
                ContainmentVerdict branchVerdict = validate(candidate, wrap(schema, branches.get(i)), context);
                if (branchVerdict == ContainmentVerdict.YES) {
                    matchedAny = true;
                    break;
                }
                if (branchVerdict == ContainmentVerdict.UNKNOWN) {
                    anyUnknown = true;
                }
            }
            if (!matchedAny) {
                return anyUnknown ? ContainmentVerdict.UNKNOWN : ContainmentVerdict.NO;
            }
        }

        JsonNode oneOfNode = schema.getKeyword("oneOf");
        if (oneOfNode != null && JsonUtil.isArray(oneOfNode)) {
            List<JsonNode> branches = JsonUtil.toList(oneOfNode);
            int matchCount = 0;
            boolean anyUnknown = false;
            for (int i = 0; i < branches.size(); i++) {
                ContainmentVerdict branchVerdict = validate(candidate, wrap(schema, branches.get(i)), context);
                if (branchVerdict == ContainmentVerdict.YES) {
                    matchCount++;
                } else if (branchVerdict == ContainmentVerdict.UNKNOWN) {
                    anyUnknown = true;
                }
            }
            if (matchCount != 1) {
                return anyUnknown ? ContainmentVerdict.UNKNOWN : ContainmentVerdict.NO;
            }
        }

        JsonNode notNode = schema.getKeyword("not");
        if (notNode != null) {
            ContainmentVerdict innerVerdict = validate(candidate, wrap(schema, notNode), context);
            if (innerVerdict == ContainmentVerdict.YES) {
                return ContainmentVerdict.NO;
            }
            if (innerVerdict == ContainmentVerdict.UNKNOWN) {
                return ContainmentVerdict.UNKNOWN;
            }
        }

        return ContainmentVerdict.YES;
    }

    private static SchemaView wrap(SchemaView parent, JsonNode node) {
        return new SchemaView(node, parent.getDialect(), parent.getResourceUri(), parent.getPointer());
    }

    private static ContainmentVerdict validateNumeric(JsonNode candidate, SchemaView schema) {
        ExactDecimal value = ExactDecimal.parse(JsonUtil.toNumber(candidate).toString());

        JsonNode multipleOfNode = schema.getKeyword("multipleOf");
        if (multipleOfNode != null && JsonUtil.isNumber(multipleOfNode)) {
            ExactDecimal multipleOf = ExactDecimal.parse(JsonUtil.toNumber(multipleOfNode).toString());
            if (!value.isIntegralMultipleOf(multipleOf)) {
                return ContainmentVerdict.NO;
            }
        }

        SchemaNormalizer.Bound minimum = SchemaNormalizer.normalizeMinimum(schema);
        if (minimum != null) {
            int comparison = value.compareTo(minimum.getValue());
            if (comparison < 0 || (comparison == 0 && minimum.isExclusive())) {
                return ContainmentVerdict.NO;
            }
        }
        SchemaNormalizer.Bound maximum = SchemaNormalizer.normalizeMaximum(schema);
        if (maximum != null) {
            int comparison = value.compareTo(maximum.getValue());
            if (comparison > 0 || (comparison == 0 && maximum.isExclusive())) {
                return ContainmentVerdict.NO;
            }
        }
        return ContainmentVerdict.YES;
    }

    private static ContainmentVerdict validateString(JsonNode candidate, SchemaView schema) {
        String text = JsonUtil.toString(candidate);
        // JSON Schema counts Unicode codepoints, not UTF-16 code units; but
        // String.codePointCount has no TypeScript equivalent, and every
        // constructed witness in this package is plain ASCII, where the two
        // counts coincide, so the simpler length() is used here.
        int length = text.length();

        JsonNode minLengthNode = schema.getKeyword("minLength");
        if (minLengthNode != null && JsonUtil.isNumber(minLengthNode)
                && length < JsonUtil.toNumber(minLengthNode).intValue()) {
            return ContainmentVerdict.NO;
        }
        JsonNode maxLengthNode = schema.getKeyword("maxLength");
        if (maxLengthNode != null && JsonUtil.isNumber(maxLengthNode)
                && length > JsonUtil.toNumber(maxLengthNode).intValue()) {
            return ContainmentVerdict.NO;
        }
        JsonNode patternNode = schema.getKeyword("pattern");
        if (patternNode != null && JsonUtil.isString(patternNode)) {
            // JSON Schema `pattern` uses search semantics (the regex may match
            // anywhere in the string), not a whole-string match.
            if (RegexUtil.findMatches(text, JsonUtil.toString(patternNode)).isEmpty()) {
                return ContainmentVerdict.NO;
            }
        }
        return ContainmentVerdict.YES;
    }

    private static ContainmentVerdict validateObject(JsonNode candidate, SchemaView schema, ContainmentContext context) {
        ObjectNode object = JsonUtil.toObject(candidate);
        List<String> propertyNames = JsonUtil.keys(object);

        JsonNode minPropertiesNode = schema.getKeyword("minProperties");
        if (minPropertiesNode != null && JsonUtil.isNumber(minPropertiesNode)
                && propertyNames.size() < JsonUtil.toNumber(minPropertiesNode).intValue()) {
            return ContainmentVerdict.NO;
        }
        JsonNode maxPropertiesNode = schema.getKeyword("maxProperties");
        if (maxPropertiesNode != null && JsonUtil.isNumber(maxPropertiesNode)
                && propertyNames.size() > JsonUtil.toNumber(maxPropertiesNode).intValue()) {
            return ContainmentVerdict.NO;
        }

        JsonNode requiredNode = schema.getKeyword("required");
        if (requiredNode != null && JsonUtil.isArray(requiredNode)) {
            List<JsonNode> requiredNames = JsonUtil.toList(requiredNode);
            for (int i = 0; i < requiredNames.size(); i++) {
                if (!propertyNames.contains(JsonUtil.toString(requiredNames.get(i)))) {
                    return ContainmentVerdict.NO;
                }
            }
        }

        boolean unresolved = false;
        for (int i = 0; i < propertyNames.size(); i++) {
            String name = propertyNames.get(i);
            JsonNode value = JsonUtil.getProperty(object, name);
            SchemaView effective = ObjectContainment.effectivePropertySchema(schema, name);
            ContainmentVerdict propertyVerdict = validate(value, effective, context);
            if (propertyVerdict == ContainmentVerdict.NO) {
                return ContainmentVerdict.NO;
            }
            if (propertyVerdict == ContainmentVerdict.UNKNOWN) {
                unresolved = true;
            }
        }
        return unresolved ? ContainmentVerdict.UNKNOWN : ContainmentVerdict.YES;
    }

    private static ContainmentVerdict validateArray(JsonNode candidate, SchemaView schema, ContainmentContext context) {
        ArrayNode array = JsonUtil.toArray(candidate);
        List<JsonNode> items = JsonUtil.toList(array);

        JsonNode minItemsNode = schema.getKeyword("minItems");
        if (minItemsNode != null && JsonUtil.isNumber(minItemsNode) && items.size() < JsonUtil.toNumber(minItemsNode).intValue()) {
            return ContainmentVerdict.NO;
        }
        JsonNode maxItemsNode = schema.getKeyword("maxItems");
        if (maxItemsNode != null && JsonUtil.isNumber(maxItemsNode) && items.size() > JsonUtil.toNumber(maxItemsNode).intValue()) {
            return ContainmentVerdict.NO;
        }
        JsonNode uniqueItemsNode = schema.getKeyword("uniqueItems");
        if (uniqueItemsNode != null && JsonUtil.isBoolean(uniqueItemsNode) && JsonUtil.toBoolean(uniqueItemsNode).booleanValue()) {
            for (int i = 0; i < items.size(); i++) {
                for (int j = i + 1; j < items.size(); j++) {
                    if (PortableSchemaUtil.jsonEquals(items.get(i), items.get(j))) {
                        return ContainmentVerdict.NO;
                    }
                }
            }
        }

        boolean unresolved = false;
        for (int i = 0; i < items.size(); i++) {
            SchemaView effective = ArrayContainment.effectiveItemSchema(schema, i);
            if (effective == null) {
                unresolved = true;
                continue;
            }
            ContainmentVerdict itemVerdict = validate(items.get(i), effective, context);
            if (itemVerdict == ContainmentVerdict.NO) {
                return ContainmentVerdict.NO;
            }
            if (itemVerdict == ContainmentVerdict.UNKNOWN) {
                unresolved = true;
            }
        }

        JsonNode containsNode = schema.getKeyword("contains");
        if (containsNode != null) {
            SchemaView containsSchema = schema.childView("contains");
            int matchCount = 0;
            for (int i = 0; i < items.size(); i++) {
                if (validate(items.get(i), containsSchema, context) == ContainmentVerdict.YES) {
                    matchCount++;
                }
            }
            JsonNode minContainsNode = schema.getKeyword("minContains");
            int minContains = minContainsNode != null && JsonUtil.isNumber(minContainsNode)
                    ? JsonUtil.toNumber(minContainsNode).intValue() : 1;
            JsonNode maxContainsNode = schema.getKeyword("maxContains");
            int maxContains = maxContainsNode != null && JsonUtil.isNumber(maxContainsNode)
                    ? JsonUtil.toNumber(maxContainsNode).intValue() : Integer.MAX_VALUE;
            if (matchCount < minContains || matchCount > maxContains) {
                return ContainmentVerdict.NO;
            }
        }

        return unresolved ? ContainmentVerdict.UNKNOWN : ContainmentVerdict.YES;
    }
}
