import {ResourceDiscovery} from "./ResourceDiscovery";
import {ResourceRequest} from "./ResourceRequest";
import {ResourceProblem} from "./ResourceProblem";
import {ResourceDocument} from "./ResourceDocument";
import {AsyncResourceLoader} from "./AsyncResourceLoader";
import {CheckSession} from "../CheckSession";
import {CheckCancellation} from "../CheckCancellation";
import {CheckOptions} from "../CheckOptions";
import {FindingCode} from "../FindingCode";

/**
 * Drives {@link ResourceDiscovery} and an {@link AsyncResourceLoader} to
 * completion: repeatedly discovers unattempted resource requests, loads all of
 * them concurrently, records what came back (or failed) on the session, and
 * discovers again -- since a newly acquired document can itself reveal further
 * external references -- until a round finds nothing new to attempt.
 * <p>
 * This is the TypeScript-native counterpart of the Java {@code AsyncResourceAcquirer}
 * (excluded from JSweet transpilation; see that class's Javadoc). It must be
 * kept in sync by hand with the Java implementation: only the platform
 * concurrency primitive differs (native {@code Promise} here, {@code CompletableFuture}
 * there); which references are reachable and how failures are recorded both
 * come from the shared, synchronous {@link ResourceDiscovery} and
 * {@link CheckSession} used identically by both.
 * <p>
 * Unlike the Java implementation, no explicit trampoline/loop is needed to stay
 * stack-safe for a long synchronous chain: a JavaScript `Promise.then` callback
 * is always scheduled as a microtask, never invoked inline even when the
 * promise is already resolved, so recursive calls to `runRound` here never
 * nest within a single native call stack frame the way a Java
 * `CompletableFuture` callback can when the future is already complete.
 */
export class AsyncResourceAcquirer {

    public acquire(session: CheckSession, loader: AsyncResourceLoader): Promise<CheckSession> {
        return new Promise<CheckSession>((resolve, reject) => {
            const cancellation: CheckCancellation = session.getOptions().getCancellation();
            let settled: boolean = false;
            let unregister: () => void = null;

            const finishReject = (error: any): void => {
                if (settled) {
                    return;
                }
                settled = true;
                if (unregister != null) {
                    unregister();
                }
                reject(error);
            };
            const finishResolve = (value: CheckSession): void => {
                if (settled) {
                    return;
                }
                settled = true;
                if (unregister != null) {
                    unregister();
                }
                resolve(value);
            };

            if (cancellation != null) {
                unregister = cancellation.onCancel(() => {
                    finishReject(new Error("Compatibility check was cancelled"));
                });
            }

            const attempted: Set<string> = new Set<string>();
            let rounds: number = 0;

            const requestKey = (request: ResourceRequest): string => {
                return request.getSide() + "|" + request.getUri();
            };

            const recordLimitReached = (request: ResourceRequest): void => {
                session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                    FindingCode.ANALYSIS_LIMIT_REACHED,
                    "Resource-count budget (" + session.getOptions().getMaxResourceCount()
                        + ") was reached before this resource could be acquired"));
            };

            const recordLimitReachedForRemaining = (): void => {
                const discovered: ResourceRequest[] = ResourceDiscovery.discover(session);
                discovered.forEach((request) => {
                    if (attempted.has(requestKey(request))) {
                        return;
                    }
                    session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                        FindingCode.ANALYSIS_LIMIT_REACHED,
                        "Discovery-round budget (" + session.getOptions().getMaxDiscoveryRounds()
                            + ") was reached before this resource could be acquired"));
                });
            };

            const loadOne = (request: ResourceRequest): Promise<void> => {
                let stage: Promise<ResourceDocument>;
                try {
                    stage = loader.load(request);
                } catch (e) {
                    session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                        FindingCode.RESOURCE_UNRESOLVED, "Loader threw while starting the load: " + e));
                    return Promise.resolve();
                }
                if (stage == null) {
                    session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                        FindingCode.RESOURCE_UNRESOLVED, "Loader returned a null promise"));
                    return Promise.resolve();
                }
                return stage.then((document) => {
                    if (document == null) {
                        session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                            FindingCode.RESOURCE_UNRESOLVED, "Loader completed without a document"));
                    } else {
                        session.addResource(request.getSide(), document);
                    }
                }, (error) => {
                    session.addResourceProblem(request, new ResourceProblem(request.getSide(), request.getUri(),
                        FindingCode.RESOURCE_UNRESOLVED, "Failed to acquire resource: " + error));
                });
            };

            const runRound = (): void => {
                if (settled) {
                    return;
                }
                if (cancellation != null && cancellation.isCancelled()) {
                    finishReject(new Error("Compatibility check was cancelled"));
                    return;
                }
                const options: CheckOptions = session.getOptions();
                if (rounds >= options.getMaxDiscoveryRounds()) {
                    recordLimitReachedForRemaining();
                    finishResolve(session);
                    return;
                }
                const discovered: ResourceRequest[] = ResourceDiscovery.discover(session);
                const unattempted: ResourceRequest[] = discovered.filter((request) => !attempted.has(requestKey(request)));
                if (unattempted.length === 0) {
                    finishResolve(session);
                    return;
                }
                rounds++;

                const toLoad: ResourceRequest[] = [];
                unattempted.forEach((request) => {
                    if (attempted.size >= options.getMaxResourceCount()) {
                        recordLimitReached(request);
                        return;
                    }
                    attempted.add(requestKey(request));
                    toLoad.push(request);
                });

                if (toLoad.length === 0) {
                    finishResolve(session);
                    return;
                }

                const batch: Promise<void>[] = toLoad.map((request) => loadOne(request));
                Promise.all(batch).then(() => runRound(), () => runRound());
            };

            runRound();
        });
    }
}
