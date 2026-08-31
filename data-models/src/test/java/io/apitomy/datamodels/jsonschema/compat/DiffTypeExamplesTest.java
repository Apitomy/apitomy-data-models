package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffTypeExamplesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> DIRECTIONS = List.of("backward", "forward");

    @Test
    void everyTaggedDiffTypeHasExamplesContainingIt() {
        Set<String> tagged = new LinkedHashSet<>();
        for (JsonNode testCase : CompatCaseSupport.readCatalog().get("tests")) {
            JsonNode expected = testCase.get("expected");
            if (expected == null) {
                continue;
            }
            for (String dir : DIRECTIONS) {
                JsonNode diffTypes = expected.path(dir).get("diffTypes");
                if (diffTypes != null) {
                    diffTypes.forEach(n -> tagged.add(n.asText()));
                }
            }
        }
        assertFalse(tagged.isEmpty(), "catalog should carry tagged diffTypes");
        for (String name : tagged) {
            DiffType diffType = DiffType.valueOf(name);
            List<CompatibilityExample> examples = diffType.getExamples();
            assertFalse(examples.isEmpty(), "expected at least one example for " + name);
            assertTrue(examples.stream().allMatch(e -> e.getDiffTypes().contains(diffType)),
                    "every example indexed under " + name + " must list it in getDiffTypes()");
        }
    }

    @Test
    void everyHelpTypeHasAtLeastOneExample() {
        // C42e coverage guarantee: any DiffType worth a curated --explain help entry must also
        // carry at least one worked example, so users always have something concrete to look at.
        List<String> missing = new ArrayList<>();
        for (DiffType diffType : DiffType.values()) {
            if (diffType.getHelp().isPresent() && diffType.getExamples().isEmpty()) {
                missing.add(diffType.name());
            }
        }
        assertTrue(missing.isEmpty(),
                "DiffTypes with curated help but no tagged example: " + missing);
    }

    @Test
    void examplesAreWellFormed() throws Exception {
        for (DiffType diffType : DiffType.values()) {
            for (CompatibilityExample example : diffType.getExamples()) {
                assertNotNull(example.getId());
                assertFalse(example.getId().isBlank(), "example id must not be blank");
                assertNotNull(example.getDirection());
                assertFalse(example.getDiffTypes().isEmpty(), "example must demonstrate ≥1 diff type");
                JsonNode original = MAPPER.readTree(example.getOriginalSchema());
                JsonNode updated = MAPPER.readTree(example.getUpdatedSchema());
                assertTrue(original.isObject() || original.isBoolean(), "original must be a JSON schema");
                assertTrue(updated.isObject() || updated.isBoolean(), "updated must be a JSON schema");
            }
        }
    }

    @Test
    void distinctExampleCountMatchesTaggedDirections() {
        int taggedDirections = 0;
        for (JsonNode testCase : CompatCaseSupport.readCatalog().get("tests")) {
            JsonNode expected = testCase.get("expected");
            if (expected == null || expected.has("error")) {
                continue;
            }
            if (!testCase.path("enabled").asBoolean(true) || !testCase.path("example").asBoolean(true)) {
                continue;
            }
            if (testCase.get("original") == null || testCase.get("updated") == null) {
                continue;
            }
            for (String dir : DIRECTIONS) {
                JsonNode diffTypes = expected.path(dir).get("diffTypes");
                if (diffTypes != null && !diffTypes.isEmpty()) {
                    taggedDirections++;
                }
            }
        }

        Set<CompatibilityExample> distinct = new HashSet<>();
        for (DiffType diffType : DiffType.values()) {
            distinct.addAll(diffType.getExamples());
        }
        assertEquals(taggedDirections, distinct.size(),
                "one example per tagged direction, shared across its diff-type keys");
    }

    @Test
    void differenceDelegatesToDiffType() {
        DiffType diffType = DiffType.SUBSCHEMA_TYPE_CHANGED;
        Difference difference = new Difference(diffType, "#", "#", "{}", "{}");
        assertEquals(diffType.getExamples(), difference.getExamples());
        assertFalse(difference.getExamples().isEmpty(), "SUBSCHEMA_TYPE_CHANGED should have examples");
    }
}
