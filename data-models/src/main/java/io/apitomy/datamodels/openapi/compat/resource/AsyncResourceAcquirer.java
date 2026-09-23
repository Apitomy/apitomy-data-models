package io.apitomy.datamodels.openapi.compat.resource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.apitomy.datamodels.openapi.compat.CheckCancellation;
import io.apitomy.datamodels.openapi.compat.CheckOptions;
import io.apitomy.datamodels.openapi.compat.CheckSession;
import io.apitomy.datamodels.openapi.compat.FindingCode;

/**
 * Drives {@link ResourceDiscovery} and an {@link AsyncResourceLoader} to
 * completion: repeatedly discovers unattempted resource requests, loads all of
 * them concurrently, records what came back (or failed) on the session, and
 * discovers again — since a newly acquired document can itself reveal further
 * external references — until a round finds nothing new to attempt.
 * <p>
 * This is the Java-native async boundary; see {@link AsyncResourceLoader} for
 * why it is excluded from JSweet transpilation. Only orchestration lives here:
 * which references are reachable and how failures are recorded both come from
 * the shared, synchronous {@link ResourceDiscovery} and {@link CheckSession}
 * used identically by the curated TypeScript acquirer.
 * <p>
 * <b>Stack safety:</b> when a round's loads all complete synchronously (as a
 * fake loader in a test, or a loader backed by an in-memory cache, might),
 * the next round runs in the same iteration of an explicit loop rather than
 * as a nested recursive call — a long synchronous chain (thousands of
 * resources deep) does not grow the call stack. Only a round that is still
 * pending when checked schedules an asynchronous continuation.
 */
public final class AsyncResourceAcquirer {

    /**
     * Acquires every resource reachable from {@code session}'s documents,
     * using {@code loader} to load each one, until nothing new is reachable,
     * a configured budget is exhausted, or the session's cancellation signal
     * (if any) fires.
     *
     * @param session the session to acquire resources into; its resource sets
     *                and resource-problem log are mutated as loads complete
     * @param loader  loads a single resource, asynchronously
     * @return a stage that completes with {@code session} once acquisition is
     *         finished, or completes exceptionally with a
     *         {@link CancellationException} if the session's cancellation
     *         signal fired first
     */
    public CompletionStage<CheckSession> acquire(CheckSession session, AsyncResourceLoader loader) {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null");
        }

        CompletableFuture<CheckSession> result = new CompletableFuture<CheckSession>();
        CheckCancellation cancellation = session.getOptions().getCancellation();
        Runnable[] unregisterHolder = new Runnable[1];
        if (cancellation != null) {
            unregisterHolder[0] = cancellation.onCancel(new Runnable() {
                @Override
                public void run() {
                    result.completeExceptionally(new CancellationException("Compatibility check was cancelled"));
                }
            });
            result.whenComplete(new java.util.function.BiConsumer<CheckSession, Throwable>() {
                @Override
                public void accept(CheckSession finished, Throwable error) {
                    unregisterHolder[0].run();
                }
            });
        }

        Set<ResourceRequest> attempted = new LinkedHashSet<ResourceRequest>();
        AcquisitionState state = new AcquisitionState(session, loader, attempted, cancellation, result);
        runLoop(state);
        return result;
    }

    /**
     * Runs discovery/load rounds in a plain loop for as long as each round
     * completes synchronously, only falling back to a scheduled continuation
     * once a round is still pending. See the class Javadoc's "Stack safety"
     * note: this is what keeps a long synchronous chain from recursing.
     */
    private void runLoop(AcquisitionState state) {
        while (true) {
            if (state.cancellation != null && state.cancellation.isCancelled()) {
                state.result.completeExceptionally(new CancellationException("Compatibility check was cancelled"));
                return;
            }

            CheckOptions options = state.session.getOptions();
            if (state.rounds >= options.getMaxDiscoveryRounds()) {
                recordLimitReachedForRemaining(state);
                state.result.complete(state.session);
                return;
            }

            List<ResourceRequest> unattempted = unattemptedRequests(state);
            if (unattempted.isEmpty()) {
                state.result.complete(state.session);
                return;
            }
            state.rounds++;

            List<ResourceRequest> toLoad = new ArrayList<ResourceRequest>();
            for (int i = 0; i < unattempted.size(); i++) {
                ResourceRequest request = unattempted.get(i);
                if (state.attempted.size() >= options.getMaxResourceCount()) {
                    recordLimitReached(state, request);
                    continue;
                }
                state.attempted.add(request);
                toLoad.add(request);
            }

            if (toLoad.isEmpty()) {
                // Every remaining request in this round hit the resource-count budget;
                // nothing left to attempt.
                state.result.complete(state.session);
                return;
            }

            List<CompletableFuture<Void>> batch = new ArrayList<CompletableFuture<Void>>();
            for (int i = 0; i < toLoad.size(); i++) {
                batch.add(loadOne(state, toLoad.get(i)));
            }

            boolean allDone = true;
            for (int i = 0; i < batch.size(); i++) {
                if (!batch.get(i).isDone()) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                continue;
            }

            CompletableFuture<?>[] batchArray = new CompletableFuture<?>[batch.size()];
            for (int i = 0; i < batch.size(); i++) {
                batchArray[i] = batch.get(i);
            }
            CompletableFuture.allOf(batchArray).whenComplete(new java.util.function.BiConsumer<Void, Throwable>() {
                @Override
                public void accept(Void ignoredValue, Throwable ignoredError) {
                    runLoop(state);
                }
            });
            return;
        }
    }

    private List<ResourceRequest> unattemptedRequests(AcquisitionState state) {
        List<ResourceRequest> discovered = ResourceDiscovery.discover(state.session);
        List<ResourceRequest> result = new ArrayList<ResourceRequest>();
        for (int i = 0; i < discovered.size(); i++) {
            ResourceRequest request = discovered.get(i);
            if (!state.attempted.contains(request)) {
                result.add(request);
            }
        }
        return result;
    }

    private void recordLimitReached(AcquisitionState state, ResourceRequest request) {
        state.session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                FindingCode.ANALYSIS_LIMIT_REACHED,
                "Resource-count budget (" + state.session.getOptions().getMaxResourceCount()
                        + ") was reached before this resource could be acquired"));
    }

    private void recordLimitReachedForRemaining(AcquisitionState state) {
        List<ResourceRequest> remaining = unattemptedRequests(state);
        for (int i = 0; i < remaining.size(); i++) {
            ResourceRequest request = remaining.get(i);
            state.session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                    FindingCode.ANALYSIS_LIMIT_REACHED,
                    "Discovery-round budget (" + state.session.getOptions().getMaxDiscoveryRounds()
                            + ") was reached before this resource could be acquired"));
        }
    }

    /**
     * Loads one request, recording either the acquired resource or a problem
     * on the session, and completes the returned future either way — a
     * per-resource loader failure never fails the overall acquisition.
     */
    private CompletableFuture<Void> loadOne(AcquisitionState state, ResourceRequest request) {
        CompletableFuture<Void> done = new CompletableFuture<Void>();
        CompletionStage<ResourceDocument> stage;
        try {
            stage = state.loader.load(request);
        } catch (RuntimeException e) {
            state.session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                    FindingCode.RESOURCE_UNRESOLVED, "Loader threw while starting the load: " + e.getMessage()));
            done.complete(null);
            return done;
        }
        if (stage == null) {
            state.session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                    FindingCode.RESOURCE_UNRESOLVED, "Loader returned a null stage"));
            done.complete(null);
            return done;
        }
        stage.whenComplete(new java.util.function.BiConsumer<ResourceDocument, Throwable>() {
            @Override
            public void accept(ResourceDocument document, Throwable error) {
                if (error != null) {
                    state.session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                            FindingCode.RESOURCE_UNRESOLVED, "Failed to acquire resource: " + error.getMessage()));
                } else if (document == null) {
                    state.session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                            FindingCode.RESOURCE_UNRESOLVED, "Loader completed without a document"));
                } else {
                    state.session.addResource(request.getSide(), document);
                }
                done.complete(null);
            }
        });
        return done;
    }

    /** Mutable state threaded through one {@link #acquire} call. */
    private static final class AcquisitionState {
        private final CheckSession session;
        private final AsyncResourceLoader loader;
        private final Set<ResourceRequest> attempted;
        private final CheckCancellation cancellation;
        private final CompletableFuture<CheckSession> result;
        private int rounds = 0;

        AcquisitionState(CheckSession session, AsyncResourceLoader loader, Set<ResourceRequest> attempted,
                CheckCancellation cancellation, CompletableFuture<CheckSession> result) {
            this.session = session;
            this.loader = loader;
            this.attempted = attempted;
            this.cancellation = cancellation;
            this.result = result;
        }
    }
}
