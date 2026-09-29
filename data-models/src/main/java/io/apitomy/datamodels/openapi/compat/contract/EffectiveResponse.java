package io.apitomy.datamodels.openapi.compat.contract;

import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.util.CollectionUtil;

/**
 * One documented response outcome: an exact status code (e.g. {@code "200"}),
 * a range (e.g. {@code "2XX"}), or {@code "default"}. The status key itself
 * (and how it competes with sibling keys) is a routing/coverage concern for
 * later rules; this type only carries what the outcome guarantees.
 */
public final class EffectiveResponse {

    private final String statusKey;
    private final Map<String, SchemaView> contentByMediaType;
    private final List<EffectiveParameter> headers;
    private final String declarationPointer;

    public EffectiveResponse(String statusKey, Map<String, SchemaView> contentByMediaType,
            List<EffectiveParameter> headers, String declarationPointer) {
        if (statusKey == null || statusKey.length() == 0) {
            throw new IllegalArgumentException("statusKey must not be null or empty");
        }
        this.statusKey = statusKey;
        this.contentByMediaType = CollectionUtil.copyOfMap(contentByMediaType);
        this.headers = CollectionUtil.copyOfList(headers);
        this.declarationPointer = declarationPointer;
    }

    /** The status code, range (e.g. {@code "2XX"}), or {@code "default"} this outcome is documented under. */
    public String getStatusKey() {
        return statusKey;
    }

    /** This response's body schema for each media type it is documented under (2.0 has at most one, implicitly keyed by the operation's {@code produces}). */
    public Map<String, SchemaView> getContentByMediaType() {
        return CollectionUtil.copyOfMap(contentByMediaType);
    }

    /** This response's documented headers, materialized as {@link EffectiveParameter} with {@code in = "header"}. */
    public List<EffectiveParameter> getHeaders() {
        return CollectionUtil.copyOfList(headers);
    }

    /** The JSON Pointer to the declaration that produced this effective response. */
    public String getDeclarationPointer() {
        return declarationPointer;
    }
}
