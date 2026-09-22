package io.apitomy.datamodels.openapi.compat;

/**
 * Whether a schema usage is produced by consumers (an input the provider
 * accepts) or produced by the provider (an output consumers must accept).
 * <p>
 * This drives which direction of schema containment is required for backward
 * compatibility: an input must widen (old accepted values remain accepted by
 * the updated schema), while an output must narrow or stay the same (new
 * emitted values must remain acceptable to old consumers). It is independent
 * of {@link HttpRole} -- see that type for why.
 */
public enum ProviderRole {

    /** Consumers send this value; the provider accepts it. */
    INPUT,

    /** The provider sends this value; consumers must accept it. */
    OUTPUT
}
