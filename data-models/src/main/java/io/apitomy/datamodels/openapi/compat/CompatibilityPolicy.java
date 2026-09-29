package io.apitomy.datamodels.openapi.compat;

import java.util.Set;

import io.apitomy.datamodels.util.CollectionUtil;

/**
 * Named, versioned bundle of compatibility-checking assumptions.
 * <p>
 * A policy identifier is recorded on every {@link CompatibilityResult} so that
 * callers and stored results can tell which assumptions were in effect --
 * for example, whether an added response representation was treated as
 * compatible under a negotiation-stability assumption.
 */
public final class CompatibilityPolicy {

    /** Identifier for the default "preserve existing consumers' documented contract" policy. */
    public static final String EXISTING_CONSUMER_V1 = "existing-consumer-v1";

    private final String id;
    private final Set<String> optedInReverseInteractionAdditions;

    private CompatibilityPolicy(String id, Set<String> optedInReverseInteractionAdditions) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("id must not be null or empty");
        }
        this.id = id;
        this.optedInReverseInteractionAdditions = CollectionUtil.copyOfSet(optedInReverseInteractionAdditions);
    }

    /** The default policy: preserve existing consumers' documented contract; no added callback/webhook is assumed compatible. */
    public static CompatibilityPolicy defaults() {
        return new CompatibilityPolicy(EXISTING_CONSUMER_V1, null);
    }

    /**
     * A copy of this policy that additionally treats a newly added callback or
     * webhook interaction, identified by {@code interactionId}, as an
     * explicit, caller-approved opt-in: its addition is reported informational
     * rather than the default Indeterminate. Per the design contract, "Added
     * callbacks/webhooks" default to Indeterminate; this is the only
     * documented way to establish compatibility for one instead.
     */
    public CompatibilityPolicy withOptedInReverseInteractionAddition(String interactionId) {
        Set<String> next = CollectionUtil.copyOfSet(optedInReverseInteractionAdditions);
        next.add(interactionId);
        return new CompatibilityPolicy(id, next);
    }

    /** True if {@code interactionId} was explicitly opted in via {@link #withOptedInReverseInteractionAddition}. */
    public boolean isReverseInteractionAdditionOptedIn(String interactionId) {
        return optedInReverseInteractionAdditions.contains(interactionId);
    }

    /** The stable identifier for this policy, recorded on every result it produces. */
    public String getId() {
        return id;
    }
}
