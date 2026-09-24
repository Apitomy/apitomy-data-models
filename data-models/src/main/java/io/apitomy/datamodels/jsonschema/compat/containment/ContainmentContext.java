package io.apitomy.datamodels.jsonschema.compat.containment;

import io.apitomy.datamodels.openapi.compat.CheckDirection;
import io.apitomy.datamodels.openapi.compat.CompatibilityPolicy;
import io.apitomy.datamodels.openapi.compat.HttpRole;
import io.apitomy.datamodels.openapi.compat.ProviderRole;
import io.apitomy.datamodels.openapi.compat.resource.ReferenceGraph;

/**
 * Everything a containment proof needs beyond the two {@link SchemaView}s
 * being compared: which HTTP message and provider direction they occur in
 * (for {@code readOnly}/{@code writeOnly} interpretation), which replacement
 * direction is being evaluated, the policy in effect, each side's reference
 * graph (so a proof can follow a {@code $ref} without re-resolving it), and a
 * bounded proof-search budget.
 */
public final class ContainmentContext {

    /** The default proof-search budget: a generous but finite limit on proof-rule work for one comparison. */
    public static final int DEFAULT_PROOF_BUDGET = 10000;

    private final HttpRole httpRole;
    private final ProviderRole providerRole;
    private final CheckDirection direction;
    private final CompatibilityPolicy policy;
    private final ReferenceGraph sourceGraph;
    private final ReferenceGraph targetGraph;
    private final int proofBudget;

    public ContainmentContext(HttpRole httpRole, ProviderRole providerRole, CheckDirection direction,
            CompatibilityPolicy policy, ReferenceGraph sourceGraph, ReferenceGraph targetGraph, int proofBudget) {
        if (httpRole == null) {
            throw new IllegalArgumentException("httpRole must not be null");
        }
        if (providerRole == null) {
            throw new IllegalArgumentException("providerRole must not be null");
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null");
        }
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        if (proofBudget <= 0) {
            throw new IllegalArgumentException("proofBudget must be positive");
        }
        this.httpRole = httpRole;
        this.providerRole = providerRole;
        this.direction = direction;
        this.policy = policy;
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
        this.proofBudget = proofBudget;
    }

    /** A context with the default proof budget and no reference graphs. */
    public static ContainmentContext of(HttpRole httpRole, ProviderRole providerRole, CheckDirection direction,
            CompatibilityPolicy policy) {
        return new ContainmentContext(httpRole, providerRole, direction, policy, null, null, DEFAULT_PROOF_BUDGET);
    }

    /** A copy of this context with the given reference graphs attached. */
    public ContainmentContext withGraphs(ReferenceGraph sourceGraph, ReferenceGraph targetGraph) {
        return new ContainmentContext(httpRole, providerRole, direction, policy, sourceGraph, targetGraph, proofBudget);
    }

    /** A copy of this context with a different proof budget. */
    public ContainmentContext withProofBudget(int proofBudget) {
        return new ContainmentContext(httpRole, providerRole, direction, policy, sourceGraph, targetGraph, proofBudget);
    }

    /** Whether this schema occurs in a request or a response, driving readOnly/writeOnly interpretation. */
    public HttpRole getHttpRole() {
        return httpRole;
    }

    /** Whether this schema describes input accepted or output produced, driving containment direction. */
    public ProviderRole getProviderRole() {
        return providerRole;
    }

    /** Which replacement direction is being evaluated. */
    public CheckDirection getDirection() {
        return direction;
    }

    /** The policy in effect. */
    public CompatibilityPolicy getPolicy() {
        return policy;
    }

    /** The original side's reference graph, or {@code null} if none is available. */
    public ReferenceGraph getSourceGraph() {
        return sourceGraph;
    }

    /** The updated side's reference graph, or {@code null} if none is available. */
    public ReferenceGraph getTargetGraph() {
        return targetGraph;
    }

    /** The remaining proof-search budget for this comparison. */
    public int getProofBudget() {
        return proofBudget;
    }
}
