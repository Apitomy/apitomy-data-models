package io.apitomy.datamodels.jsonschema.compat.containment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * A schema value under comparison: a boolean schema ({@code true}/{@code false})
 * or an object schema, together with the dialect it is interpreted under and
 * where it came from.
 * <p>
 * Deliberately holds a plain {@link JsonNode}, not a
 * {@link io.apitomy.datamodels.models.jsonschema.JFullSchema} (or any other
 * generated entity): callers construct a view from whichever concrete model
 * type they have (an OpenAPI 3.1 {@code Schema}, a bare JSON Schema document,
 * ...), so this package's proof rules work uniformly across dialects without
 * a cast to one specific spec version's generated class, and without
 * requiring every caller to route through the full JSON Schema object model
 * for a document family that does not have one.
 * <p>
 * Like {@link PortableSchemaUtil}, every read here goes through {@link JsonUtil}'s
 * static helpers rather than a {@link JsonNode} instance method, since a
 * "JsonNode" may be a raw value with no such methods in the TypeScript build.
 */
public final class SchemaView {

    private final JsonNode node;
    private final SchemaDialect dialect;
    private final String resourceUri;
    private final String pointer;

    public SchemaView(JsonNode node, SchemaDialect dialect, String resourceUri, String pointer) {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        if (!JsonUtil.isBoolean(node) && !JsonUtil.isObject(node)) {
            throw new IllegalArgumentException("A schema view must wrap a boolean or object JSON value");
        }
        if (dialect == null) {
            throw new IllegalArgumentException("dialect must not be null");
        }
        this.node = node;
        this.dialect = dialect;
        this.resourceUri = resourceUri;
        this.pointer = pointer;
    }

    /** True if this is a boolean schema ({@code true} or {@code false}). */
    public boolean isBoolean() {
        return JsonUtil.isBoolean(node);
    }

    /** True if this is an object schema. */
    public boolean isObject() {
        return JsonUtil.isObject(node);
    }

    /**
     * This view's boolean value.
     *
     * @throws IllegalStateException if {@link #isBoolean()} is false
     */
    public boolean booleanValue() {
        if (!isBoolean()) {
            throw new IllegalStateException("Not a boolean schema");
        }
        return JsonUtil.toBoolean(node).booleanValue();
    }

    /** True if this is the {@code true} schema (an object schema is never "effectively true" here; see {@link CoverageRegistry}). */
    public boolean isTrue() {
        return isBoolean() && booleanValue();
    }

    /** True if this is the {@code false} schema. */
    public boolean isFalse() {
        return isBoolean() && !booleanValue();
    }

    /** The dialect this view is interpreted under. */
    public SchemaDialect getDialect() {
        return dialect;
    }

    /** The retrieval URI of the resource this schema lives in, or {@code null} if not known. */
    public String getResourceUri() {
        return resourceUri;
    }

    /** This schema's own JSON Pointer within its resource, or {@code null} if not known/applicable. */
    public String getPointer() {
        return pointer;
    }

    /** The raw underlying JSON value (a boolean or object node). */
    public JsonNode getNode() {
        return node;
    }

    /**
     * The value of keyword {@code name}, or {@code null} if this is a boolean
     * schema or the keyword is absent.
     */
    public JsonNode getKeyword(String name) {
        if (!isObject()) {
            return null;
        }
        ObjectNode object = JsonUtil.toObject(node);
        return JsonUtil.getProperty(object, name);
    }

    /** True if this is an object schema with a present (non-missing) keyword {@code name}. */
    public boolean hasKeyword(String name) {
        return getKeyword(name) != null;
    }

    /**
     * A view of the value at keyword {@code name}, under the same dialect and
     * resource as this view, at the child pointer -- or {@code null} if the
     * keyword is absent or is not itself a boolean/object schema value (for a
     * keyword whose value is not schema-shaped, use {@link #getKeyword} directly).
     */
    public SchemaView childView(String name) {
        JsonNode child = getKeyword(name);
        if (child == null || !(JsonUtil.isBoolean(child) || JsonUtil.isObject(child))) {
            return null;
        }
        return new SchemaView(child, dialect, resourceUri, childPointer(name));
    }

    private String childPointer(String name) {
        if (pointer == null) {
            return null;
        }
        return pointer + "/" + escapePointerToken(name);
    }

    private static String escapePointerToken(String token) {
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
}
