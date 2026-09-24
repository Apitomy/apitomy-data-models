package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.jsonschema.JsonSchema;

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
 * <p>
 * The result is a {@link JsonSchema}: a full schema, or a boolean schema when the
 * document is the literal {@code true} or {@code false}.
 * <p>
 * <b>Identity contract:</b> Implementations should return the same instance for
 * the same logical resource, even if invoked multiple times with
 * the same resource identifier. The returned schema is not cloned by the caller
 * and may be mutated (e.g., by the {@link JsonSchemaRefDereferencer}).
 * If two different resource identifiers refer to the same logical document,
 * implementations should return the same instance for both to enable correct
 * cycle detection during dereferencing.
 */
@FunctionalInterface
public interface ResourceResolver {

    /**
     * Resolve an external resource to a parsed document node.
     * <p>
     * Implementations that detect an error should throw
     * {@link ReferenceResolutionException}. Any other exception thrown by this
     * method will be wrapped in a {@link ReferenceResolutionException} by the
     * dereferencer.
     *
     * @param resource the resource identifier (the part before {@code #}, e.g., {@code "other.json"})
     * @param context  resolution context
     * @return the parsed document node, or empty if this resolver cannot handle the resource
     * @throws ReferenceResolutionException if resolution fails
     */
    Optional<JsonSchema> resolveResource(String resource, RefResolutionContext context);
}
