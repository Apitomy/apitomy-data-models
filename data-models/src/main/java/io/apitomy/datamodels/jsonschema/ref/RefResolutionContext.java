package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Context provided to a {@link JsonSchemaRefResolver} during reference resolution.
 */
public final class RefResolutionContext {

    private final Node from;
    private final String baseUri;
    private final Map<String, Object> attributes;

    private RefResolutionContext(Node from, String baseUri, Map<String, Object> attributes) {
        this.from = Objects.requireNonNull(from);
        this.baseUri = baseUri;
        this.attributes = Map.copyOf(attributes);
    }

    /**
     * The node containing the {@code $ref}. Provides access to the document tree.
     */
    public Node from() {
        return from;
    }

    /**
     * The base URI for resolving relative references, derived from the nearest
     * ancestor {@code $id} or the document retrieval URI. May be null if unknown.
     */
    public String baseUri() {
        return baseUri;
    }

    /**
     * Extensible attributes for future resolver needs (e.g., authentication context,
     * registry coordinates, caching hints).
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public static Builder builder(Node from) {
        return new Builder(from);
    }

    public static final class Builder {

        private final Node from;
        private String baseUri;
        private final Map<String, Object> attributes = new HashMap<>();

        private Builder(Node from) {
            this.from = from;
        }

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder attribute(String key, Object value) {
            attributes.put(key, value);
            return this;
        }

        public RefResolutionContext build() {
            return new RefResolutionContext(from, baseUri, attributes);
        }
    }
}
