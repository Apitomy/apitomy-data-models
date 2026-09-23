import {Library} from "../src/io/apitomy/datamodels/Library";
import {Document} from "../src/io/apitomy/datamodels/models/Document";
import {RootCapable} from "../src/io/apitomy/datamodels/models/RootCapable";
import {ResourceSide} from "../src/io/apitomy/datamodels/openapi/compat/ResourceSide";
import {CheckOptions} from "../src/io/apitomy/datamodels/openapi/compat/CheckOptions";
import {CheckSession} from "../src/io/apitomy/datamodels/openapi/compat/CheckSession";
import {CheckCancellation} from "../src/io/apitomy/datamodels/openapi/compat/CheckCancellation";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {ResourceDocument} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceDocument";
import {ResourceRequest} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceRequest";
import {AsyncResourceLoader} from "../src/io/apitomy/datamodels/openapi/compat/resource/AsyncResourceLoader";
import {AsyncResourceAcquirer} from "../src/io/apitomy/datamodels/openapi/compat/resource/AsyncResourceAcquirer";

const ORIGINAL_URI: string = "urn:test:original";
const UPDATED_URI: string = "urn:test:updated";

function emptyDocument(title: string): any {
    return {openapi: "3.0.4", info: {title: title, version: "1"}, paths: {}};
}

function refDocument(title: string, ref: string): any {
    const doc: any = emptyDocument(title);
    doc.components = {schemas: {Chained: {$ref: ref}}};
    return doc;
}

function newSession(originalJson: any, updatedJson: any, options?: CheckOptions): CheckSession {
    const original: Document = Library.readDocument(originalJson);
    const updated: Document = Library.readDocument(updatedJson);
    const opts: CheckOptions = options || CheckOptions.defaults().withOriginalUri(ORIGINAL_URI).withUpdatedUri(UPDATED_URI);
    return CheckSession.snapshot(<any>original, <any>updated, opts, CompatibilityPolicy.defaults());
}

class FakeLoader implements AsyncResourceLoader {
    private documents: Map<string, any> = new Map<string, any>();
    private failures: Map<string, string> = new Map<string, string>();
    private loadCounts: Map<string, number> = new Map<string, number>();

    withDocument(uri: string, json: any): void {
        this.documents.set(uri, json);
    }

    withFailure(uri: string, message: string): void {
        this.failures.set(uri, message);
    }

    loadCount(uri: string): number {
        return this.loadCounts.get(uri) || 0;
    }

    load(request: ResourceRequest): Promise<ResourceDocument> {
        const uri: string = request.getUri();
        this.loadCounts.set(uri, this.loadCount(uri) + 1);
        if (this.failures.has(uri)) {
            return Promise.reject(new Error(this.failures.get(uri)));
        }
        const json: any = this.documents.get(uri);
        if (json == null) {
            return Promise.reject(new Error("no fake document for " + uri));
        }
        const document: Document = Library.readDocument(json);
        return Promise.resolve(new ResourceDocument(request.getSide(), uri, <RootCapable><any>document));
    }
}

test("resolves nested relative resources across multiple rounds", async () => {
    const session: CheckSession = newSession(refDocument("Root", "a.json#/components/schemas/Chained"), emptyDocument("Updated"));
    const loader: FakeLoader = new FakeLoader();
    loader.withDocument("a.json", refDocument("A", "b.json#/components/schemas/Chained"));
    loader.withDocument("b.json", emptyDocument("B"));

    const finished: CheckSession = await new AsyncResourceAcquirer().acquire(session, loader);

    expect(finished.getResourceSet(ResourceSide.ORIGINAL).contains("a.json")).toBe(true);
    expect(finished.getResourceSet(ResourceSide.ORIGINAL).contains("b.json")).toBe(true);
    expect(loader.loadCount("a.json")).toBe(1);
    expect(loader.loadCount("b.json")).toBe(1);
    expect(finished.getResourceProblems().length).toBe(0);
});

test("records a failure as a problem without failing the whole acquisition", async () => {
    const session: CheckSession = newSession(refDocument("Root", "missing.json#/components/schemas/Chained"), emptyDocument("Updated"));
    const loader: FakeLoader = new FakeLoader();
    loader.withFailure("missing.json", "network error");

    const finished: CheckSession = await new AsyncResourceAcquirer().acquire(session, loader);

    expect(finished.getResourceProblems().length).toBe(1);
    expect(finished.getResourceProblems()[0].getCode()).toBe(FindingCode.RESOURCE_UNRESOLVED);
    expect(finished.getResourceSet(ResourceSide.ORIGINAL).contains("missing.json")).toBe(false);
});

test("a reference on one side does not acquire anything for the other side", async () => {
    const session: CheckSession = newSession(refDocument("Root", "a.json#/components/schemas/Chained"), emptyDocument("Updated"));
    const loader: FakeLoader = new FakeLoader();
    loader.withDocument("a.json", emptyDocument("A"));

    const finished: CheckSession = await new AsyncResourceAcquirer().acquire(session, loader);

    expect(finished.getResourceSet(ResourceSide.ORIGINAL).contains("a.json")).toBe(true);
    expect(finished.getResourceSet(ResourceSide.UPDATED).contains("a.json")).toBe(false);
});

test("cyclic external references terminate", async () => {
    const session: CheckSession = newSession(refDocument("Root", "a.json#/components/schemas/Chained"), emptyDocument("Updated"));
    const loader: FakeLoader = new FakeLoader();
    loader.withDocument("a.json", refDocument("A", ORIGINAL_URI + "#/components/schemas/Chained"));

    const finished: CheckSession = await new AsyncResourceAcquirer().acquire(session, loader);

    expect(finished.getResourceSet(ResourceSide.ORIGINAL).contains("a.json")).toBe(true);
    expect(loader.loadCount("a.json")).toBe(1);
});

test("cancellation rejects and does not wait for a non-completing loader", async () => {
    const session: CheckSession = newSession(refDocument("Root", "a.json#/components/schemas/Chained"), emptyDocument("Updated"));
    const cancellation: CheckCancellation = new CheckCancellation();
    const options: CheckOptions = session.getOptions().withCancellation(cancellation);
    const cancellableSession: CheckSession = CheckSession.snapshot(session.getOriginal(), session.getUpdated(), options, session.getPolicy());

    const neverSettling: AsyncResourceLoader = {
        load: (_request: ResourceRequest): Promise<ResourceDocument> => new Promise<ResourceDocument>(() => {
            // never resolves or rejects
        }),
    };

    const promise: Promise<CheckSession> = new AsyncResourceAcquirer().acquire(cancellableSession, neverSettling);
    cancellation.cancel();

    await expect(promise).rejects.toBeTruthy();
});
