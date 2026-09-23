package io.apitomy.datamodels.openapi.compat.resource;

import java.util.concurrent.CompletionStage;

/**
 * Loads a single resource, asynchronously, for one side of a check.
 * <p>
 * This is the Java-native async boundary. It is deliberately excluded from
 * JSweet transpilation (see {@code data-models/pom.xml}'s {@code jsweet-maven-plugin}
 * excludes): {@link CompletionStage} has no TypeScript equivalent, and the
 * TypeScript build ships a separately hand-written, curated
 * {@code AsyncResourceLoader.ts} using native {@code Promise} instead. The two
 * are kept in sync by hand; neither performs semantic analysis, so the surface
 * they need to agree on is small: "given a request, asynchronously produce a
 * resource or fail."
 * <p>
 * A null result is a loader programming error, not an empty/missing resource —
 * report a missing resource by failing the returned stage instead. A failed
 * stage is recorded as an acquisition problem and does not stop the rest of
 * acquisition; other requests in the same or a later round still proceed.
 */
public interface AsyncResourceLoader {

    /**
     * Asynchronously loads the resource named by {@code request}.
     *
     * @param request the side and absolute retrieval URI to load
     * @return a stage that completes with the acquired document, or completes
     *         exceptionally if it cannot be acquired
     */
    CompletionStage<ResourceDocument> load(ResourceRequest request);
}
