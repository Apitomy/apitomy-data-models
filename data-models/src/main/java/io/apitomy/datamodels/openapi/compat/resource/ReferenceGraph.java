package io.apitomy.datamodels.openapi.compat.resource;

import io.apitomy.datamodels.openapi.compat.ResourceSide;
import io.apitomy.datamodels.util.CollectionUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The typed reference edges discovered for one side of a check, together with
 * whichever of them resolved to a concrete {@link ReferenceTarget} and
 * whichever produced a {@link ResourceProblem} instead.
 * <p>
 * A graph is built once per side by {@link ResourceIndex#index}; later tasks
 * read it to walk from a reference to its target without re-parsing or
 * re-resolving. Every edge is present in exactly one of "resolved" or
 * "problems" after indexing completes.
 */
public final class ReferenceGraph {

    private final ResourceSide side;
    private final List<ReferenceEdge> edges = new ArrayList<ReferenceEdge>();
    private final Map<ReferenceEdge, ReferenceTarget> resolved = new LinkedHashMap<ReferenceEdge, ReferenceTarget>();
    private final Map<ReferenceEdge, ResourceProblem> problems = new LinkedHashMap<ReferenceEdge, ResourceProblem>();

    public ReferenceGraph(ResourceSide side) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        this.side = side;
    }

    /** Which side this graph was built for. */
    public ResourceSide getSide() {
        return side;
    }

    /** Registers a discovered edge. Package-visible: only {@link ResourceIndex} builds a graph. */
    void addEdge(ReferenceEdge edge) {
        edges.add(edge);
    }

    /** Records that an edge resolved to a target. */
    void resolve(ReferenceEdge edge, ReferenceTarget target) {
        resolved.put(edge, target);
    }

    /** Records that an edge could not be resolved. */
    void unresolved(ReferenceEdge edge, ResourceProblem problem) {
        problems.put(edge, problem);
    }

    /** Every edge discovered while indexing, in discovery order. */
    public List<ReferenceEdge> getEdges() {
        return CollectionUtil.copyOfList(edges);
    }

    /** The resolved target for {@code edge}, or {@code null} if it did not resolve. */
    public ReferenceTarget getTarget(ReferenceEdge edge) {
        return resolved.get(edge);
    }

    /** The problem recorded for {@code edge}, or {@code null} if it resolved. */
    public ResourceProblem getProblem(ReferenceEdge edge) {
        return problems.get(edge);
    }

    /** All problems recorded while resolving this graph's edges, in discovery order. */
    public List<ResourceProblem> getProblems() {
        List<ResourceProblem> result = new ArrayList<ResourceProblem>();
        for (int i = 0; i < edges.size(); i++) {
            ResourceProblem problem = problems.get(edges.get(i));
            if (problem != null) {
                result.add(problem);
            }
        }
        return result;
    }
}
