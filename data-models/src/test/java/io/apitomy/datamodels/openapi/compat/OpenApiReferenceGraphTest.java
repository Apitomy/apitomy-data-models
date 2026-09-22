package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.openapi.compat.resource.ReferenceEdge;
import io.apitomy.datamodels.openapi.compat.resource.ReferenceGraph;
import io.apitomy.datamodels.openapi.compat.resource.ReferenceKind;
import io.apitomy.datamodels.openapi.compat.resource.ReferenceTarget;
import io.apitomy.datamodels.openapi.compat.resource.ResourceDocument;
import io.apitomy.datamodels.openapi.compat.resource.ResourceDiscovery;
import io.apitomy.datamodels.openapi.compat.resource.ResourceIndex;
import io.apitomy.datamodels.openapi.compat.resource.ResourceProblem;
import io.apitomy.datamodels.openapi.compat.resource.ResourceRequest;
import io.apitomy.datamodels.openapi.compat.resource.ResourceSet;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * Exercises {@code ResourceIndex}, {@code ReferenceGraph}, {@code ResourceDiscovery},
 * and {@code CheckSession} together: static-reference discovery and resolution
 * across cyclic, escaped, cross-side, and unresolved cases from {@code references.json}.
 */
class OpenApiReferenceGraphTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ORIGINAL_URI = "urn:test:original";

    private static JsonNode fixture(String caseId) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/references.json");
        assertNotNull(in, "references.json fixture not found");
        JsonNode root = MAPPER.readTree(in);
        in.close();
        for (JsonNode testCase : root.get("cases")) {
            if (testCase.get("id").asText().equals(caseId)) {
                return testCase;
            }
        }
        throw new IllegalStateException("No fixture case named '" + caseId + "'");
    }

    private static ResourceDocument readResourceDocument(ResourceSide side, String uri, ObjectNode json) {
        Document document = Library.readDocument(json);
        return new ResourceDocument(side, uri, (RootCapable) document);
    }

    @Test
    void internalPointerEscapingResolvesThroughTildeAndSlash() throws Exception {
        JsonNode testCase = fixture("pointer-escaping");
        ObjectNode json = (ObjectNode) testCase.get("document");
        ResourceDocument resource = readResourceDocument(ResourceSide.ORIGINAL, ORIGINAL_URI, json);
        ResourceSet resourceSet = new ResourceSet(ResourceSide.ORIGINAL);
        resourceSet.add(resource);

        ReferenceGraph graph = ResourceIndex.index(resource, resourceSet);
        List<ReferenceEdge> edges = graph.getEdges();
        assertEquals(2, edges.size(), "Expected one edge per $ref-bearing parameter schema");

        boolean sawSlashProperty = false;
        boolean sawTildeProperty = false;
        for (int i = 0; i < edges.size(); i++) {
            ReferenceEdge edge = edges.get(i);
            assertEquals(ReferenceKind.SCHEMA_REFERENCE, edge.getKind());
            ReferenceTarget target = graph.getTarget(edge);
            assertNotNull(target, "Expected " + edge.getRawReference() + " to resolve");
            Object type = NodeUtil.getNodeProperty(target.getNode(), "type");
            if (edge.getRawReference().endsWith("a~1b")) {
                assertEquals("string", type);
                sawSlashProperty = true;
            } else if (edge.getRawReference().endsWith("c~0d")) {
                assertEquals("integer", type);
                sawTildeProperty = true;
            }
        }
        assertTrue(sawSlashProperty, "Expected a resolved reference into the 'a/b' property (escaped as ~1)");
        assertTrue(sawTildeProperty, "Expected a resolved reference into the 'c~d' property (escaped as ~0)");
    }

    @Test
    void unresolvedExternalResourceAffectsOnlyItsOwnOperation() throws Exception {
        JsonNode testCase = fixture("unresolved-resource-affects-one-operation");
        ObjectNode json = (ObjectNode) testCase.get("document");
        ResourceDocument resource = readResourceDocument(ResourceSide.ORIGINAL, ORIGINAL_URI, json);
        ResourceSet resourceSet = new ResourceSet(ResourceSide.ORIGINAL);
        resourceSet.add(resource);

        ReferenceGraph graph = ResourceIndex.index(resource, resourceSet);
        // The /known operation's inline schema carries no $ref, so it never becomes an
        // edge; only /unknown's parameter does.
        assertEquals(1, graph.getEdges().size());
        ReferenceEdge edge = graph.getEdges().get(0);
        assertTrue(edge.getRawReference().startsWith("missing.json"));

        List<ResourceProblem> problems = graph.getProblems();
        assertEquals(1, problems.size());
        assertEquals(FindingCode.RESOURCE_UNRESOLVED, problems.get(0).getCode());
        assertEquals(ResourceSide.ORIGINAL, problems.get(0).getSide());
    }

    @Test
    void cyclicInternalReferencesResolveWithoutLooping() throws Exception {
        JsonNode testCase = fixture("cyclic-internal-references");
        ObjectNode json = (ObjectNode) testCase.get("document");
        ResourceDocument resource = readResourceDocument(ResourceSide.ORIGINAL, ORIGINAL_URI, json);
        ResourceSet resourceSet = new ResourceSet(ResourceSide.ORIGINAL);
        resourceSet.add(resource);

        // Indexing must terminate (this call itself is the assertion under a test
        // timeout) and every edge must resolve to the opposite schema.
        ReferenceGraph graph = ResourceIndex.index(resource, resourceSet);
        assertEquals(2, graph.getEdges().size());
        assertEquals(0, graph.getProblems().size());
        for (int i = 0; i < graph.getEdges().size(); i++) {
            ReferenceEdge edge = graph.getEdges().get(i);
            ReferenceTarget target = graph.getTarget(edge);
            assertNotNull(target);
            Object allOf = NodeUtil.getNodeProperty(target.getNode(), "allOf");
            assertNotNull(allOf, "The resolved node should be the other cyclic schema, which also has allOf");
        }
    }

    @Test
    void sameUriResolvesToIndependentContentPerSide() throws Exception {
        JsonNode testCase = fixture("same-uri-different-content-per-side");
        String sharedUri = testCase.get("original").get("uri").asText();
        ObjectNode originalDoc = (ObjectNode) testCase.get("original").get("document");
        ObjectNode updatedDoc = (ObjectNode) testCase.get("updated").get("document");

        ResourceDocument originalResource = readResourceDocument(ResourceSide.ORIGINAL, sharedUri, originalDoc);
        ResourceDocument updatedResource = readResourceDocument(ResourceSide.UPDATED, sharedUri, updatedDoc);

        ResourceSet originalResources = new ResourceSet(ResourceSide.ORIGINAL);
        originalResources.add(originalResource);
        ResourceSet updatedResources = new ResourceSet(ResourceSide.UPDATED);
        updatedResources.add(updatedResource);

        assertEquals(sharedUri, originalResources.get(sharedUri).getUri());
        assertEquals(sharedUri, updatedResources.get(sharedUri).getUri());

        OpenApiDocument original30 = (OpenApiDocument) originalResource.getRoot();
        OpenApiDocument updated30 = (OpenApiDocument) updatedResource.getRoot();
        Map<String, ?> originalProperties = propertiesOf(original30, "Address");
        Map<String, ?> updatedProperties = propertiesOf(updated30, "Address");

        assertEquals(1, originalProperties.size(), "Original Address should have only 'city'");
        assertEquals(2, updatedProperties.size(), "Updated Address should have gained 'postalCode'");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> propertiesOf(OpenApiDocument document, String schemaName) {
        Object components = NodeUtil.getNodeProperty((Node) document, "components");
        Object schemas = NodeUtil.getNodeProperty((Node) components, "schemas");
        Object schema = ((Map<String, ?>) schemas).get(schemaName);
        return (Map<String, ?>) NodeUtil.getNodeProperty((Node) schema, "properties");
    }

    @Test
    void resourceDiscoveryReportsMissingExternalResourceThenStopsAfterItIsAdded() throws Exception {
        JsonNode testCase = fixture("unresolved-resource-affects-one-operation");
        ObjectNode json = (ObjectNode) testCase.get("document");
        OpenApiDocument document = (OpenApiDocument) Library.readDocument(json);

        ObjectNode updatedJson = MAPPER.createObjectNode();
        updatedJson.put("openapi", "3.0.4");
        ObjectNode updatedInfo = MAPPER.createObjectNode();
        updatedInfo.put("title", "Partial");
        updatedInfo.put("version", "2");
        updatedJson.set("info", updatedInfo);
        updatedJson.set("paths", MAPPER.createObjectNode());
        OpenApiDocument updatedDocument = (OpenApiDocument) Library.readDocument(updatedJson);

        CheckOptions options = CheckOptions.defaults()
                .withOriginalUri(ORIGINAL_URI)
                .withUpdatedUri("urn:test:updated");
        CheckSession session = CheckSession.snapshot(document, updatedDocument, options, CompatibilityPolicy.defaults());

        List<ResourceRequest> requests = ResourceDiscovery.discover(session);
        ResourceRequest missingRequest = null;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getSide() == ResourceSide.ORIGINAL) {
                missingRequest = requests.get(i);
            }
        }
        assertNotNull(missingRequest, "Expected a discovery request for the missing external resource, on the original side only");
        assertTrue(missingRequest.getUri().endsWith("missing.json"));
        for (int i = 0; i < requests.size(); i++) {
            assertFalse(requests.get(i).getSide() == ResourceSide.UPDATED,
                    "The updated side has no such reference and must not be affected");
        }

        // Simulate acquisition: register a stub document at the resolved external URI.
        ObjectNode stub = MAPPER.createObjectNode();
        stub.put("openapi", "3.0.4");
        ObjectNode info = MAPPER.createObjectNode();
        info.put("title", "Stub");
        info.put("version", "1");
        stub.set("info", info);
        stub.set("paths", MAPPER.createObjectNode());
        OpenApiDocument stubDocument = (OpenApiDocument) Library.readDocument(stub);
        session.addResource(ResourceSide.ORIGINAL, new ResourceDocument(ResourceSide.ORIGINAL, missingRequest.getUri(), (RootCapable) stubDocument));

        List<ResourceRequest> requestsAfter = ResourceDiscovery.discover(session);
        for (int i = 0; i < requestsAfter.size(); i++) {
            assertFalse(requestsAfter.get(i).getUri().equals(missingRequest.getUri()) && requestsAfter.get(i).getSide() == ResourceSide.ORIGINAL,
                    "The now-acquired resource must not be re-requested");
        }
    }

    @Test
    void cancellationNotifiesListenersAndIsIdempotent() {
        CheckCancellation cancellation = new CheckCancellation();
        int[] callCount = new int[] { 0 };
        Runnable unregister = cancellation.onCancel(new Runnable() {
            @Override
            public void run() {
                callCount[0]++;
            }
        });

        assertFalse(cancellation.isCancelled());
        cancellation.cancel();
        assertTrue(cancellation.isCancelled());
        assertEquals(1, callCount[0]);

        cancellation.cancel();
        assertEquals(1, callCount[0], "A second cancel() must not notify listeners again");

        unregister.run();

        CheckCancellation late = new CheckCancellation();
        late.cancel();
        int[] lateCallCount = new int[] { 0 };
        late.onCancel(new Runnable() {
            @Override
            public void run() {
                lateCallCount[0]++;
            }
        });
        assertEquals(1, lateCallCount[0], "A listener registered after cancellation must be invoked immediately");
    }
}
