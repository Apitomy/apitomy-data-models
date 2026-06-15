package io.apitomy.datamodels.jsonschema.ref;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonRefTest {

    @Test
    public void testInternalPointer() {
        var ref = JsonRef.parse("#/definitions/Address");
        Assertions.assertTrue(ref.isInternal());
        Assertions.assertTrue(ref.isPointer());
        Assertions.assertFalse(ref.isAnchor());
        Assertions.assertFalse(ref.isExternal());
        Assertions.assertNull(ref.resource());
        Assertions.assertEquals("/definitions/Address", ref.fragment());
        Assertions.assertEquals(2, ref.pointer().segments().size());
        Assertions.assertEquals("definitions", ref.pointer().segments().get(0));
        Assertions.assertEquals("Address", ref.pointer().segments().get(1));
    }

    @Test
    public void testInternalAnchor() {
        var ref = JsonRef.parse("#Address");
        Assertions.assertTrue(ref.isInternal());
        Assertions.assertFalse(ref.isPointer());
        Assertions.assertTrue(ref.isAnchor());
        Assertions.assertNull(ref.resource());
        Assertions.assertEquals("Address", ref.anchor());
        Assertions.assertNull(ref.pointer());
    }

    @Test
    public void testRootRef() {
        var ref = JsonRef.parse("#");
        Assertions.assertTrue(ref.isInternal());
        Assertions.assertTrue(ref.isRoot());
        Assertions.assertFalse(ref.isPointer());
        Assertions.assertFalse(ref.isAnchor());
        Assertions.assertEquals("", ref.fragment());
    }

    @Test
    public void testExternalWithPointer() {
        var ref = JsonRef.parse("other.json#/defs/Bar");
        Assertions.assertTrue(ref.isExternal());
        Assertions.assertFalse(ref.isInternal());
        Assertions.assertTrue(ref.isPointer());
        Assertions.assertEquals("other.json", ref.resource());
        Assertions.assertEquals(2, ref.pointer().segments().size());
        Assertions.assertEquals("defs", ref.pointer().segments().get(0));
        Assertions.assertEquals("Bar", ref.pointer().segments().get(1));
    }

    @Test
    public void testExternalNoFragment() {
        var ref = JsonRef.parse("https://example.com/schema.json");
        Assertions.assertTrue(ref.isExternal());
        Assertions.assertFalse(ref.isPointer());
        Assertions.assertFalse(ref.isAnchor());
        Assertions.assertNull(ref.fragment());
        Assertions.assertEquals("https://example.com/schema.json", ref.resource());
    }

    @Test
    public void testExternalWithAnchor() {
        var ref = JsonRef.parse("other.json#myAnchor");
        Assertions.assertTrue(ref.isExternal());
        Assertions.assertTrue(ref.isAnchor());
        Assertions.assertEquals("other.json", ref.resource());
        Assertions.assertEquals("myAnchor", ref.anchor());
    }

    @Test
    public void testDefsPointer() {
        var ref = JsonRef.parse("#/$defs/Foo");
        Assertions.assertTrue(ref.isPointer());
        Assertions.assertEquals("$defs", ref.pointer().segments().get(0));
        Assertions.assertEquals("Foo", ref.pointer().segments().get(1));
    }

    @Test
    public void testRawPreserved() {
        var raw = "other.json#/definitions/Address";
        var ref = JsonRef.parse(raw);
        Assertions.assertEquals(raw, ref.raw());
        Assertions.assertEquals(raw, ref.toString());
    }
}
