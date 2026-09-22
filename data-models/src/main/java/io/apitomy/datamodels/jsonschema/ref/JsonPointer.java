package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.MappedNode;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
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
     * @return the target node, or null if the path cannot be resolved
     */
    @SuppressWarnings("rawtypes")
    public Node evaluate(Node root) {
        Object current = root;
        for (String segment : segments) {
            if (current == null) return null;
            current = resolveSegment(current, segment);
        }
        return toNode(current);
    }

    @SuppressWarnings("rawtypes")
    private static Object resolveSegment(Object current, String segment) {
        if (current instanceof MappedNode) {
            MappedNode mn = (MappedNode) current;
            return mn.getItem(segment);
        }
        if (current instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) current;
            try {
                return list.get(Integer.parseInt(segment));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return null;
            }
        }
        // Both a Node's own properties and a plain (non-MappedNode) map/object field
        // are reached the same way here: NodeUtil.getProperty performs a Node getter
        // call, or (in the TypeScript build) a plain bracket-property access, which
        // is what a Schema's "properties"/"$defs"/etc. field actually is at runtime
        // in both languages. Do not reintroduce an inline `(Map<?, ?>) current`
        // cast-and-`.get()` here: that pattern transpiles to a java.util.Map
        // emulation that assumes an `entries` array, which these plain fields do
        // not have.
        return NodeUtil.getProperty(current, segment);
    }

    private static Node toNode(Object obj) {
        if (obj instanceof Node) return ((Node) obj);
        if (obj instanceof JsonSchema) {
            JsonSchema union = (JsonSchema) obj;
            if (union.isFullSchema()) return union.asFullSchema();
        }
        return null;
    }

    // RFC 6901 §4: ~1 → '/', ~0 → '~' (order matters: ~1 first)
    private static String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~");
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
