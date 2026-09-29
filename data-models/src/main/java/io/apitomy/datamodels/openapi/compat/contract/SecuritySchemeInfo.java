package io.apitomy.datamodels.openapi.compat.contract;

/**
 * The semantically relevant fields of one resolved security scheme
 * definition, used to test whether two schemes (possibly under different
 * component names, including across a rename) are the same mechanism.
 */
public final class SecuritySchemeInfo {

    private final String type;
    private final String in;
    private final String name;
    private final String httpScheme;
    private final boolean hasFlow;

    public SecuritySchemeInfo(String type, String in, String name, String httpScheme, boolean hasFlow) {
        this.type = type;
        this.in = in;
        this.name = name;
        this.httpScheme = httpScheme;
        this.hasFlow = hasFlow;
    }

    /** {@code "apiKey"}, {@code "http"}, {@code "oauth2"}, {@code "openIdConnect"}, or a family-specific mTLS type name. */
    public String getType() {
        return type;
    }

    /** The API key location ({@code "query"}/{@code "header"}/{@code "cookie"}), or {@code null} for non-apiKey schemes. */
    public String getIn() {
        return in;
    }

    /** The API key parameter/header name, or {@code null} for non-apiKey schemes. */
    public String getName() {
        return name;
    }

    /** The HTTP authentication scheme name (e.g. {@code "bearer"}), or {@code null} for non-http schemes. Compared case-insensitively by callers. */
    public String getHttpScheme() {
        return httpScheme;
    }

    /** True if this scheme documents at least one credential-acquisition mechanism (an OAuth2 flow, for schemes where that applies). */
    public boolean hasFlow() {
        return hasFlow;
    }
}
