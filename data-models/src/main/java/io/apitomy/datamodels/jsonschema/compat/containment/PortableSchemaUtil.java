package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Portable JSON operations needed for exact schema comparison: structural
 * equality that treats object key order as insignificant (and array order as
 * significant, per JSON Schema/RFC 8259 semantics), and a lexical scan that
 * recovers a JSON number literal's exact source text before any parser could
 * round it to a double.
 */
public final class PortableSchemaUtil {

    private PortableSchemaUtil() {
    }

    /**
     * Structural JSON equality: object keys compare unordered (each key in
     * {@code a} must appear in {@code b} with an equal value, and vice versa,
     * which the equal sizes plus one-directional check below together imply);
     * array elements compare in order; numbers compare by exact decimal value
     * (so {@code 1} and {@code 1.0} are equal); a missing value (a Java
     * {@code null} reference -- there is no property at all) is never equal to
     * an explicit JSON {@code null}. (This one distinction is Java-only: the
     * transpiled TypeScript build represents both as the same {@code == null}
     * check, since JavaScript's {@code undefined} and {@code null} are both
     * loosely equal to {@code null} in Java's transpiled {@code ==}; every
     * other comparison in this method is identical in both runtimes.)
     * <p>
     * Deliberately does not call any {@link JsonNode} instance method
     * ({@code isNull()}, {@code asText()}, {@code asBoolean()}, ...): in the
     * TypeScript build a "JsonNode" is often a raw parsed JS value with no
     * such methods at all, so every classification and conversion here goes
     * through {@link JsonUtil}'s static helpers, which have a curated
     * TypeScript implementation that works on raw values instead.
     */
    public static boolean jsonEquals(JsonNode a, JsonNode b) {
        boolean aMissing = a == null;
        boolean bMissing = b == null;
        if (aMissing || bMissing) {
            return aMissing && bMissing;
        }
        boolean aIsObject = JsonUtil.isObject(a);
        boolean bIsObject = JsonUtil.isObject(b);
        if (aIsObject || bIsObject) {
            return aIsObject && bIsObject && objectsEqual(JsonUtil.toObject(a), JsonUtil.toObject(b));
        }
        boolean aIsArray = JsonUtil.isArray(a);
        boolean bIsArray = JsonUtil.isArray(b);
        if (aIsArray || bIsArray) {
            return aIsArray && bIsArray && arraysEqual(JsonUtil.toArray(a), JsonUtil.toArray(b));
        }
        boolean aIsNumber = JsonUtil.isNumber(a);
        boolean bIsNumber = JsonUtil.isNumber(b);
        if (aIsNumber || bIsNumber) {
            if (!aIsNumber || !bIsNumber) {
                return false;
            }
            ExactDecimal decimalA = ExactDecimal.parse(JsonUtil.toNumber(a).toString());
            ExactDecimal decimalB = ExactDecimal.parse(JsonUtil.toNumber(b).toString());
            return decimalA.compareTo(decimalB) == 0;
        }
        boolean aIsBoolean = JsonUtil.isBoolean(a);
        boolean bIsBoolean = JsonUtil.isBoolean(b);
        if (aIsBoolean || bIsBoolean) {
            return aIsBoolean && bIsBoolean && JsonUtil.toBoolean(a).booleanValue() == JsonUtil.toBoolean(b).booleanValue();
        }
        boolean aIsString = JsonUtil.isString(a);
        boolean bIsString = JsonUtil.isString(b);
        if (aIsString || bIsString) {
            return aIsString && bIsString && JsonUtil.toString(a).equals(JsonUtil.toString(b));
        }
        // Neither is an object, array, number, boolean, or string, and neither is
        // missing (checked above): both are JSON null.
        return true;
    }

    private static boolean objectsEqual(ObjectNode a, ObjectNode b) {
        List<String> keysA = JsonUtil.keys(a);
        List<String> keysB = JsonUtil.keys(b);
        if (keysA.size() != keysB.size()) {
            return false;
        }
        for (int i = 0; i < keysA.size(); i++) {
            String key = keysA.get(i);
            if (!keysB.contains(key)) {
                return false;
            }
            if (!jsonEquals(JsonUtil.getProperty(a, key), JsonUtil.getProperty(b, key))) {
                return false;
            }
        }
        return true;
    }

    private static boolean arraysEqual(ArrayNode a, ArrayNode b) {
        List<JsonNode> itemsA = JsonUtil.toList(a);
        List<JsonNode> itemsB = JsonUtil.toList(b);
        if (itemsA.size() != itemsB.size()) {
            return false;
        }
        for (int i = 0; i < itemsA.size(); i++) {
            if (!jsonEquals(itemsA.get(i), itemsB.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Scans raw JSON source text for every number literal, returning a map from
     * that number's JSON Pointer (RFC 6901, with {@code ~} and {@code /}
     * escaped in object keys) to its exact literal source text.
     * <p>
     * This is a lexical scan, not a full parser: it assumes {@code json} is
     * already known to be valid JSON (an ordinary parser is responsible for
     * that) and exists solely to recover number tokens before Jackson's parser
     * would round them to a double. It correctly skips over string content
     * (including escaped quotes and backslashes) so that a number-looking
     * substring inside a string is never mistaken for a literal, and it tracks
     * object and array nesting to build each number's pointer.
     *
     * @param json well-formed JSON source text
     * @return a map from JSON Pointer to exact literal number text, in
     *         encounter order
     */
    public static Map<String, String> scanNumericTokens(String json) {
        Map<String, String> tokens = new LinkedHashMap<String, String>();
        new NumericTokenScanner(json, tokens).scanValue(new ArrayList<String>());
        return tokens;
    }

    /** Escapes a single JSON Pointer reference token per RFC 6901: {@code ~} to {@code ~0}, {@code /} to {@code ~1}. */
    private static String escapeToken(String token) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '~') {
                result.append("~0");
            } else if (c == '/') {
                result.append("~1");
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static String pointerOf(List<String> path) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            result.append('/');
            result.append(escapeToken(path.get(i)));
        }
        return result.toString();
    }

    /** A minimal, single-pass, index-based JSON lexer: tracks position and the current pointer path only. */
    private static final class NumericTokenScanner {
        private final String json;
        private final Map<String, String> tokens;
        private int index;

        NumericTokenScanner(String json, Map<String, String> tokens) {
            this.json = json;
            this.tokens = tokens;
            this.index = 0;
        }

        void scanValue(List<String> path) {
            skipWhitespace();
            if (index >= json.length()) {
                return;
            }
            char c = json.charAt(index);
            if (c == '{') {
                scanObject(path);
            } else if (c == '[') {
                scanArray(path);
            } else if (c == '"') {
                skipString();
            } else if (c == 't' || c == 'f' || c == 'n') {
                skipLiteral();
            } else {
                scanNumber(path);
            }
        }

        private void scanObject(List<String> path) {
            index++; // '{'
            skipWhitespace();
            if (index < json.length() && json.charAt(index) == '}') {
                index++;
                return;
            }
            while (true) {
                skipWhitespace();
                String key = readStringLiteral();
                skipWhitespace();
                index++; // ':'
                List<String> childPath = new ArrayList<String>(path);
                childPath.add(key);
                scanValue(childPath);
                skipWhitespace();
                if (index < json.length() && json.charAt(index) == ',') {
                    index++;
                    continue;
                }
                break;
            }
            skipWhitespace();
            index++; // '}'
        }

        private void scanArray(List<String> path) {
            index++; // '['
            skipWhitespace();
            if (index < json.length() && json.charAt(index) == ']') {
                index++;
                return;
            }
            int arrayIndex = 0;
            while (true) {
                List<String> childPath = new ArrayList<String>(path);
                childPath.add(Integer.toString(arrayIndex));
                scanValue(childPath);
                arrayIndex++;
                skipWhitespace();
                if (index < json.length() && json.charAt(index) == ',') {
                    index++;
                    continue;
                }
                break;
            }
            skipWhitespace();
            index++; // ']'
        }

        /** Reads and returns a string literal's decoded content, leaving {@link #index} just past the closing quote. */
        private String readStringLiteral() {
            index++; // opening '"'
            StringBuilder result = new StringBuilder();
            while (index < json.length()) {
                char c = json.charAt(index);
                if (c == '"') {
                    index++;
                    return result.toString();
                }
                if (c == '\\') {
                    index++;
                    if (index < json.length()) {
                        char escaped = json.charAt(index);
                        if (escaped == 'u' && index + 4 < json.length()) {
                            String hex = json.substring(index + 1, index + 5);
                            result.append((char) Integer.parseInt(hex, 16));
                            index += 5;
                        } else {
                            result.append(unescape(escaped));
                            index++;
                        }
                    }
                    continue;
                }
                result.append(c);
                index++;
            }
            return result.toString();
        }

        private char unescape(char escaped) {
            if (escaped == 'n') {
                return '\n';
            }
            if (escaped == 't') {
                return '\t';
            }
            if (escaped == 'r') {
                return '\r';
            }
            if (escaped == 'b') {
                return '\b';
            }
            if (escaped == 'f') {
                return '\f';
            }
            return escaped;
        }

        private void skipString() {
            readStringLiteral();
        }

        private void skipLiteral() {
            while (index < json.length() && isAsciiLetter(json.charAt(index))) {
                index++;
            }
        }

        private void scanNumber(List<String> path) {
            int start = index;
            if (index < json.length() && (json.charAt(index) == '-' || json.charAt(index) == '+')) {
                index++;
            }
            while (index < json.length() && isAsciiDigit(json.charAt(index))) {
                index++;
            }
            if (index < json.length() && json.charAt(index) == '.') {
                index++;
                while (index < json.length() && isAsciiDigit(json.charAt(index))) {
                    index++;
                }
            }
            if (index < json.length() && (json.charAt(index) == 'e' || json.charAt(index) == 'E')) {
                index++;
                if (index < json.length() && (json.charAt(index) == '-' || json.charAt(index) == '+')) {
                    index++;
                }
                while (index < json.length() && isAsciiDigit(json.charAt(index))) {
                    index++;
                }
            }
            if (index > start) {
                tokens.put(pointerOf(path), json.substring(start, index));
            }
        }

        private void skipWhitespace() {
            while (index < json.length() && isJsonWhitespace(json.charAt(index))) {
                index++;
            }
        }

        // java.lang.Character's static classification methods depend on a Unicode
        // helper JSweet cannot resolve in this project's setup; JSON's own grammar
        // only ever needs these plain ASCII checks.
        private static boolean isAsciiDigit(char c) {
            return c >= '0' && c <= '9';
        }

        private static boolean isAsciiLetter(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        }

        private static boolean isJsonWhitespace(char c) {
            return c == ' ' || c == '\t' || c == '\n' || c == '\r';
        }
    }
}
