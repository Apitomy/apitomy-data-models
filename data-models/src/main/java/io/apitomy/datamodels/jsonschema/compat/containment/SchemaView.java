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
 * <p>
 * Optionally carries {@code documentRoot}: the full raw JSON of the resource
 * this schema's own {@code resourceUri} identifies, needed to resolve a
 * same-document {@code $ref} anywhere in this schema's own subtree (see
 * {@link #resolveRef()}). A view constructed without one (the 4-argument
 * constructor) simply never resolves a {@code $ref} it encounters -- exactly
 * today's existing, safe behavior (an unrecognized-keyword Unknown) --
 * rather than failing; every caller that wants {@code $ref} resolution
 * supplies the document root explicitly.
 */
public final class SchemaView {

    private final JsonNode node;
    private final SchemaDialect dialect;
    private final String resourceUri;
    private final String pointer;
    private final JsonNode documentRoot;

    public SchemaView(JsonNode node, SchemaDialect dialect, String resourceUri, String pointer) {
        this(node, dialect, resourceUri, pointer, null);
    }

    public SchemaView(JsonNode node, SchemaDialect dialect, String resourceUri, String pointer, JsonNode documentRoot) {
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
        this.documentRoot = documentRoot;
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

    /** The full raw JSON of the resource this schema's {@link #getResourceUri()} identifies, or {@code null} if not supplied (see the class Javadoc). */
    public JsonNode getDocumentRoot() {
        return documentRoot;
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
        return new SchemaView(child, dialect, resourceUri, childPointer(name), documentRoot).resolveRef();
    }

    /**
     * If this is an object schema with a same-document {@code $ref} keyword
     * (a fragment-only reference, {@code "#/..."}) and a {@link #getDocumentRoot()}
     * was supplied, returns a new view of the referenced schema (same dialect,
     * resource, and document root; pointer set to the target's own location) --
     * otherwise returns {@code this} unchanged.
     * <p>
     * A sibling-keyword-bearing {@code $ref} (permitted in modern JSON Schema
     * dialects, where {@code $ref} no longer overrides its siblings) is not
     * specially merged here: this checker follows legacy/OAS semantics
     * (a present {@code $ref} replaces the schema entirely) uniformly, which
     * is the safe, conservative reading for the dialects this checker
     * actually supports.
     * <p>
     * An unresolvable {@code $ref} (no document root, an external reference,
     * or a pointer that does not resolve) is left exactly as before this
     * method existed: the schema still carries its own {@code $ref} keyword,
     * still falls through to {@link SchemaContainment}'s existing
     * unrecognized-keyword handling, and is still reported {@code UNKNOWN} --
     * never silently treated as absent or as {@code true}.
     */
    public SchemaView resolveRef() {
        SchemaView current = this;
        // Bounded, not recursive: follows a chain of $ref-to-$ref up to a small
        // fixed depth. A ref chain that is itself cyclic (A -> B -> A, with no
        // other keyword ever reached) stops here rather than looping forever;
        // SchemaContainment's own visited-pair guard is what protects against a
        // cycle reached through ordinary schema structure (allOf/properties/...),
        // which this single-schema loop cannot see.
        for (int hop = 0; hop < 10; hop++) {
            SchemaView next = current.resolveRefOnce();
            if (next == current) {
                return current;
            }
            current = next;
        }
        return current;
    }

    private SchemaView resolveRefOnce() {
        if (!isObject() || documentRoot == null) {
            return this;
        }
        JsonNode refNode = getKeyword("$ref");
        if (refNode == null || !JsonUtil.isString(refNode)) {
            return this;
        }
        String ref = JsonUtil.toString(refNode);
        if (ref.length() == 0 || ref.charAt(0) != '#') {
            // Not a same-document fragment (an external or unsupported reference form).
            return this;
        }
        String fragment = ref.substring(1);
        JsonNode target = SchemaRefResolver.resolve(documentRoot, fragment);
        if (target == null || !(JsonUtil.isBoolean(target) || JsonUtil.isObject(target))) {
            return this;
        }
        return new SchemaView(target, dialect, resourceUri, fragment, documentRoot);
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
