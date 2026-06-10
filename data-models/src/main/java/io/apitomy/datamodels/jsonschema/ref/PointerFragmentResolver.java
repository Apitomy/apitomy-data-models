package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;

import java.util.Optional;

/**
 * Resolves JSON Pointer fragments ({@code #/definitions/Foo}, {@code #/$defs/Bar}).
 * Delegates tree walking to {@link JsonPointer#evaluate}.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6901">RFC 6901 — JSON Pointer</a>
 */
public class PointerFragmentResolver implements FragmentResolver {

    @Override
    public Optional<Node> resolveFragment(JsonRef ref, Node targetDocument, RefResolutionContext context) {
        if (!ref.isPointer()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ref.pointer().evaluate(targetDocument));
    }
}
