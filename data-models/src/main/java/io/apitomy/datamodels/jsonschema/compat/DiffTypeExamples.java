package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Lazily-loaded, cached index of worked {@link CompatibilityExample}s keyed by {@link DiffType}.
 *
 * <p>Examples are sourced from the bundled example catalog ({@code compatibility-test-data.json},
 * beside this class) — the same file the compatibility test-suite runs against. Each catalog case
 * records, per direction, the {@link DiffType}s the checker emits ({@code expected.<dir>.diffTypes});
 * this class inverts that mapping so a {@link DiffType} can list the cases that demonstrate it.
 *
 * <p>Only <em>example-worthy</em> directions are indexed: a case must be {@code enabled}, not opted
 * out via {@code "example": false}, free of an {@code expected.error}, and the direction must emit at
 * least one diff type (a no-op "nothing changed" direction demonstrates nothing).
 *
 * <p>Kept separate from the {@link DiffType} enum so the enum stays free of Jackson and
 * resource-loading concerns. The index is built once, on first access, and cached; access is guarded
 * by {@code synchronized} since example lookups are rare.
 */
final class DiffTypeExamples {

    private static final String RESOURCE = "compatibility-test-data.json";

    private static Map<DiffType, List<CompatibilityExample>> index;

    private DiffTypeExamples() {
    }

    /**
     * Returns the examples that demonstrate the given diff type.
     *
     * @param diffType the diff type to look up
     * @return an unmodifiable list of examples, empty if none is catalogued
     */
    static synchronized List<CompatibilityExample> get(DiffType diffType) {
        if (index == null) {
            index = load();
        }
        return index.getOrDefault(diffType, List.of());
    }

    private static Map<DiffType, List<CompatibilityExample>> load() {
        var mapper = new ObjectMapper();
        try (InputStream in = DiffTypeExamples.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing example catalog resource: " + RESOURCE);
            }
            JsonNode root = mapper.readTree(in);
            JsonNode tests = root.get("tests");
            if (tests == null || !tests.isArray()) {
                throw new IllegalStateException(
                        "Example catalog is missing a 'tests' array: " + RESOURCE);
            }
            Map<DiffType, List<CompatibilityExample>> result = new EnumMap<>(DiffType.class);
            for (JsonNode testCase : tests) {
                indexCase(testCase, result);
            }
            result.replaceAll((k, v) -> Collections.unmodifiableList(v));
            return Collections.unmodifiableMap(result);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read example catalog: " + RESOURCE, e);
        }
    }

    private static void indexCase(JsonNode testCase,
                                  Map<DiffType, List<CompatibilityExample>> result) {
        if (!testCase.path("enabled").asBoolean(true)) {
            return;
        }
        if (!testCase.path("example").asBoolean(true)) {
            return;
        }
        JsonNode expected = testCase.get("expected");
        if (expected == null || expected.has("error")) {
            return;
        }
        JsonNode original = testCase.get("original");
        JsonNode updated = testCase.get("updated");
        if (original == null || updated == null) {
            return;
        }
        String id = testCase.path("id").asText();
        String originalSchema = original.toString();
        String updatedSchema = updated.toString();

        indexDirection(id, Direction.BACKWARD, expected.get("backward"),
                originalSchema, updatedSchema, result);
        indexDirection(id, Direction.FORWARD, expected.get("forward"),
                originalSchema, updatedSchema, result);
    }

    private static void indexDirection(String id, Direction direction, JsonNode dirNode,
                                       String originalSchema, String updatedSchema,
                                       Map<DiffType, List<CompatibilityExample>> result) {
        if (dirNode == null) {
            return;
        }
        JsonNode diffTypesNode = dirNode.get("diffTypes");
        if (diffTypesNode == null || !diffTypesNode.isArray() || diffTypesNode.isEmpty()) {
            return;
        }
        List<DiffType> diffTypes = new ArrayList<>(diffTypesNode.size());
        for (JsonNode name : diffTypesNode) {
            // Fail fast on typos/stale tags: every entry must name a real DiffType constant.
            diffTypes.add(DiffType.valueOf(name.asText()));
        }
        boolean compatible = dirNode.path("compatible").asBoolean();
        var example = new CompatibilityExample(id, direction, compatible,
                originalSchema, updatedSchema, diffTypes);
        for (DiffType diffType : diffTypes) {
            result.computeIfAbsent(diffType, k -> new ArrayList<>()).add(example);
        }
    }
}
