package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefResolver;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefResolverChain;

/**
 * Builder for {@link JsonSchemaCompatibilityChecker}.
 * <p>
 * Configure the checker and call {@link #build()} to create an immutable instance.
 *
 * <pre>{@code
 * var checker = JsonSchemaCompatibilityChecker.builder()
 *     .allowCrossVersionChecking(true)
 *     .refResolver(myResolver)
 *     .build();
 * }</pre>
 */
public final class JsonSchemaCompatibilityCheckerBuilder {

    private boolean allowCrossVersionChecking = false;
    private JsonSchemaRefResolver refResolver = null;
    private UnresolvableRefStrategy unresolvableRefStrategy = UnresolvableRefStrategy.COLLECT;

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
     * Set a custom reference resolver for resolving {@code $ref} values
     * during comparison.
     * <p>
     * Defaults to {@link JsonSchemaRefResolverChain#withDefaults()} which handles
     * JSON Pointer and anchor-based internal references.
     *
     * @param resolver the reference resolver to use; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code resolver} is {@code null}
     */
    public JsonSchemaCompatibilityCheckerBuilder refResolver(JsonSchemaRefResolver resolver) {
        java.util.Objects.requireNonNull(resolver, "resolver must not be null");
        this.refResolver = resolver;
        return this;
    }

    /**
     * Set the strategy for handling unresolvable {@code $ref} values.
     * <p>
     * Defaults to {@link UnresolvableRefStrategy#COLLECT} which logs unresolvable
     * references as unsupported features and continues the comparison.
     *
     * @param strategy the strategy to use
     * @return this builder
     * @see UnresolvableRefStrategy
     */
    public JsonSchemaCompatibilityCheckerBuilder onUnresolvableRef(UnresolvableRefStrategy strategy) {
        java.util.Objects.requireNonNull(strategy, "strategy must not be null");
        this.unresolvableRefStrategy = strategy;
        return this;
    }

    /**
     * Builds an immutable {@link JsonSchemaCompatibilityChecker} with the
     * configured settings.
     *
     * @return a new checker instance
     */
    public JsonSchemaCompatibilityChecker build() {
        return new JsonSchemaCompatibilityChecker(
                allowCrossVersionChecking,
                refResolver != null ? refResolver : JsonSchemaRefResolverChain.withDefaults(),
                unresolvableRefStrategy);
    }
}
