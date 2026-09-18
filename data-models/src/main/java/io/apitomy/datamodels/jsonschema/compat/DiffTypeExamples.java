package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import io.apitomy.datamodels.util.CollectionUtil;

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

    // Kept for error messages. The loader below must repeat the path as a literal,
    // because the transpiler inlines the resource at the call site and cannot
    // resolve a constant reference.
    private static final String RESOURCE = "/io/apitomy/datamodels/jsonschema/compat/compatibility-test-data.json";

    private static Map<DiffType, List<CompatibilityExample>> index;

    private DiffTypeExamples() {
    }

    /**
     * Returns the examples that demonstrate the given diff type.
     *
     * @param diffTypeName the name of the diff type to look up
     * @return an unmodifiable list of examples, empty if none is catalogued
     */
    static synchronized List<CompatibilityExample> get(String diffTypeName) {
        if (index == null) {
            index = load();
        }
        // Keyed by name rather than by the constant itself: the transpiler cannot pass
        // an enum's `this` to a parameter of its own type.
        return CollectionUtil.copyOfList(
                index.getOrDefault(DiffType.valueOf(diffTypeName), new ArrayList<>()));
    }

    private static Map<DiffType, List<CompatibilityExample>> load() {
        JsonNode root = JsonUtil.parseJSON(ResourceUtil.readResourceAsString(
                "/io/apitomy/datamodels/jsonschema/compat/compatibility-test-data.json"));
        JsonNode tests = root.get("tests");
        if (tests == null || !tests.isArray()) {
            throw new IllegalStateException(
                    "Example catalog is missing a 'tests' array: " + RESOURCE);
        }
        Map<DiffType, List<CompatibilityExample>> result = new EnumMap<>(DiffType.class);
        for (JsonNode testCase : tests) {
            indexCase(testCase, result);
        }
        return result;
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
