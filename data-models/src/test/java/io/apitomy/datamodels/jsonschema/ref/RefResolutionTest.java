package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Smoke tests for JSON Schema reference resolution — resolvers, traversal, and cycle detection.
 */
public class RefResolutionTest {

    private static final String D7 = "\"$schema\": \"http://json-schema.org/draft-07/schema#\"";

    private static JFullSchema parse(String json) {
        return (JFullSchema) Library.readRootFromJSONString(json);
    }

    @Test
    public void testLocalPointerResolution() {
        var doc = parse("""
            {%s, "type": "object",
             "definitions": {"Addr": {"type": "object", "properties": {"street": {"type": "string"}}}},
             "properties": {"home": {"$ref": "#/definitions/Addr"}}}
            """.formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();
        var result = traversal.resolveRef("#/definitions/Addr", doc);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().isAttached());
        Assertions.assertTrue(result.get() instanceof JFullSchema);
    }

    @Test
    public void testAnchorResolution() {
        var doc = parse("""
            {%s, "type": "object",
             "definitions": {"Addr": {"$id": "#Addr", "type": "object"}},
             "properties": {"home": {"$ref": "#Addr"}}}
            """.formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();
        var result = traversal.resolveRef("#Addr", doc);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().isAttached());
    }

    @Test
    public void testUnresolvableRef() {
        var doc = parse("{%s, \"type\": \"object\"}".formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();
        var result = traversal.resolveRef("#/definitions/Missing", doc);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testCycleDetection() {
        // Recursive schema: Person references itself via children
        var doc = parse("""
            {%s, "type": "object",
             "definitions": {
               "Person": {"type": "object", "properties": {
                 "name": {"type": "string"},
                 "children": {"type": "array", "items": {"$ref": "#/definitions/Person"}}
               }}
             },
             "properties": {"person": {"$ref": "#/definitions/Person"}}}
            """.formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();

        // First resolution should succeed
        var result = traversal.resolveRef("#/definitions/Person", doc);
        Assertions.assertTrue(result.isPresent());

        // Second resolution should use cache (same result)
        var cached = traversal.resolveRef("#/definitions/Person", doc);
        Assertions.assertTrue(cached.isPresent());
        Assertions.assertSame(result.get(), cached.get());
    }

    @Test
    public void testResolverChainOrder() {
        // Both pointer and anchor resolve the same node — pointer should win (registered first)
        var doc = parse("""
            {%s, "type": "object",
             "definitions": {"Addr": {"$id": "#Addr", "type": "object"}}}
            """.formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();

        var byPointer = traversal.resolveRef("#/definitions/Addr", doc);
        var byAnchor = traversal.resolveRef("#Addr", doc);

        Assertions.assertTrue(byPointer.isPresent());
        Assertions.assertTrue(byAnchor.isPresent());
        Assertions.assertSame(byPointer.get(), byAnchor.get(),
                "Both should resolve to the same node");
    }

    @Test
    public void testExternalRefNotResolvable() {
        // Default resolvers only handle internal refs
        var doc = parse("{%s, \"type\": \"object\"}".formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();
        var result = traversal.resolveRef("https://example.com/schema.json", doc);

        Assertions.assertTrue(result.isEmpty(),
                "External refs should not be resolved by default resolvers");
    }

    @Test
    public void testCustomResolver() {
        var doc = parse("{%s, \"type\": \"object\"}".formatted(D7));
        var externalDoc = parse("{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7));

        var chain = JsonSchemaRefResolverChain.builder()
                .addFragmentResolver(new PointerFragmentResolver())
                .addFragmentResolver(new AnchorFragmentResolver())
                .addResourceResolver((resource, ctx) -> {
                    if ("external.json".equals(resource)) {
                        return java.util.Optional.of(externalDoc);
                    }
                    return java.util.Optional.empty();
                })
                .build();

        var traversal = new JsonSchemaRefTraversal(chain);
        var result = traversal.resolveRef("external.json", doc);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertFalse(result.get().isAttached(), "External ref should be detached");
    }

    @Test
    public void testNestedAnchorInProperties() {
        // Anchor is inside a property sub-schema, not in definitions
        var doc = parse("""
            {%s, "type": "object",
             "properties": {
               "config": {
                 "type": "object",
                 "properties": {
                   "nested": {"$id": "#DeepAnchor", "type": "number"}
                 }
               },
               "ref": {"$ref": "#DeepAnchor"}
             }}
            """.formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();
        var result = traversal.resolveRef("#DeepAnchor", doc);
        Assertions.assertTrue(result.isPresent(), "Anchor nested in properties should be found");
    }
}
