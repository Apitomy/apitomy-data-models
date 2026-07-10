package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.jsonschema.JsonSchemaProcessingException;

/**
 * Exception for errors during JSON Schema compatibility checking.
 */
public class JsonSchemaCompatibilityException extends JsonSchemaProcessingException {

    public JsonSchemaCompatibilityException(String message) {
        super(message);
    }

    public JsonSchemaCompatibilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
