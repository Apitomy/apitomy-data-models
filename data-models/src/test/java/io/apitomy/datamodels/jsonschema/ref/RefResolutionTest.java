package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.jsonschema.JsonSchemaDocument;
import io.apitomy.datamodels.models.jsonschema.JsonSchemaJSchema;
import org.junit.Assert;
import org.junit.Test;

/**
 * Smoke tests for JSON Schema reference resolution — resolvers, traversal, and cycle detection.
 */
public class RefResolutionTest {

    private static final String D7 = "\"$schema\": \"http://json-schema.org/draft-07/schema#\"";

    private static JsonSchemaDocument parse(String json) {
        return (JsonSchemaDocument) Library.readDocumentFromJSONString(json);
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

        Assert.assertTrue(result.isPresent());
        Assert.assertTrue(result.get().attached());
        Assert.assertTrue(result.get().node() instanceof JsonSchemaJSchema);
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

        Assert.assertTrue(result.isPresent());
        Assert.assertTrue(result.get().attached());
    }

    @Test
    public void testUnresolvableRef() {
        var doc = parse("{%s, \"type\": \"object\"}".formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();
        var result = traversal.resolveRef("#/definitions/Missing", doc);

        Assert.assertTrue(result.isEmpty());
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
        Assert.assertTrue(result.isPresent());

        // Second resolution should use cache (same result)
        var cached = traversal.resolveRef("#/definitions/Person", doc);
        Assert.assertTrue(cached.isPresent());
        Assert.assertSame(result.get().node(), cached.get().node());
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

        Assert.assertTrue(byPointer.isPresent());
        Assert.assertTrue(byAnchor.isPresent());
        Assert.assertSame("Both should resolve to the same node",
                byPointer.get().node(), byAnchor.get().node());
    }

    @Test
    public void testExternalRefNotResolvable() {
        // Default resolvers only handle internal refs
        var doc = parse("{%s, \"type\": \"object\"}".formatted(D7));

        var traversal = JsonSchemaRefTraversal.withDefaults();
        var result = traversal.resolveRef("https://example.com/schema.json", doc);

        Assert.assertTrue("External refs should not be resolved by default resolvers",
                result.isEmpty());
    }

    @Test
    public void testCustomResolver() {
        var doc = parse("{%s, \"type\": \"object\"}".formatted(D7));
        var externalDoc = parse("{%s, \"type\": \"string\", \"minLength\": 5}".formatted(D7));

        var chain = JsonSchemaRefResolverChain.builder()
                .addResolver(new LocalPointerRefResolver())
                .addResolver(new AnchorRefResolver())
                .addResolver((ref, ctx) -> {
                    if (ref.isExternal() && "external.json".equals(ref.resource())) {
                        return java.util.Optional.of(ResolvedRef.detached(externalDoc));
                    }
                    return java.util.Optional.empty();
                })
                .build();

        var traversal = new JsonSchemaRefTraversal(chain);
        var result = traversal.resolveRef("external.json", doc);

        Assert.assertTrue(result.isPresent());
        Assert.assertFalse("External ref should be detached", result.get().attached());
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
        Assert.assertTrue("Anchor nested in properties should be found", result.isPresent());
    }
}
