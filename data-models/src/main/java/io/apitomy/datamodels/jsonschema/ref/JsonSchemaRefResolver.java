package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;

import java.util.Optional;

/**
 * Top-level interface for resolving a JSON Schema {@code $ref} to a node.
 * <p>
 * The default implementation ({@link JsonSchemaRefResolverChain}) splits resolution
 * into two phases: resource resolution (finding the document) and fragment resolution
 * (finding a node within the document). See {@link ResourceResolver} and {@link FragmentResolver}.
 */
@FunctionalInterface
public interface JsonSchemaRefResolver {

    /**
     * Resolve the given {@code $ref} to a schema node.
     *
     * @param ref     the parsed reference
     * @param context resolution context providing the source node and base URI
     * @return the resolved node, or empty if unresolvable
     */
    Optional<Node> resolve(JsonRef ref, RefResolutionContext context);
}
