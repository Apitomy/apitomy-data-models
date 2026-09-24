package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.MappedNode;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.union.UnionValue;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A parsed JSON Pointer (RFC 6901).
 * <p>
 * A JSON Pointer is a string of zero or more reference tokens, each prefixed by '/'.
 * Tokens use '~' escaping: ~0 represents '~' and ~1 represents '/'.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6901">RFC 6901 — JavaScript Object Notation (JSON) Pointer</a>
 */
public final class JsonPointer {

    private static final JsonPointer ROOT = new JsonPointer("", new ArrayList<String>());

    private final String raw;
    private final List<String> segments;

    private JsonPointer(String raw, List<String> segments) {
        this.raw = raw;
        this.segments = segments;
    }

    /**
     * Parse a JSON Pointer string. The input should start with '/' or be empty (root pointer).
     */
    public static JsonPointer parse(String pointer) {
        Objects.requireNonNull(pointer);
        if (pointer.isEmpty()) {
            return new JsonPointer(pointer, List.of());
        }
        if (!pointer.startsWith("/")) {
            throw new IllegalArgumentException("JSON Pointer must start with '/' or be empty: " + pointer);
        }
        String[] parts = pointer.substring(1).split("/", -1);
        ArrayList<String> segments = new ArrayList<String>();
        for (String part : parts) {
            segments.add(unescape(part));
        }
        return new JsonPointer(pointer, segments);
    }

    /** The empty pointer, which refers to the whole document. */
    public static JsonPointer root() {
        return ROOT;
    }

    /**
     * Returns a new pointer with one more reference token. The token is given unescaped, e.g. a
     * property name such as {@code "a/b"}, and is escaped in the string form.
     */
    public JsonPointer append(String segment) {
        ArrayList<String> appended = new ArrayList<String>(segments);
        appended.add(segment);
        return new JsonPointer(raw + "/" + escape(segment), appended);
    }

    public String raw() {
        return raw;
    }

    public List<String> segments() {
        return segments;
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /**
     * Evaluate this pointer against a document tree, starting from the given root node.
     *
     * @return the target schema, which may be a boolean schema, or null if the path cannot be
     *         resolved
     */
    @SuppressWarnings("rawtypes")
    public JsonSchema evaluate(JsonSchema root) {
        Object current = root;
        for (String segment : segments) {
            if (current == null) return null;
            current = resolveSegment(current, segment);
        }
        return toSchema(current);
    }

    @SuppressWarnings("rawtypes")
    private static Object resolveSegment(Object current, String segment) {
        // A union value that is not itself a node wraps the value to step into, e.g. the list of a
        // draft 4 to 2019-09 tuple 'items'.
        if (!(current instanceof Node) && current instanceof UnionValue) {
            current = ((UnionValue) current).getValue();
        }
        if (current instanceof MappedNode) {
            MappedNode mn = (MappedNode) current;
            return mn.getItem(segment);
        }
        if (current instanceof Node) {
            Node n = (Node) current;
            return NodeUtil.getProperty(n, segment);
        }
        if (current instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) current;
            return map.get(segment);
        }
        if (current instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) current;
            try {
                return list.get(Integer.parseInt(segment));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return null;
            }
        }
        return null;
    }

    private static JsonSchema toSchema(Object obj) {
        return obj instanceof JsonSchema ? (JsonSchema) obj : null;
    }

    // RFC 6901 §4: ~1 → '/', ~0 → '~' (order matters: ~1 first)
    private static String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~");
    }

    // The reverse, in the reverse order: '~' first, so the '~' of a '~1' is not escaped again
    private static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonPointer)) return false;
        JsonPointer p = (JsonPointer) o;
        return segments.equals(p.segments);
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    @Override
    public String toString() {
        return raw;
    }
}
