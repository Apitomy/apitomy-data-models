package io.apitomy.datamodels.jsonschema;

/**
 * Base exception for all JSON Schema processing errors, including
 * compatibility checking, dereferencing, and reference resolution.
 */
public class JsonSchemaProcessingException extends RuntimeException {

    public JsonSchemaProcessingException(String message) {
        super(message);
    }

    public JsonSchemaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
