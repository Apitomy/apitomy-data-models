package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.jsonschema.JsonSchemaProcessingException;

/**
 * Thrown when dereferencing fails due to structural issues such as
 * exceeding the maximum recursion depth.
 */
public class DereferenceException extends JsonSchemaProcessingException {

    public DereferenceException(String message) {
        super(message);
    }

    public DereferenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
