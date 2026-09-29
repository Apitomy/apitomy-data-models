package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import io.apitomy.datamodels.jsonschema.compat.containment.DirectionalSchemaView;
import io.apitomy.datamodels.jsonschema.compat.containment.ExactDecimal;
import io.apitomy.datamodels.jsonschema.compat.containment.FormatRegistry;
import io.apitomy.datamodels.jsonschema.compat.containment.ObjectContainment;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialect;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaNormalizer;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;

/**
 * Directional (readOnly/writeOnly) requiredness projection and format
 * compatibility: excused properties dropped from a role's effective
 * {@code required} (including through {@code allOf}), OAS 2.0/3.0's strict
 * prohibition versus OAS 3.1/3.2's uncertain enforcement of a response-side
 * {@code writeOnly}, and the int32/int64/date-time/password format
 * compatibility table.
 */
class SchemaContainmentDirectionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode fixture() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/schema-direction.json");
        JsonNode root = MAPPER.readTree(in);
        in.close();
        return root;
    }

    @TestFactory
    Stream<DynamicTest> directionalCases() throws IOException {
        JsonNode cases = fixture().get("directionalCases");
        List<DynamicTest> tests = new ArrayList<DynamicTest>();
        Iterator<JsonNode> iterator = cases.elements();
        while (iterator.hasNext()) {
            JsonNode testCase = iterator.next();
            tests.add(DynamicTest.dynamicTest(testCase.get("id").asText(), () -> runDirectionalCase(testCase)));
        }
        return tests.stream();
    }

    private void runDirectionalCase(JsonNode testCase) {
        SchemaDialect dialect = SchemaDialect.valueOf(testCase.get("dialect").asText());
        HttpRole httpRole = HttpRole.valueOf(testCase.get("httpRole").asText());
        SchemaView schema = new SchemaView(testCase.get("schema"), dialect, "urn:test:resource", "");

        DirectionalSchemaView directional = DirectionalSchemaView.of(schema, httpRole, CompatibilityPolicy.defaults());

        if (testCase.has("expectedEnforcementCertain")) {
            assertEquals(testCase.get("expectedEnforcementCertain").asBoolean(), directional.isEnforcementCertain(),
                    testCase.get("id").asText());
        }

        SchemaView effective = directional.effectiveView();
        List<String> effectiveRequired = ObjectContainment.requiredNames(effective);
        List<String> expectedRequired = new ArrayList<String>();
        Iterator<JsonNode> expectedIterator = testCase.get("expectedEffectiveRequired").elements();
        while (expectedIterator.hasNext()) {
            expectedRequired.add(expectedIterator.next().asText());
        }

        assertEquals(expectedRequired.size(), effectiveRequired.size(), testCase.get("id").asText());
        for (int i = 0; i < expectedRequired.size(); i++) {
            assertTrue(effectiveRequired.contains(expectedRequired.get(i)),
                    testCase.get("id").asText() + ": expected " + expectedRequired.get(i));
        }
    }

    @TestFactory
    Stream<DynamicTest> formatCases() throws IOException {
        JsonNode cases = fixture().get("formatCases");
        List<DynamicTest> tests = new ArrayList<DynamicTest>();
        Iterator<JsonNode> iterator = cases.elements();
        while (iterator.hasNext()) {
            JsonNode testCase = iterator.next();
            tests.add(DynamicTest.dynamicTest(testCase.get("id").asText(), () -> runFormatCase(testCase)));
        }
        return tests.stream();
    }

    private void runFormatCase(JsonNode testCase) {
        String sourceFormat = textOrNull(testCase, "sourceFormat");
        String targetFormat = textOrNull(testCase, "targetFormat");
        FormatRegistry.FormatRelation expected = FormatRegistry.FormatRelation.valueOf(testCase.get("expected").asText());

        if (testCase.has("generalRelate") && testCase.get("generalRelate").asBoolean()) {
            assertEquals(expected, FormatRegistry.relate(sourceFormat, targetFormat), testCase.get("id").asText());
            return;
        }

        SchemaNormalizer.Bound sourceMinimum = bound(testCase, "sourceMinimum");
        SchemaNormalizer.Bound sourceMaximum = bound(testCase, "sourceMaximum");
        SchemaNormalizer.Bound targetMinimum = bound(testCase, "targetMinimum");
        SchemaNormalizer.Bound targetMaximum = bound(testCase, "targetMaximum");

        FormatRegistry.FormatRelation actual = FormatRegistry.relateNumericRange(sourceFormat, sourceMinimum, sourceMaximum,
                targetFormat, targetMinimum, targetMaximum);
        assertEquals(expected, actual, testCase.get("id").asText());
    }

    private static String textOrNull(JsonNode testCase, String field) {
        if (!testCase.has(field) || testCase.get(field).isNull()) {
            return null;
        }
        return testCase.get(field).asText();
    }

    private static SchemaNormalizer.Bound bound(JsonNode testCase, String field) {
        if (!testCase.has(field) || testCase.get(field).isNull()) {
            return null;
        }
        return SchemaNormalizer.Bound.of(ExactDecimal.parse(testCase.get(field).asText()), false);
    }
}
