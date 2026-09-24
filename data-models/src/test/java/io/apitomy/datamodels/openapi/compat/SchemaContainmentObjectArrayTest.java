package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentContext;
import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentResult;
import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentVerdict;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaContainment;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialect;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;

/**
 * The T8 object/array containment test matrix, driven by
 * {@code schema-objects-arrays.json}: an open object gaining a named property
 * (both directions), requiredness in both directions, a removed optional
 * definition with open extras, an additionalProperties schema that already
 * covers a newly named property, homogeneous/tuple array items, the
 * prefixItems/items tail transition, length/uniqueness bounds, a tuple
 * position beyond the maximum reachable length, and contains count bounds.
 */
class SchemaContainmentObjectArrayTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode fixture() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/schema-objects-arrays.json");
        JsonNode root = MAPPER.readTree(in);
        in.close();
        return root;
    }

    private static ContainmentContext context() {
        return ContainmentContext.of(HttpRole.REQUEST, ProviderRole.INPUT, CheckDirection.BACKWARD, CompatibilityPolicy.defaults());
    }

    private static SchemaView view(JsonNode node) {
        return new SchemaView(node, SchemaDialect.DRAFT2020_12, "urn:test:resource", "");
    }

    @TestFactory
    List<DynamicTest> objectContainmentMatrix() throws Exception {
        return matrixFrom("objectCases");
    }

    @TestFactory
    List<DynamicTest> arrayContainmentMatrix() throws Exception {
        return matrixFrom("arrayCases");
    }

    private static List<DynamicTest> matrixFrom(String group) throws IOException {
        List<DynamicTest> tests = new ArrayList<DynamicTest>();
        for (JsonNode testCase : fixture().get(group)) {
            String id = testCase.get("id").asText();
            tests.add(DynamicTest.dynamicTest(id, () -> runCase(testCase)));
        }
        return tests;
    }

    private static void runCase(JsonNode testCase) {
        SchemaView source = view(testCase.get("source"));
        SchemaView target = view(testCase.get("target"));
        ContainmentVerdict expected = ContainmentVerdict.valueOf(testCase.get("expected").asText());

        ContainmentResult result = SchemaContainment.compare(source, target, context());
        assertEquals(expected, result.getVerdict(), testCase.toString());

        if (expected == ContainmentVerdict.NO) {
            assertNotNull(result.getWitness(), "A NO verdict must carry a validated witness: " + testCase);
        }
    }
}
