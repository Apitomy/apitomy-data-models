package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.List;

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
            return ContainmentResult.unknown(evidence(source, target, "empty-target",
                    "The target schema is `false`; whether the source schema is itself satisfiable by no instance "
                            + "is not established here"));
        }

        // From here, target is a (non-false) object schema.
        ContainmentResult targetGap = coverageGap(target);
        if (targetGap != null) {
            return targetGap;
        }

        if (source.isTrue()) {
            if (hasAnyAssertion(target)) {
                return ContainmentResult.unknown(evidence(source, target, "no-proof-rule",
                        "The source schema is `true` (matches every instance); the target schema has at least one "
                                + "constraining keyword, and no scalar/object/array/composition proof rule is "
                                + "implemented yet to determine whether it excludes some instance"));
            }
            return ContainmentResult.yes(evidence(source, target, "universal-source-unconstrained-target",
                    "The source schema is `true`, and the target schema has no constraining keyword under its dialect"));
        }

        ContainmentResult sourceGap = coverageGap(source);
        if (sourceGap != null) {
            return sourceGap;
        }

        if (!hasAnyAssertion(source) && !hasAnyAssertion(target)) {
            return ContainmentResult.yes(evidence(source, target, "both-unconstrained",
                    "Neither schema has a constraining keyword under its dialect"));
        }

        return ContainmentResult.unknown(evidence(source, target, "no-proof-rule",
                "No scalar/object/array/composition proof rule is implemented yet for this schema shape"));
    }

    /**
     * {@code null} if every keyword present on {@code schema} is a recognized
     * assertion, schema-child, or annotation; otherwise an
     * {@link ContainmentVerdict#UNKNOWN} result naming the first unsupported or
     * unrecognized keyword found.
     */
    private static ContainmentResult coverageGap(SchemaView schema) {
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

    /** True if {@code schema} is an object schema with at least one keyword classified as {@link CoverageRegistry.Category#ASSERTION}. */
    private static boolean hasAnyAssertion(SchemaView schema) {
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

    private static SchemaEvidence evidence(SchemaView source, SchemaView target, String rule, String message) {
        return new SchemaEvidence(source.getPointer(), target.getPointer(), rule, message);
    }
}
