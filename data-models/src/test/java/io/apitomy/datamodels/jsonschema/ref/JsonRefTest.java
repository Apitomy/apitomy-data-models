package io.apitomy.datamodels.jsonschema.ref;

import org.junit.Assert;
import org.junit.Test;

public class JsonRefTest {

    @Test
    public void testInternalPointer() {
        var ref = JsonRef.parse("#/definitions/Address");
        Assert.assertTrue(ref.isInternal());
        Assert.assertTrue(ref.isPointer());
        Assert.assertFalse(ref.isAnchor());
        Assert.assertFalse(ref.isExternal());
        Assert.assertNull(ref.resource());
        Assert.assertEquals("/definitions/Address", ref.fragment());
        Assert.assertEquals(2, ref.pointer().segments().size());
        Assert.assertEquals("definitions", ref.pointer().segments().get(0));
        Assert.assertEquals("Address", ref.pointer().segments().get(1));
    }

    @Test
    public void testInternalAnchor() {
        var ref = JsonRef.parse("#Address");
        Assert.assertTrue(ref.isInternal());
        Assert.assertFalse(ref.isPointer());
        Assert.assertTrue(ref.isAnchor());
        Assert.assertNull(ref.resource());
        Assert.assertEquals("Address", ref.anchor());
        Assert.assertNull(ref.pointer());
    }

    @Test
    public void testRootRef() {
        var ref = JsonRef.parse("#");
        Assert.assertTrue(ref.isInternal());
        Assert.assertTrue(ref.isRoot());
        Assert.assertFalse(ref.isPointer());
        Assert.assertFalse(ref.isAnchor());
        Assert.assertEquals("", ref.fragment());
    }

    @Test
    public void testExternalWithPointer() {
        var ref = JsonRef.parse("other.json#/defs/Bar");
        Assert.assertTrue(ref.isExternal());
        Assert.assertFalse(ref.isInternal());
        Assert.assertTrue(ref.isPointer());
        Assert.assertEquals("other.json", ref.resource());
        Assert.assertEquals(2, ref.pointer().segments().size());
        Assert.assertEquals("defs", ref.pointer().segments().get(0));
        Assert.assertEquals("Bar", ref.pointer().segments().get(1));
    }

    @Test
    public void testExternalNoFragment() {
        var ref = JsonRef.parse("https://example.com/schema.json");
        Assert.assertTrue(ref.isExternal());
        Assert.assertFalse(ref.isPointer());
        Assert.assertFalse(ref.isAnchor());
        Assert.assertNull(ref.fragment());
        Assert.assertEquals("https://example.com/schema.json", ref.resource());
    }

    @Test
    public void testExternalWithAnchor() {
        var ref = JsonRef.parse("other.json#myAnchor");
        Assert.assertTrue(ref.isExternal());
        Assert.assertTrue(ref.isAnchor());
        Assert.assertEquals("other.json", ref.resource());
        Assert.assertEquals("myAnchor", ref.anchor());
    }

    @Test
    public void testDefsPointer() {
        var ref = JsonRef.parse("#/$defs/Foo");
        Assert.assertTrue(ref.isPointer());
        Assert.assertEquals("$defs", ref.pointer().segments().get(0));
        Assert.assertEquals("Foo", ref.pointer().segments().get(1));
    }

    @Test
    public void testRawPreserved() {
        var raw = "other.json#/definitions/Address";
        var ref = JsonRef.parse(raw);
        Assert.assertEquals(raw, ref.raw());
        Assert.assertEquals(raw, ref.toString());
    }
}
