package io.apitomy.datamodels.jsonschema.compat;

/**
 * Thrown when a {@code $ref} cannot be resolved and the
 * {@link UnresolvableRefStrategy#FAIL} strategy is configured.
 */
public class UnresolvableRefException extends RuntimeException {

    private final String ref;

    public UnresolvableRefException(String ref) {
        super("Unresolvable $ref: " + ref);
        this.ref = ref;
    }

    /**
     * Returns the {@code $ref} value that could not be resolved.
     */
    public String getRef() {
        return ref;
    }
}
