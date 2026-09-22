package io.apitomy.datamodels.openapi.compat;

/**
 * Distinguishes the original document/resources from the updated document/resources
 * within a single compatibility check.
 * <p>
 * The original and updated sides have independent resource sets: the same URI can
 * resolve to different content on each side, and a resource acquired for one side
 * must never be substituted for the other.
 */
public enum ResourceSide {

    /** The original (pre-change) document and its resources. */
    ORIGINAL,

    /** The updated (post-change) document and its resources. */
    UPDATED
}
