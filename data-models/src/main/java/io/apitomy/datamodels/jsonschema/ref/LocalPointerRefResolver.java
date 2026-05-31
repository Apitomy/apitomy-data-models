package io.apitomy.datamodels.jsonschema.ref;

import java.util.Optional;

/**
 * Resolves internal JSON Pointer references ({@code #/definitions/Foo}, {@code #/$defs/Bar}).
 * Delegates tree walking to {@link JsonPointer#evaluate}.
 */
public class LocalPointerRefResolver implements JsonSchemaRefResolver {

    @Override
    public Optional<ResolvedRef> resolve(JsonRef ref, RefResolutionContext context) {
        if (!ref.isInternal() || !ref.isPointer()) {
            return Optional.empty();
        }
        var node = ref.pointer().evaluate(context.from().root());
        return Optional.ofNullable(node).map(ResolvedRef::attached);
    }
}
