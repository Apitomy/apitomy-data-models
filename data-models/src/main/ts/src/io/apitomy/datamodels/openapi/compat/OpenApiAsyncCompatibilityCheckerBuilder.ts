import {CompatibilityPolicy} from "./CompatibilityPolicy";
import {OpenApiAsyncCompatibilityChecker} from "./OpenApiAsyncCompatibilityChecker";

/**
 * Builds an immutable {@link OpenApiAsyncCompatibilityChecker}.
 * <p>
 * This is the TypeScript-native counterpart of the Java
 * {@code OpenApiAsyncCompatibilityCheckerBuilder} (excluded from JSweet
 * transpilation solely because it references the excluded
 * {@code OpenApiAsyncCompatibilityChecker}, not because it uses any
 * JVM-only API itself).
 */
export class OpenApiAsyncCompatibilityCheckerBuilder {

    private policyValue: CompatibilityPolicy;

    constructor() {
        this.policyValue = CompatibilityPolicy.defaults();
    }

    /** Sets the compatibility policy the built checker uses. Defaults to {@link CompatibilityPolicy.defaults}. */
    public policy(policy: CompatibilityPolicy): OpenApiAsyncCompatibilityCheckerBuilder {
        if (policy == null) {
            throw new Error("policy must not be null");
        }
        this.policyValue = policy;
        return this;
    }

    /** Builds the immutable checker. */
    public build(): OpenApiAsyncCompatibilityChecker {
        return new OpenApiAsyncCompatibilityChecker(this.policyValue);
    }
}
