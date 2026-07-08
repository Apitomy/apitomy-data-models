package io.apitomy.datamodels.jsonschema.compat;

/**
 * Result of a full (bidirectional) compatibility check, combining the results
 * of both a backward and a forward compatibility check.
 * <p>
 * Instances are created by {@link JsonSchemaCompatibilityChecker#checkFull}
 * and are not directly constructible by callers.
 */
public final class FullCompatibilityCheckResult {

    private final CompatibilityCheckResult backward;
    private final CompatibilityCheckResult forward;

    /**
     * Package-private constructor — created by the checker.
     */
    FullCompatibilityCheckResult(CompatibilityCheckResult backward,
                                 CompatibilityCheckResult forward) {
        this.backward = backward;
        this.forward = forward;
    }

    /**
     * Returns {@code true} if the schemas are both backward and forward
     * compatible (i.e. fully compatible in both directions).
     *
     * @return {@code true} when the schemas are fully compatible
     */
    public boolean isFullyCompatible() {
        return backward.isCompatible() && forward.isCompatible();
    }

    /**
     * Returns {@code true} if the updated schema is backward compatible
     * with the original (data written with the original can be read with
     * the updated).
     *
     * @return {@code true} when backward compatible
     */
    public boolean isBackwardCompatible() {
        return backward.isCompatible();
    }

    /**
     * Returns {@code true} if the updated schema is forward compatible
     * with the original (data written with the updated can be read with
     * the original).
     *
     * @return {@code true} when forward compatible
     */
    public boolean isForwardCompatible() {
        return forward.isCompatible();
    }

    /**
     * Returns the detailed backward compatibility check result.
     *
     * @return the backward compatibility result
     */
    public CompatibilityCheckResult getBackwardResult() {
        return backward;
    }

    /**
     * Returns the detailed forward compatibility check result.
     *
     * @return the forward compatibility result
     */
    public CompatibilityCheckResult getForwardResult() {
        return forward;
    }
}
