package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefDereferencer;

/**
 * Builder for {@link JsonSchemaCompatibilityChecker}.
 * <p>
 * Configure the checker and call {@link #build()} to create an immutable instance.
 *
 * <pre>{@code
 * var deref = JsonSchemaRefDereferencer.builder()
 *     .refResolver(myResolver)
 *     .onUnresolvableRef(UnresolvableRefStrategy.FAIL)
 *     .build();
 *
 * var checker = JsonSchemaCompatibilityChecker.builder()
 *     .allowCrossVersionChecking(true)
 *     .dereferencer(deref)
 *     .build();
 * }</pre>
 */
public final class JsonSchemaCompatibilityCheckerBuilder {

    private boolean allowCrossVersionChecking = false;
    private JsonSchemaRefDereferencer dereferencer = null;

    JsonSchemaCompatibilityCheckerBuilder() {
    }

    /**
     * Allow comparing schemas from different JSON Schema draft versions.
     * <p>
     * When disabled (the default), comparing a draft-4 schema with a draft-7
     * schema throws {@link IllegalArgumentException}. Enable this to permit
     * cross-version comparisons.
     *
     * @param allow {@code true} to allow cross-version checking
     * @return this builder
     */
    public JsonSchemaCompatibilityCheckerBuilder allowCrossVersionChecking(boolean allow) {
        this.allowCrossVersionChecking = allow;
        return this;
    }

    /**
     * Set a dereferencer for resolving {@code $ref} values before comparison.
     * <p>
     * When set, all {@code $ref} nodes are resolved and inlined before the
     * schemas are compared. When not set, {@code $ref} values are compared
     * as plain strings — equal references are compatible, different references
     * are incompatible.
     *
     * @param dereferencer the dereferencer instance
     * @return this builder
     */
    public JsonSchemaCompatibilityCheckerBuilder dereferencer(JsonSchemaRefDereferencer dereferencer) {
        java.util.Objects.requireNonNull(dereferencer, "dereferencer must not be null");
        this.dereferencer = dereferencer;
        return this;
    }

    /**
     * Builds an immutable {@link JsonSchemaCompatibilityChecker} with the
     * configured settings.
     *
     * @return a new checker instance
     */
    public JsonSchemaCompatibilityChecker build() {
        return new JsonSchemaCompatibilityChecker(allowCrossVersionChecking, dereferencer);
    }
}
