package io.apitomy.datamodels.openapi.compat;

/**
 * Compatibility consequence carried by a single {@link CompatibilityFinding}.
 * <p>
 * The impact -- not the {@link FindingCode} alone -- determines how a finding
 * affects the overall {@link CompatibilityVerdict}. The same code can carry
 * different impacts in different contexts (for example, a schema comparison
 * that is definitely compatible still produces a finding so that its evidence
 * is visible in {@link CompatibilityResult#getFindings()}).
 */
public enum FindingImpact {

    /** The change breaks the documented contract in the checked direction. */
    BREAKING,

    /** The relevant obligation could not be established as compatible or breaking. */
    UNRESOLVED,

    /** The change was checked and found to preserve the documented contract. */
    COMPATIBLE,

    /** The finding does not affect compatibility; it is provided for context. */
    INFORMATIONAL
}
