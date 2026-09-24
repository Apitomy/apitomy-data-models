package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Sound containment proofs (and validated counterexamples) for schemas whose
 * only assertions are {@code type}/{@code enum}/{@code const} and the scalar
 * numeric/string keywords -- no {@code properties}, {@code items}, or
 * composition keyword. {@link SchemaContainment#compare} routes here once it
 * has confirmed both schemas are scalar-only; see that class for the
 * dispatch boundary.
 * <p>
 * Every rule below either establishes a definite proof, produces and
 * validates ({@link WitnessValidator}) a concrete counterexample, or gives up
 * with {@link ContainmentVerdict#UNKNOWN} -- there is no path that reports
 * {@link ContainmentVerdict#NO} without a witness that a validator has
 * independently confirmed satisfies the source schema and fails the target
 * schema, and no path that reports {@link ContainmentVerdict#YES} for a
 * constraint this class does not actually understand.
 */
public final class ScalarContainment {

    private ScalarContainment() {
    }

    /**
     * True if {@code schema}'s own numeric or string-length bounds admit no
     * value at all (for example, {@code minimum} greater than {@code maximum}).
     * Used to establish containment against the {@code false} target schema
     * vacuously, and as the first check in {@link #compare}.
     */
    public static boolean isProvenUnsatisfiable(SchemaView schema) {
        SchemaNormalizer.Bound minimum = SchemaNormalizer.normalizeMinimum(schema);
        SchemaNormalizer.Bound maximum = SchemaNormalizer.normalizeMaximum(schema);
        if (minimum != null && maximum != null) {
            int comparison = minimum.getValue().compareTo(maximum.getValue());
            if (comparison > 0) {
                return true;
            }
            if (comparison == 0 && (minimum.isExclusive() || maximum.isExclusive())) {
                return true;
            }
        }
        JsonNode minLengthNode = schema.getKeyword("minLength");
        JsonNode maxLengthNode = schema.getKeyword("maxLength");
        if (minLengthNode != null && maxLengthNode != null && JsonUtil.isNumber(minLengthNode) && JsonUtil.isNumber(maxLengthNode)) {
            if (JsonUtil.toNumber(minLengthNode).intValue() > JsonUtil.toNumber(maxLengthNode).intValue()) {
                return true;
            }
        }
        return false;
    }

    /** Some concrete scalar value satisfying {@code schema}, or {@code null} if one could not be confidently constructed. */
    public static JsonNode buildSatisfyingWitness(SchemaView schema, ContainmentContext context) {
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

    public static ContainmentResult compare(SchemaView source, SchemaView target, ContainmentContext context) {
        if (isProvenUnsatisfiable(source)) {
            return ContainmentResult.yes(evidence(source, target, "empty-source-interval",
                    "The source schema's own numeric/length constraints admit no value"));
        }

        ContainmentResult enumResult = checkEnum(source, target, context);
        if (enumResult != null) {
            return enumResult;
        }

        ContainmentResult constResult = checkConst(source, target, context);
        if (constResult != null) {
            return constResult;
        }

        ContainmentResult typeResult = checkType(source, target, context);
        if (typeResult != null) {
            return typeResult;
        }

        ContainmentResult minimumResult = checkMinimum(source, target, context);
        if (minimumResult != null) {
            return minimumResult;
        }
        ContainmentResult maximumResult = checkMaximum(source, target, context);
        if (maximumResult != null) {
            return maximumResult;
        }
        ContainmentResult multipleOfResult = checkMultipleOf(source, target, context);
        if (multipleOfResult != null) {
            return multipleOfResult;
        }
        ContainmentResult lengthResult = checkLength(source, target, context);
        if (lengthResult != null) {
            return lengthResult;
        }
        ContainmentResult patternResult = checkPattern(source, target, context);
        if (patternResult != null) {
            return patternResult;
        }

        return ContainmentResult.yes(evidence(source, target, "no-conflicting-constraint",
                "No supported scalar constraint on the target schema excludes an instance admitted by the source schema"));
    }

    /**
     * For finite enum sources: every source enum member that itself satisfies
     * the source schema must also satisfy the target schema. A member that
     * fails the source schema's own other constraints is not reachable and is
     * not a counterexample.
     */
    private static ContainmentResult checkEnum(SchemaView source, SchemaView target, ContainmentContext context) {
        JsonNode sourceEnum = source.getKeyword("enum");
        if (sourceEnum == null) {
            return null;
        }
        List<JsonNode> values = JsonUtil.toList(sourceEnum);
        for (int i = 0; i < values.size(); i++) {
            JsonNode value = values.get(i);
            if (WitnessValidator.validate(value, source, context) != ContainmentVerdict.YES) {
                continue;
            }
            ContainmentVerdict targetVerdict = WitnessValidator.validate(value, target, context);
            if (targetVerdict == ContainmentVerdict.NO) {
                return ContainmentResult.no(evidence(source, target, "enum-member-rejected",
                        "A source enum member does not satisfy the target schema"), value);
            }
            if (targetVerdict == ContainmentVerdict.UNKNOWN) {
                return ContainmentResult.unknown(evidence(source, target, "enum-member-unknown",
                        "Could not determine whether a source enum member satisfies the target schema"));
            }
        }
        return ContainmentResult.yes(evidence(source, target, "enum-inclusion",
                "Every source enum member that satisfies the source schema also satisfies the target schema"));
    }

    private static ContainmentResult checkConst(SchemaView source, SchemaView target, ContainmentContext context) {
        JsonNode sourceConst = source.getKeyword("const");
        if (sourceConst == null) {
            return null;
        }
        ContainmentVerdict targetVerdict = WitnessValidator.validate(sourceConst, target, context);
        if (targetVerdict == ContainmentVerdict.YES) {
            return ContainmentResult.yes(evidence(source, target, "const-inclusion",
                    "The source const value satisfies the target schema"));
        }
        if (targetVerdict == ContainmentVerdict.NO) {
            return ContainmentResult.no(evidence(source, target, "const-rejected",
                    "The source const value does not satisfy the target schema"), sourceConst);
        }
        return ContainmentResult.unknown(evidence(source, target, "const-unknown",
                "Could not determine whether the source const value satisfies the target schema"));
    }

    private static final String[] ALL_TYPES = { ScalarTypes.STRING, ScalarTypes.NUMBER, ScalarTypes.BOOLEAN,
            ScalarTypes.OBJECT, ScalarTypes.ARRAY, ScalarTypes.NULL };

    private static ContainmentResult checkType(SchemaView source, SchemaView target, ContainmentContext context) {
        List<String> targetTypes = SchemaNormalizer.normalizeEffectiveTypes(target);
        if (targetTypes == null) {
            return null;
        }
        List<String> sourceTypes = SchemaNormalizer.normalizeEffectiveTypes(source);
        if (sourceTypes == null) {
            // The source admits every instance type; find one the target does not.
            for (int i = 0; i < ALL_TYPES.length; i++) {
                String candidateType = ALL_TYPES[i];
                if (ScalarTypes.matchesAny(candidateType, targetTypes)) {
                    continue;
                }
                JsonNode witness = ScalarWitness.exampleOfType(candidateType);
                if (witness != null && WitnessValidator.validate(witness, source, context) == ContainmentVerdict.YES
                        && WitnessValidator.validate(witness, target, context) == ContainmentVerdict.NO) {
                    return ContainmentResult.no(evidence(source, target, "type-mismatch",
                            "The source schema is unconstrained by `type` and so admits '" + candidateType
                                    + "', which the target schema does not admit"), witness);
                }
            }
            return ContainmentResult.unknown(evidence(source, target, "type-mismatch-unresolved",
                    "The source schema is unconstrained by `type`, which the target schema restricts, but no "
                            + "witness could be validated"));
        }
        for (int i = 0; i < sourceTypes.size(); i++) {
            String sourceType = sourceTypes.get(i);
            if (ScalarTypes.matchesAny(sourceType, targetTypes)) {
                continue;
            }
            JsonNode witness = ScalarWitness.exampleOfType(sourceType);
            if (witness != null && WitnessValidator.validate(witness, source, context) == ContainmentVerdict.YES
                    && WitnessValidator.validate(witness, target, context) == ContainmentVerdict.NO) {
                return ContainmentResult.no(evidence(source, target, "type-mismatch",
                        "The source schema admits type '" + sourceType + "', which the target schema does not admit"), witness);
            }
            return ContainmentResult.unknown(evidence(source, target, "type-mismatch-unresolved",
                    "The source schema admits type '" + sourceType + "', which the target schema does not admit, "
                            + "but no witness could be validated"));
        }
        return null;
    }

    private static ContainmentResult checkMinimum(SchemaView source, SchemaView target, ContainmentContext context) {
        SchemaNormalizer.Bound targetMinimum = SchemaNormalizer.normalizeMinimum(target);
        if (targetMinimum == null) {
            return null;
        }
        SchemaNormalizer.Bound sourceMinimum = SchemaNormalizer.normalizeMinimum(source);
        if (isAtLeastAsTightLowerBound(sourceMinimum, targetMinimum)) {
            return null;
        }
        ExactDecimal witnessValue = sourceMinimum != null ? sourceMinimum.getValue() : null;
        JsonNode witness = witnessValue != null ? ScalarWitness.numberValue(witnessValue)
                : ScalarWitness.numberValueBelow(targetMinimum.getValue());
        return checkWitnessOrUnknown(source, target, context, witness, "lower-bound-narrower",
                "The target schema's `minimum`/`exclusiveMinimum` is not implied by the source schema's own lower bound");
    }

    private static ContainmentResult checkMaximum(SchemaView source, SchemaView target, ContainmentContext context) {
        SchemaNormalizer.Bound targetMaximum = SchemaNormalizer.normalizeMaximum(target);
        if (targetMaximum == null) {
            return null;
        }
        SchemaNormalizer.Bound sourceMaximum = SchemaNormalizer.normalizeMaximum(source);
        if (isAtLeastAsTightUpperBound(sourceMaximum, targetMaximum)) {
            return null;
        }
        ExactDecimal witnessValue = sourceMaximum != null ? sourceMaximum.getValue() : null;
        JsonNode witness = witnessValue != null ? ScalarWitness.numberValue(witnessValue)
                : ScalarWitness.numberValueAbove(targetMaximum.getValue());
        return checkWitnessOrUnknown(source, target, context, witness, "upper-bound-wider",
                "The target schema's `maximum`/`exclusiveMaximum` is not implied by the source schema's own upper bound");
    }

    private static boolean isAtLeastAsTightLowerBound(SchemaNormalizer.Bound source, SchemaNormalizer.Bound target) {
        if (source == null) {
            return false;
        }
        int comparison = source.getValue().compareTo(target.getValue());
        if (comparison > 0) {
            return true;
        }
        if (comparison < 0) {
            return false;
        }
        return !target.isExclusive() || source.isExclusive();
    }

    private static boolean isAtLeastAsTightUpperBound(SchemaNormalizer.Bound source, SchemaNormalizer.Bound target) {
        if (source == null) {
            return false;
        }
        int comparison = source.getValue().compareTo(target.getValue());
        if (comparison < 0) {
            return true;
        }
        if (comparison > 0) {
            return false;
        }
        return !target.isExclusive() || source.isExclusive();
    }

    private static ContainmentResult checkMultipleOf(SchemaView source, SchemaView target, ContainmentContext context) {
        JsonNode targetMultipleOfNode = target.getKeyword("multipleOf");
        if (targetMultipleOfNode == null || !JsonUtil.isNumber(targetMultipleOfNode)) {
            return null;
        }
        JsonNode sourceMultipleOfNode = source.getKeyword("multipleOf");
        if (sourceMultipleOfNode == null || !JsonUtil.isNumber(sourceMultipleOfNode)) {
            return ContainmentResult.unknown(evidence(source, target, "multiple-of-unresolved",
                    "The target schema requires `multipleOf`, and the source schema does not itself guarantee it"));
        }
        ExactDecimal sourceMultipleOf = ExactDecimal.parse(JsonUtil.toNumber(sourceMultipleOfNode).toString());
        ExactDecimal targetMultipleOf = ExactDecimal.parse(JsonUtil.toNumber(targetMultipleOfNode).toString());
        if (sourceMultipleOf.isIntegralMultipleOf(targetMultipleOf)) {
            return null;
        }
        JsonNode witness = ScalarWitness.numberValue(sourceMultipleOf);
        return checkWitnessOrUnknown(source, target, context, witness, "multiple-of-mismatch",
                "The source schema's `multipleOf` is not an exact multiple of the target schema's `multipleOf`");
    }

    private static ContainmentResult checkLength(SchemaView source, SchemaView target, ContainmentContext context) {
        JsonNode targetMinLength = target.getKeyword("minLength");
        if (targetMinLength != null && JsonUtil.isNumber(targetMinLength)) {
            JsonNode sourceMinLength = source.getKeyword("minLength");
            int sourceValue = sourceMinLength != null && JsonUtil.isNumber(sourceMinLength)
                    ? JsonUtil.toNumber(sourceMinLength).intValue() : 0;
            int targetValue = JsonUtil.toNumber(targetMinLength).intValue();
            if (sourceValue < targetValue) {
                JsonNode witness = ScalarWitness.stringOfLength(sourceValue);
                ContainmentResult result = checkWitnessOrUnknown(source, target, context, witness, "min-length-narrower",
                        "The target schema's `minLength` is not implied by the source schema's own `minLength`");
                if (result != null) {
                    return result;
                }
            }
        }
        JsonNode targetMaxLength = target.getKeyword("maxLength");
        if (targetMaxLength != null && JsonUtil.isNumber(targetMaxLength)) {
            JsonNode sourceMaxLength = source.getKeyword("maxLength");
            if (sourceMaxLength == null || !JsonUtil.isNumber(sourceMaxLength)
                    || JsonUtil.toNumber(sourceMaxLength).intValue() > JsonUtil.toNumber(targetMaxLength).intValue()) {
                int witnessLength = JsonUtil.toNumber(targetMaxLength).intValue() + 1;
                JsonNode witness = ScalarWitness.stringOfLength(witnessLength);
                return checkWitnessOrUnknown(source, target, context, witness, "max-length-wider",
                        "The target schema's `maxLength` is not implied by the source schema's own `maxLength`");
            }
        }
        return null;
    }

    private static ContainmentResult checkPattern(SchemaView source, SchemaView target, ContainmentContext context) {
        JsonNode targetPattern = target.getKeyword("pattern");
        if (targetPattern == null || !JsonUtil.isString(targetPattern)) {
            return null;
        }
        JsonNode sourcePattern = source.getKeyword("pattern");
        if (sourcePattern != null && JsonUtil.isString(sourcePattern)
                && JsonUtil.toString(sourcePattern).equals(JsonUtil.toString(targetPattern))) {
            return null;
        }
        // General regex-language inclusion is not attempted: a different (or
        // absent) source pattern is Unknown, never assumed compatible or
        // assumed incompatible without a validated witness this class does not
        // know how to construct for an arbitrary target pattern.
        return ContainmentResult.unknown(evidence(source, target, "pattern-inclusion-unsupported",
                "No general regular-language inclusion proof is attempted between distinct `pattern` values"));
    }

    private static ContainmentResult checkWitnessOrUnknown(SchemaView source, SchemaView target, ContainmentContext context,
            JsonNode witness, String rule, String message) {
        if (witness != null && WitnessValidator.validate(witness, source, context) == ContainmentVerdict.YES
                && WitnessValidator.validate(witness, target, context) == ContainmentVerdict.NO) {
            return ContainmentResult.no(evidence(source, target, rule, message), witness);
        }
        return ContainmentResult.unknown(evidence(source, target, rule + "-unresolved", message));
    }

    private static SchemaEvidence evidence(SchemaView source, SchemaView target, String rule, String message) {
        return new SchemaEvidence(source.getPointer(), target.getPointer(), rule, message);
    }
}
