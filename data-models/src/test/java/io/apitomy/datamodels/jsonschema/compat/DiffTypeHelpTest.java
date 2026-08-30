package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class DiffTypeHelpTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String RESOURCE = "difftype-help.json";

    private JsonNode readManifest() throws IOException {
        try (InputStream in = DiffType.class.getResourceAsStream(RESOURCE)) {
            Assertions.assertNotNull(in, "help manifest resource must exist: " + RESOURCE);
            return MAPPER.readTree(in);
        }
    }

    @Test
    public void manifestHasExpectedEnvelope() throws IOException {
        JsonNode root = readManifest();
        Assertions.assertEquals(1, root.path("version").asInt(-1),
                "manifest 'version' must be 1");
        Assertions.assertTrue(root.path("help").isObject(),
                "manifest must contain a 'help' object");
        Assertions.assertTrue(root.get("help").size() > 0,
                "manifest 'help' object must not be empty");
    }

    @Test
    public void everyKeyIsAValidDiffTypeName() throws IOException {
        JsonNode help = readManifest().get("help");
        var names = help.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            // Throws IllegalArgumentException if the key is not a real DiffType constant.
            Assertions.assertDoesNotThrow(() -> DiffType.valueOf(name),
                    "help key must name a real DiffType constant: " + name);
        }
    }

    @Test
    public void everyManifestKeyResolvesToNonBlankHelp() throws IOException {
        JsonNode help = readManifest().get("help");
        var names = help.fieldNames();
        while (names.hasNext()) {
            DiffType type = DiffType.valueOf(names.next());
            Optional<String> text = type.getHelp();
            Assertions.assertTrue(text.isPresent(),
                    "curated type must return help: " + type);
            Assertions.assertFalse(text.get().isBlank(),
                    "help text must not be blank: " + type);
        }
    }

    @Test
    public void curatedConstantsReturnHelp() {
        // A representative curated entry, including the subtle additionalProperties-interaction case.
        Assertions.assertTrue(
                DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_ADDED.getHelp().isPresent());
        Assertions.assertTrue(
                DiffType.OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES
                        .getHelp().isPresent());
        Assertions.assertTrue(DiffType.CONST_TYPE_VALUE_CHANGED.getHelp().isPresent());
    }

    @Test
    public void nonCuratedConstantReturnsEmpty() {
        // A real, emitted diff type that is intentionally not part of the curated help set.
        Assertions.assertTrue(
                DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_REMOVED.getHelp().isEmpty());
    }

    @Test
    public void differenceDelegatesToDiffType() {
        DiffType type = DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_ADDED;
        Difference diff = new Difference(type, "#/foo", "#/foo", "{}", "{}");
        Assertions.assertEquals(type.getShortDescription(), diff.getShortDescription());
        Assertions.assertEquals(type.getHelp(), diff.getHelp());
        Assertions.assertTrue(diff.getHelp().isPresent());
    }
}
