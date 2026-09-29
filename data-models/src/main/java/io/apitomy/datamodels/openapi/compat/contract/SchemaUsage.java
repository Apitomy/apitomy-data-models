package io.apitomy.datamodels.openapi.compat.contract;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.openapi.compat.HttpRole;
import io.apitomy.datamodels.openapi.compat.ProviderRole;

/**
 * One schema, at one concrete place it is used to constrain a value, with the
 * roles that determine both read/write interpretation ({@link HttpRole}) and
 * replacement-direction obligation ({@link ProviderRole}) for that usage.
 * <p>
 * A shared component schema is not inherently an input or an output: the same
 * {@code Widget} schema referenced from both a request body and a response
 * body is two different {@code SchemaUsage} instances (with opposite roles),
 * even though {@link #getSchema()} may wrap the identical underlying node.
 * Later rules attach findings per usage, never merely per definition.
 */
public final class SchemaUsage {

    private final SchemaView schema;
    private final String interactionId;
    private final HttpRole httpRole;
    private final ProviderRole providerRole;
    private final String mediaType;
    private final String location;

    public SchemaUsage(SchemaView schema, String interactionId, HttpRole httpRole, ProviderRole providerRole,
            String mediaType, String location) {
        if (schema == null) {
            throw new IllegalArgumentException("schema must not be null");
        }
        if (interactionId == null || interactionId.length() == 0) {
            throw new IllegalArgumentException("interactionId must not be null or empty");
        }
        if (httpRole == null) {
            throw new IllegalArgumentException("httpRole must not be null");
        }
        if (providerRole == null) {
            throw new IllegalArgumentException("providerRole must not be null");
        }
        if (location == null || location.length() == 0) {
            throw new IllegalArgumentException("location must not be null or empty");
        }
        this.schema = schema;
        this.interactionId = interactionId;
        this.httpRole = httpRole;
        this.providerRole = providerRole;
        this.mediaType = mediaType;
        this.location = location;
    }

    /** The schema at this usage, under its own dialect. */
    public SchemaView getSchema() {
        return schema;
    }

    /** The stable identity of the interaction (see {@link EffectiveInteraction#getInteractionId()}) this usage belongs to. */
    public String getInteractionId() {
        return interactionId;
    }

    /** Which HTTP message role (request/response) this usage occupies. */
    public HttpRole getHttpRole() {
        return httpRole;
    }

    /** Which containment direction (input/output) this usage requires for backward compatibility. */
    public ProviderRole getProviderRole() {
        return providerRole;
    }

    /** The media type this usage is negotiated under, or {@code null} if not media-type-specific (e.g. a query parameter). */
    public String getMediaType() {
        return mediaType;
    }

    /** A human-readable description of where within the interaction this usage occurs (e.g. {@code "parameter:id"}, {@code "response:200:application/json"}). */
    public String getLocation() {
        return location;
    }
}
