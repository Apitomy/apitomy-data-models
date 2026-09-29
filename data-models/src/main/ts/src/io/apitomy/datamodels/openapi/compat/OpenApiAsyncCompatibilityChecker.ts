import {Library} from "../../Library";
import {RootCapable} from "../../models/RootCapable";
import {CheckDirection} from "./CheckDirection";
import {CheckOptions} from "./CheckOptions";
import {CheckSession} from "./CheckSession";
import {CompatibilityPolicy} from "./CompatibilityPolicy";
import {CompatibilityResult} from "./CompatibilityResult";
import {FullCompatibilityResult} from "./FullCompatibilityResult";
import {OpenApiCompatibilityChecker} from "./OpenApiCompatibilityChecker";
import {OpenApiAsyncCompatibilityCheckerBuilder} from "./OpenApiAsyncCompatibilityCheckerBuilder";
import {AsyncResourceAcquirer} from "./resource/AsyncResourceAcquirer";
import {AsyncResourceLoader} from "./resource/AsyncResourceLoader";

/**
 * The public, asynchronous entry point for OpenAPI compatibility checking:
 * identical to {@link OpenApiCompatibilityChecker}'s snapshot -> interpret ->
 * match -> rules -> aggregate pipeline, but resolves external resources
 * reachable from either document first, using a caller-supplied
 * {@link AsyncResourceLoader}, via the shared {@link AsyncResourceAcquirer}.
 * <p>
 * This is the TypeScript-native counterpart of the Java
 * {@code OpenApiAsyncCompatibilityChecker} (excluded from JSweet
 * transpilation because it uses {@code CompletionStage}; see that class's
 * Javadoc, and {@code AsyncResourceAcquirer}'s, for why). It must be kept in
 * sync by hand with the Java implementation: only the platform concurrency
 * primitive differs (native {@code Promise} here, {@code CompletionStage}
 * there); the actual pipeline (delegating to {@link OpenApiCompatibilityChecker}
 * once acquisition finishes) is identical.
 * <p>
 * A document with no external references to resolve never invokes the
 * loader at all -- acquisition discovers nothing to do and the check
 * proceeds immediately, identically to the synchronous checker.
 */
export class OpenApiAsyncCompatibilityChecker {

    private readonly policy: CompatibilityPolicy;

    constructor(policy: CompatibilityPolicy) {
        if (policy == null) {
            throw new Error("policy must not be null");
        }
        this.policy = policy;
    }

    public static builder(): OpenApiAsyncCompatibilityCheckerBuilder {
        return new OpenApiAsyncCompatibilityCheckerBuilder();
    }

    public checkBackward(original: RootCapable, updated: RootCapable, options: CheckOptions,
            loader: AsyncResourceLoader): Promise<CompatibilityResult> {
        return this.check(original, updated, options, loader, CheckDirection.BACKWARD);
    }

    public checkForward(original: RootCapable, updated: RootCapable, options: CheckOptions,
            loader: AsyncResourceLoader): Promise<CompatibilityResult> {
        return this.check(original, updated, options, loader, CheckDirection.FORWARD);
    }

    public checkFull(original: RootCapable, updated: RootCapable, options: CheckOptions,
            loader: AsyncResourceLoader): Promise<FullCompatibilityResult> {
        return this.acquire(original, updated, options, loader).then((session) => {
            const checker: OpenApiCompatibilityChecker = new OpenApiCompatibilityChecker(this.policy);
            const originalJson: any = Library.writeDocument(<any>session.getOriginal());
            const updatedJson: any = Library.writeDocument(<any>session.getUpdated());
            const backward: CompatibilityResult = checker.checkJson(originalJson, updatedJson, options, CheckDirection.BACKWARD);
            const forward: CompatibilityResult = checker.checkJson(originalJson, updatedJson, options, CheckDirection.FORWARD);
            return new FullCompatibilityResult(backward, forward);
        });
    }

    public checkBackwardJson(originalJson: string, updatedJson: string, options: CheckOptions,
            loader: AsyncResourceLoader): Promise<CompatibilityResult> {
        return this.checkBackward(OpenApiAsyncCompatibilityChecker.readRoot(originalJson),
            OpenApiAsyncCompatibilityChecker.readRoot(updatedJson), options, loader);
    }

    public checkForwardJson(originalJson: string, updatedJson: string, options: CheckOptions,
            loader: AsyncResourceLoader): Promise<CompatibilityResult> {
        return this.checkForward(OpenApiAsyncCompatibilityChecker.readRoot(originalJson),
            OpenApiAsyncCompatibilityChecker.readRoot(updatedJson), options, loader);
    }

    public checkFullJson(originalJson: string, updatedJson: string, options: CheckOptions,
            loader: AsyncResourceLoader): Promise<FullCompatibilityResult> {
        return this.checkFull(OpenApiAsyncCompatibilityChecker.readRoot(originalJson),
            OpenApiAsyncCompatibilityChecker.readRoot(updatedJson), options, loader);
    }

    private check(original: RootCapable, updated: RootCapable, options: CheckOptions, loader: AsyncResourceLoader,
            direction: CheckDirection): Promise<CompatibilityResult> {
        return this.acquire(original, updated, options, loader).then((session) => {
            const checker: OpenApiCompatibilityChecker = new OpenApiCompatibilityChecker(this.policy);
            const originalJson: any = Library.writeDocument(<any>session.getOriginal());
            const updatedJson: any = Library.writeDocument(<any>session.getUpdated());
            return checker.checkJson(originalJson, updatedJson, options, direction);
        });
    }

    private acquire(original: RootCapable, updated: RootCapable, options: CheckOptions,
            loader: AsyncResourceLoader): Promise<CheckSession> {
        if (original == null) {
            throw new Error("original must not be null");
        }
        if (updated == null) {
            throw new Error("updated must not be null");
        }
        if (options == null) {
            throw new Error("options must not be null");
        }
        if (loader == null) {
            throw new Error("loader must not be null");
        }
        const session: CheckSession = CheckSession.snapshot(<any>original, <any>updated, options, this.policy);
        const acquirer: AsyncResourceAcquirer = new AsyncResourceAcquirer();
        return acquirer.acquire(session, loader);
    }

    private static readRoot(json: string): RootCapable {
        if (json == null) {
            throw new Error("json must not be null");
        }
        return <RootCapable><any>Library.readRootFromJSONString(json);
    }
}
