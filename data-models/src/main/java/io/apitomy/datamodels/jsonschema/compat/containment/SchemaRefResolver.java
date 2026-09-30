package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Resolves a JSON Pointer (RFC 6901) fragment against a raw {@link JsonNode}
 * document root -- used by {@link SchemaView#resolveRef()} to follow a
 * same-document {@code $ref}.
 * <p>
 * Deliberately independent of the model-tree {@code JsonPointer}/{@code NodePath}
 * helpers used elsewhere in this codebase: those resolve a pointer against a
 * parsed model {@code Node} tree (with typed getters), not a raw
 * {@link JsonNode}, which is what {@link SchemaView} and the rest of the
 * containment package work with throughout.
 */
final class SchemaRefResolver {

    private SchemaRefResolver() {
    }

    /**
     * Resolves {@code fragment} (the part of a {@code "#/a/b/c"} reference
     * after the {@code #}, e.g. {@code "/components/schemas/Widget"}) against
     * {@code root}, or {@code null} if any segment does not resolve.
     */
    static JsonNode resolve(JsonNode root, String fragment) {
        if (fragment == null || fragment.length() == 0) {
            return root;
        }
        List<String> segments = split(fragment);
        JsonNode current = root;
        for (int i = 0; i < segments.size(); i++) {
            if (current == null) {
                return null;
            }
            String segment = segments.get(i);
            if (JsonUtil.isArray(current)) {
                int index = parseIndex(segment);
                if (index < 0) {
                    return null;
                }
                List<JsonNode> items = JsonUtil.toList(current);
                current = index < items.size() ? items.get(index) : null;
            } else if (JsonUtil.isObject(current)) {
                current = JsonUtil.getProperty(JsonUtil.toObject(current), segment);
            } else {
                return null;
            }
        }
        return current;
    }

    private static int parseIndex(String segment) {
        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
        }
        if (segment.length() == 0) {
            return -1;
        }
        return Integer.parseInt(segment);
    }

    /** Splits {@code "/a/b~1c/d~0e"} into unescaped segments {@code ["a", "b/c", "d~e"]}, per RFC 6901. */
    private static List<String> split(String fragment) {
        List<String> segments = new ArrayList<String>();
        String remaining = fragment.charAt(0) == '/' ? fragment.substring(1) : fragment;
        int start = 0;
        for (int i = 0; i <= remaining.length(); i++) {
            if (i == remaining.length() || remaining.charAt(i) == '/') {
                segments.add(unescape(remaining.substring(start, i)));
                start = i + 1;
            }
        }
        return segments;
    }

    private static String unescape(String segment) {
        if (segment.indexOf('~') < 0) {
            return segment;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (c == '~' && i + 1 < segment.length()) {
                char next = segment.charAt(i + 1);
                if (next == '1') {
                    result.append('/');
                    i++;
                    continue;
                }
                if (next == '0') {
                    result.append('~');
                    i++;
                    continue;
                }
            }
            result.append(c);
        }
        return result.toString();
    }
}
