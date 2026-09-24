package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.jsonschema.ref.JsonPointer;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_SUBSCHEMA_NOT_COMPATIBLE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MINIMUM_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MAX_LENGTH_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MAX_LENGTH_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MIN_LENGTH_INCREASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The shape of a result: the tree of locations and the paths of the differences in it. The
 * catalogue test checks which difference types are reported; this checks where.
 */
public class DifferenceTreeTest {

    private static final String D7 = "\"$schema\": \"http://json-schema.org/draft-07/schema#\"";
    private static final String D2020 = "\"$schema\": \"https://json-schema.org/draft/2020-12/schema\"";

    private static final JsonSchemaCompatibilityChecker CHECKER =
            JsonSchemaCompatibilityChecker.builder().allowCrossVersionChecking(true).build();

    /**
     * A change of the object itself, two changes inside one property, and a compatible change
     * two levels down: each difference sits at its own location, and only locations with a
     * difference at or below them appear.
     */
    @Test
    public void testTreeMirrorsLocations() {
        var original = """
                {%s, "type": "object", "required": ["name"],
                 "properties": {
                   "name": {"type": "string", "minLength": 1, "maxLength": 10},
                   "tags": {"type": "array", "items": {"type": "string", "maxLength": 20}},
                   "id":   {"type": "integer"}}}""".formatted(D7);
        var updated = """
                {%s, "type": "object", "required": ["name", "email"],
                 "properties": {
                   "name": {"type": "string", "minLength": 2, "maxLength": 5},
                   "tags": {"type": "array", "items": {"type": "string", "maxLength": 30}},
                   "id":   {"type": "integer"}}}""".formatted(D7);
        var result = CHECKER.checkBackward(original, updated);

        var root = result.getRoot();
        assertEquals(JsonPointer.root(), root.getPathUpdated());
        assertTrue(root.getDifferences().stream().allMatch(d -> d.getPathUpdated().toString().startsWith("/required")));
        var memberAdded = root.getDifferences().stream()
                .filter(d -> d.getDiffType() == OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_ADDED).findFirst().orElseThrow();
        assertEquals("/required/1", memberAdded.getPathUpdated().toString(), "An added member points at its element");
        assertEquals("/required", memberAdded.getPathOriginal().toString(), "which the original does not have");
        assertEquals(2, root.getChildren().size(), "'id' is unchanged, so it has no node");

        var name = root.getChildren().get(0);
        assertEquals("/properties/name", name.getPathUpdated().toString());
        assertFalse(name.isCompatible());
        assertEquals(List.of(STRING_TYPE_MIN_LENGTH_INCREASED, STRING_TYPE_MAX_LENGTH_DECREASED),
                name.getDifferences().stream().map(Difference::getDiffType).toList());
        assertEquals("/properties/name/maxLength", name.getDifferences().get(1).getPathUpdated().toString());

        var tags = root.getChildren().get(1);
        assertEquals("/properties/tags", tags.getPathUpdated().toString());
        assertTrue(tags.isCompatible(), "A compatible nested change is reported too");
        assertTrue(tags.getDifferences().isEmpty());
        var items = tags.getChildren().get(0);
        assertOnlyDifference(items, STRING_TYPE_MAX_LENGTH_INCREASED, "/properties/tags/items/maxLength");

        assertFalse(root.isCompatible());
        assertEquals(root.getDifferences().size() + 3, root.flatten().size());
        assertEquals(new LinkedHashSet<>(root.flatten()), result.getDifferences());
    }

    /**
     * A comparison that only matches alternatives reports one difference where the keyword is,
     * not the scratch work behind it. A nested comparison that fails without a difference to
     * explain it reports the container at the nested location.
     */
    @Test
    public void testProbesAndFallbacksStayOnTheirLocation() {
        var original = """
                {%s, "properties": {
                   "choice": {"anyOf": [{"type": "string", "maxLength": 10}, {"type": "integer"}]},
                   "a/b": true}}""".formatted(D7);
        var updated = """
                {%s, "properties": {
                   "choice": {"anyOf": [{"type": "string", "maxLength": 5}, {"type": "integer"}]},
                   "a/b": {"type": "string"}}}""".formatted(D7);
        var root = CHECKER.checkBackward(original, updated).getRoot();

        var choice = root.getChildren().get(0);
        assertOnlyDifference(choice, COMBINED_TYPE_SUBSCHEMA_NOT_COMPATIBLE, "/properties/choice/anyOf");
        assertTrue(choice.getChildren().isEmpty(), "Probes never enter the tree");

        var escaped = root.getChildren().get(1);
        assertEquals("/properties/a~1b", escaped.getPathUpdated().toString());
        assertOnlyDifference(escaped, OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED, "/properties/a~1b");
    }

    /**
     * Compound conversion renames a few keywords; paths name them as each schema wrote them, so a
     * location's path resolves against the schema the user wrote.
     */
    @Test
    public void testPathsUseEachSchemasOwnKeywords() {
        var original = """
                {%s, "items": [{"maxLength": 10}],
                 "dependencies": {"a": {"maxLength": 10}},
                 "exclusiveMinimum": 5}""".formatted(D7);
        var updated = """
                {%s, "prefixItems": [{"maxLength": 5}],
                 "dependentSchemas": {"a": {"maxLength": 5}},
                 "exclusiveMinimum": 6}""".formatted(D2020);
        var differences = List.copyOf(CHECKER.checkBackward(original, updated).getDifferences());

        assertPaths(differences, STRING_TYPE_MAX_LENGTH_DECREASED, "/items/0/maxLength", "/prefixItems/0/maxLength");
        assertPaths(differences, STRING_TYPE_MAX_LENGTH_DECREASED, "/dependencies/a/maxLength",
                "/dependentSchemas/a/maxLength");
        assertPaths(differences, NUMBER_TYPE_MINIMUM_INCREASED, "/exclusiveMinimum", "/exclusiveMinimum");

        var tupleElement = CHECKER.checkBackward(original, original.replace("10", "3")).getRoot().getChildren().get(0);
        var parsed = (JsonSchema) Library.readRootFromJSONString(original);
        assertEquals("/items/0", tupleElement.getPathOriginal().toString());
        assertNotNull(tupleElement.getPathOriginal().evaluate(parsed));
    }

    private static void assertOnlyDifference(DifferenceNode node, DiffType type, String pathUpdated) {
        assertEquals(1, node.getDifferences().size(), () -> "Differences at " + node.getPathUpdated() + ": "
                + node.getDifferences());
        var difference = node.getDifferences().get(0);
        assertEquals(type, difference.getDiffType());
        assertEquals(pathUpdated, difference.getPathUpdated().toString());
    }

    private static void assertPaths(List<Difference> differences, DiffType type, String pathOriginal,
                                    String pathUpdated) {
        assertTrue(differences.stream().anyMatch(d -> d.getDiffType() == type
                        && d.getPathOriginal().toString().equals(pathOriginal)
                        && d.getPathUpdated().toString().equals(pathUpdated)),
                () -> type + " at " + pathOriginal + " / " + pathUpdated + " not in " + differences.stream()
                        .map(d -> d.getDiffType() + " " + d.getPathOriginal() + " / " + d.getPathUpdated()).toList());
    }
}
