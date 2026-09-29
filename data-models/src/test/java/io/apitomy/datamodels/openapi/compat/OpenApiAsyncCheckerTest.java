package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.openapi.compat.resource.AsyncResourceLoader;
import io.apitomy.datamodels.openapi.compat.resource.ResourceDocument;
import io.apitomy.datamodels.openapi.compat.resource.ResourceRequest;

/**
 * {@code OpenApiAsyncCompatibilityChecker} (T18): the async entry points
 * produce the same verdicts as their synchronous counterparts for documents
 * with no external references, and never invoke the loader to do so.
 */
class OpenApiAsyncCheckerTest {

    private static final String ORIGINAL_30 = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
            + "\"paths\":{\"/widgets\":{\"get\":{\"parameters\":[{\"name\":\"limit\",\"in\":\"query\",\"schema\":{\"type\":\"integer\"}}],"
            + "\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

    private static final String UPDATED_30_BREAKING = "{\"openapi\":\"3.0.4\",\"info\":{\"title\":\"T\",\"version\":\"1\"},"
            + "\"paths\":{\"/widgets\":{\"get\":{\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";

    /** A loader that fails the test if it is ever invoked -- proves no-external-reference documents never trigger acquisition. */
    private static final class NeverCalledLoader implements AsyncResourceLoader {
        @Override
        public CompletionStage<ResourceDocument> load(ResourceRequest request) {
            fail("Loader should never be invoked for a document with no external references: " + request.getUri());
            return null;
        }
    }

    private static <T> T waitFor(CompletionStage<T> stage) throws Exception {
        try {
            return ((CompletableFuture<T>) stage).get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    @Test
    void checkBackwardJsonMatchesTheSynchronousResultAndNeverInvokesTheLoader() throws Exception {
        OpenApiAsyncCompatibilityChecker checker = OpenApiAsyncCompatibilityChecker.builder().build();
        CompatibilityResult result = waitFor(checker.checkBackwardJson(ORIGINAL_30, UPDATED_30_BREAKING,
                CheckOptions.defaults(), new NeverCalledLoader()));
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getVerdict());
    }

    @Test
    void checkForwardJsonMatchesTheSynchronousResult() throws Exception {
        OpenApiAsyncCompatibilityChecker checker = OpenApiAsyncCompatibilityChecker.builder().build();
        CompatibilityResult result = waitFor(checker.checkForwardJson(ORIGINAL_30, UPDATED_30_BREAKING,
                CheckOptions.defaults(), new NeverCalledLoader()));
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getVerdict());
    }

    @Test
    void checkFullJsonPreservesBothDirections() throws Exception {
        OpenApiAsyncCompatibilityChecker checker = OpenApiAsyncCompatibilityChecker.builder().build();
        FullCompatibilityResult result = waitFor(checker.checkFullJson(ORIGINAL_30, UPDATED_30_BREAKING,
                CheckOptions.defaults(), new NeverCalledLoader()));
        assertEquals(CompatibilityVerdict.INCOMPATIBLE, result.getBackwardResult().getVerdict());
        assertEquals(CompatibilityVerdict.COMPATIBLE, result.getForwardResult().getVerdict());
    }
}
