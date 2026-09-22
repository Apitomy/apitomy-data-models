package io.apitomy.datamodels.openapi.compat.resource;

import java.util.List;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.jsonschema.ref.JsonPointer;
import io.apitomy.datamodels.jsonschema.ref.JsonRef;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.visitors.AllNodeVisitor;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.ResourceSide;
import io.apitomy.datamodels.refs.ReferenceUtil;

/**
 * Builds a {@link ReferenceGraph} for one document: discovers every
 * {@code $ref}-bearing node, classifies it, and resolves it against the
 * document itself (for internal references) or against a {@link ResourceSet}
 * (for external references).
 * <p>
 * Indexing is a single pass: complete-document discovery happens first (every
 * edge in the document is registered), and each edge is then resolved once.
 * References are not chased transitively while resolving one edge — a target
 * that is itself a {@code $ref} is registered separately at its own document
 * position when that document is indexed. Chasing multi-hop chains is a
 * concern for later analysis over the graph, not for indexing, and keeping
 * indexing single-hop is what keeps it safe against cyclic static references
 * without extra bookkeeping.
 */
public final class ResourceIndex {

    private ResourceIndex() {
    }

    /**
     * Indexes {@code resource}, producing a graph of every reference it
     * contains, each resolved against {@code resourceSet} (which must contain
     * {@code resource} itself for internal references to resolve).
     */
    public static ReferenceGraph index(ResourceDocument resource, ResourceSet resourceSet) {
        if (resource == null) {
            throw new IllegalArgumentException("resource must not be null");
        }
        if (resourceSet == null) {
            throw new IllegalArgumentException("resourceSet must not be null");
        }
        ReferenceGraph graph = new ReferenceGraph(resource.getSide());
        ReferenceCollectingVisitor collector = new ReferenceCollectingVisitor(resource, graph);
        Library.visitTree((Node) resource.getRoot(), collector, TraverserDirection.down);

        List<ReferenceEdge> edges = graph.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            ReferenceEdge edge = edges.get(i);
            Resolution resolution = resolve(edge, resourceSet);
            if (resolution.isResolved()) {
                graph.resolve(edge, resolution.getTarget());
            } else {
                graph.unresolved(edge, resolution.getProblem());
            }
        }
        return graph;
    }

    /**
     * Resolves a single edge, returning either the target it reaches or a
     * localized problem. Does not mutate {@code edge}'s graph; callers that
     * want the result recorded use {@link #index}.
     */
    public static Resolution resolve(ReferenceEdge edge, ResourceSet resourceSet) {
        JsonRef parsed = JsonRef.parse(edge.getRawReference());
        if (parsed.isInternal()) {
            return resolveInternal(edge, parsed, resourceSet);
        }
        return resolveExternal(edge, parsed, resourceSet);
    }

    private static Resolution resolveInternal(ReferenceEdge edge, JsonRef parsed, ResourceSet resourceSet) {
        ResourceDocument owner = resourceSet.get(edge.getOwnerUri());
        if (owner == null) {
            return Resolution.problem(new ResourceProblem(edge.getSide(), edge.getOwnerUri(), FindingCode.RESOURCE_UNRESOLVED,
                    "Owning document '" + edge.getOwnerUri() + "' is not present in the resource set"));
        }
        if (parsed.isRoot()) {
            return Resolution.target(new ReferenceTarget(owner, (Node) owner.getRoot(), null));
        }
        if (parsed.isPointer()) {
            Node resolvedNode = parsed.pointer().evaluate((Node) owner.getRoot());
            if (resolvedNode == null) {
                return Resolution.problem(new ResourceProblem(edge.getSide(), edge.getOwnerUri(), FindingCode.RESOURCE_UNRESOLVED,
                        "Could not resolve internal reference '" + edge.getRawReference() + "' within " + edge.getOwnerUri()));
            }
            return Resolution.target(new ReferenceTarget(owner, resolvedNode, parsed.fragment()));
        }
        // Anchor-based internal references ($anchor, or a plain-name $id used as an
        // anchor) are not indexed by JsonPointer; fall back to the legacy resolver,
        // which understands document-wide anchor registration.
        Node resolvedNode = ReferenceUtil.resolveRef(edge.getRawReference(), edge.getSource());
        if (resolvedNode == null) {
            return Resolution.problem(new ResourceProblem(edge.getSide(), edge.getOwnerUri(), FindingCode.RESOURCE_UNRESOLVED,
                    "Could not resolve internal reference '" + edge.getRawReference() + "' within " + edge.getOwnerUri()));
        }
        return Resolution.target(new ReferenceTarget(owner, resolvedNode, parsed.fragment()));
    }

    private static Resolution resolveExternal(ReferenceEdge edge, JsonRef parsed, ResourceSet resourceSet) {
        String externalUri = resolveExternalUri(edge.getOwnerUri(), parsed.resource());
        ResourceDocument externalDocument = resourceSet.get(externalUri);
        if (externalDocument == null) {
            return Resolution.problem(new ResourceProblem(edge.getSide(), externalUri, FindingCode.RESOURCE_UNRESOLVED,
                    "External resource '" + externalUri + "' referenced from " + edge.getOwnerUri() + " was not acquired"));
        }
        if (parsed.fragment() == null || parsed.fragment().length() == 0) {
            return Resolution.target(new ReferenceTarget(externalDocument, (Node) externalDocument.getRoot(), null));
        }
        if (parsed.isPointer()) {
            Node resolvedNode = parsed.pointer().evaluate((Node) externalDocument.getRoot());
            if (resolvedNode == null) {
                return Resolution.problem(new ResourceProblem(edge.getSide(), externalUri, FindingCode.RESOURCE_INVALID,
                        "Fragment '" + parsed.fragment() + "' did not resolve within " + externalUri));
            }
            return Resolution.target(new ReferenceTarget(externalDocument, resolvedNode, parsed.fragment()));
        }
        // Anchor-based external fragments ($anchor / plain-name $id) are not yet
        // resolved here; report as unresolved rather than silently misreporting a
        // wrong target.
        return Resolution.problem(new ResourceProblem(edge.getSide(), externalUri, FindingCode.RESOURCE_UNRESOLVED,
                "Anchor-based fragment '" + parsed.fragment() + "' in " + externalUri + " is not yet resolved"));
    }

    /**
     * Resolves the resource part of a reference against the URI of the document
     * that contains it. A resource part that already has a scheme (an absolute
     * URI) is used as-is; otherwise it is resolved as a relative reference
     * against {@code ownerUri}, per RFC 3986.
     */
    static String resolveExternalUri(String ownerUri, String resourcePart) {
        if (resourcePart == null || resourcePart.length() == 0) {
            return ownerUri;
        }
        if (hasScheme(resourcePart)) {
            return resourcePart;
        }
        String base = stripFragment(ownerUri);
        if (resourcePart.charAt(0) == '/') {
            String schemeAndAuthority = schemeAndAuthorityOf(base);
            if (schemeAndAuthority != null) {
                return schemeAndAuthority + resourcePart;
            }
            return resourcePart;
        }
        int lastSlash = base.lastIndexOf('/');
        if (lastSlash < 0) {
            return resourcePart;
        }
        return base.substring(0, lastSlash + 1) + resourcePart;
    }

    /** True if {@code uri} begins with an RFC 3986 scheme (e.g. {@code https:}). */
    private static boolean hasScheme(String uri) {
        int colon = uri.indexOf(':');
        if (colon <= 0) {
            return false;
        }
        for (int i = 0; i < colon; i++) {
            char c = uri.charAt(i);
            boolean letter = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
            if (i == 0) {
                if (!letter) {
                    return false;
                }
                continue;
            }
            boolean digitOrPunct = (c >= '0' && c <= '9') || c == '+' || c == '-' || c == '.';
            if (!letter && !digitOrPunct) {
                return false;
            }
        }
        return true;
    }

    private static String stripFragment(String uri) {
        int hash = uri.indexOf('#');
        if (hash < 0) {
            return uri;
        }
        return uri.substring(0, hash);
    }

    /** The {@code scheme://authority} prefix of {@code uri}, or {@code null} if it has none. */
    private static String schemeAndAuthorityOf(String uri) {
        int schemeEnd = uri.indexOf("://");
        if (schemeEnd < 0) {
            return null;
        }
        int pathStart = uri.indexOf('/', schemeEnd + 3);
        if (pathStart < 0) {
            return uri;
        }
        return uri.substring(0, pathStart);
    }

    /** The outcome of resolving one {@link ReferenceEdge}: exactly one of a target or a problem. */
    public static final class Resolution {
        private final ReferenceTarget target;
        private final ResourceProblem problem;

        private Resolution(ReferenceTarget target, ResourceProblem problem) {
            this.target = target;
            this.problem = problem;
        }

        public static Resolution target(ReferenceTarget target) {
            return new Resolution(target, null);
        }

        public static Resolution problem(ResourceProblem problem) {
            return new Resolution(null, problem);
        }

        public boolean isResolved() {
            return target != null;
        }

        public ReferenceTarget getTarget() {
            return target;
        }

        public ResourceProblem getProblem() {
            return problem;
        }
    }

    /**
     * Walks every node in a document, registering an edge for each node that
     * carries a non-empty {@code $ref}.
     */
    private static final class ReferenceCollectingVisitor extends AllNodeVisitor {

        private final ResourceDocument resource;
        private final ReferenceGraph graph;

        ReferenceCollectingVisitor(ResourceDocument resource, ReferenceGraph graph) {
            this.resource = resource;
            this.graph = graph;
        }

        @Override
        protected void visitNode(Node node) {
            if (!(node instanceof Referenceable)) {
                return;
            }
            Referenceable referenceable = (Referenceable) node;
            String rawReference = referenceable.get$ref();
            if (rawReference == null || rawReference.length() == 0) {
                return;
            }
            ReferenceKind kind = classify(node);
            graph.addEdge(new ReferenceEdge(resource.getSide(), resource.getUri(), node, rawReference, kind));
        }

        private ReferenceKind classify(Node node) {
            if (node instanceof Schema) {
                return ReferenceKind.SCHEMA_REFERENCE;
            }
            if (node instanceof OpenApiPathItem) {
                return ReferenceKind.PATH_ITEM_REFERENCE;
            }
            return ReferenceKind.REFERENCE_OBJECT;
        }
    }
}
