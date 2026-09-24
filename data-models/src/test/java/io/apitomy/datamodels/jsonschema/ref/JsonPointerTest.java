package io.apitomy.datamodels.jsonschema.ref;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonPointerTest {

    @Test
    public void testSimplePointer() {
        var ptr = JsonPointer.parse("/definitions/Address");
        Assertions.assertEquals(2, ptr.segments().size());
        Assertions.assertEquals("definitions", ptr.segments().get(0));
        Assertions.assertEquals("Address", ptr.segments().get(1));
    }

    @Test
    public void testEmptyPointer() {
        var ptr = JsonPointer.parse("");
        Assertions.assertTrue(ptr.isEmpty());
        Assertions.assertEquals(0, ptr.segments().size());
    }

    @Test
    public void testEscaping() {
        // RFC 6901 §5: ~0 = ~, ~1 = /
        var ptr = JsonPointer.parse("/a~1b/c~0d");
        Assertions.assertEquals(2, ptr.segments().size());
        Assertions.assertEquals("a/b", ptr.segments().get(0));
        Assertions.assertEquals("c~d", ptr.segments().get(1));
    }

    @Test
    public void testDeepPath() {
        var ptr = JsonPointer.parse("/properties/address/properties/street");
        Assertions.assertEquals(4, ptr.segments().size());
        Assertions.assertEquals("properties", ptr.segments().get(0));
        Assertions.assertEquals("address", ptr.segments().get(1));
        Assertions.assertEquals("properties", ptr.segments().get(2));
        Assertions.assertEquals("street", ptr.segments().get(3));
    }

    @Test
    public void testInvalidPointerNoLeadingSlash() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            JsonPointer.parse("definitions/Address");
        });
    }

    @Test
    public void testEvaluateAgainstDocument() {
        var schema = """
            {"$schema": "http://json-schema.org/draft-07/schema#",
             "definitions": {
               "Address": {"type": "object", "properties": {"street": {"type": "string"}}},
               "Nothing": false
             }}""";
        var doc = (io.apitomy.datamodels.models.jsonschema.JsonSchema) io.apitomy.datamodels.Library.readRootFromJSONString(schema);

        var ptr = JsonPointer.parse("/definitions/Address");
        var result = ptr.evaluate(doc);
        Assertions.assertNotNull(result, "Should resolve #/definitions/Address");

        var booleanTarget = JsonPointer.parse("/definitions/Nothing").evaluate(doc);
        Assertions.assertTrue(booleanTarget != null && booleanTarget.isBoolean() && !booleanTarget.asBoolean(),
                "A boolean schema is a valid pointer target");
    }

    @Test
    public void testEvaluateNonExistentPath() {
        var schema = """
            {"$schema": "http://json-schema.org/draft-07/schema#", "type": "object"}""";
        var doc = (io.apitomy.datamodels.models.jsonschema.JsonSchema) io.apitomy.datamodels.Library.readRootFromJSONString(schema);

        var ptr = JsonPointer.parse("/definitions/Missing");
        var result = ptr.evaluate(doc);
        Assertions.assertNull(result, "Non-existent path should return null");
    }

    @Test
    public void testAppendEscapesAndRoundTrips() {
        var ptr = JsonPointer.root().append("properties").append("a/b~c").append("0");
        Assertions.assertEquals("/properties/a~1b~0c/0", ptr.toString());
        Assertions.assertEquals(ptr, JsonPointer.parse(ptr.toString()));
        Assertions.assertEquals("", JsonPointer.root().toString());
    }

    @Test
    public void testEvaluateIntoTupleItems() {
        var schema = """
            {"$schema": "http://json-schema.org/draft-07/schema#",
             "items": [{"type": "string"}, false]}""";
        var doc = (io.apitomy.datamodels.models.jsonschema.JsonSchema) io.apitomy.datamodels.Library.readRootFromJSONString(schema);

        var first = JsonPointer.parse("/items/0").evaluate(doc);
        Assertions.assertTrue(first != null && first.isFullSchema(), "A tuple element resolves through the union");
        var second = JsonPointer.parse("/items/1").evaluate(doc);
        Assertions.assertTrue(second != null && second.isBoolean(), "A boolean tuple element resolves");
    }
}
