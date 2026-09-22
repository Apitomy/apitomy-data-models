package io.apitomy.datamodels.openapi.compat;

/**
 * Combined result of checking compatibility in both directions.
 * <p>
 * The two directional results ({@link #getBackwardResult()} and
 * {@link #getForwardResult()}) are preserved independently; {@link #getVerdict()}
 * is derived from them but never replaces either one.
 */
public final class FullCompatibilityResult {

    private final CompatibilityResult backwardResult;
    private final CompatibilityResult forwardResult;

    /**
     * @param backwardResult the result of checking whether the updated
     *        document can replace the original document
     * @param forwardResult the result of checking whether the original
     *        document can replace the updated document
     */
    public FullCompatibilityResult(CompatibilityResult backwardResult, CompatibilityResult forwardResult) {
        if (backwardResult == null) {
            throw new IllegalArgumentException("backwardResult must not be null");
        }
        if (forwardResult == null) {
            throw new IllegalArgumentException("forwardResult must not be null");
        }
        this.backwardResult = backwardResult;
        this.forwardResult = forwardResult;
    }

    /**
     * The overall verdict: {@code INCOMPATIBLE} if either direction is
     * incompatible; otherwise {@code INDETERMINATE} if either direction is
     * indeterminate; otherwise {@code COMPATIBLE}.
     */
    public CompatibilityVerdict getVerdict() {
        CompatibilityVerdict backward = backwardResult.getVerdict();
        CompatibilityVerdict forward = forwardResult.getVerdict();
        if (backward == CompatibilityVerdict.INCOMPATIBLE || forward == CompatibilityVerdict.INCOMPATIBLE) {
            return CompatibilityVerdict.INCOMPATIBLE;
        }
        if (backward == CompatibilityVerdict.INDETERMINATE || forward == CompatibilityVerdict.INDETERMINATE) {
            return CompatibilityVerdict.INDETERMINATE;
        }
        return CompatibilityVerdict.COMPATIBLE;
    }

    /** The result of checking whether the updated document can replace the original. */
    public CompatibilityResult getBackwardResult() {
        return backwardResult;
    }

    /** The result of checking whether the original document can replace the updated document. */
    public CompatibilityResult getForwardResult() {
        return forwardResult;
    }
}
