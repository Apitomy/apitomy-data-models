package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A resource resolver that looks up schemas from an in-memory map.
 * Useful for testing and for providing schemas from a registry or other storage.
 *
 * <pre>{@code
 * var resolver = MapResourceResolver.builder()
 *     .addSchema("http://example.com/address.json", addressSchemaJson)
 *     .build();
 * }</pre>
 */
public class MapResourceResolver implements ResourceResolver {

    private final Map<String, String> schemas; // URI → JSON string

    private MapResourceResolver(Map<String, String> schemas) {
        this.schemas = Map.copyOf(schemas);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Optional<Node> resolveResource(String resource, RefResolutionContext context) {
        var json = schemas.get(resource);
        if (json == null) {
            return Optional.empty();
        }
        return Optional.of((Node) Library.readRootFromJSONString(json));
    }

    public static final class Builder {

        private final Map<String, String> schemas = new LinkedHashMap<>();

        /**
         * Add a schema that can be resolved by URI.
         *
         * @param uri        the URI used in $ref (e.g., "http://example.com/schema.json")
         * @param schemaJson the JSON Schema document as a string
         */
        public Builder addSchema(String uri, String schemaJson) {
            schemas.put(uri, schemaJson);
            return this;
        }

        public MapResourceResolver build() {
            return new MapResourceResolver(schemas);
        }
    }
}
