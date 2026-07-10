package io.apitomy.datamodels.jsonschema.ref;

/**
 * Strategy for handling {@code $ref} values that cannot be resolved
 * during dereferencing.
 *
 * @see JsonSchemaRefDereferencer
 */
public enum UnresolvableRefStrategy {

    /**
     * Record the unresolvable reference and continue processing.
     * Unresolved references are reported in {@link DereferenceResult#unresolvedRefs()}.
     * The {@code $ref} node is left as-is in the tree.
     * <p>
     * This is the default strategy.
     */
    COLLECT,

    /**
     * Throw an exception immediately when an unresolvable
     * reference is encountered. Use this when all references are expected to be
     * resolvable and an unresolvable one indicates a configuration error.
     */
    FAIL
}
