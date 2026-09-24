package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.jsonschema.JsonSchema;

import java.util.Optional;

/**
 * Resolves the fragment part of a JSON Schema {@code $ref} within a target document.
 * <p>
 * Fragment resolvers handle finding a specific node inside a document, e.g.:
 * <ul>
 *   <li>JSON Pointer fragments ({@code #/definitions/Foo}) — see {@link PointerFragmentResolver}</li>
 *   <li>Anchor fragments ({@code #Address}) — see {@link AnchorFragmentResolver}</li>
 * </ul>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.5">RFC 3986 §3.5 — Fragment</a>
 */
@FunctionalInterface
public interface FragmentResolver {

    /**
     * Resolve the fragment within the given document.
     * <p>
     * Implementations that detect an error should throw
     * {@link ReferenceResolutionException}. Any other exception thrown by this
     * method will be wrapped in a {@link ReferenceResolutionException} by the
     * dereferencer.
     *
     * @param ref            the parsed reference (use {@link JsonRef#pointer()} or {@link JsonRef#anchor()})
     * @param targetDocument the document to search within, which may be a boolean schema
     * @param context        resolution context
     * @return the resolved schema, which may be a boolean schema, or empty if this resolver
     *         cannot handle the fragment type
     * @throws ReferenceResolutionException if resolution fails
     */
    Optional<JsonSchema> resolveFragment(JsonRef ref, JsonSchema targetDocument, RefResolutionContext context);
}
