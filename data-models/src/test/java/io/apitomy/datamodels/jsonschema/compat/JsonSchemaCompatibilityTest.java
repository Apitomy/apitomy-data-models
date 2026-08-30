package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.jsonschema.JsonSchemaProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class JsonSchemaCompatibilityTest {

    private static final JsonSchemaCompatibilityChecker CHECKER =
            JsonSchemaCompatibilityChecker.builder()
                    .allowCrossVersionChecking(true)
                    .build();

    private static final String D7 = "\"$schema\": \"http://json-schema.org/draft-07/schema#\"";

    /**
     * Data-driven check over the shipped example catalog. Every enabled case is checked in each
     * direction present under {@code expected}: the {@code compatible} verdict, the
     * {@code unsupportedFeatures} count (when asserted), and the exact set of {@link DiffType}s that
     * fired.
     * <p>
     * The {@code diffTypes} assertion is a strict golden check: the emitted differences must match
     * the listed types exactly (an absent {@code diffTypes} means no differences are expected). The
     * catalog's {@code diffTypes} are regenerated from the checker by {@link ExampleCatalogSeeder},
     * so any change to which diffs fire is caught here and must be reconciled by re-seeding.
     */
    @Test
    public void testCompatibilityTestData() {
        var support = new CompatCaseSupport();
        var tests = CompatCaseSupport.readCatalog().get("tests");

        var failed = new ArrayList<String>();
        var passed = 0;
        var skipped = 0;

        for (var testCase : tests) {
            var id = testCase.get("id").asText();
            if (!testCase.path("enabled").asBoolean(true)) {
                skipped++;
                continue;
            }

            var original = CompatCaseSupport.schemaString(testCase.get("original"));
            var updated = CompatCaseSupport.schemaString(testCase.get("updated"));
            if (original == null || updated == null) {
                skipped++;
                continue;
            }

            var checker = support.checkerFor(testCase.get("config"), testCase.get("externalRefs"));
            var expected = testCase.get("expected");

            // Expected-error case: the check must throw with a message containing the given text.
            if (expected != null && expected.has("error")) {
                var expectedError = expected.get("error").asText();
                try {
                    checker.checkBackward(original, updated);
                    failed.add(id + " (expected error containing '" + expectedError + "' but none thrown)");
                } catch (JsonSchemaProcessingException e) {
                    if (e.getMessage().contains(expectedError)) {
                        passed++;
                    } else {
                        failed.add(id + " (expected message containing '" + expectedError
                                + "' but got '" + e.getMessage() + "')");
                    }
                } catch (Exception e) {
                    failed.add(id + " (expected JsonSchemaProcessingException but got "
                            + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
                }
                continue;
            }

            try {
                var problems = new ArrayList<String>();
                if (expected != null && expected.has("backward")) {
                    checkDirection(problems, "backward", expected.get("backward"),
                            checker.checkBackward(original, updated));
                }
                if (expected != null && expected.has("forward")) {
                    checkDirection(problems, "forward", expected.get("forward"),
                            checker.checkForward(original, updated));
                }
                if (problems.isEmpty()) {
                    passed++;
                } else {
                    failed.add(id + " " + problems);
                }
            } catch (Exception e) {
                failed.add(id + " (exception: " + e.getMessage() + ")");
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

    /**
     * Verifies one direction of a case against its expected sub-node ({@code backward}/{@code forward}):
     * the {@code compatible} verdict, the {@code unsupportedFeatures} count (when asserted), and that
     * the emitted differences match the listed {@code diffTypes} exactly (absent list ⇒ no differences).
     * Any mismatch is appended to {@code problems} rather than thrown, so a single case reports all
     * of its failures at once.
     */
    private static void checkDirection(List<String> problems, String direction, JsonNode exp,
                                       CompatibilityCheckResult result) {
        if (exp.has("compatible") && result.isCompatible() != exp.get("compatible").asBoolean()) {
            problems.add(direction + ": expected compatible=" + exp.get("compatible").asBoolean()
                    + " but was " + result.isCompatible());
        }
        if (exp.has("unsupportedFeatures")
                && result.getUnsupportedFeatures().size() != exp.get("unsupportedFeatures").asInt()) {
            problems.add(direction + ": expected " + exp.get("unsupportedFeatures").asInt()
                    + " unsupported feature(s) but was " + result.getUnsupportedFeatures().size());
        }
        Set<String> expectedTypes = new TreeSet<>();
        var diffTypes = exp.get("diffTypes");
        if (diffTypes != null) {
            diffTypes.forEach(n -> expectedTypes.add(n.asText()));
        }
        Set<String> actualTypes = result.getDifferences().stream()
                .map(d -> d.getDiffType().name())
                .collect(Collectors.toCollection(TreeSet::new));
        if (!actualTypes.equals(expectedTypes)) {
            problems.add(direction + ": expected diffTypes " + expectedTypes + " but was " + actualTypes);
        }
    }

    @Test
    public void testSimpleBackwardCompatible() {
        var original = "{%s, \"type\": \"string\", \"minLength\": 10}".formatted(D7);
        var updated = "{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7);
        var result = CHECKER.checkBackward(original, updated);
        Assertions.assertTrue(result.isCompatible(),
                "minLength decrease should be backward compatible. Diffs: " + result.getDifferences());
    }

    @Test
    public void testSimpleBackwardIncompatible() {
        var original = "{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7);
        var updated = "{%s, \"type\": \"string\", \"minLength\": 10}".formatted(D7);
        var result = CHECKER.checkBackward(original, updated);
        Assertions.assertFalse(result.isCompatible(),
                "minLength increase should be backward incompatible. Diffs: " + result.getDifferences());
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
}
