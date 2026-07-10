package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.jsonschema.JsonSchemaProcessingException;

/**
 * Thrown when a {@code $ref} cannot be resolved, either because the
 * {@link UnresolvableRefStrategy#FAIL} strategy is configured, or because
 * the reference resolver itself threw an exception.
 */
public class ReferenceResolutionException extends JsonSchemaProcessingException {

    private final String ref;

    public ReferenceResolutionException(String message, String ref) {
        super(message);
        this.ref = ref;
    }

    public ReferenceResolutionException(String message, String ref, Throwable cause) {
        super(message, cause);
        this.ref = ref;
    }

    /**
     * Returns the {@code $ref} value that could not be resolved.
     */
    public String getRef() {
        return ref;
    }
}
