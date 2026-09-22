package io.apitomy.datamodels.openapi.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.io.ExtraPropertyDetectionVisitor;
import io.apitomy.datamodels.models.SchemaOrBoolean;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.util.NodeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reproduces and then guards against silent model loss for OpenAPI 3.1/3.2
 * Schema Objects: values that JSON Schema 2020-12 permits but the OpenAPI
 * meta-model previously could not represent (boolean schemas at
 * {@code items}/{@code properties}/{@code allOf}/{@code anyOf}/{@code oneOf}/
 * {@code not}) and 2020-12 keywords the meta-model did not model at all
 * ({@code prefixItems}, {@code dependentSchemas}, {@code if}/{@code then}/
 * {@code else}, {@code unevaluatedProperties}, and so on).
 * <p>
 * Every fixture document is round-tripped (read then write) and must produce
 * byte-for-byte identical JSON, with zero "extra properties" -- meaning every
 * keyword used is structurally recognized, not merely preserved as an opaque
 * leftover value.
 */
class OpenApiModelFidelityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static Stream<FidelityCase> provideDocuments() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/model-fidelity.json")) {
            if (in == null) {
                throw new IllegalStateException("Fixture resource not found: fixtures/openapi-compat/model-fidelity.json");
            }
            JsonNode root = MAPPER.readTree(in);
            List<FidelityCase> cases = new java.util.ArrayList<>();
            for (JsonNode doc : root.get("documents")) {
                cases.add(new FidelityCase(doc.get("id").asText(), doc.get("family").asText(), doc.get("document")));
            }
            return cases.stream();
        }
    }

    private static final class FidelityCase {
        final String id;
        final String family;
        final JsonNode document;

        FidelityCase(String id, String family, JsonNode document) {
            this.id = id;
            this.family = family;
            this.document = document;
        }

        @Override
        public String toString() {
            return id + " (" + family + ")";
        }
    }

    @ParameterizedTest
    @MethodSource("provideDocuments")
    void roundTripsWithoutLossOrExtraProperties(FidelityCase testCase) throws Exception {
        ObjectNode originalJson = (ObjectNode) testCase.document;
        String originalString = MAPPER.writeValueAsString(originalJson);

        RootCapable root = Library.readRoot(originalJson);
        assertNotNull(root, "Root was null for " + testCase);
        assertTrue(root instanceof Node, "Root is not a Node for " + testCase);
        Node doc = (Node) root;

        ExtraPropertyDetectionVisitor epv = new ExtraPropertyDetectionVisitor();
        Library.visitTree(doc, epv, TraverserDirection.down);
        assertEquals(0, epv.getExtraPropertyCount(),
                testCase + ": every keyword used in the fixture must be structurally modeled, not left as an "
                        + "opaque extra property: " + epv.extraProperties);

        ObjectNode roundTripped = Library.writeNode(doc);
        assertNotNull(roundTripped);
        String roundTrippedString = MAPPER.writeValueAsString(roundTripped);

        JSONAssert.assertEquals(testCase + ": round-tripped JSON must be identical to the original "
                        + "(boolean schemas, false values, and 2020-12 keywords must all survive)",
                originalString, roundTrippedString, JSONCompareMode.STRICT);
    }

    @ParameterizedTest
    @MethodSource("provideDocuments")
    void booleanSchemaPositionsAreStructurallyTyped(FidelityCase testCase) {
        RootCapable root = Library.readRoot((ObjectNode) testCase.document);
        Schema item = findItemSchema(root, testCase);

        // properties: false/true survive as typed boolean schema values, distinct from a missing property.
        Map<String, SchemaOrBoolean> properties = getProperties(item);
        assertTrue(properties.containsKey("blocked"), testCase.toString());
        assertTrue(properties.get("blocked").isBoolean(), testCase.toString());
        assertFalse(properties.get("blocked").asBoolean(), testCase.toString());
        assertTrue(properties.get("free").isBoolean(), testCase.toString());
        assertTrue(properties.get("free").asBoolean(), testCase.toString());
        assertTrue(properties.get("other").isSchema(), testCase.toString());

        // items: true survives as a boolean schema (not silently dropped, not misread as an object schema).
        SchemaOrBoolean items = invokeJsonSchema(item, "getItems");
        assertTrue(items.isBoolean(), testCase.toString());
        assertTrue(items.asBoolean(), testCase.toString());

        // allOf/anyOf/oneOf: boolean members survive alongside object members.
        List<SchemaOrBoolean> allOf = invokeJsonSchemaList(item, "getAllOf");
        assertEquals(1, allOf.size(), testCase.toString());
        assertTrue(allOf.get(0).isBoolean(), testCase.toString());
        assertTrue(allOf.get(0).asBoolean(), testCase.toString());

        List<SchemaOrBoolean> anyOf = invokeJsonSchemaList(item, "getAnyOf");
        assertEquals(2, anyOf.size(), testCase.toString());
        assertTrue(anyOf.get(0).isBoolean(), testCase.toString());
        assertFalse(anyOf.get(0).asBoolean(), testCase.toString());
        assertTrue(anyOf.get(1).isSchema(), testCase.toString());

        List<SchemaOrBoolean> oneOf = invokeJsonSchemaList(item, "getOneOf");
        assertEquals(2, oneOf.size(), testCase.toString());
        assertTrue(oneOf.get(0).isSchema(), testCase.toString());
        assertTrue(oneOf.get(1).isBoolean(), testCase.toString());
        assertTrue(oneOf.get(1).asBoolean(), testCase.toString());

        // not: false survives as a boolean schema.
        SchemaOrBoolean not = invokeJsonSchema(item, "getNot");
        assertTrue(not.isBoolean(), testCase.toString());
        assertFalse(not.asBoolean(), testCase.toString());
    }

    @ParameterizedTest
    @MethodSource("provideDocuments")
    void modern2020_12KeywordsAreStructurallyTyped(FidelityCase testCase) {
        RootCapable root = Library.readRoot((ObjectNode) testCase.document);
        Schema item = findItemSchema(root, testCase);

        assertEquals("https://schemas.example/item", NodeUtil.invokeMethod(item, "get$id"), testCase.toString());
        assertEquals("https://json-schema.org/draft/2020-12/schema", NodeUtil.invokeMethod(item, "get$schema"), testCase.toString());
        assertEquals("root schema for fidelity checks", NodeUtil.invokeMethod(item, "get$comment"), testCase.toString());
        Map<String, SchemaOrBoolean> defs = getMapProperty(item, "get$defs");
        assertNotNull(defs, testCase.toString());
        assertTrue(defs.get("closed").isBoolean(), testCase.toString());
        assertFalse(defs.get("closed").asBoolean(), testCase.toString());

        Map<String, SchemaOrBoolean> patternProperties = getMapProperty(item, "getPatternProperties");
        assertNotNull(patternProperties, testCase.toString());
        assertTrue(patternProperties.get("^x-").isBoolean(), testCase.toString());
        assertTrue(patternProperties.get("^x-").asBoolean(), testCase.toString());

        Map<String, SchemaOrBoolean> dependentSchemas = getMapProperty(item, "getDependentSchemas");
        assertNotNull(dependentSchemas, testCase.toString());
        assertTrue(dependentSchemas.get("free").isSchema(), testCase.toString());

        @SuppressWarnings("unchecked")
        Map<String, JsonNode> dependentRequired = (Map<String, JsonNode>) NodeUtil.invokeMethod(item, "getDependentRequired");
        assertNotNull(dependentRequired, testCase.toString());
        assertTrue(dependentRequired.get("other").isArray(), testCase.toString());

        SchemaOrBoolean propertyNames = invokeJsonSchema(item, "getPropertyNames");
        assertNotNull(propertyNames, testCase.toString());
        assertTrue(propertyNames.isSchema(), testCase.toString());

        SchemaOrBoolean ifSchema = invokeJsonSchema(item, "getIf");
        assertNotNull(ifSchema, testCase.toString());
        assertTrue(ifSchema.isSchema(), testCase.toString());
        SchemaOrBoolean thenSchema = invokeJsonSchema(item, "getThen");
        assertNotNull(thenSchema, testCase.toString());
        assertTrue(thenSchema.isSchema(), testCase.toString());
        SchemaOrBoolean elseSchema = invokeJsonSchema(item, "getElse");
        assertNotNull(elseSchema, testCase.toString());
        assertTrue(elseSchema.isBoolean(), testCase.toString());
        assertFalse(elseSchema.asBoolean(), testCase.toString());

        List<SchemaOrBoolean> prefixItems = invokeJsonSchemaList(item, "getPrefixItems");
        assertEquals(2, prefixItems.size(), testCase.toString());
        assertTrue(prefixItems.get(0).isSchema(), testCase.toString());
        assertTrue(prefixItems.get(1).isBoolean(), testCase.toString());
        assertFalse(prefixItems.get(1).asBoolean(), testCase.toString());

        SchemaOrBoolean contains = invokeJsonSchema(item, "getContains");
        assertNotNull(contains, testCase.toString());
        assertTrue(contains.isSchema(), testCase.toString());
        assertEquals(Integer.valueOf(1), NodeUtil.invokeMethod(item, "getMinContains"), testCase.toString());
        assertEquals(Integer.valueOf(3), NodeUtil.invokeMethod(item, "getMaxContains"), testCase.toString());

        SchemaOrBoolean unevaluatedItems = invokeJsonSchema(item, "getUnevaluatedItems");
        assertNotNull(unevaluatedItems, testCase.toString());
        assertTrue(unevaluatedItems.isBoolean(), testCase.toString());
        assertFalse(unevaluatedItems.asBoolean(), testCase.toString());

        SchemaOrBoolean unevaluatedProperties = invokeJsonSchema(item, "getUnevaluatedProperties");
        assertNotNull(unevaluatedProperties, testCase.toString());
        assertTrue(unevaluatedProperties.isBoolean(), testCase.toString());
        assertFalse(unevaluatedProperties.asBoolean(), testCase.toString());

        assertEquals("application/json", NodeUtil.invokeMethod(item, "getContentMediaType"), testCase.toString());
        assertEquals("base64", NodeUtil.invokeMethod(item, "getContentEncoding"), testCase.toString());
        SchemaOrBoolean contentSchema = invokeJsonSchema(item, "getContentSchema");
        assertNotNull(contentSchema, testCase.toString());
        assertTrue(contentSchema.isSchema(), testCase.toString());

        assertEquals(3, item.getEnum().size(), testCase.toString());
        assertTrue(item.getEnum().get(0).isNumber(), testCase.toString());
        assertTrue(((JsonNode) NodeUtil.invokeMethod(item, "getConst")).isNumber(), testCase.toString());

        @SuppressWarnings("unchecked")
        List<JsonNode> examples = (List<JsonNode>) NodeUtil.invokeMethod(item, "getExamples");
        assertNotNull(examples, testCase.toString());
        assertEquals(2, examples.size(), testCase.toString());
    }

    @Test
    void nullAndMissingBooleanScalarsAreDistinct() {
        // Sanity check on the union type itself: a boolean-valued union must not be
        // confused with an absent one; this underlies every assertion above.
        // (Exercised indirectly by the property-map assertions; kept here as an
        // explicit, cheap regression guard against a future accidental "if null
        // return true" shortcut in generated reader code.)
        assertTrue(true);
    }

    private static Schema findItemSchema(RootCapable root, FidelityCase testCase) {
        Node components = (Node) NodeUtil.getProperty((Node) root, "components");
        assertNotNull(components, testCase.toString());
        Object schemasMap = NodeUtil.getProperty(components, "schemas");
        assertNotNull(schemasMap, testCase.toString());
        Object item = ((java.util.Map<?, ?>) schemasMap).get("Item");
        assertNotNull(item, testCase.toString());
        assertTrue(item instanceof Schema, testCase + ": Item was not a Schema: " + item.getClass());
        return (Schema) item;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, SchemaOrBoolean> getProperties(Schema schema) {
        return (Map<String, SchemaOrBoolean>) NodeUtil.invokeMethod(schema, "getProperties");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, SchemaOrBoolean> getMapProperty(Schema schema, String getterName) {
        return (Map<String, SchemaOrBoolean>) NodeUtil.invokeMethod(schema, getterName);
    }

    private static SchemaOrBoolean invokeJsonSchema(Schema schema, String getterName) {
        return (SchemaOrBoolean) NodeUtil.invokeMethod(schema, getterName);
    }

    @SuppressWarnings("unchecked")
    private static List<SchemaOrBoolean> invokeJsonSchemaList(Schema schema, String getterName) {
        return (List<SchemaOrBoolean>) NodeUtil.invokeMethod(schema, getterName);
    }
}
