package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Maintenance tool (NOT part of the normal test run) that regenerates the modern
 * {@code expected} block — {@code compatible} verdict plus {@code diffTypes} tags — for every
 * case in the example catalog by running the checker over each case in both directions.
 *
 * <p>The seeded {@code diffTypes} both drive {@link DiffType#getExamples()} and act as golden
 * assertions in {@link JsonSchemaCompatibilityTest} (which asserts the emitted differences
 * contain the tagged types). Re-run this after an <em>intentional</em> change to which diffs fire:
 *
 * <pre>
 *   JAVA_HOME=/usr/lib/jvm/temurin-21-jdk \
 *     mvn test -pl data-models -Dtest=ExampleCatalogSeeder -DseedCatalog=true \
 *       -Dsurefire.failIfNoSpecifiedTests=false
 * </pre>
 *
 * <p>It rewrites {@code src/main/resources/.../compatibility-test-data.json} in place; review the
 * diff before committing. Empty {@code diffTypes} are omitted; {@code error} expectations and all
 * non-outcome fields (id, enabled, example, original, updated, config, externalRefs, comments) are
 * preserved in their original order.
 */
public class ExampleCatalogSeeder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Path OUTPUT = Path.of(
            "src/main/resources/io/apitomy/datamodels/jsonschema/compat/compatibility-test-data.json");

    @Test
    public void seed() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("seedCatalog"),
                "set -DseedCatalog=true to regenerate the example catalog");

        var support = new CompatCaseSupport();
        var root = (ObjectNode) CompatCaseSupport.readCatalog();
        var tests = (ArrayNode) root.get("tests");

        var seededTags = 0;
        for (int i = 0; i < tests.size(); i++) {
            var testCase = (ObjectNode) tests.get(i);
            var expected = computeExpected(support, testCase);
            tests.set(i, rebuildWithExpected(testCase, expected));
            if (expected.has("backward") && expected.get("backward").has("diffTypes")) seededTags++;
            if (expected.has("forward") && expected.get("forward").has("diffTypes")) seededTags++;
        }

        var printer = new DefaultPrettyPrinter();
        var indenter = new DefaultIndenter("  ", "\n");
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
        var json = MAPPER.writer(printer).writeValueAsString(root) + "\n";

        Files.writeString(OUTPUT, json);
        System.out.printf("Seeded %d cases (%d tagged directions) -> %s%n",
                tests.size(), seededTags, OUTPUT.toAbsolutePath());
    }

    /** Computes the {@code expected} block for a case, preserving {@code error} expectations. */
    private ObjectNode computeExpected(CompatCaseSupport support, ObjectNode testCase) {
        var existing = testCase.get("expected");
        if (existing != null && existing.has("error")) {
            return existing.deepCopy();
        }

        var original = CompatCaseSupport.schemaString(testCase.get("original"));
        var updated = CompatCaseSupport.schemaString(testCase.get("updated"));

        if (original == null || updated == null) {
            // Un-runnable (boolean-root) case: fall back to the legacy compatibility verdict.
            return fromLegacy(testCase.get("compatibility"));
        }

        var checker = support.checkerFor(testCase.get("config"), testCase.get("externalRefs"));
        var expected = MAPPER.createObjectNode();
        expected.set("backward", direction(checker.checkBackward(original, updated)));
        expected.set("forward", direction(checker.checkForward(original, updated)));
        return expected;
    }

    /** Renders one direction's outcome: {@code compatible} plus sorted, de-duplicated {@code diffTypes}. */
    private ObjectNode direction(CompatibilityCheckResult result) {
        var node = MAPPER.createObjectNode();
        node.put("compatible", result.isCompatible());
        var names = result.getDifferences().stream()
                .map(d -> d.getDiffType().name())
                .distinct()
                .sorted()
                .toList();
        if (!names.isEmpty()) {
            var arr = node.putArray("diffTypes");
            names.forEach(arr::add);
        }
        return node;
    }

    private ObjectNode fromLegacy(JsonNode compatibility) {
        var expected = MAPPER.createObjectNode();
        var value = compatibility != null ? compatibility.asText() : "";
        boolean backward = switch (value) {
            case "backward", "both" -> true;
            default -> false;
        };
        boolean forward = "both".equals(value);
        expected.putObject("backward").put("compatible", backward);
        expected.putObject("forward").put("compatible", forward);
        return expected;
    }

    /**
     * Rebuilds the case node preserving original field order, replacing the legacy
     * {@code compatibility} (or prior {@code expected}) with the freshly computed {@code expected}.
     */
    private ObjectNode rebuildWithExpected(ObjectNode original, ObjectNode expected) {
        var rebuilt = MAPPER.createObjectNode();
        boolean expectedPlaced = false;
        for (Iterator<Map.Entry<String, JsonNode>> it = original.fields(); it.hasNext(); ) {
            var entry = it.next();
            var name = entry.getKey();
            if (name.equals("compatibility") || name.equals("expected")) {
                if (!expectedPlaced) {
                    rebuilt.set("expected", expected);
                    expectedPlaced = true;
                }
                continue;
            }
            rebuilt.set(name, entry.getValue());
        }
        if (!expectedPlaced) {
            rebuilt.set("expected", expected);
        }
        return rebuilt;
    }
}
