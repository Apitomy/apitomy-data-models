package io.apitomy.datamodels.jsonschema.compat.containment;

/**
 * The outcome of a containment/comparison proof: whether every instance
 * satisfying one schema is guaranteed to satisfy another.
 * <p>
 * There is no boolean collapse of {@code UNKNOWN} into either {@code YES} or
 * {@code NO} anywhere in this engine: an unsupported keyword, an exhausted
 * proof budget, or an unrecognized dialect must surface as {@code UNKNOWN} so
 * that a caller can report {@code SCHEMA_UNRESOLVED} rather than a false
 * compatibility guarantee.
 */
public enum ContainmentVerdict {

    /** Every instance satisfying the source schema is proven to satisfy the target schema. */
    YES,

    /** A validated counterexample instance satisfies the source schema but not the target schema. */
    NO,

    /** Neither a proof nor a counterexample could be established with the rules and budget available. */
    UNKNOWN
}
