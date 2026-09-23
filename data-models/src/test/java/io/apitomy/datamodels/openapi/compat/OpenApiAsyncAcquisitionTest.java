package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.openapi.compat.resource.AsyncResourceAcquirer;
import io.apitomy.datamodels.openapi.compat.resource.AsyncResourceLoader;
import io.apitomy.datamodels.openapi.compat.resource.ResourceDocument;
import io.apitomy.datamodels.openapi.compat.resource.ResourceRequest;
import io.apitomy.datamodels.openapi.compat.CheckCancellation;
import io.apitomy.datamodels.openapi.compat.CheckOptions;
import io.apitomy.datamodels.openapi.compat.CheckSession;
import io.apitomy.datamodels.openapi.compat.CompatibilityPolicy;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.ResourceSide;

/**
 * Exercises {@link AsyncResourceAcquirer} against fake, in-memory loaders:
 * nested relative resources, one load per side/URI, failures, cycles,
 * opposite-side isolation, immediate vs. delayed completions, a long
 * synchronous chain, resource/discovery budgets, and cancellation.
 */
class OpenApiAsyncAcquisitionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";
    private static final String UPDATED_URI = "urn:test:updated";

    private static CheckSession newSession(ObjectNode originalJson, ObjectNode updatedJson) {
        OpenApiDocument original = (OpenApiDocument) Library.readDocument(originalJson);
        OpenApiDocument updated = (OpenApiDocument) Library.readDocument(updatedJson);
        CheckOptions options = CheckOptions.defaults().withOriginalUri(ORIGINAL_URI).withUpdatedUri(UPDATED_URI);
        return CheckSession.snapshot(original, updated, options, CompatibilityPolicy.defaults());
    }

    private static ObjectNode emptyDocument(String title) {
        ObjectNode json = MAPPER.createObjectNode();
        json.put("openapi", "3.0.4");
        ObjectNode info = MAPPER.createObjectNode();
        info.put("title", title);
        info.put("version", "1");
        json.set("info", info);
        json.set("paths", MAPPER.createObjectNode());
        return json;
    }

    private static ObjectNode refDocument(String title, String ref) {
        ObjectNode json = emptyDocument(title);
        ObjectNode components = MAPPER.createObjectNode();
        ObjectNode schemas = MAPPER.createObjectNode();
        ObjectNode chained = MAPPER.createObjectNode();
        chained.put("$ref", ref);
        schemas.set("Chained", chained);
        components.set("schemas", schemas);
        json.set("components", components);
        return json;
    }

    private static CheckSession waitFor(CompletionStage<CheckSession> stage) throws Exception {
        try {
            return ((CompletableFuture<CheckSession>) stage).get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    /** An in-memory fake loader: each URI resolves to a fixed document, or fails, or hangs until triggered. */
    private static final class FakeLoader implements AsyncResourceLoader {
        private final Map<String, ObjectNode> documents = new HashMap<String, ObjectNode>();
        private final Map<String, String> failures = new HashMap<String, String>();
        private final Map<String, CompletableFuture<ResourceDocument>> pending = new LinkedHashMap<String, CompletableFuture<ResourceDocument>>();
        private final Map<String, AtomicInteger> loadCounts = new HashMap<String, AtomicInteger>();
        private boolean deferAll = false;

        void withDocument(String uri, ObjectNode json) {
            documents.put(uri, json);
        }

        void withFailure(String uri, String message) {
            failures.put(uri, message);
        }

        void deferAll() {
            deferAll = true;
        }

        void complete(String uri) {
            CompletableFuture<ResourceDocument> future = pending.get(uri);
            assertNotNull(future, "No pending load for " + uri);
            ObjectNode json = documents.get(uri);
            Document document = Library.readDocument(json);
            future.complete(new ResourceDocument(ResourceSide.ORIGINAL, uri, (RootCapable) document));
        }

        int loadCount(String uri) {
            AtomicInteger count = loadCounts.get(uri);
            return count == null ? 0 : count.get();
        }

        @Override
        public CompletionStage<ResourceDocument> load(ResourceRequest request) {
            String uri = request.getUri();
            loadCounts.computeIfAbsent(uri, key -> new AtomicInteger()).incrementAndGet();
            if (failures.containsKey(uri)) {
                CompletableFuture<ResourceDocument> failed = new CompletableFuture<ResourceDocument>();
                failed.completeExceptionally(new RuntimeException(failures.get(uri)));
                return failed;
            }
            if (deferAll) {
                CompletableFuture<ResourceDocument> future = new CompletableFuture<ResourceDocument>();
                pending.put(uri, future);
                return future;
            }
            ObjectNode json = documents.get(uri);
            if (json == null) {
                CompletableFuture<ResourceDocument> failed = new CompletableFuture<ResourceDocument>();
                failed.completeExceptionally(new RuntimeException("no fake document for " + uri));
                return failed;
            }
            Document document = Library.readDocument(json);
            return CompletableFuture.completedFuture(
                    new ResourceDocument(request.getSide(), uri, (RootCapable) document));
        }
    }

    @Test
    void resolvesNestedRelativeResourcesAcrossMultipleRounds() throws Exception {
        CheckSession session = newSession(refDocument("Root", "a.json#/components/schemas/Chained"), emptyDocument("Updated"));
        FakeLoader loader = new FakeLoader();
        loader.withDocument("a.json", refDocument("A", "b.json#/components/schemas/Chained"));
        loader.withDocument("b.json", emptyDocument("B"));

        CheckSession finished = waitFor(new AsyncResourceAcquirer().acquire(session, loader));

        assertTrue(finished.getResourceSet(ResourceSide.ORIGINAL).contains("a.json"));
        assertTrue(finished.getResourceSet(ResourceSide.ORIGINAL).contains("b.json"));
        assertEquals(1, loader.loadCount("a.json"));
        assertEquals(1, loader.loadCount("b.json"));
        assertEquals(0, finished.getResourceProblems().size());
    }

    @Test
    void loadsEachRequestAtMostOnce() throws Exception {
        // Two operations both reference the same external schema.
        ObjectNode root = emptyDocument("Root");
        ObjectNode components = MAPPER.createObjectNode();
        ObjectNode schemas = MAPPER.createObjectNode();
        ObjectNode first = MAPPER.createObjectNode();
        first.put("$ref", "shared.json#/components/schemas/Thing");
        ObjectNode second = MAPPER.createObjectNode();
        second.put("$ref", "shared.json#/components/schemas/Thing");
        schemas.set("First", first);
        schemas.set("Second", second);
        components.set("schemas", schemas);
        root.set("components", components);

        CheckSession session = newSession(root, emptyDocument("Updated"));
        FakeLoader loader = new FakeLoader();
        ObjectNode shared = emptyDocument("Shared");
        ObjectNode sharedComponents = MAPPER.createObjectNode();
        ObjectNode sharedSchemas = MAPPER.createObjectNode();
        sharedSchemas.set("Thing", MAPPER.createObjectNode());
        sharedComponents.set("schemas", sharedSchemas);
        shared.set("components", sharedComponents);
        loader.withDocument("shared.json", shared);

        waitFor(new AsyncResourceAcquirer().acquire(session, loader));

        assertEquals(1, loader.loadCount("shared.json"));
    }

    @Test
    void recordsAFailureAsAProblemWithoutFailingTheWholeAcquisition() throws Exception {
        CheckSession session = newSession(refDocument("Root", "missing.json#/components/schemas/Chained"), emptyDocument("Updated"));
        FakeLoader loader = new FakeLoader();
        loader.withFailure("missing.json", "network error");

        CheckSession finished = waitFor(new AsyncResourceAcquirer().acquire(session, loader));

        assertEquals(1, finished.getResourceProblems().size());
        assertEquals(FindingCode.RESOURCE_UNRESOLVED, finished.getResourceProblems().get(0).getCode());
        assertFalse(finished.getResourceSet(ResourceSide.ORIGINAL).contains("missing.json"));
    }

    @Test
    void cyclicExternalReferencesTerminate() throws Exception {
        CheckSession session = newSession(refDocument("Root", "a.json#/components/schemas/Chained"), emptyDocument("Updated"));
        FakeLoader loader = new FakeLoader();
        loader.withDocument("a.json", refDocument("A", ORIGINAL_URI + "#/components/schemas/Chained"));

        CheckSession finished = waitFor(new AsyncResourceAcquirer().acquire(session, loader));

        assertTrue(finished.getResourceSet(ResourceSide.ORIGINAL).contains("a.json"));
        assertEquals(1, loader.loadCount("a.json"));
    }

    @Test
    void aReferenceOnOneSideDoesNotAcquireAnythingForTheOtherSide() throws Exception {
        CheckSession session = newSession(
                refDocument("Root", "a.json#/components/schemas/Chained"),
                emptyDocument("Updated"));
        FakeLoader loader = new FakeLoader();
        loader.withDocument("a.json", emptyDocument("A"));

        CheckSession finished = waitFor(new AsyncResourceAcquirer().acquire(session, loader));

        assertTrue(finished.getResourceSet(ResourceSide.ORIGINAL).contains("a.json"));
        assertFalse(finished.getResourceSet(ResourceSide.UPDATED).contains("a.json"));
    }

    @Test
    void aLongSynchronousChainDoesNotOverflowTheStack() throws Exception {
        int depth = 1000;
        OpenApiDocument original = (OpenApiDocument) Library.readDocument(refDocument("Root", "r0.json#/components/schemas/Chained"));
        OpenApiDocument updated = (OpenApiDocument) Library.readDocument(emptyDocument("Updated"));
        // Each resource in this chain is only discoverable one hop at a time (its
        // referrer must be indexed before the next URI is known), so this
        // deliberately deep fixture needs a round budget to match -- unrelated to
        // the stack-safety property under test, which is that any number of
        // synchronously-completing rounds run in a loop, not as nested recursion.
        CheckOptions options = CheckOptions.defaults()
                .withOriginalUri(ORIGINAL_URI)
                .withUpdatedUri(UPDATED_URI)
                .withMaxDiscoveryRounds(depth + 10)
                .withMaxResourceCount(depth + 10);
        CheckSession session = CheckSession.snapshot(original, updated, options, CompatibilityPolicy.defaults());
        FakeLoader loader = new FakeLoader();
        for (int i = 0; i < depth; i++) {
            String uri = "r" + i + ".json";
            String nextRef = i == depth - 1 ? null : ("r" + (i + 1) + ".json#/components/schemas/Chained");
            loader.withDocument(uri, nextRef == null ? emptyDocument("Leaf") : refDocument("Node " + i, nextRef));
        }

        CheckSession finished = waitFor(new AsyncResourceAcquirer().acquire(session, loader));

        assertTrue(finished.getResourceSet(ResourceSide.ORIGINAL).contains("r0.json"));
        assertTrue(finished.getResourceSet(ResourceSide.ORIGINAL).contains("r" + (depth - 1) + ".json"));
    }

    @Test
    void delayedLoaderCompletionsStillResolve() throws Exception {
        CheckSession session = newSession(refDocument("Root", "a.json#/components/schemas/Chained"), emptyDocument("Updated"));
        FakeLoader loader = new FakeLoader();
        loader.withDocument("a.json", emptyDocument("A"));
        loader.deferAll();

        CompletionStage<CheckSession> stage = new AsyncResourceAcquirer().acquire(session, loader);
        // Not yet resolved: the fake loader is holding its future open.
        loader.complete("a.json");
        CheckSession finished = waitFor(stage);

        assertTrue(finished.getResourceSet(ResourceSide.ORIGINAL).contains("a.json"));
    }

    @Test
    void resourceCountBudgetStopsAcquisitionAndRecordsTheLimit() throws Exception {
        ObjectNode root = emptyDocument("Root");
        ObjectNode components = MAPPER.createObjectNode();
        ObjectNode schemas = MAPPER.createObjectNode();
        for (int i = 0; i < 5; i++) {
            ObjectNode ref = MAPPER.createObjectNode();
            ref.put("$ref", "r" + i + ".json#/components/schemas/Chained");
            schemas.set("S" + i, ref);
        }
        components.set("schemas", schemas);
        root.set("components", components);

        OpenApiDocument original = (OpenApiDocument) Library.readDocument(root);
        OpenApiDocument updated = (OpenApiDocument) Library.readDocument(emptyDocument("Updated"));
        CheckOptions options = CheckOptions.defaults()
                .withOriginalUri(ORIGINAL_URI)
                .withUpdatedUri(UPDATED_URI)
                .withMaxResourceCount(2);
        CheckSession session = CheckSession.snapshot(original, updated, options, CompatibilityPolicy.defaults());

        FakeLoader loader = new FakeLoader();
        for (int i = 0; i < 5; i++) {
            loader.withDocument("r" + i + ".json", emptyDocument("R" + i));
        }

        CheckSession finished = waitFor(new AsyncResourceAcquirer().acquire(session, loader));

        int acquiredCount = finished.getResourceSet(ResourceSide.ORIGINAL).getDocuments().size() - 1; // minus the root itself
        assertEquals(2, acquiredCount);
        boolean sawLimitReached = false;
        for (int i = 0; i < finished.getResourceProblems().size(); i++) {
            if (finished.getResourceProblems().get(i).getCode() == FindingCode.ANALYSIS_LIMIT_REACHED) {
                sawLimitReached = true;
            }
        }
        assertTrue(sawLimitReached, "Expected at least one ANALYSIS_LIMIT_REACHED problem");
    }

    @Test
    void cancellationCompletesExceptionallyAndDoesNotWaitForANonCompletingLoader() throws Exception {
        CheckSession session = newSession(refDocument("Root", "a.json#/components/schemas/Chained"), emptyDocument("Updated"));
        FakeLoader loader = new FakeLoader();
        loader.withDocument("a.json", emptyDocument("A"));
        loader.deferAll();

        CheckCancellation cancellation = session.getOptions().getCancellation();
        // The session was already snapshotted without a cancellation signal; build a
        // fresh session that has one, since CheckOptions is immutable.
        CheckOptions options = session.getOptions().withCancellation(new CheckCancellation());
        CheckSession cancellableSession = CheckSession.snapshot(session.getOriginal(), session.getUpdated(), options,
                session.getPolicy());

        CompletionStage<CheckSession> stage = new AsyncResourceAcquirer().acquire(cancellableSession, loader);
        options.getCancellation().cancel();

        try {
            waitFor(stage);
            fail("Expected cancellation to fail the acquisition");
        } catch (CancellationException expected) {
            // expected: cancellation must not wait for the deferred, never-completing load.
        } catch (TimeoutException e) {
            fail("Cancellation must not wait for a non-completing loader");
        }
    }
}
