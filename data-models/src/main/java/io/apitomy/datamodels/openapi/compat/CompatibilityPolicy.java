package io.apitomy.datamodels.openapi.compat;

/**
 * Named, versioned bundle of compatibility-checking assumptions.
 * <p>
 * A policy identifier is recorded on every {@link CompatibilityResult} so that
 * callers and stored results can tell which assumptions were in effect --
 * for example, whether an added response representation was treated as
 * compatible under a negotiation-stability assumption.
 * <p>
 * This class currently exposes only its identifier. Later tasks add the
 * concrete policy toggles (read/write interpretation, negotiation stability,
 * callback opt-in mapping, and so on) without changing this shape.
 */
public final class CompatibilityPolicy {

    /** Identifier for the default "preserve existing consumers' documented contract" policy. */
    public static final String EXISTING_CONSUMER_V1 = "existing-consumer-v1";

    private final String id;

    private CompatibilityPolicy(String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("id must not be null or empty");
        }
        this.id = id;
    }

    /** The default policy: preserve existing consumers' documented contract. */
    public static CompatibilityPolicy defaults() {
        return new CompatibilityPolicy(EXISTING_CONSUMER_V1);
    }

    /** The stable identifier for this policy, recorded on every result it produces. */
    public String getId() {
        return id;
    }
}
