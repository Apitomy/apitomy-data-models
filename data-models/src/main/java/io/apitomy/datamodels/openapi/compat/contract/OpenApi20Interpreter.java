package io.apitomy.datamodels.openapi.compat.contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialect;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiPaths;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.OpenApiResponses;
import io.apitomy.datamodels.models.openapi.v2x.OpenApi2xParameter;
import io.apitomy.datamodels.models.openapi.v2x.OpenApi2xResponse;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.util.CollectionUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * Interprets a Swagger/OpenAPI 2.0 document into a family-neutral
 * {@link ContractDocument}. 2.0 has no {@code servers} (uses {@code host} +
 * {@code basePath} + {@code schemes} instead), no {@code requestBody} (uses
 * {@code in: body}/{@code in: formData} parameters instead), and no
 * {@code webhooks}.
 */
final class OpenApi20Interpreter {

    private static final String[] HTTP_METHODS = { "get", "put", "post", "delete", "options", "head", "patch" };

    private OpenApi20Interpreter() {
    }

    static ContractDocument interpret(OpenApi20Document document, com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot, String resourceUri) {
        List<String> problems = new ArrayList<String>();
        List<EffectiveServer> rootServers = effectiveLegacyServers(document);
        List<String> rootConsumes = CollectionUtil.copyOfList(document.getConsumes());
        List<String> rootProduces = CollectionUtil.copyOfList(document.getProduces());

        List<EffectiveInteraction> interactions = new ArrayList<EffectiveInteraction>();
        OpenApiPaths paths = document.getPaths();
        if (paths != null) {
            List<String> pathTemplates = ContractInterpreterSupport.itemNamesOf(paths);
            for (int i = 0; i < pathTemplates.size(); i++) {
                String pathTemplate = pathTemplates.get(i);
                OpenApiPathItem rawPathItem = paths.getItem(pathTemplate);
                Node resolvedPathItemNode = ReferenceUtil.resolveNodeRef((Node) rawPathItem);
                if (resolvedPathItemNode == null || NodeUtil.getProperty(resolvedPathItemNode, "$ref") != null) {
                    problems.add("Path '" + pathTemplate + "': could not resolve Path Item reference");
                    continue;
                }
                OpenApiPathItem pathItem = (OpenApiPathItem) resolvedPathItemNode;
                @SuppressWarnings("unchecked")
                List<OpenApiParameter> pathParameters = (List<OpenApiParameter>) NodeUtil.getNodeProperty(pathItem, "parameters");

                for (int m = 0; m < HTTP_METHODS.length; m++) {
                    String method = HTTP_METHODS[m];
                    OpenApiOperation operation = (OpenApiOperation) NodeUtil.getNodeProperty(pathItem, method);
                    if (operation == null) {
                        continue;
                    }
                    interactions.add(interpretOperation(document, method, pathTemplate, operation, pathParameters,
                            rootServers, rootConsumes, rootProduces, resourceUri, rawDocumentRoot, problems));
                }
            }
        }

        boolean componentOnly = interactions.isEmpty();
        String coverageNote = componentOnly
                ? "Document declares no reachable paths; only reusable component definitions (if any) are present. No interaction-level compatibility surface to check."
                : null;
        return new ContractDocument(interactions, problems, componentOnly, coverageNote);
    }

    private static EffectiveInteraction interpretOperation(OpenApi20Document document, String method,
            String pathTemplate, OpenApiOperation operation, List<OpenApiParameter> pathParameters,
            List<EffectiveServer> rootServers, List<String> rootConsumes, List<String> rootProduces, String resourceUri,
            com.fasterxml.jackson.databind.node.ObjectNode rawDocumentRoot, List<String> problems) {
        String interactionId = method.toUpperCase() + " " + pathTemplate;
        String contextLabel = interactionId;

        @SuppressWarnings("unchecked")
        List<OpenApiParameter> operationParameters = (List<OpenApiParameter>) NodeUtil.getNodeProperty(operation, "parameters");
        List<ContractInterpreterSupport.ResolvedParameter> merged = ContractInterpreterSupport.mergeParameters(
                pathParameters, operationParameters, contextLabel, problems);

        List<EffectiveParameter> effectiveParameters = new ArrayList<EffectiveParameter>();
        OpenApiParameter bodyParameter = null;
        List<OpenApiParameter> formDataParameters = new ArrayList<OpenApiParameter>();
        boolean bodyDeclaredAtOperationLevel = false;
        String bodyDeclarationPointer = null;

        for (int i = 0; i < merged.size(); i++) {
            ContractInterpreterSupport.ResolvedParameter resolved = merged.get(i);
            OpenApiParameter parameter = resolved.getParameter();
            String in = parameter.getIn();
            if ("body".equals(in)) {
                bodyParameter = parameter;
                bodyDeclaredAtOperationLevel = resolved.isDeclaredAtOperationLevel();
                bodyDeclarationPointer = ContractInterpreterSupport.declarationPointer((Node) parameter);
                continue;
            }
            if ("formData".equals(in)) {
                formDataParameters.add(parameter);
                continue;
            }
            effectiveParameters.add(toEffectiveParameter(parameter, resourceUri, resolved.isDeclaredAtOperationLevel()));
        }

        List<String> effectiveConsumes = effectiveList((List<String>) NodeUtil.getNodeProperty(operation, "consumes"), rootConsumes);
        List<String> effectiveProduces = effectiveList((List<String>) NodeUtil.getNodeProperty(operation, "produces"), rootProduces);

        EffectiveRequestBody requestBody = null;
        if (bodyParameter != null) {
            SchemaView schema = ContractInterpreterSupport.schemaViewOf(bodyParameter.getSchema(), SchemaDialect.OAS20,
                    resourceUri, (Node) bodyParameter);
            Map<String, SchemaView> content = new LinkedHashMap<String, SchemaView>();
            if (effectiveConsumes.isEmpty()) {
                content.put("*/*", schema);
            } else {
                for (int i = 0; i < effectiveConsumes.size(); i++) {
                    content.put(effectiveConsumes.get(i), schema);
                }
            }
            requestBody = new EffectiveRequestBody(Boolean.TRUE.equals(bodyParameter.isRequired()), content, bodyDeclarationPointer);
        } else if (!formDataParameters.isEmpty()) {
            ObjectNode formSchema = JsonUtil.objectNode();
            JsonUtil.setProperty(formSchema, "type", JsonUtil.toJsonNode("object"));
            ObjectNode properties = JsonUtil.objectNode();
            ArrayNode required = JsonUtil.arrayNode();
            for (int i = 0; i < formDataParameters.size(); i++) {
                OpenApi2xParameter formParam = (OpenApi2xParameter) formDataParameters.get(i);
                JsonUtil.setProperty(properties, formParam.getName(), ContractInterpreterSupport.synthesizeLegacySchema((Node) formParam));
                if (Boolean.TRUE.equals(formParam.isRequired())) {
                    JsonUtil.addToArray(required, JsonUtil.toJsonNode(formParam.getName()));
                }
            }
            JsonUtil.setProperty(formSchema, "properties", properties);
            JsonUtil.setProperty(formSchema, "required", required);
            SchemaView schema = new SchemaView(formSchema, SchemaDialect.OAS20, resourceUri,
                    ContractInterpreterSupport.declarationPointer((Node) formDataParameters.get(0)));
            Map<String, SchemaView> content = new LinkedHashMap<String, SchemaView>();
            content.put("application/x-www-form-urlencoded", schema);
            requestBody = new EffectiveRequestBody(true, content,
                    ContractInterpreterSupport.declarationPointer((Node) formDataParameters.get(0)));
        }

        Map<String, EffectiveResponse> responses = interpretResponses(operation, effectiveProduces, resourceUri);
        List<Map<String, List<String>>> security = ContractInterpreterSupport.effectiveSecurity((Node) operation, (Node) document, rawDocumentRoot);
        List<String> tags = CollectionUtil.copyOfList(operation.getTags());

        return new EffectiveInteraction(interactionId, method.toUpperCase(), pathTemplate, null, false,
                effectiveParameters, requestBody, responses, security, rootServers,
                Boolean.TRUE.equals(operation.isDeprecated()), tags, operation.getOperationId(),
                ContractInterpreterSupport.declarationPointer((Node) operation));
    }

    private static Map<String, EffectiveResponse> interpretResponses(OpenApiOperation operation, List<String> effectiveProduces,
            String resourceUri) {
        Map<String, EffectiveResponse> result = new LinkedHashMap<String, EffectiveResponse>();
        OpenApiResponses responses = operation.getResponses();
        if (responses == null) {
            return result;
        }
        List<String> statusKeys = ContractInterpreterSupport.itemNamesOf(responses);
        for (int i = 0; i < statusKeys.size(); i++) {
            String statusKey = statusKeys.get(i);
            OpenApiResponse rawResponse = responses.getItem(statusKey);
            result.put(statusKey, toEffectiveResponse(statusKey, rawResponse, effectiveProduces, resourceUri));
        }
        OpenApiResponse defaultResponse = responses.getDefault();
        if (defaultResponse != null) {
            result.put("default", toEffectiveResponse("default", defaultResponse, effectiveProduces, resourceUri));
        }
        return result;
    }

    private static EffectiveResponse toEffectiveResponse(String statusKey, OpenApiResponse rawResponse,
            List<String> effectiveProduces, String resourceUri) {
        Node resolvedNode = ReferenceUtil.resolveNodeRef((Node) rawResponse);
        OpenApi2xResponse response = (OpenApi2xResponse) resolvedNode;
        Map<String, SchemaView> content = new LinkedHashMap<String, SchemaView>();
        if (response.getSchema() != null) {
            SchemaView schema = ContractInterpreterSupport.schemaViewOf(response.getSchema(), SchemaDialect.OAS20, resourceUri,
                    (Node) response);
            List<String> mediaTypes = effectiveProduces;
            if (mediaTypes.isEmpty()) {
                content.put("*/*", schema);
            } else {
                for (int i = 0; i < mediaTypes.size(); i++) {
                    content.put(mediaTypes.get(i), schema);
                }
            }
        }
        List<EffectiveParameter> headers = new ArrayList<EffectiveParameter>();
        Object headerMap = NodeUtil.getNodeProperty(response, "headers");
        if (headerMap != null) {
            List<String> headerNames = new ArrayList<String>(NodeUtil.getMapKeys((Map<String, ?>) headerMap));
            for (int i = 0; i < headerNames.size(); i++) {
                String headerName = headerNames.get(i);
                Node header = (Node) NodeUtil.getMapItem((Map) headerMap, headerName);
                ObjectNode headerSchema = ContractInterpreterSupport.synthesizeLegacySchema(header);
                SchemaView schema = new SchemaView(headerSchema, SchemaDialect.OAS20, resourceUri,
                        ContractInterpreterSupport.declarationPointer(header));
                headers.add(new EffectiveParameter(headerName, "header", false, null, false, false, null, schema, null,
                        ContractInterpreterSupport.declarationPointer(header), true));
            }
        }
        return new EffectiveResponse(statusKey, content, headers, ContractInterpreterSupport.declarationPointer((Node) response));
    }

    private static EffectiveParameter toEffectiveParameter(OpenApiParameter parameter, String resourceUri,
            boolean declaredAtOperationLevel) {
        OpenApi2xParameter legacy = (OpenApi2xParameter) parameter;
        String in = legacy.getIn();
        boolean required = "path".equals(in) || Boolean.TRUE.equals(legacy.isRequired());
        String collectionFormat = legacy.getCollectionFormat();
        if (collectionFormat == null && "array".equals(legacy.getType())) {
            collectionFormat = "csv";
        }
        SchemaView schema = new SchemaView(ContractInterpreterSupport.synthesizeLegacySchema((Node) legacy), SchemaDialect.OAS20,
                resourceUri, ContractInterpreterSupport.declarationPointer((Node) legacy));
        return new EffectiveParameter(legacy.getName(), in, required, null, false, false, collectionFormat, schema, null,
                ContractInterpreterSupport.declarationPointer((Node) legacy), declaredAtOperationLevel);
    }

    /** Non-null override if present, else the root list (possibly empty). */
    private static List<String> effectiveList(List<String> override, List<String> root) {
        if (override != null) {
            return CollectionUtil.copyOfList(override);
        }
        return CollectionUtil.copyOfList(root);
    }

    /**
     * A single synthesized "server" per declared scheme, from {@code host} +
     * {@code basePath} + {@code schemes} (2.0 has no per-path/operation server
     * override, so this is the same for every interaction in the document).
     * Falls back to the spec's implicit root {@code "/"} when nothing at all
     * is declared.
     */
    private static List<EffectiveServer> effectiveLegacyServers(OpenApi20Document document) {
        String host = document.getHost();
        String basePath = document.getBasePath();
        List<String> schemes = document.getSchemes();
        String path = basePath == null ? "" : basePath;

        List<EffectiveServer> result = new ArrayList<EffectiveServer>();
        if (host == null) {
            result.add(new EffectiveServer(path.length() == 0 ? "/" : path, null, null));
            return result;
        }
        if (schemes == null || schemes.isEmpty()) {
            result.add(new EffectiveServer("//" + host + path, null, null));
            return result;
        }
        for (int i = 0; i < schemes.size(); i++) {
            result.add(new EffectiveServer(schemes.get(i) + "://" + host + path, null, null));
        }
        return result;
    }
}
