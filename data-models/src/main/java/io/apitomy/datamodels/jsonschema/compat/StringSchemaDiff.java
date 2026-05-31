package io.apitomy.datamodels.jsonschema.compat;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.*;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.*;

public class StringSchemaDiff {

    private final DiffContext ctx;
    private final SchemaAccessor original;
    private final SchemaAccessor updated;

    public StringSchemaDiff(DiffContext ctx, SchemaAccessor original, SchemaAccessor updated) {
        this.ctx = ctx;
        this.original = original;
        this.updated = updated;
    }

    public void visit() {
        diffInteger(ctx, original.getMinLength(), updated.getMinLength(),
                STRING_TYPE_MIN_LENGTH_ADDED, STRING_TYPE_MIN_LENGTH_REMOVED,
                STRING_TYPE_MIN_LENGTH_INCREASED, STRING_TYPE_MIN_LENGTH_DECREASED);

        diffInteger(ctx, original.getMaxLength(), updated.getMaxLength(),
                STRING_TYPE_MAX_LENGTH_ADDED, STRING_TYPE_MAX_LENGTH_REMOVED,
                STRING_TYPE_MAX_LENGTH_INCREASED, STRING_TYPE_MAX_LENGTH_DECREASED);

        diffObject(ctx, original.getPattern(), updated.getPattern(),
                STRING_TYPE_PATTERN_ADDED, STRING_TYPE_PATTERN_REMOVED, STRING_TYPE_PATTERN_CHANGED);

        diffObject(ctx, original.getFormat(), updated.getFormat(),
                STRING_TYPE_FORMAT_ADDED, STRING_TYPE_FORMAT_REMOVED, STRING_TYPE_FORMAT_CHANGED);

        diffContentEncoding();
        diffContentMediaType();
    }

    private void diffContentEncoding() {
        var origCE = getContentEncoding(original);
        var updCE = getContentEncoding(updated);
        diffObject(ctx, origCE, updCE,
                STRING_TYPE_CONTENT_ENCODING_ADDED, STRING_TYPE_CONTENT_ENCODING_REMOVED,
                STRING_TYPE_CONTENT_ENCODING_CHANGED);
    }

    private void diffContentMediaType() {
        var origCMT = getContentMediaType(original);
        var updCMT = getContentMediaType(updated);
        diffObject(ctx, origCMT, updCMT,
                STRING_TYPE_CONTENT_MEDIA_TYPE_ADDED, STRING_TYPE_CONTENT_MEDIA_TYPE_REMOVED,
                STRING_TYPE_CONTENT_MEDIA_TYPE_CHANGED);
    }

    private static String getContentEncoding(SchemaAccessor schema) {
        var node = schema.node();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document d) return d.getContentEncoding();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema s) return s.getContentEncoding();
        return null;
    }

    private static String getContentMediaType(SchemaAccessor schema) {
        var node = schema.node();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document d) return d.getContentMediaType();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema s) return s.getContentMediaType();
        return null;
    }
}
