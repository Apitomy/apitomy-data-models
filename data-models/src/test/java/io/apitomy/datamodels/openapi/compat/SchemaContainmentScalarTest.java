package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

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

import java.util.ArrayList;
import java.util.List;

/**
 * The T7 scalar containment test matrix, driven by {@code schema-scalars.json}:
 * type-set inclusion, enum/const structural equality, exact numeric intervals,
 * length ranges, and exact multipleOf, each with a validated witness for a NO
 * verdict, plus reordered-object-enum-value equality, null-vs-missing, and
 * the T5 near-integer/adjacent-large-bound cases carried into a full schema
 * comparison rather than only the arithmetic-helper level.
 */
class SchemaContainmentScalarTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode fixture() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/schema-scalars.json");
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
    List<DynamicTest> scalarContainmentMatrix() throws Exception {
        List<DynamicTest> tests = new ArrayList<DynamicTest>();
        for (JsonNode testCase : fixture().get("cases")) {
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
