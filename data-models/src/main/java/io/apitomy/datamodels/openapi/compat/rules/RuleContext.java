package io.apitomy.datamodels.openapi.compat.rules;

import io.apitomy.datamodels.openapi.compat.CheckDirection;
import io.apitomy.datamodels.openapi.compat.CompatibilityPolicy;
import io.apitomy.datamodels.openapi.compat.ResourceSide;
import io.apitomy.datamodels.openapi.compat.SourceLocation;
import io.apitomy.datamodels.openapi.compat.contract.ContractDocument;

/**
 * Shared, read-only context passed to every rule module (routing, input,
 * representation, response, security, relationship, schema): the two
 * effective contracts, their check-scoped URIs (for {@link SourceLocation}
 * construction), which replacement direction is being evaluated, and the
 * active policy.
 * <p>
 * A single {@code RuleContext} is built once per direction per check and
 * shared by every rule module for that direction, so they agree on URIs and
 * policy without each rebuilding or re-deriving them.
 */
public final class RuleContext {

    private final ContractDocument originalDocument;
    private final ContractDocument updatedDocument;
    private final String originalUri;
    private final String updatedUri;
    private final CheckDirection direction;
    private final CompatibilityPolicy policy;

    public RuleContext(ContractDocument originalDocument, ContractDocument updatedDocument, String originalUri,
            String updatedUri, CheckDirection direction, CompatibilityPolicy policy) {
        if (originalDocument == null) {
            throw new IllegalArgumentException("originalDocument must not be null");
        }
        if (updatedDocument == null) {
            throw new IllegalArgumentException("updatedDocument must not be null");
        }
        if (originalUri == null || originalUri.length() == 0) {
            throw new IllegalArgumentException("originalUri must not be null or empty");
        }
        if (updatedUri == null || updatedUri.length() == 0) {
            throw new IllegalArgumentException("updatedUri must not be null or empty");
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null");
        }
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        this.originalDocument = originalDocument;
        this.updatedDocument = updatedDocument;
        this.originalUri = originalUri;
        this.updatedUri = updatedUri;
        this.direction = direction;
        this.policy = policy;
    }

    /** The effective contract this check treats as "original" (the currently-relied-upon document, regardless of {@link #getDirection()}). */
    public ContractDocument getOriginalDocument() {
        return originalDocument;
    }

    /** The effective contract this check treats as "updated" (the replacement being evaluated, regardless of {@link #getDirection()}). */
    public ContractDocument getUpdatedDocument() {
        return updatedDocument;
    }

    /** Which replacement direction this context evaluates. Rule modules use this to decide which side's removal is the breaking one. */
    public CheckDirection getDirection() {
        return direction;
    }

    /** The active compatibility policy. */
    public CompatibilityPolicy getPolicy() {
        return policy;
    }

    /** A {@link SourceLocation} at {@code pointer} within the original document. */
    public SourceLocation originalLocation(String pointer) {
        return new SourceLocation(ResourceSide.ORIGINAL, originalUri, pointer);
    }

    /** A {@link SourceLocation} at {@code pointer} within the updated document. */
    public SourceLocation updatedLocation(String pointer) {
        return new SourceLocation(ResourceSide.UPDATED, updatedUri, pointer);
    }
}
