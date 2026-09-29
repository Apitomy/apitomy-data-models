package io.apitomy.datamodels.openapi.compat;

import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.openapi.compat.resource.AsyncResourceAcquirer;
import io.apitomy.datamodels.openapi.compat.resource.AsyncResourceLoader;

/**
 * The public, asynchronous entry point for OpenAPI compatibility checking:
 * identical to {@link OpenApiCompatibilityChecker}'s snapshot -&gt; interpret
 * -&gt; match -&gt; rules -&gt; aggregate pipeline, but resolves external
 * resources reachable from either document first, using a caller-supplied
 * {@link AsyncResourceLoader} (which receives requests for both sides,
 * distinguished by {@link io.apitomy.datamodels.openapi.compat.resource.ResourceRequest#getSide()}),
 * via the shared {@link AsyncResourceAcquirer} built in an earlier task.
 * <p>
 * A document with no external references to resolve never invokes the
 * loader at all -- acquisition discovers nothing to do and the check
 * proceeds immediately, identically to the synchronous checker.
 * <p>
 * <b>Known integration gap:</b> resources acquired this way are recorded on
 * the check session and available for {@link io.apitomy.datamodels.openapi.compat.resource.ResourceProblem}
 * reporting, but are not yet consulted by schema containment or contract
 * interpretation for an external {@code $ref} target's actual content --
 * see {@code SchemaContainment}'s own current limitation (schema {@code $ref}
 * is not dereferenced at all yet, internal or external). Acquisition
 * therefore does not yet change a schema comparison's outcome; closing that
 * gap is future work, not claimed complete here.
 */
public final class OpenApiAsyncCompatibilityChecker {

    private final CompatibilityPolicy policy;

    OpenApiAsyncCompatibilityChecker(CompatibilityPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        this.policy = policy;
    }

    public static OpenApiAsyncCompatibilityCheckerBuilder builder() {
        return new OpenApiAsyncCompatibilityCheckerBuilder();
    }

    public CompletionStage<CompatibilityResult> checkBackward(RootCapable original, RootCapable updated,
            CheckOptions options, AsyncResourceLoader loader) {
        return check(original, updated, options, loader, CheckDirection.BACKWARD);
    }

    public CompletionStage<CompatibilityResult> checkForward(RootCapable original, RootCapable updated,
            CheckOptions options, AsyncResourceLoader loader) {
        return check(original, updated, options, loader, CheckDirection.FORWARD);
    }

    public CompletionStage<FullCompatibilityResult> checkFull(RootCapable original, RootCapable updated,
            CheckOptions options, AsyncResourceLoader loader) {
        return acquire(original, updated, options, loader)
                .thenApply(new java.util.function.Function<CheckSession, FullCompatibilityResult>() {
                    @Override
                    public FullCompatibilityResult apply(CheckSession session) {
                        OpenApiCompatibilityChecker checker = new OpenApiCompatibilityChecker(policy);
                        ObjectNode originalJson = Library.writeDocument((Document) session.getOriginal());
                        ObjectNode updatedJson = Library.writeDocument((Document) session.getUpdated());
                        CompatibilityResult backward = checker.checkJson(originalJson, updatedJson, options,
                                CheckDirection.BACKWARD);
                        CompatibilityResult forward = checker.checkJson(originalJson, updatedJson, options,
                                CheckDirection.FORWARD);
                        return new FullCompatibilityResult(backward, forward);
                    }
                });
    }

    public CompletionStage<CompatibilityResult> checkBackwardJson(String originalJson, String updatedJson,
            CheckOptions options, AsyncResourceLoader loader) {
        return checkBackward(readRoot(originalJson), readRoot(updatedJson), options, loader);
    }

    public CompletionStage<CompatibilityResult> checkForwardJson(String originalJson, String updatedJson,
            CheckOptions options, AsyncResourceLoader loader) {
        return checkForward(readRoot(originalJson), readRoot(updatedJson), options, loader);
    }

    public CompletionStage<FullCompatibilityResult> checkFullJson(String originalJson, String updatedJson,
            CheckOptions options, AsyncResourceLoader loader) {
        return checkFull(readRoot(originalJson), readRoot(updatedJson), options, loader);
    }

    private CompletionStage<CompatibilityResult> check(RootCapable original, RootCapable updated, CheckOptions options,
            AsyncResourceLoader loader, CheckDirection direction) {
        return acquire(original, updated, options, loader)
                .thenApply(new java.util.function.Function<CheckSession, CompatibilityResult>() {
                    @Override
                    public CompatibilityResult apply(CheckSession session) {
                        OpenApiCompatibilityChecker checker = new OpenApiCompatibilityChecker(policy);
                        ObjectNode originalJson = Library.writeDocument((Document) session.getOriginal());
                        ObjectNode updatedJson = Library.writeDocument((Document) session.getUpdated());
                        return checker.checkJson(originalJson, updatedJson, options, direction);
                    }
                });
    }

    private CompletionStage<CheckSession> acquire(RootCapable original, RootCapable updated, CheckOptions options,
            AsyncResourceLoader loader) {
        if (original == null) {
            throw new IllegalArgumentException("original must not be null");
        }
        if (updated == null) {
            throw new IllegalArgumentException("updated must not be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null");
        }
        CheckSession session = CheckSession.snapshot((OpenApiDocument) original, (OpenApiDocument) updated, options, policy);
        AsyncResourceAcquirer acquirer = new AsyncResourceAcquirer();
        return acquirer.acquire(session, loader);
    }

    private static RootCapable readRoot(String json) {
        if (json == null) {
            throw new IllegalArgumentException("json must not be null");
        }
        return (RootCapable) Library.readRootFromJSONString(json);
    }
}
