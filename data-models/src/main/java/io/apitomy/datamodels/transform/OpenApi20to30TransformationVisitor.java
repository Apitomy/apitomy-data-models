package io.apitomy.datamodels.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.models.Contact;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Example;
import io.apitomy.datamodels.models.Extensible;
import io.apitomy.datamodels.models.ExternalDocumentation;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.models.License;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.SecurityRequirement;
import io.apitomy.datamodels.models.SecurityRequirementsParent;
import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.openapi.OpenApiDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.OpenApiItems;
import io.apitomy.datamodels.models.openapi.OpenApiOAuthFlow;
import io.apitomy.datamodels.models.openapi.OpenApiParameterDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiParametersParent;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiPaths;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.OpenApiResponseDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiResponses;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.openapi.OpenApiScopes;
import io.apitomy.datamodels.models.openapi.OpenApiSecurityDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiTag;
import io.apitomy.datamodels.models.openapi.OpenApiXML;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Contact;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20ExternalDocumentation;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Header;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Info;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20License;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Operation;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Parameter;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20PathItem;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Paths;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Response;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Responses;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Schema;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20SecurityRequirement;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20SecurityScheme;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20XML;
import io.apitomy.datamodels.models.openapi.v2x.v20.visitors.OpenApi20Visitor;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xMediaType;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Components;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Contact;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Discriminator;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30ExternalDocumentation;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Header;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Info;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30License;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30MediaType;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30OAuthFlows;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Operation;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Parameter;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30PathItem;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Paths;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30RequestBody;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Response;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Responses;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30SecurityRequirement;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30SecurityScheme;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Server;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30ServerVariable;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30XML;
import io.apitomy.datamodels.models.union.BooleanUnionValue;
import io.apitomy.datamodels.models.union.BooleanUnionValueImpl;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.models.visitors.TraversalContext;
import io.apitomy.datamodels.models.visitors.TraversingVisitor;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.visitors.ConsumesProducesFinder;
import io.apitomy.datamodels.visitors.OperationFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A visitor used to transform an OpenAPI 2.0 document into an OpenAPI 3.0.x document.
 * @author eric.wittmann@gmail.com
 */
public class OpenApi20to30TransformationVisitor implements OpenApi20Visitor, TraversingVisitor {

    private OpenApi30Document doc30;
    private TraversalContext traversalContext;

    private Map<String, Node> _nodeMap = new HashMap<>();
    private List<OpenApi30Response> _postProcessResponses = new ArrayList<>();
    private boolean _postProcessingComplete = false;

    @Override
    public void setTraversalContext(TraversalContext context) {
        this.traversalContext = context;
    }

    /**
     * Retrieves the final result.
     */
    public OpenApi30Document getResult() {
        if (!this._postProcessingComplete) {
            this.postProcess();
        }
        return this.doc30;
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.Visitor#visitDocument(Document) 
     */
    @Override
    public void visitDocument(Document node) {
        this.doc30 = (OpenApi30Document) Library.createDocument(ModelType.OPENAPI30);
        this.doc30.setOpenapi("3.0.3");

        OpenApi20Document doc20 = (OpenApi20Document) node;
        if (!NodeUtil.isNullOrUndefined(doc20.getHost())) {
            String basePath = doc20.getBasePath();
            if (NodeUtil.isNullOrUndefined(basePath)) {
                basePath = "";
            }
            List<String> schemes = doc20.getSchemes();
            if (NodeUtil.isNullOrUndefined(schemes) || schemes.size() == 0) {
                schemes = NodeUtil.asList("http");
            }

            OpenApi30Server server30 = (OpenApi30Server) this.doc30.createServer();
            this.doc30.addServer(server30);
            if (schemes.size() == 1) {
                server30.setUrl(schemes.get(0) + "://" + doc20.getHost() + basePath);
            } else {
                server30.setUrl("{scheme}://" + doc20.getHost() + basePath);
                OpenApi30ServerVariable var30 = (OpenApi30ServerVariable) server30.createServerVariable();
                server30.addVariable("scheme", var30);
                var30.setDefault(schemes.get(0));
                var30.setEnum(NodeUtil.copyList(schemes));
                var30.setDescription("The supported protocol schemes.");
            }
        }

        this.copyExtensions(doc20, doc30);
        this.mapNode(doc20, this.doc30);
    }

    @Override
    public void visitInfo(Info node) {
        OpenApi20Info info20 = (OpenApi20Info) node;
        OpenApi30Info info30 = (OpenApi30Info) this.doc30.createInfo();
        this.doc30.setInfo(info30);
        info30.setTitle(info20.getTitle());

        info30.setDescription(info20.getDescription());
        info30.setTermsOfService(info20.getTermsOfService());
        info30.setVersion(info20.getVersion());

        this.copyExtensions(info20, info30);
        this.mapNode(info20, info30);
    }

    @Override
    public void visitContact(Contact node) {
        OpenApi30Info info30 = (OpenApi30Info) this.lookup(node.parent());
        OpenApi30Contact contact30 = (OpenApi30Contact) info30.createContact();
        info30.setContact(contact30);
        contact30.setName(node.getName());
        contact30.setUrl(node.getUrl());
        contact30.setEmail(node.getEmail());

        this.copyExtensions((OpenApi20Contact) node, contact30);
        this.mapNode(node, contact30);
    }

    @Override
    public void visitLicense(License node) {
        OpenApi30Info info30 = (OpenApi30Info) this.lookup(node.parent());
        OpenApi30License license30 = (OpenApi30License) info30.createLicense();
        info30.setLicense(license30);
        license30.setName(node.getName());
        license30.setUrl(node.getUrl());

        this.copyExtensions((OpenApi20License) node, license30);
        this.mapNode(node, license30);
    }

    @Override
    public void visitPaths(OpenApiPaths node) {
        OpenApiPaths paths30 = this.doc30.createPaths();
        this.doc30.setPaths(paths30);

        this.copyExtensions((OpenApi20Paths) node, (OpenApi30Paths) paths30);
        this.mapNode(node, this.doc30.getPaths());
    }

    @Override
    public void visitPathItem(OpenApiPathItem node) {
        OpenApi20PathItem pathItem20 = (OpenApi20PathItem) node;

        OpenApi30Paths paths30 = (OpenApi30Paths) this.lookup(pathItem20.parent());
        OpenApi30PathItem pathItem30 = (OpenApi30PathItem) paths30.createPathItem();
        String pathName = node.mapPropertyName();
        paths30.addItem(pathName, pathItem30);

        pathItem30.set$ref(this.updateRef(pathItem20.get$ref()));

        this.copyExtensions(pathItem20, pathItem30);
        this.mapNode(pathItem20, pathItem30);
    }

    @Override
    public void visitOperation(Operation node) {
        OpenApi20Operation op = (OpenApi20Operation) node;
        OpenApi30PathItem pathItem30 = (OpenApi30PathItem) this.lookup(node.parent());
        OpenApi30Operation operation30 = (OpenApi30Operation) pathItem30.createOperation();

        String operationType = node.parentPropertyName();
        if (operationType.equals("get")) {
            pathItem30.setGet(operation30);
        } else if (operationType.equals("put")) {
            pathItem30.setPut(operation30);
        } else if (operationType.equals("post")) {
            pathItem30.setPost(operation30);
        } else if (operationType.equals("delete")) {
            pathItem30.setDelete(operation30);
        } else if (operationType.equals("head")) {
            pathItem30.setHead(operation30);
        } else if (operationType.equals("options")) {
            pathItem30.setOptions(operation30);
        } else if (operationType.equals("patch")) {
            pathItem30.setPatch(operation30);
        } else if (operationType.equals("trace")) {
            pathItem30.setTrace(operation30);
        }

        operation30.setTags(op.getTags());
        operation30.setSummary(op.getSummary());
        operation30.setDescription(op.getDescription());
        operation30.setOperationId(op.getOperationId());
        operation30.setDeprecated(op.isDeprecated());

        if (!NodeUtil.isNullOrUndefined(op.getSchemes()) &&
                op.getSchemes().size() > 0 &&
                !NodeUtil.isNullOrUndefined(this.doc30.getServers()) &&
                this.doc30.getServers().size() > 0)
        {
            OpenApi30Server server30 = (OpenApi30Server) operation30.createServer();
            operation30.addServer(server30);

            server30.setUrl(((OpenApi30Server) this.doc30.getServers().get(0)).getUrl());
            if (op.getSchemes().size() == 1) {
                server30.setUrl(server30.getUrl().replace("{scheme}", op.getSchemes().get(0)));
                server30.removeVariable("scheme");
            } else {
                server30.setUrl("{scheme}" + server30.getUrl().substring(server30.getUrl().indexOf("://")));
                OpenApi30ServerVariable var30 = (OpenApi30ServerVariable) server30.createServerVariable();
                server30.addVariable("scheme", var30);
                var30.setDescription("The supported protocol schemes.");
                var30.setDefault(op.getSchemes().get(0));
                var30.setEnum(NodeUtil.copyList(op.getSchemes()));
            }
        }

        // Note: consumes/produces will be handled elsewhere (when Request Body and Response models are created)

        this.copyExtensions(op, operation30);
        this.mapNode(op, operation30);
    }

    @Override
    public void visitParameter(Parameter node) {
        if (isParameterDefinition(node)) {
            visitParameterDefinition(node);
            return;
        }

        OpenApi20Parameter param20 = (OpenApi20Parameter) node;
        if (NodeUtil.equals(param20.getIn(), "body")) {
            OpenApi30Operation operation30 = (OpenApi30Operation) this.lookup(this.findParentOperation(param20));
            if (!NodeUtil.isNullOrUndefined(operation30)) {
                OpenApi30RequestBody body30 = (OpenApi30RequestBody) operation30.createRequestBody();
                operation30.setRequestBody(body30);

                body30.setDescription(param20.getDescription());
                body30.setRequired(param20.isRequired());

                if (!NodeUtil.isNullOrUndefined(param20.getSchema())) {
                    List<String> consumes = this.findConsumes(param20);
                    OpenApi20Schema schema = (OpenApi20Schema) param20.getSchema();
                    consumes.forEach( ct -> {
                        OpenApi30MediaType mediaType30 = (OpenApi30MediaType) body30.createMediaType();
                        body30.addContent(ct, mediaType30);

                        OpenApi30Schema schema30 = (OpenApi30Schema) mediaType30.createSchema();
                        mediaType30.setSchema(this.toSchema(schema, schema30, true));

                        this.mapNode(schema, schema30);
                    });
                }
            }
        } else if (NodeUtil.equals(param20.getIn(), "formData")) {
            OpenApi30Operation operation30 = (OpenApi30Operation) this.lookup(this.findParentOperation(param20));
            if (!NodeUtil.isNullOrUndefined(operation30)) {
                List<String> consumes = this.findConsumes(param20);
                if (!this.hasFormDataMimeType(consumes)) {
                    consumes = NodeUtil.asList("application/x-www-form-urlencoded");
                }
                consumes.forEach(ct -> {
                    if (this.isFormDataMimeType(ct)) {
                        OpenApi30RequestBody body30 = (OpenApi30RequestBody) operation30.getRequestBody();
                        if (NodeUtil.isNullOrUndefined(body30)) {
                            body30 = (OpenApi30RequestBody) operation30.createRequestBody();
                            operation30.setRequestBody(body30);
                            body30.setRequired(true);
                        }
                        OpenApi30MediaType mediaType30 = null;
                        if (!NodeUtil.isNullOrUndefined(body30.getContent())) {
                            mediaType30 = (OpenApi30MediaType) body30.getContent().get(ct);
                        }

                        if (NodeUtil.isNullOrUndefined(mediaType30)) {
                            mediaType30 = (OpenApi30MediaType) body30.createMediaType();
                            body30.addContent(ct, mediaType30);
                        }
                        OpenApi30Schema schema30 = (OpenApi30Schema) mediaType30.getSchema();
                        if (NodeUtil.isNullOrUndefined(schema30)) {
                            schema30 = (OpenApi30Schema) mediaType30.createSchema();
                            mediaType30.setSchema(schema30);
                            schema30.setType("object");
                        }

                        OpenApi30Schema property30 = (OpenApi30Schema) schema30.createSchema();
                        schema30.addProperty(param20.getName(), property30);
                        property30.setDescription(param20.getDescription());
                        this.toSchema(param20, property30, false);

                        this.mapNode(param20, schema30);
                    }
                });
            }
        } else {
            if (this.isRef(param20)) {
                OpenApi20Parameter paramDef = (OpenApi20Parameter) ReferenceUtil.resolveRef(param20.get$ref(), param20);

                // Handle the case where there is a parameter $ref to a "body" param.  All body params become
                // Request Bodies.  So a reference to a "body" param should become a reference to a request body.
                if (!NodeUtil.isNullOrUndefined(paramDef) && NodeUtil.equals(paramDef.getIn(), "body")) {
                    OpenApi30Operation parent30 = (OpenApi30Operation) this.lookup(this.findParentOperation(param20));
                    if (!NodeUtil.isNullOrUndefined(parent30)) {
                        OpenApi30RequestBody body30 = (OpenApi30RequestBody) parent30.createRequestBody();
                        parent30.setRequestBody(body30);

                        String newRef = param20.get$ref().replace("#/parameters/", "#/components/requestBodies/");
                        body30.set$ref(newRef);

                        this.mapNode(param20, body30);
                        return;
                    }
                }

                // Handle the case where the parameter is a $ref to a formData param.  In this case we want to
                // treat the param as though it is inlined (which will result in a requestBody model).
                if (!NodeUtil.isNullOrUndefined(paramDef) && NodeUtil.equals(paramDef.getIn(), "formData")) {
                    // Inline the parameter definition and then re-visit it.
                    Library.readNode(Library.writeNode(paramDef), param20);
                    param20.set$ref(null);
                    this.visitParameter(param20);
                    return;
                }
            }

            // Now we have normal handling of a parameter, examples include path params, query params, header params, etc.
            Node parent30 = this.lookup(param20.parent());
            OpenApi30Parameter param30 = createAndAddParameter((OpenApiParametersParent) parent30);
            this.transformParam(param20, param30);

            this.copyExtensions(param20, param30);
            this.mapNode(param20, param30);
        }
    }

    private OpenApi30Parameter createAndAddParameter(OpenApiParametersParent parent30) {
        OpenApi30Parameter newParam = (OpenApi30Parameter) parent30.createParameter();
        parent30.addParameter(newParam);
        return newParam;
    }

    private OpenApi30Parameter transformParam(OpenApi20Parameter node, OpenApi30Parameter param30) {
        param30.set$ref(this.updateRef(node.get$ref()));
        if (!NodeUtil.isNullOrUndefined(param30.get$ref())) {
            return param30;
        }
        param30.setName(node.getName());
        param30.setIn(node.getIn());
        param30.setDescription(node.getDescription());
        param30.setRequired(node.isRequired());
        param30.setAllowEmptyValue(node.isAllowEmptyValue());
        param30.setSchema(this.toSchema(node, (OpenApi30Schema) param30.createSchema(), false));
        this.collectionFormatToStyleAndExplode(node, param30);
        return param30;
    }

    public void visitParameterDefinition(Parameter node) {
        String name = node.mapPropertyName();
        OpenApi20Parameter pd20 = (OpenApi20Parameter) node;
        if (NodeUtil.equals(pd20.getIn(), "body")) {
            OpenApi30Components parent30 = this.getOrCreateComponents();
            OpenApi30RequestBody bodyDef30 = (OpenApi30RequestBody) parent30.createRequestBody();
            parent30.addRequestBody(name, bodyDef30);

            bodyDef30.setDescription(pd20.getDescription());
            bodyDef30.setRequired(pd20.isRequired());
            if (!NodeUtil.isNullOrUndefined(pd20.getSchema())) {
                List<String> consumes = this.findConsumes(pd20);
                OpenApi20Schema schema = (OpenApi20Schema) pd20.getSchema();
                consumes.forEach(ct -> {
                    OpenApi30MediaType mediaType30 = (OpenApi30MediaType) bodyDef30.createMediaType();
                    bodyDef30.addContent(ct, mediaType30);

                    OpenApi30Schema schema30 = (OpenApi30Schema) mediaType30.createSchema();
                    mediaType30.setSchema(this.toSchema(schema, schema30, true));

                    this.copyExtensions(schema, schema30);
                    this.mapNode(schema, schema30);
                });
            }
        } else if (NodeUtil.equals(pd20.getIn(), "formData")) {
            // Exclude any re-usable formData parameters - they are currently being inlined elsewhere.  I'm not sure
            // what we would do with them anyway.
        } else {
            OpenApi30Components components30 = this.getOrCreateComponents();
            OpenApi30Parameter paramDef30 = (OpenApi30Parameter) components30.createParameter();
            components30.addParameter(name, paramDef30);
            this.transformParam(pd20, paramDef30);
            this.copyExtensions(pd20, paramDef30);
            this.mapNode(pd20, paramDef30);
        }
    }

    @Override
    public void visitExternalDocumentation(ExternalDocumentation node) {
        Node parent30 = this.lookup(node.parent());

        ExternalDocsCreator externalDocsCreator = new ExternalDocsCreator();
        parent30.accept(externalDocsCreator);
        OpenApi30ExternalDocumentation externalDocs30 = (OpenApi30ExternalDocumentation) externalDocsCreator.externalDocs;

        externalDocs30.setDescription(node.getDescription());
        externalDocs30.setUrl(node.getUrl());

        this.copyExtensions((OpenApi20ExternalDocumentation) node, externalDocs30);
        this.mapNode(node, externalDocs30);
    }

    @Override
    public void visitSecurityRequirement(SecurityRequirement node) {
        OpenApi20SecurityRequirement req = (OpenApi20SecurityRequirement) node;
        SecurityRequirementsParent parent30 = (SecurityRequirementsParent) this.lookup(req.parent());
        OpenApi30SecurityRequirement securityRequirement30 = (OpenApi30SecurityRequirement) parent30.createSecurityRequirement();
        parent30.addSecurity(securityRequirement30);

        req.getItemNames().forEach( name -> {
            securityRequirement30.addItem(name, req.getItem(name));
        });

        this.mapNode(req, securityRequirement30);
    }

    @Override
    public void visitResponses(OpenApiResponses node) {
        OpenApi30Operation parent30 = (OpenApi30Operation) this.lookup(node.parent());
        OpenApi30Responses responses30 = (OpenApi30Responses) parent30.createResponses();
        parent30.setResponses(responses30);

        this.copyExtensions((OpenApi20Responses) node, responses30);
        this.mapNode(node, responses30);
    }

    @Override
    public void visitResponse(OpenApiResponse node) {
        if (isResponseDefinition(node)) {
            visitResponseDefinition(node);
            return;
        }
        OpenApi20Response response20 = (OpenApi20Response) node;
        OpenApi30Responses parent30 = (OpenApi30Responses) this.lookup(node.parent());
        OpenApi30Response response30 = (OpenApi30Response) parent30.createResponse();

        String statusCode = node.mapPropertyName();
        if (statusCode == null) {
            statusCode = node.parentPropertyName();
        }
        if ("default".equals(statusCode)) {
            parent30.setDefault(response30);
        } else {
            parent30.addItem(statusCode, response30);
        }

        response30.set$ref(this.updateRef(response20.get$ref()));
        this.transformResponse((OpenApi20Response) node, response30);

        this.copyExtensions((OpenApi20Response) node, response30);
        this.mapNode(node, response30);
    }

    public void visitResponseDefinition(OpenApiResponse node) {
        String name = node.mapPropertyName();
        OpenApi30Components parent30 = this.getOrCreateComponents();
        OpenApi30Response responseDef30 = (OpenApi30Response) parent30.createResponse();
        parent30.addResponse(name, responseDef30);

        this.transformResponse((OpenApi20Response) node, responseDef30);

        this.copyExtensions((OpenApi20Response) node, responseDef30);
        this.mapNode(node, responseDef30);
    }

    private void transformResponse(OpenApi20Response node, OpenApi30Response response30) {
        response30.setDescription(node.getDescription());

        if (!NodeUtil.isNullOrUndefined(node.getSchema())) {
            List<String> produces = this.findProduces(node);
            OpenApi20Schema schema = (OpenApi20Schema) node.getSchema();
            produces.forEach( ct -> {
                OpenApi30MediaType mediaType30 = (OpenApi30MediaType) response30.createMediaType();
                response30.addContent(ct, mediaType30);

                OpenApi30Schema schema30 = (OpenApi30Schema) mediaType30.createSchema();
                mediaType30.setSchema(this.toSchema(schema, schema30, true));

                if (!NodeUtil.isNullOrUndefined(node.getExamples())) {
                    JsonNode ctexample = node.getExamples().getItem(ct);
                    if (!NodeUtil.isNullOrUndefined(ctexample)) {
                        mediaType30.setExample(ctexample);
                    }
                }

                this.copyExtensions(schema, schema30);
                this.mapNode(schema, schema30);
            });
            // mark the Response as needing Content post-processing
            if (produces.size() > 1) {
                this._postProcessResponses.add(response30);
            }
        }
    }

    @Override
    public void visitSchema(Schema node) {
        if (isSchemaDefinition(node)) {
            visitSchemaDefinition((OpenApiSchema) node);
        } else {
            OpenApi20Schema schema20 = (OpenApi20Schema) node;
            // It's a property schema
            String propertyName = node.parentPropertyName();
            if ("properties".equals(propertyName)) {
                visitPropertySchema(schema20);
            } else if ("allOf".equals(propertyName)) {
                visitAllOfSchema(schema20);
            } else if ("items".equals(propertyName)) {
                visitItemsSchema(schema20);
            } else if ("additionalProperties".equals(propertyName)) {
                visitAdditionalPropertiesSchema(schema20);
            }
        }
    }

    @Override
    public void visitHeader(OpenApiHeader node) {
        OpenApi20Header header20 = (OpenApi20Header) node;
        String headerName = node.mapPropertyName();
        OpenApi30Response parent30 = (OpenApi30Response) this.lookup(node.parent());
        OpenApi30Header header30 = parent30.createHeader();
        parent30.addHeader(headerName, header30);

        header30.setDescription(header20.getDescription());
        header30.setSchema(this.toSchema(node, (OpenApi30Schema) header30.createSchema(), false));

        this.copyExtensions(header20, header30);
        this.mapNode(node, header30);
    }

    @Override
    public void visitExample(Example node) {
        // Examples are processed as part of "transformResponse"
    }

    @Override
    public void visitItems(OpenApiItems node) {
        OpenApi30Schema parent30 = this.findItemsParent(node);
        OpenApi30Schema items30 = (OpenApi30Schema) parent30.createSchema();
        parent30.setItems(items30);

        this.toSchema(node, items30, false);

        this.mapNode(node, items30);
    }

    @Override
    public void visitTag(Tag node) {
        OpenApi30Document parent30 = this.doc30;
        OpenApiTag tag30 = parent30.createTag();
        tag30.setName(node.getName());
        tag30.setDescription(node.getDescription());
        parent30.addTag(tag30);
        this.mapNode(node, tag30);
    }

    @Override
    public void visitSecurityDefinitions(OpenApiSecurityDefinitions node) {
        // OpenAPI 3 has no "Security Definitions" wrapper entity.
    }

    @Override
    public void visitSecurityScheme(SecurityScheme node) {
        String name = node.mapPropertyName();
        OpenApi20SecurityScheme scheme = (OpenApi20SecurityScheme) node;
        OpenApi30Components parent30 = this.getOrCreateComponents();
        OpenApi30SecurityScheme scheme30 = (OpenApi30SecurityScheme) parent30.createSecurityScheme();
        parent30.addSecurityScheme(name, scheme30);

        scheme30.setType(scheme.getType());
        scheme30.setDescription(scheme.getDescription());
        scheme30.setName(scheme.getName());
        scheme30.setIn(scheme.getIn());

        if (NodeUtil.equals(scheme.getType(), "oauth2")) {
            OpenApi30OAuthFlows flows30 = (OpenApi30OAuthFlows) scheme30.createOAuthFlows();
            if (NodeUtil.equals(scheme.getFlow(), "implicit")) {
                scheme30.setFlows(flows30);
                flows30.setImplicit(flows30.createOAuthFlow());
                flows30.getImplicit().setAuthorizationUrl(scheme.getAuthorizationUrl());
                if (!NodeUtil.isNullOrUndefined(scheme.getScopes())) {
                    ((OpenApiOAuthFlow) flows30.getImplicit()).setScopes(new LinkedHashMap<>());
                    scheme.getScopes().getItemNames().forEach(scopeName -> {
                        ((OpenApiOAuthFlow) flows30.getImplicit()).getScopes().put(scopeName, scheme.getScopes().getItem(scopeName));
                    });
                }
            }
            if (NodeUtil.equals(scheme.getFlow(), "accessCode")) {
                scheme30.setFlows(flows30);
                flows30.setAuthorizationCode(flows30.createOAuthFlow());
                flows30.getAuthorizationCode().setAuthorizationUrl(scheme.getAuthorizationUrl());
                flows30.getAuthorizationCode().setTokenUrl(scheme.getTokenUrl());
                if (!NodeUtil.isNullOrUndefined(scheme.getScopes())) {
                    ((OpenApiOAuthFlow) flows30.getAuthorizationCode()).setScopes(new LinkedHashMap<>());
                    scheme.getScopes().getItemNames().forEach(scopeName -> {
                        ((OpenApiOAuthFlow) flows30.getAuthorizationCode()).getScopes().put(scopeName, scheme.getScopes().getItem(scopeName));
                    });
                }
            }
            if (NodeUtil.equals(scheme.getFlow(), "password")) {
                scheme30.setFlows(flows30);
                flows30.setPassword(flows30.createOAuthFlow());
                flows30.getPassword().setTokenUrl(scheme.getTokenUrl());
                if (!NodeUtil.isNullOrUndefined(scheme.getScopes())) {
                    ((OpenApiOAuthFlow) flows30.getPassword()).setScopes(new LinkedHashMap<>());
                    scheme.getScopes().getItemNames().forEach(scopeName -> {
                        ((OpenApiOAuthFlow) flows30.getPassword()).getScopes().put(scopeName, scheme.getScopes().getItem(scopeName));
                    });
                }
            }
            if (NodeUtil.equals(scheme.getFlow(), "application")) {
                scheme30.setFlows(flows30);
                flows30.setClientCredentials(flows30.createOAuthFlow());
                flows30.getClientCredentials().setTokenUrl(scheme.getTokenUrl());
                if (!NodeUtil.isNullOrUndefined(scheme.getScopes())) {
                    ((OpenApiOAuthFlow) flows30.getClientCredentials()).setScopes(new LinkedHashMap<>());
                    scheme.getScopes().getItemNames().forEach(scopeName -> {
                        ((OpenApiOAuthFlow) flows30.getClientCredentials()).getScopes().put(scopeName, scheme.getScopes().getItem(scopeName));
                    });
                }
            }
        }

        this.mapNode(scheme, scheme30);
    }

    @Override
    public void visitScopes(OpenApiScopes node) {
        // Note: scopes are handled during the processing of the security scheme.  See `visitSecurityScheme` for details.
    }


    @Override
    public void visitXML(OpenApiXML node) {
        OpenApi20XML xml20 = (OpenApi20XML) node;
        OpenApi30Schema parent30 = (OpenApi30Schema) this.lookup(node.parent());
        OpenApi30XML xml30 = (OpenApi30XML) parent30.createXML();
        parent30.setXml(xml30);

        xml30.setName(node.getName());
        xml30.setNamespace(node.getNamespace());
        xml30.setPrefix(node.getPrefix());
        xml30.setAttribute(xml20.isAttribute());
        xml30.setWrapped(node.isWrapped());

        this.copyExtensions(xml20, xml30);
        this.mapNode(node, xml30);
    }

    public void visitSchemaDefinition(OpenApiSchema node) {
        String name = node.mapPropertyName();
        OpenApi20Schema sd20 = (OpenApi20Schema) node;

        OpenApi30Components parent30 = this.getOrCreateComponents();
        OpenApi30Schema schemaDef30 = (OpenApi30Schema) parent30.createSchema();
        parent30.addSchema(name, schemaDef30);

        this.toSchema(sd20, schemaDef30, true);

        this.copyExtensions(sd20, schemaDef30);
        this.mapNode(sd20, schemaDef30);
    }

    public void visitPropertySchema(OpenApiSchema node) {
        OpenApi20Schema ps20 = (OpenApi20Schema) node;

        String name = node.mapPropertyName();

        OpenApi30Schema parent30 = (OpenApi30Schema) this.lookup(ps20.parent());
        OpenApi30Schema property30 = (OpenApi30Schema) parent30.createSchema();
        parent30.addProperty(name, property30);

        this.toSchema(ps20, property30, true);

        this.copyExtensions(ps20, property30);
        this.mapNode(ps20, property30);
    }

    public void visitAdditionalPropertiesSchema(OpenApiSchema node) {
        OpenApi30Schema parent30 = (OpenApi30Schema) this.lookup(node.parent());
        OpenApi30Schema additionalProps30 = (OpenApi30Schema) parent30.createSchema();
        parent30.setAdditionalProperties(additionalProps30);

        this.toSchema(node, additionalProps30, true);

        this.copyExtensions((OpenApi20Schema) node, additionalProps30);
        this.mapNode(node, additionalProps30);
    }

    public void visitAllOfSchema(OpenApiSchema node) {
        OpenApi30Schema parent30 =  (OpenApi30Schema) this.lookup(node.parent());
        OpenApi30Schema allOf30 = (OpenApi30Schema) parent30.createSchema();
        parent30.addAllOf(allOf30);

        this.toSchema(node, allOf30, true);

        this.copyExtensions((OpenApi20Schema) node, allOf30);
        this.mapNode(node, allOf30);
    }

    public void visitItemsSchema(OpenApiSchema node) {
        OpenApi30Schema parent30 = (OpenApi30Schema) this.lookup(node.parent());
        OpenApi30Schema items30 = (OpenApi30Schema) parent30.getItems();

        // Note: OpenAPI 3+ does not support an array of Schemas for the "items" property.  So this
        // part of the transformation is potentially lossy.  We'll keep the first schema and drop the
        // rest (if any).
        if (items30 == null) {
            items30 = (OpenApi30Schema) parent30.createSchema();
            parent30.setItems(items30);
        }

        this.toSchema(node, items30, true);

        this.copyExtensions((OpenApi20Schema) node, items30);
        this.mapNode(node, items30);
    }

    @Override
    public void visitDefinitions(OpenApiDefinitions node) {
        // Note: there is no "definitions" entity in 3.0, so nothing to do here.
    }

    @Override
    public void visitParameterDefinitions(OpenApiParameterDefinitions node) {
        // Note: there is no "parameters definitions" entity in 3.0, so nothing to do here.
    }

    @Override
    public void visitResponseDefinitions(OpenApiResponseDefinitions node) {
        // Note: there is no "responses definitions" entity in 3.0, so nothing to do here.
    }

    // TODO we have a visitor to determine if the parent node is a definition container. Should use that instead.

    private boolean isParameterDefinition(Node node) {
        return ("parameters".equals(this.traversalContext.getMostRecentPropertyStep()) && this.traversalContext.getAllSteps().size() == 2);
    }

    private boolean isSchemaDefinition(Node node) {
        return ("definitions".equals(this.traversalContext.getMostRecentPropertyStep()) && this.traversalContext.getAllSteps().size() == 2);
    }

    private boolean isResponseDefinition(Node node) {
        return ("responses".equals(this.traversalContext.getMostRecentPropertyStep()) && this.traversalContext.getAllSteps().size() == 2);
    }

    private void mapNode(Node from, Node to) {
        NodePath nodePath = NodePathUtil.createNodePath(from);
        String mapIndex = nodePath.toString();
        this._nodeMap.put(mapIndex, to);
    }

    private Node lookup(Node node) {
        NodePath nodePath = NodePathUtil.createNodePath(node);
        String mapIndex = nodePath.toString();
        return this._nodeMap.get(mapIndex);
    }

    private OpenApi30Components getOrCreateComponents() {
        if (NodeUtil.isNullOrUndefined(this.doc30.getComponents())) {
            this.doc30.setComponents(this.doc30.createComponents());
        }
        return (OpenApi30Components) this.doc30.getComponents();
    }

    // from : OpenApi20ParameterBase | OpenApi20Header | OpenApi20Items | OpenApi20Schema | OpenApi20SchemaDefinition
    @SuppressWarnings("unchecked")
    private OpenApi30Schema toSchema(Node from, OpenApi30Schema schema30, boolean isSchema) {
        String type = (String) NodeUtil.getProperty(from, "type");
        String format = (String) NodeUtil.getProperty(from, "format");
        Object items = NodeUtil.getProperty(from, "items");
        JsonNode default_ = (JsonNode) NodeUtil.getProperty(from, "default");
        Number maximum = (Number) NodeUtil.getProperty(from, "maximum");
        Boolean exclusiveMaximum = (Boolean) NodeUtil.getProperty(from, "exclusiveMaximum");
        Number minimum = (Number) NodeUtil.getProperty(from, "minimum");
        Boolean exclusiveMinimum = (Boolean) NodeUtil.getProperty(from, "exclusiveMinimum");
        Integer maxLength = (Integer) NodeUtil.getProperty(from, "maxLength");
        Integer minLength = (Integer) NodeUtil.getProperty(from, "minLength");
        String pattern = (String) NodeUtil.getProperty(from, "pattern");
        Integer maxItems = (Integer) NodeUtil.getProperty(from, "maxItems");
        Integer minItems = (Integer) NodeUtil.getProperty(from, "minItems");
        Boolean uniqueItems = (Boolean) NodeUtil.getProperty(from, "uniqueItems");
        List<JsonNode> enum_ = (List<JsonNode>) NodeUtil.getProperty(from, "enum");
        Number multipleOf = (Number) NodeUtil.getProperty(from, "multipleOf");

        schema30.setType(type);
        schema30.setFormat(format);
        if (NodeUtil.equals(type, "file")) {
            schema30.setType("string");
            schema30.setFormat("binary");
        }

        if (NodeUtil.isNode(items)) {
            // This is done so that we can lookup the appropriate parent for an "Items" object later
            // on in the visit.  This is a special case because we're introducing a new OpenApi30Schema
            // entity in between e.g. a Response and the ItemsSchema - whereas in 2.0 the ItemsSchema
            // is a direct child of Response and Parameter.  So when visiting a Items, we cannot lookup
            // the new 3.0 Schema using the Items' parent (because the parent maps to something else -
            // the grandparent, in fact).  THIS IS ONLY A PROBLEM FOR "ITEMS" ON PARAM AND RESPONSE.
            ((Node) items).setNodeAttribute("_transformation_items-parent", schema30);
        } else {
            // TODO handle the case where "items" is a list of items!!
        }
        // Note: Not sure what to do with the "collectionFormat" of a schema.  Dropping it for now.
        //schema30.collectionFormat = collectionFormat;
        schema30.setDefault(default_);
        schema30.setMaximum(maximum);
        schema30.setExclusiveMaximum(exclusiveMaximum);
        schema30.setMinimum(minimum);
        schema30.setExclusiveMinimum(exclusiveMinimum);
        schema30.setMaxLength(maxLength);
        schema30.setMinLength(minLength);
        schema30.setPattern(pattern);
        schema30.setMaxItems(maxItems);
        schema30.setMinItems(minItems);
        schema30.setUniqueItems(uniqueItems);
        schema30.setEnum(enum_);
        schema30.setMultipleOf(multipleOf);

        if (isSchema) {
            OpenApi20Schema schema20 = (OpenApi20Schema) from;
            schema30.set$ref(this.updateRef(schema20.get$ref()));
            if (schema20.getAdditionalProperties() != null && schema20.getAdditionalProperties().isBoolean()) {
                BooleanUnionValue booleanValue = new BooleanUnionValueImpl(schema20.getAdditionalProperties().asBoolean());
                schema30.setAdditionalProperties(booleanValue);
            }
            schema30.setReadOnly(schema20.isReadOnly());
            schema30.setExample(schema20.getExample());
            schema30.setTitle(schema20.getTitle());
            schema30.setDescription(schema20.getDescription());
            schema30.setMaxProperties(schema20.getMaxProperties());
            schema30.setMinProperties(schema20.getMinProperties());
            schema30.setRequired(schema20.getRequired());

            if (!NodeUtil.isNullOrUndefined(schema20.getDiscriminator())) {
                OpenApi30Discriminator discriminator30 = (OpenApi30Discriminator) schema30.createDiscriminator();
                schema30.setDiscriminator(discriminator30);
                discriminator30.setPropertyName(schema20.getDiscriminator());
            }
        }

        return schema30;
    }

    private OpenApi30Schema findItemsParent(OpenApiItems node) {
        OpenApi30Schema itemsParent = (OpenApi30Schema) node.getNodeAttribute("_transformation_items-parent");
        if (NodeUtil.isNullOrUndefined(itemsParent)) {
            itemsParent = (OpenApi30Schema) this.lookup(node.parent());
        }
        return itemsParent;
    }

    private OpenApi20Operation findParentOperation(Parameter node) {
        OperationFinder finder = new OperationFinder();
        Library.visitTree(node, finder, TraverserDirection.up);
        return (OpenApi20Operation) finder.found;
    }

    private List<String> findProduces(Node node) {
        ConsumesProducesFinder finder = new ConsumesProducesFinder();
        Library.visitTree(node, finder, TraverserDirection.up);
        List<String> produces = finder.produces;
        if (NodeUtil.isNullOrUndefined(produces) || produces.size() == 0) {
            produces = new ArrayList<>();
            produces.add("application/json");
        }
        return produces;
    }

    private List<String> findConsumes(Node node) {
        ConsumesProducesFinder finder = new ConsumesProducesFinder();
        Library.visitTree(node, finder, TraverserDirection.up);
        List<String> consumes = finder.consumes;
        if (NodeUtil.isNullOrUndefined(consumes) || consumes.size() == 0) {
            consumes = new ArrayList<>();
            consumes.add("application/json");
        }
        return consumes;
    }

    private void collectionFormatToStyleAndExplode(OpenApi20Parameter node, OpenApi30Parameter param30) {
        if (NodeUtil.equals(node.getType(), "array") && NodeUtil.equals(node.getCollectionFormat(), "multi") && (NodeUtil.equals(node.getIn(), "query") || NodeUtil.equals(node.getIn(), "cookie"))) {
            param30.setStyle("form");
            param30.setExplode(true);
            return;
        }
        if (NodeUtil.equals(node.getType(), "array") && NodeUtil.equals(node.getCollectionFormat(), "csv") && (NodeUtil.equals(node.getIn(), "query") || NodeUtil.equals(node.getIn(), "cookie"))) {
            param30.setStyle("form");
            param30.setExplode(false);
            return;
        }
        if (NodeUtil.equals(node.getType(), "array") && NodeUtil.equals(node.getCollectionFormat(), "csv") && (NodeUtil.equals(node.getIn(), "path") || NodeUtil.equals(node.getIn(), "header"))) {
            param30.setStyle("simple");
            return;
        }
        if (NodeUtil.equals(node.getType(), "array") && NodeUtil.equals(node.getCollectionFormat(), "ssv") && NodeUtil.equals(node.getIn(), "query")) {
            param30.setStyle("spaceDelimited");
            return;
        }
        if (NodeUtil.equals(node.getType(), "array") && NodeUtil.equals(node.getCollectionFormat(), "pipes") && NodeUtil.equals(node.getIn(), "query")) {
            param30.setStyle("pipeDelimited");
            return;
        }
    }

    private boolean isFormDataMimeType(String mimetype) {
        return !NodeUtil.isNullOrUndefined(mimetype) && (NodeUtil.equals(mimetype, "multipart/form-data") || NodeUtil.equals(mimetype, "application/x-www-form-urlencoded"));
    }

    private boolean hasFormDataMimeType(List<String> mimetypes) {
        if (!NodeUtil.isNullOrUndefined(mimetypes)) {
            for (String mt : mimetypes) {
                if (this.isFormDataMimeType(mt)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRef(Referenceable node) {
        String $ref = node.get$ref();
        return !NodeUtil.isNullOrUndefined($ref) && $ref.length() > 0;
    }

    private String updateRef(String $ref) {
        if (NodeUtil.isNullOrUndefined($ref) || $ref.length() == 0) {
            return $ref;
        }
        String[] split = $ref.split("/");
        if (NodeUtil.equals(split[0], "#")) {
            if (NodeUtil.equals(split[1], "definitions")) {
                return $ref.replace("#/definitions/", "#/components/schemas/");
            } else if (NodeUtil.equals(split[1], "parameters")) {
                return $ref.replace("#/parameters/", "#/components/parameters/");
            } else if (NodeUtil.equals(split[1], "responses")) {
                return $ref.replace("#/responses/", "#/components/responses/");
            }
        }
        return $ref;
    }

    /**
     * Called when visiting is complete.  Any additional processing of the result can
     * be done here.
     */
    private void postProcess() {
        // Post process all of the responses that require it.  Responses may require post-processing
        // when a response has multiple @Produces content types, which results in multiple MimeType
        // entities in the 3.0 Response 'content'.  When this happens, only one of the mime types
        // will contain the visited (and thus transformed) data model.  So we must post-process them
        // to "clone" that info to the other mime types.  Otherwise we'll have a full mime type
        // definition for only ONE of the mime types, and partial definitions for the rest.
        this._postProcessResponses.forEach( response -> {
            // First, figure out which of the media types is the largest (has the most content)
            int largest = 0;
            OpenApi30MediaType srcMt = null;
            Collection<OpenApi3xMediaType> mediaTypes = NodeUtil.getMapValues(response.getContent());
            for (OpenApi3xMediaType mt : mediaTypes) {
                int size = JsonUtil.stringify(Library.writeNode(mt.getSchema())).length();
                if (size > largest) {
                    largest = size;
                    srcMt = (OpenApi30MediaType) mt;
                }
            }
            // Now clone the contents of the largest media type into all the others.
            for (OpenApi3xMediaType mt : mediaTypes) {
                if (srcMt != mt) {
                    ObjectNode src = Library.writeNode(srcMt.getSchema());
                    Library.readNode(src, mt.getSchema());
                }
            }
        });
    }

    private void copyExtensions(Extensible from, Extensible to) {
        Map<String, JsonNode> extensions = from.getExtensions();
        if (extensions != null) {
            Collection<String> keys = extensions.keySet();
            for (String key : keys) {
                JsonNode value = extensions.get(key);
                to.addExtension(key, value);
            }
        }
    }

}
