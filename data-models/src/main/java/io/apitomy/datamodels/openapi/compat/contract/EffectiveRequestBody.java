package io.apitomy.datamodels.openapi.compat.contract;

import java.util.Map;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.util.CollectionUtil;

/**
 * An operation's effective request body: 3.x's {@code requestBody}, or a
 * synthesized equivalent for a 2.0 {@code in: body}/{@code in: formData}
 * parameter (which are not otherwise distinguishable from other parameters at
 * the model level except by their {@code in} value).
 */
public final class EffectiveRequestBody {

    private final boolean required;
    private final Map<String, SchemaView> contentByMediaType;
    private final String declarationPointer;

    public EffectiveRequestBody(boolean required, Map<String, SchemaView> contentByMediaType, String declarationPointer) {
        this.required = required;
        this.contentByMediaType = CollectionUtil.copyOfMap(contentByMediaType);
        this.declarationPointer = declarationPointer;
    }

    /** Whether a body is mandatory for this operation. */
    public boolean isRequired() {
        return required;
    }

    /** This body's schema for each media type it is documented under. A 2.0 body/formData parameter always has exactly one synthesized entry. */
    public Map<String, SchemaView> getContentByMediaType() {
        return CollectionUtil.copyOfMap(contentByMediaType);
    }

    /** The JSON Pointer to the declaration that produced this effective request body. */
    public String getDeclarationPointer() {
        return declarationPointer;
    }
}
