import {Library} from "../src/io/apitomy/datamodels/Library";
import {Document} from "../src/io/apitomy/datamodels/models/Document";
import {RootCapable} from "../src/io/apitomy/datamodels/models/RootCapable";
import {NodeUtil} from "../src/io/apitomy/datamodels/util/NodeUtil";
import {ResourceSide} from "../src/io/apitomy/datamodels/openapi/compat/ResourceSide";
import {CheckOptions} from "../src/io/apitomy/datamodels/openapi/compat/CheckOptions";
import {CheckSession} from "../src/io/apitomy/datamodels/openapi/compat/CheckSession";
import {CheckCancellation} from "../src/io/apitomy/datamodels/openapi/compat/CheckCancellation";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {FindingCode} from "../src/io/apitomy/datamodels/openapi/compat/FindingCode";
import {ReferenceKind} from "../src/io/apitomy/datamodels/openapi/compat/resource/ReferenceKind";
import {ResourceDocument} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceDocument";
import {ResourceSet} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceSet";
import {ResourceIndex} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceIndex";
import {ResourceDiscovery} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceDiscovery";
import {ResourceRequest} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceRequest";
import {readJSON} from "./util/tutils";

const ORIGINAL_URI: string = "urn:test:original";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/references.json");
    for (const testCase of root.cases) {
        if (testCase.id === id) {
            return testCase;
        }
    }
    throw new Error("No fixture case named '" + id + "'");
}

function readResourceDocument(side: ResourceSide, uri: string, json: any): ResourceDocument {
    const document: Document = Library.readDocument(json);
    return new ResourceDocument(side, uri, <RootCapable><any>document);
}

test("internal pointer references escape ~0 and ~1", () => {
    const testCase: any = fixtureCase("pointer-escaping");
    const resource: ResourceDocument = readResourceDocument(ResourceSide.ORIGINAL, ORIGINAL_URI, testCase.document);
    const resourceSet: ResourceSet = new ResourceSet(ResourceSide.ORIGINAL);
    resourceSet.add(resource);

    const graph = ResourceIndex.index(resource, resourceSet);
    const edges = graph.getEdges();
    expect(edges.length).toBe(2);

    let sawSlashProperty: boolean = false;
    let sawTildeProperty: boolean = false;
    edges.forEach(edge => {
        expect(edge.getKind()).toBe(ReferenceKind.SCHEMA_REFERENCE);
        const target = graph.getTarget(edge);
        expect(target).not.toBeNull();
        const type: any = NodeUtil.getNodeProperty(target.getNode(), "type");
        if (edge.getRawReference().endsWith("a~1b")) {
            expect(type).toBe("string");
            sawSlashProperty = true;
        } else if (edge.getRawReference().endsWith("c~0d")) {
            expect(type).toBe("integer");
            sawTildeProperty = true;
        }
    });
    expect(sawSlashProperty).toBe(true);
    expect(sawTildeProperty).toBe(true);
});

test("an unresolved external resource affects only its own operation", () => {
    const testCase: any = fixtureCase("unresolved-resource-affects-one-operation");
    const resource: ResourceDocument = readResourceDocument(ResourceSide.ORIGINAL, ORIGINAL_URI, testCase.document);
    const resourceSet: ResourceSet = new ResourceSet(ResourceSide.ORIGINAL);
    resourceSet.add(resource);

    const graph = ResourceIndex.index(resource, resourceSet);
    expect(graph.getEdges().length).toBe(1);
    expect(graph.getEdges()[0].getRawReference().indexOf("missing.json")).toBe(0);

    const problems = graph.getProblems();
    expect(problems.length).toBe(1);
    expect(problems[0].getCode()).toBe(FindingCode.RESOURCE_UNRESOLVED);
    expect(problems[0].getSide()).toBe(ResourceSide.ORIGINAL);
});

test("cyclic internal references resolve without looping", () => {
    const testCase: any = fixtureCase("cyclic-internal-references");
    const resource: ResourceDocument = readResourceDocument(ResourceSide.ORIGINAL, ORIGINAL_URI, testCase.document);
    const resourceSet: ResourceSet = new ResourceSet(ResourceSide.ORIGINAL);
    resourceSet.add(resource);

    const graph = ResourceIndex.index(resource, resourceSet);
    expect(graph.getEdges().length).toBe(2);
    expect(graph.getProblems().length).toBe(0);
    graph.getEdges().forEach(edge => {
        const target = graph.getTarget(edge);
        expect(target).not.toBeNull();
        expect(NodeUtil.getNodeProperty(target.getNode(), "allOf")).not.toBeNull();
    });
});

test("the same absolute URI resolves to independent content per side", () => {
    const testCase: any = fixtureCase("same-uri-different-content-per-side");
    const sharedUri: string = testCase.original.uri;

    const originalResource: ResourceDocument = readResourceDocument(ResourceSide.ORIGINAL, sharedUri, testCase.original.document);
    const updatedResource: ResourceDocument = readResourceDocument(ResourceSide.UPDATED, sharedUri, testCase.updated.document);

    const originalResources: ResourceSet = new ResourceSet(ResourceSide.ORIGINAL);
    originalResources.add(originalResource);
    const updatedResources: ResourceSet = new ResourceSet(ResourceSide.UPDATED);
    updatedResources.add(updatedResource);

    expect(originalResources.get(sharedUri).getUri()).toBe(sharedUri);
    expect(updatedResources.get(sharedUri).getUri()).toBe(sharedUri);

    const originalComponents: any = NodeUtil.getNodeProperty(<any>originalResource.getRoot(), "components");
    const originalSchemas: any = NodeUtil.getNodeProperty(originalComponents, "schemas");
    const originalAddress: any = originalSchemas["Address"];
    const originalProperties: any = NodeUtil.getNodeProperty(originalAddress, "properties");

    const updatedComponents: any = NodeUtil.getNodeProperty(<any>updatedResource.getRoot(), "components");
    const updatedSchemas: any = NodeUtil.getNodeProperty(updatedComponents, "schemas");
    const updatedAddress: any = updatedSchemas["Address"];
    const updatedProperties: any = NodeUtil.getNodeProperty(updatedAddress, "properties");

    expect(Object.keys(originalProperties).length).toBe(1);
    expect(Object.keys(updatedProperties).length).toBe(2);
});

test("resource discovery reports a missing external resource, then stops after it is added", () => {
    const testCase: any = fixtureCase("unresolved-resource-affects-one-operation");
    const document: Document = Library.readDocument(testCase.document);

    const updatedJson: any = {openapi: "3.0.4", info: {title: "Partial", version: "2"}, paths: {}};
    const updatedDocument: Document = Library.readDocument(updatedJson);

    const options: CheckOptions = CheckOptions.defaults()
        .withOriginalUri(ORIGINAL_URI)
        .withUpdatedUri("urn:test:updated");
    const session: CheckSession = CheckSession.snapshot(<any>document, <any>updatedDocument, options, CompatibilityPolicy.defaults());

    const requests: ResourceRequest[] = ResourceDiscovery.discover(session);
    let missingRequest: ResourceRequest = null;
    requests.forEach(request => {
        if (request.getSide() === ResourceSide.ORIGINAL) {
            missingRequest = request;
        }
        expect(request.getSide()).not.toBe(ResourceSide.UPDATED);
    });
    expect(missingRequest).not.toBeNull();
    expect(missingRequest.getUri().endsWith("missing.json")).toBe(true);

    const stubJson: any = {openapi: "3.0.4", info: {title: "Stub", version: "1"}, paths: {}};
    const stubDocument: Document = Library.readDocument(stubJson);
    session.addResource(ResourceSide.ORIGINAL, new ResourceDocument(ResourceSide.ORIGINAL, missingRequest.getUri(), <RootCapable><any>stubDocument));

    const requestsAfter: ResourceRequest[] = ResourceDiscovery.discover(session);
    requestsAfter.forEach(request => {
        const stillMissing: boolean = request.getUri() === missingRequest.getUri() && request.getSide() === ResourceSide.ORIGINAL;
        expect(stillMissing).toBe(false);
    });
});

test("cancellation notifies listeners and is idempotent", () => {
    const cancellation: CheckCancellation = new CheckCancellation();
    let callCount: number = 0;
    const unregister: () => void = cancellation.onCancel(() => {
        callCount++;
    });

    expect(cancellation.isCancelled()).toBe(false);
    cancellation.cancel();
    expect(cancellation.isCancelled()).toBe(true);
    expect(callCount).toBe(1);

    cancellation.cancel();
    expect(callCount).toBe(1);

    unregister();

    const late: CheckCancellation = new CheckCancellation();
    late.cancel();
    let lateCallCount: number = 0;
    late.onCancel(() => {
        lateCallCount++;
    });
    expect(lateCallCount).toBe(1);
});
