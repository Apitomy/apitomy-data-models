package io.apitomy.datamodels.openapi.compat;

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.openapi.compat.resource.ReferenceGraph;
import io.apitomy.datamodels.openapi.compat.resource.ResourceDocument;
import io.apitomy.datamodels.openapi.compat.resource.ResourceIndex;
import io.apitomy.datamodels.openapi.compat.resource.ResourceProblem;
import io.apitomy.datamodels.openapi.compat.resource.ResourceRequest;
import io.apitomy.datamodels.openapi.compat.resource.ResourceSet;
import io.apitomy.datamodels.util.CollectionUtil;

/**
 * The mutable, per-invocation state for a single compatibility check: the two
 * documents under comparison, the options and policy in effect, each side's
 * resource set as it grows with acquired external resources, and any
 * resolution problems recorded along the way.
 * <p>
 * A session is created once per check via {@link #snapshot} and then threaded
 * through resource discovery, contract interpretation, and rule evaluation.
 * It is not itself a public result type; {@code OpenApiCompatibilityChecker}
 * reads a session to produce a {@link CompatibilityResult}.
 * <p>
 * Node-identity caches (visited sets, resolved-node maps) built while working
 * with a session must be scoped to that session and released with it — never
 * placed on a reusable, long-lived instance and assumed to still reflect the
 * current invocation.
 */
public final class CheckSession {

    private final OpenApiDocument original;
    private final OpenApiDocument updated;
    private final CheckOptions options;
    private final CompatibilityPolicy policy;
    private final ResourceSet originalResources;
    private final ResourceSet updatedResources;
    private final List<ResourceProblemEntry> resourceProblems = new ArrayList<ResourceProblemEntry>();

    private CheckSession(OpenApiDocument original, OpenApiDocument updated, CheckOptions options,
            CompatibilityPolicy policy, ResourceSet originalResources, ResourceSet updatedResources) {
        this.original = original;
        this.updated = updated;
        this.options = options;
        this.policy = policy;
        this.originalResources = originalResources;
        this.updatedResources = updatedResources;
    }

    /**
     * Creates a new session for comparing {@code original} against
     * {@code updated}. Both documents are deep-cloned before being placed in
     * the session's resource sets, so nothing done with the session — reading
     * or, in later tasks, dereferencing — modifies the parent links or
     * attributes on the models the caller supplied.
     * <p>
     * Each side's resource set starts as a copy of {@code options}' pre-acquired
     * resources for that side, plus the cloned document itself registered at
     * its side's retrieval URI ({@link CheckOptions#getOriginalUri()} /
     * {@link CheckOptions#getUpdatedUri()}).
     */
    public static CheckSession snapshot(OpenApiDocument original, OpenApiDocument updated, CheckOptions options,
            CompatibilityPolicy policy) {
        if (original == null) {
            throw new IllegalArgumentException("original must not be null");
        }
        if (updated == null) {
            throw new IllegalArgumentException("updated must not be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }

        OpenApiDocument clonedOriginal = (OpenApiDocument) Library.cloneDocument((Document) original);
        OpenApiDocument clonedUpdated = (OpenApiDocument) Library.cloneDocument((Document) updated);

        ResourceSet originalResources = copyResourceSet(options.getOriginalResources(), ResourceSide.ORIGINAL);
        ResourceSet updatedResources = copyResourceSet(options.getUpdatedResources(), ResourceSide.UPDATED);
        originalResources.add(new ResourceDocument(ResourceSide.ORIGINAL, options.getOriginalUri(), (RootCapable) clonedOriginal));
        updatedResources.add(new ResourceDocument(ResourceSide.UPDATED, options.getUpdatedUri(), (RootCapable) clonedUpdated));

        return new CheckSession(clonedOriginal, clonedUpdated, options, policy, originalResources, updatedResources);
    }

    private static ResourceSet copyResourceSet(ResourceSet source, ResourceSide side) {
        ResourceSet copy = new ResourceSet(side);
        List<ResourceDocument> documents = source.getDocuments();
        for (int i = 0; i < documents.size(); i++) {
            copy.add(documents.get(i));
        }
        return copy;
    }

    /** The (cloned) original document under comparison. */
    public OpenApiDocument getOriginal() {
        return original;
    }

    /** The (cloned) updated document under comparison. */
    public OpenApiDocument getUpdated() {
        return updated;
    }

    /** The options this session was created with. */
    public CheckOptions getOptions() {
        return options;
    }

    /** The policy in effect for this session. */
    public CompatibilityPolicy getPolicy() {
        return policy;
    }

    /** The resource set for {@code side}. */
    public ResourceSet getResourceSet(ResourceSide side) {
        if (side == ResourceSide.ORIGINAL) {
            return originalResources;
        }
        return updatedResources;
    }

    /**
     * Registers a newly acquired resource on {@code side}. The document's own
     * {@link ResourceDocument#getSide()} must match {@code side}.
     */
    public void addResource(ResourceSide side, ResourceDocument document) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        getResourceSet(side).add(document);
    }

    /**
     * Records that {@code request} could not be satisfied, attaching
     * {@code problem} as the reason. Recorded problems are available via
     * {@link #getResourceProblems()} for translation into findings by later
     * tasks; they do not themselves affect any resource set.
     */
    public void addResourceProblem(ResourceRequest request, ResourceProblem problem) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (problem == null) {
            throw new IllegalArgumentException("problem must not be null");
        }
        resourceProblems.add(new ResourceProblemEntry(request, problem));
    }

    /** Every resource problem recorded so far, in recording order. */
    public List<ResourceProblem> getResourceProblems() {
        List<ResourceProblem> result = new ArrayList<ResourceProblem>();
        for (int i = 0; i < resourceProblems.size(); i++) {
            result.add(resourceProblems.get(i).problem);
        }
        return CollectionUtil.copyOfList(result);
    }

    /**
     * Indexes the document at {@code side}'s own retrieval URI (as registered
     * by {@link #snapshot}) against that side's current resource set,
     * producing a fresh {@link ReferenceGraph}. Resource acquisition between
     * calls (via {@link #addResource}) is reflected in the next call's
     * resolutions; a graph returned by an earlier call is not retroactively
     * updated.
     */
    public ReferenceGraph indexRootDocument(ResourceSide side) {
        if (side == null) {
            throw new IllegalArgumentException("side must not be null");
        }
        String rootUri = side == ResourceSide.ORIGINAL ? options.getOriginalUri() : options.getUpdatedUri();
        ResourceSet resourceSet = getResourceSet(side);
        ResourceDocument rootDocument = resourceSet.get(rootUri);
        return ResourceIndex.index(rootDocument, resourceSet);
    }

    private static final class ResourceProblemEntry {
        private final ResourceRequest request;
        private final ResourceProblem problem;

        ResourceProblemEntry(ResourceRequest request, ResourceProblem problem) {
            this.request = request;
            this.problem = problem;
        }
    }
}
