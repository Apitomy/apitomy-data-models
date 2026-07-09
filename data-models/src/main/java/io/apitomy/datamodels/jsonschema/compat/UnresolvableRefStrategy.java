package io.apitomy.datamodels.jsonschema.compat;

/**
 * Strategy for handling {@code $ref} values that cannot be resolved during
 * compatibility checking.
 *
 * @see JsonSchemaCompatibilityCheckerBuilder#onUnresolvableRef(UnresolvableRefStrategy)
 */
public enum UnresolvableRefStrategy {

    /**
     * Log the unresolvable reference as an unsupported feature and continue
     * the comparison. The unresolved schema is compared structurally (as-is,
     * with the {@code $ref} field present). The result will include
     * {@link CompatibilityCheckResult#hasUnsupportedFeatures() unsupported features}.
     * <p>
     * This is the default strategy.
     */
    COLLECT,

    /**
     * Throw a {@link JsonSchemaCompatibilityException} immediately when an unresolvable
     * reference is encountered. Use this when all references are expected to be
     * resolvable and an unresolvable one indicates a configuration error.
     */
    FAIL,

    /**
     * Silently ignore the unresolvable reference and skip comparing the
     * referenced schema. No unsupported feature is recorded. Use this when
     * unresolved external references are expected and should not affect the result.
     */
    IGNORE
}
