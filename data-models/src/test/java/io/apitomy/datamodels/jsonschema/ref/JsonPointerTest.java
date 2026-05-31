package io.apitomy.datamodels.jsonschema.ref;

import org.junit.Assert;
import org.junit.Test;

public class JsonPointerTest {

    @Test
    public void testSimplePointer() {
        var ptr = JsonPointer.parse("/definitions/Address");
        Assert.assertEquals(2, ptr.segments().size());
        Assert.assertEquals("definitions", ptr.segments().get(0));
        Assert.assertEquals("Address", ptr.segments().get(1));
    }

    @Test
    public void testEmptyPointer() {
        var ptr = JsonPointer.parse("");
        Assert.assertTrue(ptr.isEmpty());
        Assert.assertEquals(0, ptr.segments().size());
    }

    @Test
    public void testEscaping() {
        // RFC 6901 §5: ~0 = ~, ~1 = /
        var ptr = JsonPointer.parse("/a~1b/c~0d");
        Assert.assertEquals(2, ptr.segments().size());
        Assert.assertEquals("a/b", ptr.segments().get(0));
        Assert.assertEquals("c~d", ptr.segments().get(1));
    }

    @Test
    public void testDeepPath() {
        var ptr = JsonPointer.parse("/properties/address/properties/street");
        Assert.assertEquals(4, ptr.segments().size());
        Assert.assertEquals("properties", ptr.segments().get(0));
        Assert.assertEquals("address", ptr.segments().get(1));
        Assert.assertEquals("properties", ptr.segments().get(2));
        Assert.assertEquals("street", ptr.segments().get(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPointerNoLeadingSlash() {
        JsonPointer.parse("definitions/Address");
    }

    @Test
    public void testEvaluateAgainstDocument() {
        var schema = """
            {"$schema": "http://json-schema.org/draft-07/schema#",
             "definitions": {
               "Address": {"type": "object", "properties": {"street": {"type": "string"}}}
             }}""";
        var doc = io.apitomy.datamodels.Library.readDocumentFromJSONString(schema);

        var ptr = JsonPointer.parse("/definitions/Address");
        var result = ptr.evaluate(doc);
        Assert.assertNotNull("Should resolve #/definitions/Address", result);
    }

    @Test
    public void testEvaluateNonExistentPath() {
        var schema = """
            {"$schema": "http://json-schema.org/draft-07/schema#", "type": "object"}""";
        var doc = io.apitomy.datamodels.Library.readDocumentFromJSONString(schema);

        var ptr = JsonPointer.parse("/definitions/Missing");
        var result = ptr.evaluate(doc);
        Assert.assertNull("Non-existent path should return null", result);
    }
}
