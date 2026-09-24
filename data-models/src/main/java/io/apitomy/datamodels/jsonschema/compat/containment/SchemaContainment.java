package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Direction-neutral schema comparison: does every instance satisfying
 * {@code source} also satisfy {@code target}?
 * <p>
 * This task's dispatch handles dialect/coverage gates and the boolean-schema
 * identities; the scalar, object, array, and composition proof rules that
 * decide the interesting object-schema cases are added by T7-T9, which call
 * back into {@link #compare} for every nested schema position. Until then, an
 * object-schema comparison this class cannot yet prove returns
 * {@link ContainmentVerdict#UNKNOWN} rather than guessing -- there is no
 * "unimplemented means compatible" path here.
 */
public final class SchemaContainment {

    /** Schema-child keywords that shape object instances (not array or composition). */
    static final String[] OBJECT_CHILD_KEYWORDS = { "properties", "patternProperties", "additionalProperties",
            "propertyNames", "dependentSchemas", "unevaluatedProperties" };

    /** Schema-child keywords that shape array instances (not object or composition). */
    static final String[] ARRAY_CHILD_KEYWORDS = { "items", "prefixItems", "contains", "unevaluatedItems" };

    /** Schema-child keywords that combine other schemas (require T9's composition rules). */
    static final String[] COMPOSITION_CHILD_KEYWORDS = { "allOf", "anyOf", "oneOf", "not", "if", "then", "else" };

    private SchemaContainment() {
    }

    public static ContainmentResult compare(SchemaView source, SchemaView target, ContainmentContext context) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }

        if (source.getDialect() == SchemaDialect.UNSUPPORTED || target.getDialect() == SchemaDialect.UNSUPPORTED) {
            return ContainmentResult.unknown(evidence(source, target, "unsupported-dialect",
                    "Schema dialect is not recognized, or requires a vocabulary this checker does not implement"));
        }

        if (source.isFalse()) {
            return ContainmentResult.yes(evidence(source, target, "empty-source",
                    "The source schema is `false` and matches no instance, so containment holds vacuously"));
        }
        if (target.isTrue()) {
            return ContainmentResult.yes(evidence(source, target, "universal-target",
                    "The target schema is `true` and matches every instance"));
        }
        if (target.isFalse()) {
            if (source.isTrue()) {
                return ContainmentResult.no(evidence(source, target, "universal-vs-empty",
                        "The source schema is `true` (matches every instance) but the target schema is `false` "
                                + "(matches none)"), JsonUtil.toJsonNode(Boolean.TRUE));
            }
            if (isScalarOnly(source) && ScalarContainment.isProvenUnsatisfiable(source)) {
                return ContainmentResult.yes(evidence(source, target, "empty-source-interval",
                        "The source schema's own numeric/length constraints admit no value, so containment "
                                + "against the `false` target holds vacuously"));
            }
            if (isObjectOnly(source) && ObjectContainment.isProvenUnsatisfiable(source)) {
                return ContainmentResult.yes(evidence(source, target, "empty-source-object",
                        "The source schema's own size/requiredness constraints admit no object, so containment "
                                + "against the `false` target holds vacuously"));
            }
            if (isArrayOnly(source) && ArrayContainment.isProvenUnsatisfiable(source)) {
                return ContainmentResult.yes(evidence(source, target, "empty-source-array",
                        "The source schema's own `minItems`/`maxItems` admit no array, so containment against "
                                + "the `false` target holds vacuously"));
            }
            JsonNode witness = null;
            if (isScalarOnly(source)) {
                witness = ScalarContainment.buildSatisfyingWitness(source, context);
            } else if (isObjectOnly(source)) {
                witness = ObjectContainment.buildSatisfyingWitness(source, context);
            } else if (isArrayOnly(source)) {
                witness = ArrayContainment.buildSatisfyingWitness(source, context);
            }
            if (witness != null && WitnessValidator.validate(witness, source, context) == ContainmentVerdict.YES) {
                return ContainmentResult.no(evidence(source, target, "satisfiable-source-vs-empty-target",
                        "A concrete instance satisfies the source schema, but the target schema is `false` and "
                                + "matches no instance"), witness);
            }
            return ContainmentResult.unknown(evidence(source, target, "empty-target",
                    "The target schema is `false`; whether the source schema is itself satisfiable by no instance "
                            + "is not established here"));
        }

        // From here, target is a (non-false) object schema.
        ContainmentResult targetGap = coverageGap(target);
        if (targetGap != null) {
            return targetGap;
        }

        boolean sourceIsTrue = source.isTrue();
        if (!sourceIsTrue) {
            ContainmentResult sourceGap = coverageGap(source);
            if (sourceGap != null) {
                return sourceGap;
            }
        }

        if (!sourceIsTrue && isEmptySchema(source) && isEmptySchema(target)) {
            return ContainmentResult.yes(evidence(source, target, "both-unconstrained",
                    "Neither schema has a constraining keyword under its dialect"));
        }

        if ((!sourceIsTrue && hasAnyKeyword(source, COMPOSITION_CHILD_KEYWORDS)) || hasAnyKeyword(target, COMPOSITION_CHILD_KEYWORDS)) {
            return CompositionContainment.compare(source, target, context);
        }

        if ((sourceIsTrue || isScalarOnly(source)) && isScalarOnly(target)) {
            return ScalarContainment.compare(source, target, context);
        }

        if ((sourceIsTrue || isObjectOnly(source)) && isObjectOnly(target)) {
            return ObjectContainment.compare(source, target, context);
        }

        if ((sourceIsTrue || isArrayOnly(source)) && isArrayOnly(target)) {
            return ArrayContainment.compare(source, target, context);
        }

        return ContainmentResult.unknown(evidence(source, target, "no-proof-rule",
                "No composition proof rule is implemented yet for this schema shape"));
    }

    /**
     * {@code null} if every keyword present on {@code schema} is a recognized
     * assertion, schema-child, or annotation; otherwise an
     * {@link ContainmentVerdict#UNKNOWN} result naming the first unsupported or
     * unrecognized keyword found.
     */
    static ContainmentResult coverageGap(SchemaView schema) {
        if (!schema.isObject()) {
            return null;
        }
        ObjectNode object = JsonUtil.toObject(schema.getNode());
        List<String> keys = JsonUtil.keys(object);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            CoverageRegistry.Coverage coverage = CoverageRegistry.classify(schema.getDialect(), key);
            if (coverage == null) {
                return ContainmentResult.unknown(new SchemaEvidence(schema.getPointer(), null, "unrecognized-keyword",
                        "Keyword '" + key + "' is not recognized under dialect " + schema.getDialect()));
            }
            if (coverage.getCategory() == CoverageRegistry.Category.UNSUPPORTED) {
                return ContainmentResult.unknown(new SchemaEvidence(schema.getPointer(), null, "unsupported-keyword",
                        "Keyword '" + key + "': " + coverage.getReason()));
            }
        }
        return null;
    }

    /** True if {@code schema} is the {@code true} schema, or an object schema with no keyword at all -- equivalent to {@code true}. */
    static boolean isEmptySchema(SchemaView schema) {
        if (schema.isTrue()) {
            return true;
        }
        if (!schema.isObject()) {
            return false;
        }
        return JsonUtil.keys(JsonUtil.toObject(schema.getNode())).isEmpty();
    }

    /** True if {@code schema} is an object schema with at least one keyword classified as {@link CoverageRegistry.Category#ASSERTION}. */
    static boolean hasAnyAssertion(SchemaView schema) {
        if (!schema.isObject()) {
            return false;
        }
        ObjectNode object = JsonUtil.toObject(schema.getNode());
        List<String> keys = JsonUtil.keys(object);
        for (int i = 0; i < keys.size(); i++) {
            CoverageRegistry.Coverage coverage = CoverageRegistry.classify(schema.getDialect(), keys.get(i));
            if (coverage != null && coverage.getCategory() == CoverageRegistry.Category.ASSERTION) {
                return true;
            }
        }
        return false;
    }

    /** True if {@code schema} is either the {@code true} schema or an object schema with no {@link CoverageRegistry.Category#SCHEMA_CHILD} keyword. */
    static boolean isScalarOnly(SchemaView schema) {
        if (schema.isTrue()) {
            return true;
        }
        if (!schema.isObject()) {
            return false;
        }
        ObjectNode object = JsonUtil.toObject(schema.getNode());
        List<String> keys = JsonUtil.keys(object);
        for (int i = 0; i < keys.size(); i++) {
            CoverageRegistry.Coverage coverage = CoverageRegistry.classify(schema.getDialect(), keys.get(i));
            if (coverage != null && coverage.getCategory() == CoverageRegistry.Category.SCHEMA_CHILD) {
                return false;
            }
        }
        return true;
    }

    /** True if {@code schema} has no array or composition schema-child keyword (it may have object-child keywords). */
    static boolean isObjectOnly(SchemaView schema) {
        if (schema.isTrue()) {
            return true;
        }
        if (!schema.isObject()) {
            return false;
        }
        return !hasAnyKeyword(schema, ARRAY_CHILD_KEYWORDS) && !hasAnyKeyword(schema, COMPOSITION_CHILD_KEYWORDS);
    }

    /** True if {@code schema} has no object or composition schema-child keyword (it may have array-child keywords). */
    static boolean isArrayOnly(SchemaView schema) {
        if (schema.isTrue()) {
            return true;
        }
        if (!schema.isObject()) {
            return false;
        }
        return !hasAnyKeyword(schema, OBJECT_CHILD_KEYWORDS) && !hasAnyKeyword(schema, COMPOSITION_CHILD_KEYWORDS);
    }

    static boolean hasAnyKeyword(SchemaView schema, String[] keywords) {
        for (int i = 0; i < keywords.length; i++) {
            if (schema.hasKeyword(keywords[i])) {
                return true;
            }
        }
        return false;
    }

    private static SchemaEvidence evidence(SchemaView source, SchemaView target, String rule, String message) {
        return new SchemaEvidence(source.getPointer(), target.getPointer(), rule, message);
    }
}
