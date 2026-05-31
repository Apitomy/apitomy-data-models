package io.apitomy.datamodels.jsonschema.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A chain of {@link JsonSchemaRefResolver}s tried in insertion order.
 * The first resolver that returns a non-empty result wins.
 */
public final class JsonSchemaRefResolverChain implements JsonSchemaRefResolver {

    private final List<JsonSchemaRefResolver> resolvers;

    private JsonSchemaRefResolverChain(List<JsonSchemaRefResolver> resolvers) {
        this.resolvers = List.copyOf(resolvers);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a chain with the default resolvers for internal reference resolution:
     * <ol>
     *   <li>{@link LocalPointerRefResolver} — {@code #/definitions/...} and {@code #/$defs/...}</li>
     *   <li>{@link AnchorRefResolver} — {@code #anchorName} via {@code $anchor} or {@code $id} fragment</li>
     * </ol>
     */
    public static JsonSchemaRefResolverChain withDefaults() {
        return builder()
                .addResolver(new LocalPointerRefResolver())
                .addResolver(new AnchorRefResolver())
                .build();
    }

    @Override
    public Optional<ResolvedRef> resolve(JsonRef ref, RefResolutionContext context) {
        for (var resolver : resolvers) {
            var result = resolver.resolve(ref, context);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    public static final class Builder {

        private final List<JsonSchemaRefResolver> resolvers = new ArrayList<>();

        private Builder() {
        }

        public Builder addResolver(JsonSchemaRefResolver resolver) {
            resolvers.add(resolver);
            return this;
        }

        public JsonSchemaRefResolverChain build() {
            if (resolvers.isEmpty()) {
                throw new IllegalStateException("At least one resolver must be registered");
            }
            return new JsonSchemaRefResolverChain(resolvers);
        }
    }
}
