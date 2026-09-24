package io.apitomy.datamodels.openapi.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.Test;

import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentContext;
import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentResult;
import io.apitomy.datamodels.jsonschema.compat.containment.ContainmentVerdict;
import io.apitomy.datamodels.jsonschema.compat.containment.CoverageRegistry;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaContainment;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialect;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialectResolver;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaNormalizer;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;

/**
 * Dialect-aware schema view/coverage plumbing: true/false roots and nested
 * booleans, dialect resolution from a {@code $schema}/{@code jsonSchemaDialect}
 * URI, a custom vocabulary producing Unknown, OAS 3.0 {@code nullable} only
 * with a sibling {@code type}, legacy exclusive-bound normalization, and the
 * conservative default dispatch that a known-unsupported keyword must never
 * produce a vacuous Compatible result.
 */
class SchemaContainmentCoverageTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode fixture() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("fixtures/openapi-compat/schema-coverage.json");
        JsonNode root = MAPPER.readTree(in);
        in.close();
        return root;
    }

    private static ContainmentContext context() {
        return ContainmentContext.of(HttpRole.REQUEST, ProviderRole.INPUT, CheckDirection.BACKWARD, CompatibilityPolicy.defaults());
    }

    private static SchemaView view(JsonNode node, SchemaDialect dialect) {
        return new SchemaView(node, dialect, "urn:test:resource", "");
    }

    private static SchemaView objectView(SchemaDialect dialect, String... keyValuePairs) {
        ObjectNode object = MAPPER.createObjectNode();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            object.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return view(object, dialect);
    }

    @Test
    void trueAndFalseRootsFollowBooleanSchemaIdentities() {
        SchemaView trueSchema = view(BooleanNode.TRUE, SchemaDialect.DRAFT2020_12);
        SchemaView falseSchema = view(BooleanNode.FALSE, SchemaDialect.DRAFT2020_12);

        assertEquals(ContainmentVerdict.YES, SchemaContainment.compare(falseSchema, trueSchema, context()).getVerdict());
        assertEquals(ContainmentVerdict.YES, SchemaContainment.compare(falseSchema, falseSchema, context()).getVerdict());
        assertEquals(ContainmentVerdict.YES, SchemaContainment.compare(trueSchema, trueSchema, context()).getVerdict());

        ContainmentResult trueVsFalse = SchemaContainment.compare(trueSchema, falseSchema, context());
        assertEquals(ContainmentVerdict.NO, trueVsFalse.getVerdict());
        assertNotNull(trueVsFalse.getWitness());
    }

    @Test
    void nestedBooleanChildViewsAreReachedThroughSchemaViewNavigation() {
        ObjectNode object = MAPPER.createObjectNode();
        object.set("additionalProperties", BooleanNode.FALSE);
        SchemaView parent = view(object, SchemaDialect.DRAFT2020_12);

        SchemaView additionalProperties = parent.childView("additionalProperties");
        assertNotNull(additionalProperties);
        assertTrue(additionalProperties.isFalse());
    }

    @Test
    void dialectResolvesFromASchemaOrJsonSchemaDialectUri() throws Exception {
        JsonNode cases = fixture().get("dialectDefaults");
        for (JsonNode testCase : cases) {
            String schemaUri = testCase.get("schemaUri").asText();
            SchemaDialect expected = SchemaDialect.valueOf(testCase.get("expected").asText());
            assertEquals(expected, SchemaDialectResolver.fromSchemaUri(schemaUri), schemaUri);
        }
    }

    @Test
    void noSchemaUriLeavesDialectResolutionToTheCaller() {
        assertNull(SchemaDialectResolver.fromSchemaUri(null));
    }

    @Test
    void aCustomRequiredVocabularyKeywordProducesUnknown() {
        ObjectNode object = MAPPER.createObjectNode();
        object.put("type", "string");
        object.put("x-custom-vocab-keyword", "value");
        SchemaView withCustomKeyword = view(object, SchemaDialect.DRAFT2020_12);

        ContainmentResult result = SchemaContainment.compare(withCustomKeyword, withCustomKeyword, context());
        assertEquals(ContainmentVerdict.UNKNOWN, result.getVerdict());
        assertEquals(1, result.getEvidence().size());
        assertEquals("unrecognized-keyword", result.getEvidence().get(0).getRule());
    }

    @Test
    void oas30NullableOnlyWidensTypeWhenATypeSiblingIsPresent() {
        SchemaView withType = objectView(SchemaDialect.OAS30, "type", "string");
        ObjectNode withTypeNode = (ObjectNode) withType.getNode();
        withTypeNode.put("nullable", true);
        SchemaView withTypeAndNullable = view(withTypeNode, SchemaDialect.OAS30);

        List<String> effectiveTypes = SchemaNormalizer.normalizeEffectiveTypes(withTypeAndNullable);
        assertNotNull(effectiveTypes);
        assertTrue(effectiveTypes.contains("string"));
        assertTrue(effectiveTypes.contains("null"));

        ObjectNode withoutTypeNode = MAPPER.createObjectNode();
        withoutTypeNode.put("nullable", true);
        SchemaView withoutTypeButNullable = view(withoutTypeNode, SchemaDialect.OAS30);
        assertNull(SchemaNormalizer.normalizeEffectiveTypes(withoutTypeButNullable),
                "nullable with no sibling type must not invent a type constraint");
    }

    @Test
    void legacyExclusiveBoundsNormalizeIntoAValueExclusivityPair() {
        ObjectNode legacy = MAPPER.createObjectNode();
        legacy.put("minimum", 5);
        legacy.put("exclusiveMinimum", true);
        SchemaView legacyView = view(legacy, SchemaDialect.OAS30);

        SchemaNormalizer.Bound bound = SchemaNormalizer.normalizeMinimum(legacyView);
        assertNotNull(bound);
        assertTrue(bound.isExclusive());
        assertEquals(0, bound.getValue().compareTo(io.apitomy.datamodels.jsonschema.compat.containment.ExactDecimal.parse("5")));
    }

    @Test
    void modernIndependentExclusiveBoundsCollapseToTheTighterExactBound() {
        // minimum and exclusiveMinimum are independent numeric assertions in modern
        // dialects; as doubles these two particular values are indistinguishable,
        // but the inclusive minimum is exactly one unit tighter and must win.
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("minimum", 9007199254740993L);
        schema.put("exclusiveMinimum", 9007199254740992L);
        SchemaView schemaView = view(schema, SchemaDialect.DRAFT2020_12);

        SchemaNormalizer.Bound bound = SchemaNormalizer.normalizeMinimum(schemaView);
        assertNotNull(bound);
        assertEquals(false, bound.isExclusive());
        assertEquals(0, bound.getValue().compareTo(
                io.apitomy.datamodels.jsonschema.compat.containment.ExactDecimal.parse("9007199254740993")));
    }

    @Test
    void anUnsupportedKeywordCanNeverProduceAVacuousCompatibleResult() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("$dynamicRef", "#meta");
        SchemaView withDynamicRef = view(schema, SchemaDialect.DRAFT2020_12);
        SchemaView trueSchema = view(BooleanNode.TRUE, SchemaDialect.DRAFT2020_12);

        ContainmentResult result = SchemaContainment.compare(trueSchema, withDynamicRef, context());
        assertEquals(ContainmentVerdict.UNKNOWN, result.getVerdict());
    }

    @Test
    void coverageRegistryHasNoDefaultNoOpForAnUnknownKeyword() {
        assertNull(CoverageRegistry.classify(SchemaDialect.DRAFT2020_12, "x-totally-made-up"));
        assertEquals(CoverageRegistry.Category.ASSERTION,
                CoverageRegistry.classify(SchemaDialect.DRAFT2020_12, "type").getCategory());
        assertEquals(CoverageRegistry.Category.UNSUPPORTED,
                CoverageRegistry.classify(SchemaDialect.DRAFT2020_12, "$dynamicRef").getCategory());
    }
}
