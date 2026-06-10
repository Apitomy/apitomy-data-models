package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;

import java.util.Optional;

/**
 * Resolves the resource (document) part of an external JSON Schema {@code $ref}.
 * <p>
 * Resource resolvers handle fetching or looking up an external document
 * identified by the part before {@code #} in a {@code $ref} value, e.g.:
 * <ul>
 *   <li>Registry storage lookup</li>
 *   <li>HTTP(S) URL fetching</li>
 *   <li>File system resolution</li>
 * </ul>
 * Fragment resolution (the part after {@code #}) is handled separately
 * by {@link FragmentResolver}.
 */
@FunctionalInterface
public interface ResourceResolver {

    /**
     * Resolve an external resource to a parsed document node.
     *
     * @param resource the resource identifier (the part before {@code #}, e.g., {@code "other.json"})
     * @param context  resolution context
     * @return the parsed document node, or empty if this resolver cannot handle the resource
     */
    Optional<Node> resolveResource(String resource, RefResolutionContext context);
}
