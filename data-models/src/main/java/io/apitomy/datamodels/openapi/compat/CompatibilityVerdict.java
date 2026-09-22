package io.apitomy.datamodels.openapi.compat;

/**
 * Overall result of a single-direction compatibility check.
 * <p>
 * There is no boolean shorthand for this type: callers must handle
 * {@code INDETERMINATE} explicitly rather than have it silently collapse into
 * a pass or a fail.
 */
public enum CompatibilityVerdict {

    /** Every relevant obligation was established as preserved. */
    COMPATIBLE,

    /** At least one relevant obligation was established as broken. */
    INCOMPATIBLE,

    /**
     * No obligation was established as broken, but at least one relevant
     * obligation could not be resolved as compatible or breaking.
     */
    INDETERMINATE
}
