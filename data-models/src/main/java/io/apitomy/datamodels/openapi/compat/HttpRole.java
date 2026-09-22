package io.apitomy.datamodels.openapi.compat;

/**
 * The HTTP message role a schema usage occupies, independent of which party is
 * sending it.
 * <p>
 * This drives {@code readOnly}/{@code writeOnly} interpretation. It is
 * independent of {@link ProviderRole}: a webhook request is still a request for
 * this purpose, even though it is a provider <em>output</em> for
 * compatibility-direction purposes (see {@link ProviderRole}).
 */
public enum HttpRole {

    /** The schema constrains a value sent as part of an HTTP request. */
    REQUEST,

    /** The schema constrains a value sent as part of an HTTP response. */
    RESPONSE
}
