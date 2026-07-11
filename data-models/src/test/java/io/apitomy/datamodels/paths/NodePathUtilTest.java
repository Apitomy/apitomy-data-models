package io.apitomy.datamodels.paths;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NodePathUtilTest {

    @Test
    public void testDetectPathParamNames() {
        List<String> names = NodePathUtil.detectPathParamNames("/path/to/something");
        Assert.assertNotNull(names);
        Assert.assertTrue(names.isEmpty());

        names = NodePathUtil.detectPathParamNames("/path/to/{somethingId}");
        Assert.assertNotNull(names);
        Assert.assertEquals(1, names.size());
        Assert.assertEquals("somethingId", names.get(0));

        names = NodePathUtil.detectPathParamNames("/{ pathId }/to/{somethingId}");
        Assert.assertNotNull(names);
        Assert.assertEquals(2, names.size());
        Assert.assertEquals("pathId", names.get(0));
        Assert.assertEquals("somethingId", names.get(1));
    }

    @Test
    public void testRemoveFirst() {
        NodePath path = NodePath.parse("/foo/bar/baz");
        Assert.assertEquals(3, path.getSegments().size());

        NodePathSegment first = path.removeFirst();
        Assert.assertNotNull(first);
        Assert.assertEquals("foo", first.getValue());
        Assert.assertFalse(first.isIndex());
        Assert.assertEquals(2, path.getSegments().size());
        Assert.assertEquals("/bar/baz", path.toString());
    }

    @Test
    public void testRemoveLast() {
        NodePath path = NodePath.parse("/foo/bar/baz");
        Assert.assertEquals(3, path.getSegments().size());

        NodePathSegment last = path.removeLast();
        Assert.assertNotNull(last);
        Assert.assertEquals("baz", last.getValue());
        Assert.assertFalse(last.isIndex());
        Assert.assertEquals(2, path.getSegments().size());
        Assert.assertEquals("/foo/bar", path.toString());
    }

    @Test
    public void testRemoveFirstFromEmptyPath() {
        NodePath path = new NodePath();
        NodePathSegment segment = path.removeFirst();
        Assert.assertNull(segment);
    }

    @Test
    public void testRemoveLastFromEmptyPath() {
        NodePath path = new NodePath();
        NodePathSegment segment = path.removeLast();
        Assert.assertNull(segment);
    }

    @Test
    public void testRemoveFirstUntilEmpty() {
        NodePath path = NodePath.parse("/foo/bar");
        Assert.assertEquals(2, path.getSegments().size());

        path.removeFirst();
        Assert.assertEquals(1, path.getSegments().size());

        path.removeFirst();
        Assert.assertEquals(0, path.getSegments().size());

        NodePathSegment segment = path.removeFirst();
        Assert.assertNull(segment);
    }

    @Test
    public void testRemoveLastUntilEmpty() {
        NodePath path = NodePath.parse("/foo/bar");
        Assert.assertEquals(2, path.getSegments().size());

        path.removeLast();
        Assert.assertEquals(1, path.getSegments().size());

        path.removeLast();
        Assert.assertEquals(0, path.getSegments().size());

        NodePathSegment segment = path.removeLast();
        Assert.assertNull(segment);
    }

    @Test
    public void testGetFirstSegment() {
        NodePath path = NodePath.parse("/foo/bar/baz");
        NodePathSegment first = path.getFirstSegment();
        Assert.assertNotNull(first);
        Assert.assertEquals("foo", first.getValue());
        Assert.assertEquals(3, path.getSegments().size()); // Should not modify the path
    }

    @Test
    public void testGetFirstSegmentFromEmptyPath() {
        NodePath path = new NodePath();
        NodePathSegment segment = path.getFirstSegment();
        Assert.assertNull(segment);
    }

    @Test
    public void testRemoveWithIndexSegments() {
        NodePath path = NodePath.parse("/paths[/pets]/get");
        Assert.assertEquals(3, path.getSegments().size());

        NodePathSegment first = path.removeFirst();
        Assert.assertNotNull(first);
        Assert.assertEquals("paths", first.getValue());
        Assert.assertFalse(first.isIndex());

        NodePathSegment second = path.removeFirst();
        Assert.assertNotNull(second);
        Assert.assertEquals("/pets", second.getValue());
        Assert.assertTrue(second.isIndex());

        Assert.assertEquals(1, path.getSegments().size());
    }

    @Test
    public void testAlternatingRemoves() {
        NodePath path = NodePath.parse("/a/b/c/d");
        Assert.assertEquals(4, path.getSegments().size());

        // Remove from both ends alternately
        NodePathSegment first = path.removeFirst();
        Assert.assertEquals("a", first.getValue());

        NodePathSegment last = path.removeLast();
        Assert.assertEquals("d", last.getValue());

        Assert.assertEquals(2, path.getSegments().size());
        Assert.assertEquals("/b/c", path.toString());
    }

}
