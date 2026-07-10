package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.JDFullSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonSchemaRefDereferencerTest {

    private static final String D7 = "\"$schema\": \"http://json-schema.org/draft-07/schema#\"";

    private static JFullSchema parse(String json) {
        return (JFullSchema) Library.readRootFromJSONString(json);
    }

    private static String ref(JFullSchema schema) {
        return ((Referenceable) schema).get$ref();
    }

    @Test
    public void testSimpleInternalRef() {
        var schema = parse("""
            {%s, "type": "object",
              "definitions": {"Address": {"type": "object", "properties": {"street": {"type": "string"}}}},
              "properties": {"home": {"$ref": "#/definitions/Address"}}}""".formatted(D7));

        var deref = JsonSchemaRefDereferencer.builder().build();
        var result = deref.dereference(schema);

        Assertions.assertFalse(result.hasCycles());
        Assertions.assertTrue(result.unresolvedRefs().isEmpty());

        var home = schema.getProperties().get("home").asFullSchema();
        Assertions.assertNull(ref(home), "home.$ref should be cleared after dereferencing");
        Assertions.assertEquals("object", home.getType().asString());
    }

    @Test
    public void testTransitiveRefs() {
        var schema = parse("""
            {%s, "type": "object",
              "definitions": {
                "A": {"$ref": "#/definitions/B"},
                "B": {"$ref": "#/definitions/C"},
                "C": {"type": "string", "minLength": 1}
              },
              "properties": {"value": {"$ref": "#/definitions/A"}}}""".formatted(D7));

        var deref = JsonSchemaRefDereferencer.builder().build();
        var result = deref.dereference(schema);

        Assertions.assertFalse(result.hasCycles());

        var value = schema.getProperties().get("value").asFullSchema();
        Assertions.assertEquals("string", value.getType().asString(),
                "Transitive chain A→B→C should resolve to C's type");
    }

    @Test
    public void testSelfReferencingCycle() {
        var schema = parse("""
            {%s, "type": "object",
              "definitions": {
                "Person": {"type": "object", "properties": {
                  "name": {"type": "string"},
                  "children": {"type": "array", "items": {"$ref": "#/definitions/Person"}}
                }}
              },
              "properties": {"person": {"$ref": "#/definitions/Person"}}}""".formatted(D7));

        var deref = JsonSchemaRefDereferencer.builder().build();
        var result = deref.dereference(schema);

        Assertions.assertTrue(result.hasCycles());
        Assertions.assertTrue(result.cyclicRefs().containsKey("#/definitions/Person"),
                "Self-referencing cycle should be reported");

        var person = schema.getProperties().get("person").asFullSchema();
        Assertions.assertNull(ref(person), "Entry-point ref should be resolved");
        Assertions.assertEquals("object", person.getType().asString());

        var children = (JDFullSchema) person.getProperties().get("children").asFullSchema();
        var items = children.getItems().asFullSchema();
        Assertions.assertNotNull(ref(items), "Cyclic back-edge should retain $ref");
    }

    @Test
    public void testMutualRecursion() {
        var schema = parse("""
            {%s, "definitions": {
                "Details": {"type": "object", "properties": {
                  "subject": {"$ref": "#/definitions/Subject"}}},
                "Subject": {"type": "object", "properties": {
                  "details": {"$ref": "#/definitions/Details"}}}
              },
              "type": "object",
              "properties": {"entry": {"$ref": "#/definitions/Details"}}}""".formatted(D7));

        var deref = JsonSchemaRefDereferencer.builder().build();
        var result = deref.dereference(schema);

        Assertions.assertTrue(result.hasCycles());
        Assertions.assertFalse(result.cyclicRefs().isEmpty(),
                "Mutual recursion should report at least one cyclic ref");

        var entry = schema.getProperties().get("entry").asFullSchema();
        Assertions.assertNull(ref(entry), "Entry-point ref should be resolved");
        Assertions.assertEquals("object", entry.getType().asString());
    }

    @Test
    public void testMultipleRefsToSameDefinition() {
        var schema = parse("""
            {%s, "type": "object",
              "definitions": {"Address": {"type": "object", "properties": {"street": {"type": "string"}}}},
              "properties": {
                "home": {"$ref": "#/definitions/Address"},
                "work": {"$ref": "#/definitions/Address"}
              }}""".formatted(D7));

        var deref = JsonSchemaRefDereferencer.builder().build();
        var result = deref.dereference(schema);

        Assertions.assertFalse(result.hasCycles());

        var home = schema.getProperties().get("home").asFullSchema();
        var work = schema.getProperties().get("work").asFullSchema();
        Assertions.assertNull(ref(home));
        Assertions.assertNull(ref(work));
        Assertions.assertEquals("object", home.getType().asString());
        Assertions.assertEquals("object", work.getType().asString());
    }

    @Test
    public void testUnresolvableWithCollect() {
        var schema = parse("""
            {%s, "type": "object",
              "properties": {"ext": {"$ref": "http://missing.com/schema.json"}}}""".formatted(D7));

        var deref = JsonSchemaRefDereferencer.builder()
                .onUnresolvableRef(UnresolvableRefStrategy.COLLECT)
                .build();
        var result = deref.dereference(schema);

        Assertions.assertFalse(result.unresolvedRefs().isEmpty(),
                "Unresolvable ref should be collected");
        Assertions.assertTrue(result.unresolvedRefs().get(0).contains("http://missing.com/schema.json"));

        var ext = schema.getProperties().get("ext").asFullSchema();
        Assertions.assertNotNull(ref(ext), "$ref should be left as-is when unresolvable");
    }

    @Test
    public void testUnresolvableWithFail() {
        var schema = parse("""
            {%s, "type": "object",
              "properties": {"ext": {"$ref": "http://missing.com/schema.json"}}}""".formatted(D7));

        var deref = JsonSchemaRefDereferencer.builder()
                .onUnresolvableRef(UnresolvableRefStrategy.FAIL)
                .build();

        Assertions.assertThrows(ReferenceResolutionException.class,
                () -> deref.dereference(schema));
    }

    @Test
    public void testExternalRefWithMapResolver() {
        var schema = parse("""
            {%s, "type": "object",
              "properties": {"addr": {"$ref": "http://example.com/address.json"}}}""".formatted(D7));

        var resolver = JsonSchemaRefResolverChain.builder()
                .addFragmentResolver(new PointerFragmentResolver())
                .addResourceResolver(MapResourceResolver.builder()
                        .addSchema("http://example.com/address.json",
                                "{%s, \"type\": \"object\", \"properties\": {\"street\": {\"type\": \"string\"}}}".formatted(D7))
                        .build())
                .build();

        var deref = JsonSchemaRefDereferencer.builder()
                .refResolver(resolver)
                .build();
        var result = deref.dereference(schema);

        Assertions.assertFalse(result.hasCycles());
        Assertions.assertTrue(result.unresolvedRefs().isEmpty());

        var addr = schema.getProperties().get("addr").asFullSchema();
        Assertions.assertNull(ref(addr));
        Assertions.assertEquals("object", addr.getType().asString());
    }
}
