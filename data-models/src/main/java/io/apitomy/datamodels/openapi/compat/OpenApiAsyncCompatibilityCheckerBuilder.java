package io.apitomy.datamodels.openapi.compat;

/**
 * Builds an immutable {@link OpenApiAsyncCompatibilityChecker}.
 */
public final class OpenApiAsyncCompatibilityCheckerBuilder {

    private CompatibilityPolicy policy;

    OpenApiAsyncCompatibilityCheckerBuilder() {
        this.policy = CompatibilityPolicy.defaults();
    }

    /** Sets the compatibility policy the built checker uses. Defaults to {@link CompatibilityPolicy#defaults()}. */
    public OpenApiAsyncCompatibilityCheckerBuilder policy(CompatibilityPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        this.policy = policy;
        return this;
    }

    /** Builds the immutable checker. */
    public OpenApiAsyncCompatibilityChecker build() {
        return new OpenApiAsyncCompatibilityChecker(policy);
    }
}
