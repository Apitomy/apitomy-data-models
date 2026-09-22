package io.apitomy.datamodels.openapi.compat.resource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.apitomy.datamodels.jsonschema.ref.JsonRef;
import io.apitomy.datamodels.openapi.compat.CheckSession;
import io.apitomy.datamodels.openapi.compat.ResourceSide;

/**
 * Determines which external resources a check still needs, given the
 * documents already in a {@link CheckSession}.
 * <p>
 * Discovery indexes every document currently in the session's resource sets,
 * collects every external reference edge, and reports a {@link ResourceRequest}
 * for each external URI not already present on the same side. It does not
 * mutate the session; a caller (a later, async-aware task) acquires the
 * returned requests and calls {@link CheckSession#addResource} or
 * {@link CheckSession#addResourceProblem} before discovering again to find
 * further, newly-reachable external resources.
 */
public final class ResourceDiscovery {

    private ResourceDiscovery() {
    }

    /**
     * Returns every external resource referenced from a document currently in
     * {@code session}, on either side, that is not already present in that
     * side's resource set. The same URI missing from both sides is reported
     * once per side, since the two sides' resource sets are independent.
     */
    public static List<ResourceRequest> discover(CheckSession session) {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }
        List<ResourceRequest> requests = new ArrayList<ResourceRequest>();
        discoverForSide(session, ResourceSide.ORIGINAL, requests);
        discoverForSide(session, ResourceSide.UPDATED, requests);
        return requests;
    }

    private static void discoverForSide(CheckSession session, ResourceSide side, List<ResourceRequest> requests) {
        ResourceSet resourceSet = session.getResourceSet(side);
        Set<String> seen = new LinkedHashSet<String>();
        List<ResourceDocument> documents = resourceSet.getDocuments();
        for (int i = 0; i < documents.size(); i++) {
            ResourceDocument document = documents.get(i);
            ReferenceGraph graph = ResourceIndex.index(document, resourceSet);
            List<ReferenceEdge> edges = graph.getEdges();
            for (int j = 0; j < edges.size(); j++) {
                ReferenceEdge edge = edges.get(j);
                JsonRef parsed = JsonRef.parse(edge.getRawReference());
                if (!parsed.isExternal()) {
                    continue;
                }
                String externalUri = ResourceIndex.resolveExternalUri(document.getUri(), parsed.resource());
                if (resourceSet.contains(externalUri) || seen.contains(externalUri)) {
                    continue;
                }
                seen.add(externalUri);
                requests.add(new ResourceRequest(side, externalUri));
            }
        }
    }
}
