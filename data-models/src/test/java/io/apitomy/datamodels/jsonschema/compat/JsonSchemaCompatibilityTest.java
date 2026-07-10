package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apitomy.datamodels.jsonschema.JsonSchemaProcessingException;
import io.apitomy.datamodels.jsonschema.ref.AnchorFragmentResolver;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefDereferencer;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefResolverChain;
import io.apitomy.datamodels.jsonschema.ref.MapResourceResolver;
import io.apitomy.datamodels.jsonschema.ref.PointerFragmentResolver;
import io.apitomy.datamodels.jsonschema.ref.UnresolvableRefStrategy;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JsonSchemaCompatibilityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final JsonSchemaCompatibilityChecker CHECKER =
            JsonSchemaCompatibilityChecker.builder()
                    .allowCrossVersionChecking(true)
                    .build();

    // Cache checkers by config to reuse instances
    private final Map<String, JsonSchemaCompatibilityChecker> checkerCache = new HashMap<>();

    private JsonSchemaCompatibilityChecker getChecker(JsonNode configNode, JsonNode externalRefsNode) {
        // Build cache key from config + externalRefs
        String cacheKey = (configNode != null ? configNode.toString() : "default")
                + (externalRefsNode != null ? externalRefsNode.toString() : "");

        return checkerCache.computeIfAbsent(cacheKey, k -> {
            var checkerBuilder = JsonSchemaCompatibilityChecker.builder();

            // Apply config
            if (configNode != null) {
                if (configNode.has("allowCrossVersionChecking"))
                    checkerBuilder.allowCrossVersionChecking(configNode.get("allowCrossVersionChecking").asBoolean());
            } else {
                checkerBuilder.allowCrossVersionChecking(true); // default for tests
            }

            // Build dereferencer with ref resolver and strategy
            var derefBuilder = JsonSchemaRefDereferencer.builder();

            if (configNode != null && configNode.has("onUnresolvableRef")) {
                derefBuilder.onUnresolvableRef(
                        UnresolvableRefStrategy.valueOf(configNode.get("onUnresolvableRef").asText()));
            }

            if (externalRefsNode != null && externalRefsNode.isObject()) {
                var mapBuilder = MapResourceResolver.builder();
                externalRefsNode.fields().forEachRemaining(entry ->
                        mapBuilder.addSchema(entry.getKey(), entry.getValue().toString()));

                derefBuilder.refResolver(JsonSchemaRefResolverChain.builder()
                        .addFragmentResolver(new PointerFragmentResolver())
                        .addFragmentResolver(new AnchorFragmentResolver())
                        .addResourceResolver(mapBuilder.build())
                        .build());
            }

            checkerBuilder.dereferencer(derefBuilder.build());
            return checkerBuilder.build();
        });
    }

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

            var original = nodeToSchemaString(originalNode);
            var updated = nodeToSchemaString(updatedNode);

            if (original == null || updated == null) {
                skipped++;
                continue;
            }

            var configNode = testCase.get("config");
            var externalRefsNode = testCase.get("externalRefs");
            var expectedNode = testCase.get("expected");

            var checker = getChecker(configNode, externalRefsNode);

            // Check for expected error
            if (expectedNode != null && expectedNode.has("error")) {
                var expectedErrorMessage = expectedNode.get("error").asText();
                try {
                    checker.checkBackward(original, updated);
                    failed.add(id + " (expected error containing '" + expectedErrorMessage + "' but none thrown)");
                } catch (JsonSchemaProcessingException e) {
                    if (e.getMessage().contains(expectedErrorMessage)) {
                        passed++;
                    } else {
                        failed.add(id + " (expected message containing '" + expectedErrorMessage
                                + "' but got '" + e.getMessage() + "')");
                    }
                } catch (Exception e) {
                    failed.add(id + " (expected JsonSchemaProcessingException but got "
                            + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
                }
                continue;
            }

            // Check with expected result structure
            if (expectedNode != null) {
                try {
                    boolean success = true;
                    if (expectedNode.has("backward")) {
                        var result = checker.checkBackward(original, updated);
                        var exp = expectedNode.get("backward");
                        if (exp.has("compatible") && result.isCompatible() != exp.get("compatible").asBoolean()) {
                            success = false;
                        }
                        if (exp.has("unsupportedFeatures") && result.getUnsupportedFeatures().size() != exp.get("unsupportedFeatures").asInt()) {
                            success = false;
                        }
                    }
                    if (expectedNode.has("forward")) {
                        var result = checker.checkForward(original, updated);
                        var exp = expectedNode.get("forward");
                        if (exp.has("compatible") && result.isCompatible() != exp.get("compatible").asBoolean()) {
                            success = false;
                        }
                    }
                    if (success) passed++;
                    else failed.add(id + " (expected result mismatch)");
                } catch (Exception e) {
                    failed.add(id + " (exception: " + e.getMessage() + ")");
                }
                continue;
            }

            // Legacy mode: use "compatibility" field
            var expectedCompat = testCase.get("compatibility").asText();

            try {
                var backwardResult = checker.checkBackward(original, updated);
                var forwardResult = checker.checkForward(original, updated);

                var backwardOk = backwardResult.isCompatible();
                var forwardOk = forwardResult.isCompatible();

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
            } catch (IllegalArgumentException e) {
                throw e;
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

        Assertions.assertTrue(
                failed.isEmpty(),
                "%d test cases failed:\n%s".formatted(failed.size(), String.join("\n", failed)));
    }

    private static final String D7 = "\"$schema\": \"http://json-schema.org/draft-07/schema#\"";

    @Test
    public void testSimpleBackwardCompatible() {
        var original = "{%s, \"type\": \"string\", \"minLength\": 10}".formatted(D7);
        var updated = "{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7);
        Assertions.assertTrue(CHECKER.checkBackward(original, updated).isCompatible());
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
        var doc = (io.apitomy.datamodels.models.jsonschema.JFullSchema)
                io.apitomy.datamodels.Library.readRootFromJSONString(schema);
        var traversal = io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefTraversal.withDefaults();
        var home = doc.getProperties().get("home").asFullSchema();
        var ref = ((io.apitomy.datamodels.models.Referenceable) home).get$ref();
        Assertions.assertEquals("#Address", ref);
        Assertions.assertTrue(traversal.resolveRef(ref, home).isPresent(),
                "$id-based anchor ref should be resolvable");
    }

    @Test
    public void testSimpleBackwardIncompatible() {
        var original = "{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7);
        var updated = "{%s, \"type\": \"string\", \"minLength\": 10}".formatted(D7);
        Assertions.assertFalse(CHECKER.checkBackward(original, updated).isCompatible());
    }

    @Test
    public void testTypeChange() {
        var original = "{%s, \"type\": \"string\"}".formatted(D7);
        var updated = "{%s, \"type\": \"number\"}".formatted(D7);
        Assertions.assertFalse(CHECKER.checkBackward(original, updated).isCompatible());
    }

    @Test
    public void testFullyCompatible() {
        var original = """
                {%s, "type": "object", "properties": {"name": {"type": "string"}}}
                """.formatted(D7);
        var updated = """
                {%s, "type": "object", "properties": {"name": {"type": "string"}}}
                """.formatted(D7);
        Assertions.assertTrue(CHECKER.checkFull(original, updated).isFullyCompatible());
    }

    // ======================== Compound traverser tests ========================

    @Test
    public void testCompoundTraverserTestData() throws Exception {
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

            var original = nodeToSchemaString(originalNode);
            var updated = nodeToSchemaString(updatedNode);

            if (original == null || updated == null) {
                skipped++;
                continue;
            }

            var configNode = testCase.get("config");
            var externalRefsNode = testCase.get("externalRefs");
            var expectedNode = testCase.get("expected");

            var checker = getChecker(configNode, externalRefsNode);

            // Check for expected error
            if (expectedNode != null && expectedNode.has("error")) {
                var expectedErrorMessage = expectedNode.get("error").asText();
                try {
                    checker.checkBackward(original, updated);
                    failed.add(id + " (expected error containing '" + expectedErrorMessage + "' but none thrown)");
                } catch (JsonSchemaProcessingException e) {
                    if (e.getMessage().contains(expectedErrorMessage)) {
                        passed++;
                    } else {
                        failed.add(id + " (expected message containing '" + expectedErrorMessage
                                + "' but got '" + e.getMessage() + "')");
                    }
                } catch (Exception e) {
                    failed.add(id + " (expected JsonSchemaProcessingException but got "
                            + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
                }
                continue;
            }

            // Check with expected result structure
            if (expectedNode != null) {
                try {
                    boolean success = true;
                    if (expectedNode.has("backward")) {
                        var result = checker.checkBackward(original, updated);
                        var exp = expectedNode.get("backward");
                        if (exp.has("compatible") && result.isCompatible() != exp.get("compatible").asBoolean()) {
                            success = false;
                        }
                        if (exp.has("unsupportedFeatures") && result.getUnsupportedFeatures().size() != exp.get("unsupportedFeatures").asInt()) {
                            success = false;
                        }
                    }
                    if (expectedNode.has("forward")) {
                        var result = checker.checkForward(original, updated);
                        var exp = expectedNode.get("forward");
                        if (exp.has("compatible") && result.isCompatible() != exp.get("compatible").asBoolean()) {
                            success = false;
                        }
                    }
                    if (success) passed++;
                    else failed.add(id + " (expected result mismatch)");
                } catch (Exception e) {
                    failed.add(id + " (exception: " + e.getMessage() + ")");
                }
                continue;
            }

            // Legacy mode: use "compatibility" field
            var expectedCompat = testCase.get("compatibility").asText();

            try {
                var backwardResult = checker.checkBackward(original, updated);
                var forwardResult = checker.checkForward(original, updated);

                var backwardOk = backwardResult.isCompatible();
                var forwardOk = forwardResult.isCompatible();

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
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                failed.add("%s (exception: %s)".formatted(id, e.getMessage()));
            }
        }

        System.out.printf("[CompoundTraverser] Results: %d passed, %d failed, %d skipped out of %d total%n",
                passed, failed.size(), skipped, tests.size());

        if (!failed.isEmpty()) {
            System.out.println("[CompoundTraverser] Failed tests:");
            failed.forEach(f -> System.out.println("  - " + f));
        }

        Assertions.assertTrue(
                failed.isEmpty(),
                "[CompoundTraverser] %d test cases failed:\n%s".formatted(failed.size(), String.join("\n", failed)));
    }

    @Test
    public void testCompoundTraverserSimpleBackwardCompatible() {
        var original = "{%s, \"type\": \"string\", \"minLength\": 10}".formatted(D7);
        var updated = "{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7);
        var result = CHECKER.checkBackward(original, updated);
        Assertions.assertTrue(result.isCompatible(),
                "minLength decrease should be backward compatible. Diffs: " + result.getDifferences());
    }

    @Test
    public void testCompoundTraverserSimpleBackwardIncompatible() {
        var original = "{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7);
        var updated = "{%s, \"type\": \"string\", \"minLength\": 10}".formatted(D7);
        var result = CHECKER.checkBackward(original, updated);
        Assertions.assertFalse(result.isCompatible(),
                "minLength increase should be backward incompatible. Diffs: " + result.getDifferences());
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
