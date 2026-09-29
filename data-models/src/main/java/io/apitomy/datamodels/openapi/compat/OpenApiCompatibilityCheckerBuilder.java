package io.apitomy.datamodels.openapi.compat;

/**
 * Builds an immutable {@link OpenApiCompatibilityChecker}.
 */
public final class OpenApiCompatibilityCheckerBuilder {

    private CompatibilityPolicy policy;

    OpenApiCompatibilityCheckerBuilder() {
        this.policy = CompatibilityPolicy.defaults();
    }

    /** Sets the compatibility policy the built checker uses. Defaults to {@link CompatibilityPolicy#defaults()}. */
    public OpenApiCompatibilityCheckerBuilder policy(CompatibilityPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        this.policy = policy;
        return this;
    }

    /** Builds the immutable checker. */
    public OpenApiCompatibilityChecker build() {
        return new OpenApiCompatibilityChecker(policy);
    }
}
