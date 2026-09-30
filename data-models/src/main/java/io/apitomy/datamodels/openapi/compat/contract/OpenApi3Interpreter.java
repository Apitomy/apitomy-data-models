package io.apitomy.datamodels.openapi.compat.contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialect;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.OpenApiMediaType;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiPaths;
import io.apitomy.datamodels.models.openapi.OpenApiRequestBody;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.OpenApiResponses;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xHeader;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xParameter;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.util.CollectionUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * Interprets an OpenAPI 3.0/3.1/3.2 document into a family-neutral
 * {@link ContractDocument}. All three exact versions share this single
 * interpreter (their differences relevant here -- {@code webhooks},
 * {@code jsonSchemaDialect} -- are read reflectively where they may or may not
 * be present, rather than duplicated per version).
 */
final class OpenApi3Interpreter {

    private static final String[] HTTP_METHODS = { "get", "put", "post", "delete", "options", "head", "patch", "trace" };

    private OpenApi3Interpreter() {
    }

    static ContractDocument interpret(OpenApi3xDocument document, SchemaDialect dialect, com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot, String resourceUri) {
        List<String> problems = new ArrayList<String>();
        List<EffectiveInteraction> interactions = new ArrayList<EffectiveInteraction>();

        OpenApiPaths paths = document.getPaths();
        if (paths != null) {
            List<String> pathTemplates = ContractInterpreterSupport.itemNamesOf(paths);
            for (int i = 0; i < pathTemplates.size(); i++) {
                String pathTemplate = pathTemplates.get(i);
                interpretPathItem(document, paths.getItem(pathTemplate), pathTemplate, null, false, dialect,
                        resourceUri, rawDocumentRoot, problems, interactions);
            }
        }

        Object webhooks = NodeUtil.getNodeProperty(document, "webhooks");
        if (webhooks != null) {
            List<String> webhookNames = new ArrayList<String>(NodeUtil.getMapKeys((Map<String, ?>) webhooks));
            for (int i = 0; i < webhookNames.size(); i++) {
                String webhookName = webhookNames.get(i);
                OpenApiPathItem webhookPathItem = (OpenApiPathItem) NodeUtil.getMapItem((Map) webhooks, webhookName);
                interpretPathItem(document, webhookPathItem, null, webhookName, true, dialect, resourceUri,
                        rawDocumentRoot, problems, interactions);
            }
        }

        boolean componentOnly = interactions.isEmpty();
        String coverageNote = componentOnly
                ? "Document declares no reachable paths or webhooks; only reusable component definitions (if any) are present. No interaction-level compatibility surface to check."
                : null;
        return new ContractDocument(interactions, problems, componentOnly, coverageNote);
    }

    private static void interpretPathItem(OpenApi3xDocument document, OpenApiPathItem rawPathItem, String pathTemplate,
            String webhookName, boolean webhook, SchemaDialect dialect, String resourceUri,
            com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot, List<String> problems,
            List<EffectiveInteraction> interactions) {
        String label = webhook ? ("webhook '" + webhookName + "'") : ("path '" + pathTemplate + "'");
        Node resolvedPathItemNode = ReferenceUtil.resolveNodeRef((Node) rawPathItem);
        if (resolvedPathItemNode == null || NodeUtil.getProperty(resolvedPathItemNode, "$ref") != null) {
            problems.add(label + ": could not resolve Path Item reference");
            return;
        }
        OpenApiPathItem pathItem = (OpenApiPathItem) resolvedPathItemNode;
        @SuppressWarnings("unchecked")
        List<OpenApiParameter> pathParameters = (List<OpenApiParameter>) NodeUtil.getNodeProperty(pathItem, "parameters");

        for (int m = 0; m < HTTP_METHODS.length; m++) {
            String method = HTTP_METHODS[m];
            Object operation = NodeUtil.getNodeProperty(pathItem, method);
            if (operation == null) {
                continue;
            }
            interactions.add(interpretOperation(document, pathItem, method, pathTemplate, webhookName, webhook,
                    (Node) operation, pathParameters, dialect, resourceUri, rawDocumentRoot, problems));
        }
    }

    private static EffectiveInteraction interpretOperation(OpenApi3xDocument document, OpenApiPathItem pathItem,
            String method, String pathTemplate, String webhookName, boolean webhook, Node operation,
            List<OpenApiParameter> pathParameters, SchemaDialect dialect, String resourceUri,
            com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot, List<String> problems) {
        String interactionId = webhook ? ("webhook " + method.toUpperCase() + " " + webhookName)
                : (method.toUpperCase() + " " + pathTemplate);

        @SuppressWarnings("unchecked")
        List<OpenApiParameter> operationParameters = (List<OpenApiParameter>) NodeUtil.getNodeProperty(operation, "parameters");
        List<ContractInterpreterSupport.ResolvedParameter> merged = ContractInterpreterSupport.mergeParameters(
                pathParameters, operationParameters, interactionId, problems);

        List<EffectiveParameter> effectiveParameters = new ArrayList<EffectiveParameter>();
        for (int i = 0; i < merged.size(); i++) {
            ContractInterpreterSupport.ResolvedParameter resolved = merged.get(i);
            effectiveParameters.add(toEffectiveParameter((OpenApi3xParameter) resolved.getParameter(), dialect, resourceUri,
                    rawDocumentRoot, resolved.isDeclaredAtOperationLevel()));
        }

        EffectiveRequestBody requestBody = null;
        Object rawRequestBody = NodeUtil.getNodeProperty(operation, "requestBody");
        if (rawRequestBody != null) {
            Node resolvedBodyNode = ReferenceUtil.resolveNodeRef((Node) rawRequestBody);
            if (resolvedBodyNode == null || NodeUtil.getProperty(resolvedBodyNode, "$ref") != null) {
                problems.add(interactionId + ": could not resolve requestBody reference");
            } else {
                OpenApiRequestBody body = (OpenApiRequestBody) resolvedBodyNode;
                Map<String, SchemaView> content = mediaTypeSchemas(body.getContent(), dialect, resourceUri, rawDocumentRoot);
                requestBody = new EffectiveRequestBody(Boolean.TRUE.equals(body.isRequired()), content,
                        ContractInterpreterSupport.declarationPointer((Node) body));
            }
        }

        Map<String, EffectiveResponse> responses = interpretResponses(operation, dialect, resourceUri, rawDocumentRoot, interactionId,
                problems);
        List<Map<String, List<String>>> security = ContractInterpreterSupport.effectiveSecurity(operation, (Node) document, rawDocumentRoot);
        List<EffectiveServer> servers = effectiveServers(operation, (Node) pathItem, (Node) document);
        List<String> tags = CollectionUtil.copyOfList((List<String>) NodeUtil.getNodeProperty(operation, "tags"));
        boolean deprecated = Boolean.TRUE.equals(NodeUtil.getNodeProperty(operation, "deprecated"));

        return new EffectiveInteraction(interactionId, method.toUpperCase(), webhook ? null : pathTemplate,
                webhook ? webhookName : null, webhook, effectiveParameters, requestBody, responses, security, servers,
                deprecated, tags, (String) NodeUtil.getNodeProperty(operation, "operationId"),
                ContractInterpreterSupport.declarationPointer(operation));
    }

    private static Map<String, EffectiveResponse> interpretResponses(Node operation, SchemaDialect dialect, String resourceUri,
            com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot, String interactionId, List<String> problems) {
        Map<String, EffectiveResponse> result = new LinkedHashMap<String, EffectiveResponse>();
        OpenApiResponses responses = (OpenApiResponses) NodeUtil.getNodeProperty(operation, "responses");
        if (responses == null) {
            return result;
        }
        List<String> statusKeys = ContractInterpreterSupport.itemNamesOf(responses);
        for (int i = 0; i < statusKeys.size(); i++) {
            String statusKey = statusKeys.get(i);
            EffectiveResponse response = toEffectiveResponse(statusKey, responses.getItem(statusKey), dialect, resourceUri,
                    rawDocumentRoot, interactionId, problems);
            if (response != null) {
                result.put(statusKey, response);
            }
        }
        OpenApiResponse defaultResponse = responses.getDefault();
        if (defaultResponse != null) {
            EffectiveResponse response = toEffectiveResponse("default", defaultResponse, dialect, resourceUri, rawDocumentRoot,
                    interactionId, problems);
            if (response != null) {
                result.put("default", response);
            }
        }
        return result;
    }

    private static EffectiveResponse toEffectiveResponse(String statusKey, OpenApiResponse rawResponse, SchemaDialect dialect,
            String resourceUri, com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot, String interactionId,
            List<String> problems) {
        Node resolvedNode = ReferenceUtil.resolveNodeRef((Node) rawResponse);
        if (resolvedNode == null || NodeUtil.getProperty(resolvedNode, "$ref") != null) {
            problems.add(interactionId + ": could not resolve response '" + statusKey + "' reference");
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, OpenApiMediaType> rawContent = (Map<String, OpenApiMediaType>) NodeUtil.getNodeProperty(resolvedNode, "content");
        Map<String, SchemaView> content = mediaTypeSchemas(rawContent, dialect, resourceUri, rawDocumentRoot);

        List<EffectiveParameter> headers = new ArrayList<EffectiveParameter>();
        Object headerMap = NodeUtil.getNodeProperty(resolvedNode, "headers");
        if (headerMap != null) {
            List<String> headerNames = new ArrayList<String>(NodeUtil.getMapKeys((Map<String, ?>) headerMap));
            for (int i = 0; i < headerNames.size(); i++) {
                String headerName = headerNames.get(i);
                Node rawHeader = (Node) NodeUtil.getMapItem((Map) headerMap, headerName);
                Node resolvedHeader = ReferenceUtil.resolveNodeRef(rawHeader);
                if (resolvedHeader == null || NodeUtil.getProperty(resolvedHeader, "$ref") != null) {
                    problems.add(interactionId + ": could not resolve header '" + headerName + "' reference");
                    continue;
                }
                headers.add(toEffectiveHeader(headerName, (OpenApi3xHeader) resolvedHeader, dialect, resourceUri, rawDocumentRoot));
            }
        }
        return new EffectiveResponse(statusKey, content, headers, ContractInterpreterSupport.declarationPointer(resolvedNode));
    }

    private static EffectiveParameter toEffectiveHeader(String name, OpenApi3xHeader header, SchemaDialect dialect,
            String resourceUri, com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot) {
        SchemaView schema = ContractInterpreterSupport.schemaViewOf(header.getSchema(), dialect, resourceUri, (Node) header,
                rawDocumentRoot);
        String mediaType = firstKey(header.getContent());
        boolean explode = Boolean.TRUE.equals(header.isExplode());
        return new EffectiveParameter(name, "header", Boolean.TRUE.equals(header.isRequired()), "simple", explode, false,
                null, schema, mediaType, ContractInterpreterSupport.declarationPointer((Node) header), true);
    }

    private static EffectiveParameter toEffectiveParameter(OpenApi3xParameter parameter, SchemaDialect dialect,
            String resourceUri, com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot, boolean declaredAtOperationLevel) {
        String in = parameter.getIn();
        boolean required = "path".equals(in) || Boolean.TRUE.equals(parameter.isRequired());
        String style = parameter.getStyle();
        if (style == null) {
            style = defaultStyle(in);
        }
        Boolean explodeValue = parameter.isExplode();
        boolean explode = explodeValue != null ? explodeValue.booleanValue() : "form".equals(style);
        boolean allowReserved = Boolean.TRUE.equals(parameter.isAllowReserved());
        SchemaView schema = ContractInterpreterSupport.schemaViewOf(parameter.getSchema(), dialect, resourceUri, (Node) parameter,
                rawDocumentRoot);
        String mediaType = firstKey(parameter.getContent());
        return new EffectiveParameter(parameter.getName(), in, required, style, explode, allowReserved, null, schema,
                mediaType, ContractInterpreterSupport.declarationPointer((Node) parameter), declaredAtOperationLevel);
    }

    /** OAS 3.x's default serialization style per parameter location: {@code form} for query/cookie, {@code simple} for path/header. */
    private static String defaultStyle(String in) {
        if ("query".equals(in) || "cookie".equals(in)) {
            return "form";
        }
        return "simple";
    }

    private static String firstKey(Map<String, ?> map) {
        if (map == null) {
            return null;
        }
        List<String> keys = new ArrayList<String>(NodeUtil.getMapKeys(map));
        if (keys.isEmpty()) {
            return null;
        }
        return keys.get(0);
    }

    private static Map<String, SchemaView> mediaTypeSchemas(Map<String, OpenApiMediaType> rawContent, SchemaDialect dialect,
            String resourceUri, com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot) {
        Map<String, SchemaView> content = new LinkedHashMap<String, SchemaView>();
        if (rawContent == null) {
            return content;
        }
        List<String> mediaTypes = new ArrayList<String>(NodeUtil.getMapKeys(rawContent));
        for (int i = 0; i < mediaTypes.size(); i++) {
            String mediaType = mediaTypes.get(i);
            OpenApiMediaType mt = (OpenApiMediaType) NodeUtil.getMapItem((Map) rawContent, mediaType);
            content.put(mediaType, ContractInterpreterSupport.schemaViewOf(mt.getSchema(), dialect, resourceUri, (Node) mt,
                    rawDocumentRoot));
        }
        return content;
    }

    /**
     * The effective servers: the operation's own declaration if present, else
     * the Path Item's, else the root document's, else the specification's
     * implicit default of a single {@code "/"} server (with no variables).
     */
    @SuppressWarnings("unchecked")
    private static List<EffectiveServer> effectiveServers(Node operation, Node pathItem, Node document) {
        List<Server> resolved = (List<Server>) NodeUtil.getNodeProperty(operation, "servers");
        if (resolved == null) {
            resolved = (List<Server>) NodeUtil.getNodeProperty(pathItem, "servers");
        }
        if (resolved == null) {
            resolved = (List<Server>) NodeUtil.getNodeProperty(document, "servers");
        }
        List<EffectiveServer> servers = new ArrayList<EffectiveServer>();
        if (resolved != null) {
            for (int i = 0; i < resolved.size(); i++) {
                servers.add(toEffectiveServer(resolved.get(i)));
            }
        }
        if (servers.isEmpty()) {
            servers.add(new EffectiveServer("/", null, null));
        }
        return servers;
    }

    private static EffectiveServer toEffectiveServer(Server server) {
        String url = (String) NodeUtil.getNodeProperty(server, "url");
        Object variablesObj = NodeUtil.getNodeProperty(server, "variables");
        Map<String, List<String>> variableEnums = new LinkedHashMap<String, List<String>>();
        Map<String, String> variableDefaults = new LinkedHashMap<String, String>();
        if (variablesObj != null) {
            List<String> variableNames = new ArrayList<String>(NodeUtil.getMapKeys((Map<String, ?>) variablesObj));
            for (int i = 0; i < variableNames.size(); i++) {
                String variableName = variableNames.get(i);
                Node variable = (Node) NodeUtil.getMapItem((Map) variablesObj, variableName);
                @SuppressWarnings("unchecked")
                List<String> enumValues = (List<String>) NodeUtil.getNodeProperty(variable, "enum");
                String defaultValue = (String) NodeUtil.getNodeProperty(variable, "default");
                if (enumValues != null) {
                    variableEnums.put(variableName, CollectionUtil.copyOfList(enumValues));
                }
                if (defaultValue != null) {
                    variableDefaults.put(variableName, defaultValue);
                }
            }
        }
        return new EffectiveServer(url, variableEnums, variableDefaults);
    }
}
