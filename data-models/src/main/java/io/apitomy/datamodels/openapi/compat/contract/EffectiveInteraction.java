package io.apitomy.datamodels.openapi.compat.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.openapi.compat.HttpRole;
import io.apitomy.datamodels.openapi.compat.ProviderRole;
import io.apitomy.datamodels.util.CollectionUtil;

/**
 * One operation's fully resolved (Path Item defaults merged with any
 * operation-level overrides) effective contract: what a consumer following
 * only the documented, effective behavior can actually rely on.
 * <p>
 * {@code webhook}/{@code isWebhook} flips {@link io.apitomy.datamodels.openapi.compat.ProviderRole} for
 * every usage this interaction produces (see {@link #getSchemaUsages()}): a
 * webhook request is sent by the provider (an output), and a webhook
 * response is sent back by the original consumer (an input) -- see the
 * Design Contract's HTTP-role/provider-role table. {@link HttpRole} itself
 * never flips: a webhook request is still, structurally, a request.
 */
public final class EffectiveInteraction {

    private final String interactionId;
    private final String httpMethod;
    private final String pathTemplate;
    private final String webhookName;
    private final boolean webhook;
    private final List<EffectiveParameter> parameters;
    private final EffectiveRequestBody requestBody;
    private final Map<String, EffectiveResponse> responses;
    private final List<Map<String, List<String>>> security;
    private final List<String> servers;
    private final boolean deprecated;
    private final List<String> tags;
    private final String declarationPointer;

    public EffectiveInteraction(String interactionId, String httpMethod, String pathTemplate, String webhookName,
            boolean webhook, List<EffectiveParameter> parameters, EffectiveRequestBody requestBody,
            Map<String, EffectiveResponse> responses, List<Map<String, List<String>>> security,
            List<String> servers, boolean deprecated, List<String> tags, String declarationPointer) {
        if (interactionId == null || interactionId.length() == 0) {
            throw new IllegalArgumentException("interactionId must not be null or empty");
        }
        if (httpMethod == null || httpMethod.length() == 0) {
            throw new IllegalArgumentException("httpMethod must not be null or empty");
        }
        this.interactionId = interactionId;
        this.httpMethod = httpMethod;
        this.pathTemplate = pathTemplate;
        this.webhookName = webhookName;
        this.webhook = webhook;
        this.parameters = CollectionUtil.copyOfList(parameters);
        this.requestBody = requestBody;
        this.responses = CollectionUtil.copyOfMap(responses);
        this.security = CollectionUtil.copyOfList(security);
        this.servers = CollectionUtil.copyOfList(servers);
        this.deprecated = deprecated;
        this.tags = CollectionUtil.copyOfList(tags);
        this.declarationPointer = declarationPointer;
    }

    /** A stable identity for this interaction: not routing authority (see T12), just a diagnostic label. */
    public String getInteractionId() {
        return interactionId;
    }

    /** The upper-cased HTTP method (or extension method) this interaction uses. */
    public String getHttpMethod() {
        return httpMethod;
    }

    /** This interaction's path template, or {@code null} for a webhook. */
    public String getPathTemplate() {
        return pathTemplate;
    }

    /** This webhook's declared name (the {@code webhooks} map key), or {@code null} for an ordinary path operation. */
    public String getWebhookName() {
        return webhookName;
    }

    /** True if this is a webhook (or, in later tasks, callback) interaction: the provider sends the request. */
    public boolean isWebhook() {
        return webhook;
    }

    /** This operation's effective parameters: Path Item entries, replaced (by semantic identity) with any Operation entry, in a stable order. */
    public List<EffectiveParameter> getParameters() {
        return CollectionUtil.copyOfList(parameters);
    }

    /** This operation's effective request body (3.x {@code requestBody}, or a synthesized 2.0 body/formData equivalent), or {@code null} if none is documented. */
    public EffectiveRequestBody getRequestBody() {
        return requestBody;
    }

    /** This operation's effective documented response outcomes, keyed by status code, range, or {@code "default"}. */
    public Map<String, EffectiveResponse> getResponses() {
        return CollectionUtil.copyOfMap(responses);
    }

    /** This operation's effective security alternatives (operation declaration if present, otherwise the root declaration); an empty list means explicitly no security. */
    public List<Map<String, List<String>>> getSecurity() {
        return CollectionUtil.copyOfList(security);
    }

    /** This operation's effective server URLs (operation, else Path Item, else root/default), as raw (unexpanded) templates. */
    public List<String> getServers() {
        return CollectionUtil.copyOfList(servers);
    }

    /** Whether this operation is documented as deprecated. */
    public boolean isDeprecated() {
        return deprecated;
    }

    /** This operation's declared tags. */
    public List<String> getTags() {
        return CollectionUtil.copyOfList(tags);
    }

    /** The JSON Pointer to the operation node this interaction was interpreted from. */
    public String getDeclarationPointer() {
        return declarationPointer;
    }

    /**
     * Every schema usage reachable from this interaction, with {@link HttpRole}
     * and {@link ProviderRole} assigned per the Design Contract's table:
     * request-side usages (parameters, request body) are always
     * {@link HttpRole#REQUEST}; response-side usages (response bodies,
     * response headers) are always {@link HttpRole#RESPONSE}. {@link ProviderRole}
     * is {@link ProviderRole#INPUT} for an ordinary request / webhook response,
     * and {@link ProviderRole#OUTPUT} for an ordinary response / webhook request
     * -- i.e. it flips exactly when {@link #isWebhook()} is true.
     */
    public List<SchemaUsage> getSchemaUsages() {
        ProviderRole requestProviderRole = webhook ? ProviderRole.OUTPUT : ProviderRole.INPUT;
        ProviderRole responseProviderRole = webhook ? ProviderRole.INPUT : ProviderRole.OUTPUT;

        List<SchemaUsage> usages = new ArrayList<SchemaUsage>();
        for (int i = 0; i < parameters.size(); i++) {
            EffectiveParameter parameter = parameters.get(i);
            if (parameter.getSchema() != null) {
                String location = "parameter:" + parameter.getIn() + ":" + parameter.getName();
                usages.add(new SchemaUsage(parameter.getSchema(), interactionId, HttpRole.REQUEST, requestProviderRole,
                        parameter.getMediaType(), location));
            }
        }
        if (requestBody != null) {
            Map<String, SchemaView> content = requestBody.getContentByMediaType();
            List<String> mediaTypes = new ArrayList<String>(content.keySet());
            for (int i = 0; i < mediaTypes.size(); i++) {
                String mediaType = mediaTypes.get(i);
                usages.add(new SchemaUsage(content.get(mediaType), interactionId, HttpRole.REQUEST, requestProviderRole,
                        mediaType, "requestBody:" + mediaType));
            }
        }
        List<String> statusKeys = new ArrayList<String>(responses.keySet());
        for (int i = 0; i < statusKeys.size(); i++) {
            String statusKey = statusKeys.get(i);
            EffectiveResponse response = responses.get(statusKey);
            Map<String, SchemaView> content = response.getContentByMediaType();
            List<String> mediaTypes = new ArrayList<String>(content.keySet());
            for (int j = 0; j < mediaTypes.size(); j++) {
                String mediaType = mediaTypes.get(j);
                usages.add(new SchemaUsage(content.get(mediaType), interactionId, HttpRole.RESPONSE, responseProviderRole,
                        mediaType, "response:" + statusKey + ":" + mediaType));
            }
            List<EffectiveParameter> headers = response.getHeaders();
            for (int j = 0; j < headers.size(); j++) {
                EffectiveParameter header = headers.get(j);
                if (header.getSchema() != null) {
                    usages.add(new SchemaUsage(header.getSchema(), interactionId, HttpRole.RESPONSE, responseProviderRole,
                            null, "response:" + statusKey + ":header:" + header.getName()));
                }
            }
        }
        return usages;
    }
}
