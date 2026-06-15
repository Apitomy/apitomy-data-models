package io.apitomy.datamodels.paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class NodePathUtilTest {

    @Test
    public void testDetectPathParamNames() {
        List<String> names = NodePathUtil.detectPathParamNames("/path/to/something");
        Assertions.assertNotNull(names);
        Assertions.assertTrue(names.isEmpty());

        names = NodePathUtil.detectPathParamNames("/path/to/{somethingId}");
        Assertions.assertNotNull(names);
        Assertions.assertEquals(1, names.size());
        Assertions.assertEquals("somethingId", names.get(0));

        names = NodePathUtil.detectPathParamNames("/{ pathId }/to/{somethingId}");
        Assertions.assertNotNull(names);
        Assertions.assertEquals(2, names.size());
        Assertions.assertEquals("pathId", names.get(0));
        Assertions.assertEquals("somethingId", names.get(1));
    }

    @Test
    public void testRemoveFirst() {
        NodePath path = NodePath.parse("/foo/bar/baz");
        Assertions.assertEquals(3, path.getSegments().size());

        NodePathSegment first = path.removeFirst();
        Assertions.assertNotNull(first);
        Assertions.assertEquals("foo", first.getValue());
        Assertions.assertFalse(first.isIndex());
        Assertions.assertEquals(2, path.getSegments().size());
        Assertions.assertEquals("/bar/baz", path.toString());
    }

    @Test
    public void testRemoveLast() {
        NodePath path = NodePath.parse("/foo/bar/baz");
        Assertions.assertEquals(3, path.getSegments().size());

        NodePathSegment last = path.removeLast();
        Assertions.assertNotNull(last);
        Assertions.assertEquals("baz", last.getValue());
        Assertions.assertFalse(last.isIndex());
        Assertions.assertEquals(2, path.getSegments().size());
        Assertions.assertEquals("/foo/bar", path.toString());
    }

    @Test
    public void testRemoveFirstFromEmptyPath() {
        NodePath path = new NodePath();
        NodePathSegment segment = path.removeFirst();
        Assertions.assertNull(segment);
    }

    @Test
    public void testRemoveLastFromEmptyPath() {
        NodePath path = new NodePath();
        NodePathSegment segment = path.removeLast();
        Assertions.assertNull(segment);
    }

    @Test
    public void testRemoveFirstUntilEmpty() {
        NodePath path = NodePath.parse("/foo/bar");
        Assertions.assertEquals(2, path.getSegments().size());

        path.removeFirst();
        Assertions.assertEquals(1, path.getSegments().size());

        path.removeFirst();
        Assertions.assertEquals(0, path.getSegments().size());

        NodePathSegment segment = path.removeFirst();
        Assertions.assertNull(segment);
    }

    @Test
    public void testRemoveLastUntilEmpty() {
        NodePath path = NodePath.parse("/foo/bar");
        Assertions.assertEquals(2, path.getSegments().size());

        path.removeLast();
        Assertions.assertEquals(1, path.getSegments().size());

        path.removeLast();
        Assertions.assertEquals(0, path.getSegments().size());

        NodePathSegment segment = path.removeLast();
        Assertions.assertNull(segment);
    }

    @Test
    public void testGetFirstSegment() {
        NodePath path = NodePath.parse("/foo/bar/baz");
        NodePathSegment first = path.getFirstSegment();
        Assertions.assertNotNull(first);
        Assertions.assertEquals("foo", first.getValue());
        Assertions.assertEquals(3, path.getSegments().size()); // Should not modify the path
    }

    @Test
    public void testGetFirstSegmentFromEmptyPath() {
        NodePath path = new NodePath();
        NodePathSegment segment = path.getFirstSegment();
        Assertions.assertNull(segment);
    }

    @Test
    public void testRemoveWithIndexSegments() {
        NodePath path = NodePath.parse("/paths[/pets]/get");
        Assertions.assertEquals(3, path.getSegments().size());

        NodePathSegment first = path.removeFirst();
        Assertions.assertNotNull(first);
        Assertions.assertEquals("paths", first.getValue());
        Assertions.assertFalse(first.isIndex());

        NodePathSegment second = path.removeFirst();
        Assertions.assertNotNull(second);
        Assertions.assertEquals("/pets", second.getValue());
        Assertions.assertTrue(second.isIndex());

        Assertions.assertEquals(1, path.getSegments().size());
    }

    @Test
    public void testAlternatingRemoves() {
        NodePath path = NodePath.parse("/a/b/c/d");
        Assertions.assertEquals(4, path.getSegments().size());

        // Remove from both ends alternately
        NodePathSegment first = path.removeFirst();
        Assertions.assertEquals("a", first.getValue());

        NodePathSegment last = path.removeLast();
        Assertions.assertEquals("d", last.getValue());

        Assertions.assertEquals(2, path.getSegments().size());
        Assertions.assertEquals("/b/c", path.toString());
    }

}
