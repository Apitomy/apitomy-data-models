import {ResourceRequest} from "./ResourceRequest";
import {ResourceDocument} from "./ResourceDocument";

/**
 * Loads a single resource, asynchronously, for one side of a check.
 * <p>
 * This is the TypeScript-native counterpart of the Java {@code AsyncResourceLoader}
 * interface (excluded from JSweet transpilation because {@code CompletionStage}
 * has no TypeScript equivalent). Kept in sync by hand; see
 * {@code AsyncResourceAcquirer.java}'s Javadoc for why the boundary is split
 * this way.
 * <p>
 * A rejected promise records an acquisition problem and does not stop the rest
 * of acquisition. Resolving with {@code null} is a loader programming error,
 * not an empty/missing resource -- reject instead to report a missing resource.
 */
export interface AsyncResourceLoader {

    /**
     * Asynchronously loads the resource named by `request`.
     *
     * @param request the side and absolute retrieval URI to load
     * @return a promise that resolves with the acquired document, or rejects
     *         if it cannot be acquired
     */
    load(request: ResourceRequest): Promise<ResourceDocument>;
}
