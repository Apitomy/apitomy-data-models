package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class JsonSchemaCompatibilityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testCompatibilityTestData() throws Exception {
        var testData = readResource("compatibility-test-data.json");
        var root = MAPPER.readTree(testData);
        var tests = root.get("tests");

        var failed = new ArrayList<String>();
        var passed = 0;
        var skipped = 0;

        for (var testCase : tests) {
            var id = testCase.get("id").asText();
            var enabled = testCase.get("enabled").asBoolean();
            if (!enabled) {
                skipped++;
                continue;
            }

            var originalNode = testCase.get("original");
            var updatedNode = testCase.get("updated");
            var expectedCompat = testCase.get("compatibility").asText();

            var original = nodeToSchemaString(originalNode);
            var updated = nodeToSchemaString(updatedNode);

            if (original == null || updated == null) {
                skipped++;
                continue;
            }

            try {
                var backwardResult = JsonSchemaCompatibilityChecker
                        .checkBackwardCompatibility(original, updated);
                var forwardResult = JsonSchemaCompatibilityChecker
                        .checkBackwardCompatibility(updated, original);

                var backwardOk = backwardResult.foundAllDifferencesAreCompatible();
                var forwardOk = forwardResult.foundAllDifferencesAreCompatible();

                var success = switch (expectedCompat) {
                    case "backward" -> backwardOk && !forwardOk;
                    case "both" -> backwardOk && forwardOk;
                    case "none" -> !backwardOk && !forwardOk;
                    default -> throw new IllegalArgumentException("Unknown compatibility: " + expectedCompat);
                };

                if (success) {
                    passed++;
                } else {
                    var hasUnsupported = backwardResult.hasUnsupportedFeatures()
                            || forwardResult.hasUnsupportedFeatures();
                    failed.add("%s (expected=%s, backward=%s, forward=%s, unsupported=%s)"
                            .formatted(id, expectedCompat, backwardOk, forwardOk, hasUnsupported));
                }
            } catch (Exception e) {
                failed.add("%s (exception: %s)".formatted(id, e.getMessage()));
            }
        }

        System.out.printf("Results: %d passed, %d failed, %d skipped out of %d total%n",
                passed, failed.size(), skipped, tests.size());

        if (!failed.isEmpty()) {
            System.out.println("Failed tests:");
            failed.forEach(f -> System.out.println("  - " + f));
        }

        Assert.assertTrue(
                "%d test cases failed:\n%s".formatted(failed.size(), String.join("\n", failed)),
                failed.isEmpty());
    }

    private static final String D7 = "\"$schema\": \"http://json-schema.org/draft-07/schema#\"";

    @Test
    public void testSimpleBackwardCompatible() {
        var original = "{%s, \"type\": \"string\", \"minLength\": 10}".formatted(D7);
        var updated = "{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7);
        Assert.assertTrue(JsonSchemaCompatibilityChecker.isBackwardCompatible(original, updated));
    }

    @Test
    public void testAnchorRefResolution() {
        var schema = """
            {%s, "type": "object",
              "definitions": {
                "Address": { "$id": "#Address", "type": "object",
                  "properties": { "street": { "type": "string" } } }
              },
              "properties": { "home": { "$ref": "#Address" } }
            }
            """.formatted(D7);
        var doc = (io.apitomy.datamodels.models.jsonschema.JsonSchemaDocument)
                io.apitomy.datamodels.Library.readDocumentFromJSONString(schema);
        var traversal = io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefTraversal.withDefaults();
        var home = doc.getProperties().get("home").asJSchema();
        var ref = ((io.apitomy.datamodels.models.Referenceable) home).get$ref();
        Assert.assertEquals("#Address", ref);
        Assert.assertTrue("$id-based anchor ref should be resolvable",
                traversal.resolveRef(ref, home).isPresent());
    }

    @Test
    public void testSimpleBackwardIncompatible() {
        var original = "{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7);
        var updated = "{%s, \"type\": \"string\", \"minLength\": 10}".formatted(D7);
        Assert.assertFalse(JsonSchemaCompatibilityChecker.isBackwardCompatible(original, updated));
    }

    @Test
    public void testTypeChange() {
        var original = "{%s, \"type\": \"string\"}".formatted(D7);
        var updated = "{%s, \"type\": \"number\"}".formatted(D7);
        Assert.assertFalse(JsonSchemaCompatibilityChecker.isBackwardCompatible(original, updated));
    }

    @Test
    public void testFullyCompatible() {
        var original = """
                {%s, "type": "object", "properties": {"name": {"type": "string"}}}
                """.formatted(D7);
        var updated = """
                {%s, "type": "object", "properties": {"name": {"type": "string"}}}
                """.formatted(D7);
        Assert.assertTrue(JsonSchemaCompatibilityChecker.isFullyCompatible(original, updated));
    }

    private static String nodeToSchemaString(JsonNode node) {
        if (node.isBoolean()) {
            // Boolean schemas (true/false) — not supported as top-level documents yet
            return null;
        }
        return node.toString();
    }

    private static String readResource(String name) {
        try (var is = JsonSchemaCompatibilityTest.class.getResourceAsStream(name)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + name);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
