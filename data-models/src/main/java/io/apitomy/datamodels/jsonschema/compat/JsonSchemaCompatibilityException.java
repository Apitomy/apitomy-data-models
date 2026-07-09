package io.apitomy.datamodels.jsonschema.compat;

/**
 * Base exception for errors during JSON Schema compatibility checking.
 */
public class JsonSchemaCompatibilityException extends RuntimeException {

    public JsonSchemaCompatibilityException(String message) {
        super(message);
    }

    public JsonSchemaCompatibilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
