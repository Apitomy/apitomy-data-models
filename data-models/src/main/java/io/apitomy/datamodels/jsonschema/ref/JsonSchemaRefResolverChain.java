package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves {@code $ref} values using two configurable resolver chains:
 * <ul>
 *   <li><b>Fragment resolvers</b> — find a node within a document (pointer, anchor)</li>
 *   <li><b>Resource resolvers</b> — fetch an external document by resource identifier</li>
 * </ul>
 * <p>
 * Resolution logic:
 * <ol>
 *   <li>Determine the target document: if internal ref, use current document; if external, try resource resolvers</li>
 *   <li>If the ref has a fragment, try fragment resolvers against the target document</li>
 *   <li>If no fragment, return the target document itself</li>
 * </ol>
 */
public final class JsonSchemaRefResolverChain implements JsonSchemaRefResolver {

    private final List<FragmentResolver> fragmentResolvers;
    private final List<ResourceResolver> resourceResolvers;

    private JsonSchemaRefResolverChain(List<FragmentResolver> fragmentResolvers,
                                       List<ResourceResolver> resourceResolvers) {
        this.fragmentResolvers = List.copyOf(fragmentResolvers);
        this.resourceResolvers = List.copyOf(resourceResolvers);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a chain with default fragment resolvers (pointer + anchor) and no resource resolvers.
     */
    public static JsonSchemaRefResolverChain withDefaults() {
        return builder()
                .addFragmentResolver(new PointerFragmentResolver())
                .addFragmentResolver(new AnchorFragmentResolver())
                .build();
    }

    @Override
    public Optional<Node> resolve(JsonRef ref, RefResolutionContext context) {
        // Step 1: Determine the target document
        Node targetDocument;
        if (ref.isInternal()) {
            targetDocument = context.from().root();
        } else {
            var doc = resolveResource(ref.resource(), context);
            if (doc.isEmpty()) {
                return Optional.empty();
            }
            targetDocument = doc.get();
        }

        // Step 2: Apply fragment resolution
        if (ref.isPointer() || ref.isAnchor()) {
            return resolveFragment(ref, targetDocument, context);
        }

        // No fragment — return the document itself
        return Optional.of(targetDocument);
    }

    private Optional<Node> resolveResource(String resource, RefResolutionContext context) {
        for (var resolver : resourceResolvers) {
            var result = resolver.resolveResource(resource, context);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private Optional<Node> resolveFragment(JsonRef ref, Node targetDocument, RefResolutionContext context) {
        for (var resolver : fragmentResolvers) {
            var result = resolver.resolveFragment(ref, targetDocument, context);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    public static final class Builder {

        private final List<FragmentResolver> fragmentResolvers = new ArrayList<>();
        private final List<ResourceResolver> resourceResolvers = new ArrayList<>();

        private Builder() {
        }

        public Builder addFragmentResolver(FragmentResolver resolver) {
            fragmentResolvers.add(resolver);
            return this;
        }

        public Builder addResourceResolver(ResourceResolver resolver) {
            resourceResolvers.add(resolver);
            return this;
        }

        public JsonSchemaRefResolverChain build() {
            if (fragmentResolvers.isEmpty()) {
                throw new IllegalStateException("At least one fragment resolver must be registered");
            }
            return new JsonSchemaRefResolverChain(fragmentResolvers, resourceResolvers);
        }
    }
}
