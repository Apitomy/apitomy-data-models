package io.apitomy.datamodels.openapi.compat;

/**
 * A single, actionable piece of evidence discovered while checking
 * compatibility in one direction.
 * <p>
 * A finding records <em>what</em> changed ({@link FindingCode}), <em>how</em>
 * that affects the checked direction ({@link FindingImpact}), and enough
 * context to act on it: the interaction it belongs to, both source locations
 * (when known), the relevant HTTP/provider roles, and a human-readable
 * explanation.
 * <p>
 * Instances are immutable value objects.
 */
public final class CompatibilityFinding {

    private final FindingCode code;
    private final FindingImpact impact;
    private final String interactionId;
    private final SourceLocation originalLocation;
    private final SourceLocation updatedLocation;
    private final HttpRole httpRole;
    private final ProviderRole providerRole;
    private final String message;

    /**
     * @param code the stable reason identifier
     * @param impact how this finding affects the checked direction's verdict
     * @param interactionId identifier of the affected interaction, or {@code null}
     *        when not tied to a single interaction
     * @param originalLocation where in the original document this finding
     *        applies, or {@code null} when not applicable
     * @param updatedLocation where in the updated document this finding
     *        applies, or {@code null} when not applicable
     * @param httpRole the HTTP message role involved, or {@code null} when not
     *        applicable
     * @param providerRole the provider input/output role involved, or
     *        {@code null} when not applicable
     * @param message a human-readable explanation of this finding
     */
    public CompatibilityFinding(FindingCode code, FindingImpact impact, String interactionId,
            SourceLocation originalLocation, SourceLocation updatedLocation,
            HttpRole httpRole, ProviderRole providerRole, String message) {
        if (code == null) {
            throw new IllegalArgumentException("code must not be null");
        }
        if (impact == null) {
            throw new IllegalArgumentException("impact must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        this.code = code;
        this.impact = impact;
        this.interactionId = interactionId;
        this.originalLocation = originalLocation;
        this.updatedLocation = updatedLocation;
        this.httpRole = httpRole;
        this.providerRole = providerRole;
        this.message = message;
    }

    /** The stable reason identifier. */
    public FindingCode getCode() {
        return code;
    }

    /** How this finding affects the checked direction's verdict. */
    public FindingImpact getImpact() {
        return impact;
    }

    /** Identifier of the affected interaction, or {@code null} when not tied to one. */
    public String getInteractionId() {
        return interactionId;
    }

    /** Where in the original document this finding applies, or {@code null}. */
    public SourceLocation getOriginalLocation() {
        return originalLocation;
    }

    /** Where in the updated document this finding applies, or {@code null}. */
    public SourceLocation getUpdatedLocation() {
        return updatedLocation;
    }

    /** The HTTP message role involved, or {@code null} when not applicable. */
    public HttpRole getHttpRole() {
        return httpRole;
    }

    /** The provider input/output role involved, or {@code null} when not applicable. */
    public ProviderRole getProviderRole() {
        return providerRole;
    }

    /** A human-readable explanation of this finding. */
    public String getMessage() {
        return message;
    }

    /**
     * Total order used to keep {@link CompatibilityResult#getFindings()}
     * deterministic: by interaction id, then code, then original location,
     * then updated location.
     */
    int compareForSorting(CompatibilityFinding other) {
        int byInteraction = compareNullableStrings(this.interactionId, other.interactionId);
        if (byInteraction != 0) {
            return byInteraction;
        }
        int byCode = this.code.name().compareTo(other.code.name());
        if (byCode != 0) {
            return byCode;
        }
        int byOriginal = compareNullableLocations(this.originalLocation, other.originalLocation);
        if (byOriginal != 0) {
            return byOriginal;
        }
        return compareNullableLocations(this.updatedLocation, other.updatedLocation);
    }

    private static int compareNullableStrings(String a, String b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }

    private static int compareNullableLocations(SourceLocation a, SourceLocation b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareForSorting(b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompatibilityFinding)) {
            return false;
        }
        CompatibilityFinding other = (CompatibilityFinding) o;
        return this.code == other.code
                && this.impact == other.impact
                && equalsNullable(this.interactionId, other.interactionId)
                && equalsNullable(this.originalLocation, other.originalLocation)
                && equalsNullable(this.updatedLocation, other.updatedLocation)
                && this.httpRole == other.httpRole
                && this.providerRole == other.providerRole
                && this.message.equals(other.message);
    }

    private static boolean equalsNullable(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + impact.hashCode();
        result = 31 * result + (interactionId == null ? 0 : interactionId.hashCode());
        result = 31 * result + (originalLocation == null ? 0 : originalLocation.hashCode());
        result = 31 * result + (updatedLocation == null ? 0 : updatedLocation.hashCode());
        result = 31 * result + (httpRole == null ? 0 : httpRole.hashCode());
        result = 31 * result + (providerRole == null ? 0 : providerRole.hashCode());
        result = 31 * result + message.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CompatibilityFinding{code=" + code + ", impact=" + impact
                + ", interactionId=" + interactionId + ", message='" + message + "'}";
    }
}
