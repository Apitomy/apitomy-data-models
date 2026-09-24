package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.util.RegexUtil;

/**
 * Validates a concrete JSON value against a {@link SchemaView} directly --
 * independent of any containment comparison -- for schema shapes this package
 * supports (booleans, and scalar type/enum/const/numeric/string assertions).
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
     *         every keyword on {@code schema} is one this method supports;
     *         {@link ContainmentVerdict#UNKNOWN} if {@code schema} uses a
     *         keyword this method cannot evaluate (an unsupported/unrecognized
     *         keyword, or an object/array/composition constraint -- T8/T9)
     */
    public static ContainmentVerdict validate(JsonNode candidate, SchemaView schema, ContainmentContext context) {
        if (schema.isBoolean()) {
            return schema.booleanValue() ? ContainmentVerdict.YES : ContainmentVerdict.NO;
        }
        if (SchemaContainment.coverageGap(schema) != null) {
            return ContainmentVerdict.UNKNOWN;
        }
        if (!SchemaContainment.isScalarOnly(schema)) {
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
        }

        if (ScalarTypes.STRING.equals(candidateType)) {
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
        }

        return ContainmentVerdict.YES;
    }
}
