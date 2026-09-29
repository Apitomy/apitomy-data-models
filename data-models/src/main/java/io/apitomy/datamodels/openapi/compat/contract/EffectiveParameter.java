package io.apitomy.datamodels.openapi.compat.contract;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;

/**
 * One parameter (query/path/header/cookie, or a response header, which shares
 * the same effective shape) after applying family- and location-aware
 * defaults. Effective values are materialized even when the source document
 * left them implicit, so later rules never re-derive defaulting logic; the
 * declaration's own location is preserved separately so findings can still
 * point at the JSON Pointer that actually set (or left absent) each value.
 */
public final class EffectiveParameter {

    private final String name;
    private final String in;
    private final boolean required;
    private final String style;
    private final boolean explode;
    private final boolean allowReserved;
    private final String collectionFormat;
    private final SchemaView schema;
    private final String mediaType;
    private final String declarationPointer;
    private final boolean declaredAtOperationLevel;

    public EffectiveParameter(String name, String in, boolean required, String style, boolean explode,
            boolean allowReserved, String collectionFormat, SchemaView schema, String mediaType,
            String declarationPointer, boolean declaredAtOperationLevel) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name must not be null or empty");
        }
        if (in == null || in.length() == 0) {
            throw new IllegalArgumentException("in must not be null or empty");
        }
        this.name = name;
        this.in = in;
        this.required = required;
        this.style = style;
        this.explode = explode;
        this.allowReserved = allowReserved;
        this.collectionFormat = collectionFormat;
        this.schema = schema;
        this.mediaType = mediaType;
        this.declarationPointer = declarationPointer;
        this.declaredAtOperationLevel = declaredAtOperationLevel;
    }

    /** This parameter's name, case-sensitive except where {@link #getIn()} is {@code "header"} (HTTP header names are case-insensitive). */
    public String getName() {
        return name;
    }

    /** {@code "query"}, {@code "header"}, {@code "path"}, {@code "cookie"}, or (2.0 only) {@code "formData"}. */
    public String getIn() {
        return in;
    }

    /** The effective requiredness: always {@code true} for a path parameter, defaulted to {@code false} elsewhere when absent. */
    public boolean isRequired() {
        return required;
    }

    /** The effective serialization style (OAS 3.x): defaulted per {@code in} when the source left it absent. {@code null} for 2.0. */
    public String getStyle() {
        return style;
    }

    /** The effective {@code explode} setting (OAS 3.x): defaulted from {@link #getStyle()} when the source left it absent. */
    public boolean isExplode() {
        return explode;
    }

    /** The effective {@code allowReserved} setting (OAS 3.x), defaulted to {@code false} when absent. */
    public boolean isAllowReserved() {
        return allowReserved;
    }

    /** The effective 2.0 {@code collectionFormat} for an array-typed parameter, defaulted to {@code "csv"} when absent; {@code null} for non-array or 3.x. */
    public String getCollectionFormat() {
        return collectionFormat;
    }

    /** This parameter's schema (synthesized from legacy type/format/items fields for a 2.0 non-body parameter), or {@code null} if it uses {@link #getMediaType()}-keyed content instead. */
    public SchemaView getSchema() {
        return schema;
    }

    /** The single media type this parameter's {@code content} map declares (OAS 3.x complex-serialization form), or {@code null} if it uses {@link #getSchema()} directly. */
    public String getMediaType() {
        return mediaType;
    }

    /** The JSON Pointer to the declaration that produced this effective parameter (the operation-level parameter if present, otherwise the inherited path-level one). */
    public String getDeclarationPointer() {
        return declarationPointer;
    }

    /** True if this effective parameter's value came from the operation's own declaration (an override), false if inherited unchanged from the Path Item. */
    public boolean isDeclaredAtOperationLevel() {
        return declaredAtOperationLevel;
    }
}
