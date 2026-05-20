package io.apitomy.datamodels.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.cmd.commands.AddCallbackCommand;
import io.apitomy.datamodels.cmd.commands.AddChannelItemCommand;
import io.apitomy.datamodels.cmd.commands.AddExampleCommand;
import io.apitomy.datamodels.cmd.commands.AddExampleDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.AddExtensionCommand;
import io.apitomy.datamodels.cmd.commands.AddHeaderDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.AddLinkCommand;
import io.apitomy.datamodels.cmd.commands.AddMediaTypeCommand;
import io.apitomy.datamodels.cmd.commands.AddOperationSecurityRequirementCommand;
import io.apitomy.datamodels.cmd.commands.AddParameterCommand;
import io.apitomy.datamodels.cmd.commands.AddParameterDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.AddPathItemCommand;
import io.apitomy.datamodels.cmd.commands.AddRequestBodyCommand;
import io.apitomy.datamodels.cmd.commands.AddRequestBodyDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.AddResponseCommand;
import io.apitomy.datamodels.cmd.commands.AddResponseDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.AddResponseHeaderCommand;
import io.apitomy.datamodels.cmd.commands.AddSchemaDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.AddSchemaPropertyCommand;
import io.apitomy.datamodels.cmd.commands.AddSecurityRequirementCommand;
import io.apitomy.datamodels.cmd.commands.AddSecuritySchemeCommand;
import io.apitomy.datamodels.cmd.commands.AddServerCommand;
import io.apitomy.datamodels.cmd.commands.AddServerVariableCommand;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.cmd.commands.AddTagCommand;
import io.apitomy.datamodels.cmd.commands.ChangeContactCommand;
import io.apitomy.datamodels.cmd.commands.ChangeDescriptionCommand;
import io.apitomy.datamodels.cmd.commands.ChangeExtensionCommand;
import io.apitomy.datamodels.cmd.commands.ChangeLicenseCommand;
import io.apitomy.datamodels.cmd.commands.ChangeMediaTypeSchemaCommand;
import io.apitomy.datamodels.cmd.commands.ChangePropertyCommand;
import io.apitomy.datamodels.cmd.commands.ChangeTitleCommand;
import io.apitomy.datamodels.cmd.commands.ChangeVersionCommand;
import io.apitomy.datamodels.cmd.commands.CreateOperationCommand;
import io.apitomy.datamodels.cmd.commands.CreatePathCommand;
import io.apitomy.datamodels.cmd.commands.CreateSchemaCommand;
import io.apitomy.datamodels.cmd.commands.DeleteCallbackCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllChildSchemasCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllExamplesCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllHeadersCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllOperationsCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllParametersCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllPropertiesCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllResponsesCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllSecurityRequirementsCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllSecuritySchemesCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllServersCommand;
import io.apitomy.datamodels.cmd.commands.DeleteAllTagsCommand;
import io.apitomy.datamodels.cmd.commands.DeleteContactCommand;
import io.apitomy.datamodels.cmd.commands.DeleteExtensionCommand;
import io.apitomy.datamodels.cmd.commands.DeleteLicenseCommand;
import io.apitomy.datamodels.cmd.commands.DeleteLinkCommand;
import io.apitomy.datamodels.cmd.commands.DeleteMediaTypeCommand;
import io.apitomy.datamodels.cmd.commands.DeleteOperationCommand;
import io.apitomy.datamodels.cmd.commands.DeleteOperationSecurityRequirementCommand;
import io.apitomy.datamodels.cmd.commands.DeleteParameterCommand;
import io.apitomy.datamodels.cmd.commands.DeleteExampleDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.DeleteHeaderDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.DeleteParameterDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.DeleteRequestBodyDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.DeletePathCommand;
import io.apitomy.datamodels.cmd.commands.DeleteRequestBodyCommand;
import io.apitomy.datamodels.cmd.commands.DeleteResponseCommand;
import io.apitomy.datamodels.cmd.commands.DeleteResponseHeaderCommand;
import io.apitomy.datamodels.cmd.commands.DeleteSchemaCommand;
import io.apitomy.datamodels.cmd.commands.DeleteSchemaPropertyCommand;
import io.apitomy.datamodels.cmd.commands.DeleteSecurityRequirementCommand;
import io.apitomy.datamodels.cmd.commands.DeleteSecuritySchemeCommand;
import io.apitomy.datamodels.cmd.commands.DeleteServerCommand;
import io.apitomy.datamodels.cmd.commands.DeleteServerVariableCommand;
import io.apitomy.datamodels.cmd.commands.DeleteTagCommand;
import io.apitomy.datamodels.cmd.commands.EnsureChildNodeCommand;
import io.apitomy.datamodels.cmd.commands.RefactorCallbackDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.RefactorExampleDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.RefactorHeaderDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.RefactorLinkDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.RefactorParameterDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.RefactorRequestBodyDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.RefactorResponseDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.RefactorSchemaDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.RenameTagCommand;
import io.apitomy.datamodels.cmd.commands.ReplaceOperationCommand;
import io.apitomy.datamodels.cmd.commands.ReplacePathItemCommand;
import io.apitomy.datamodels.cmd.commands.ReplaceResponseDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.ReplaceSchemaDefinitionCommand;
import io.apitomy.datamodels.cmd.commands.ReplaceSecurityRequirementCommand;
import io.apitomy.datamodels.cmd.commands.SetExternalDocsCommand;
import io.apitomy.datamodels.cmd.commands.UpdateNodeCommand;
import io.apitomy.datamodels.cmd.commands.UpdateSecuritySchemeCommand;
import io.apitomy.datamodels.models.Extensible;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.SecurityRequirement;
import io.apitomy.datamodels.models.SecurityRequirementsParent;
import io.apitomy.datamodels.models.asyncapi.AsyncApiServer;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiEncoding;
import io.apitomy.datamodels.models.openapi.OpenApiExamplesParent;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.OpenApiHeadersParent;
import io.apitomy.datamodels.models.openapi.OpenApiMediaType;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiParametersParent;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.openapi.OpenApiServer;
import io.apitomy.datamodels.models.openapi.OpenApiServersParent;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Operation;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Operation;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.util.CommandUtil;

import java.util.List;

public class CommandFactory {

    public static ICommand create(String cmdType) {
        return CommandUtil.create(cmdType);
    }

    public static ICommand unmarshall(ObjectNode from) {
        return CommandUtil.unmarshall(from);
    }

    /**
     * Marshalls a command into a JSON object for storage or transmission.
     * @param command the command to serialize
     * @return the JSON representation of the command
     */
    public static ObjectNode marshall(ICommand command) {
        return CommandUtil.marshall(command);
    }

    public static ICommand createAddHeaderExampleCommand(OpenApiHeader header, JsonNode example,
                                                         String exampleName, String exampleSummary, String exampleDescription) {
        return new AddExampleCommand((OpenApiExamplesParent) header, example, exampleName, exampleSummary, exampleDescription);
    }
    public static ICommand createAddParameterExampleCommand(OpenApiParameter parameter, JsonNode example,
                                                            String exampleName, String exampleSummary, String exampleDescription) {
        return new AddExampleCommand((OpenApiExamplesParent) parameter, example, exampleName, exampleSummary, exampleDescription);
    }
    public static ICommand createAddHeaderExampleCommand(OpenApiMediaType mediaType, JsonNode example,
                                                         String exampleName, String exampleSummary, String exampleDescription) {
        return new AddExampleCommand((OpenApiExamplesParent) mediaType, example, exampleName, exampleSummary, exampleDescription);
    }

    public static ICommand createAddResponseDefinitionCommand(String definitionName, ObjectNode from) {
        return new AddResponseDefinitionCommand(definitionName, from);
    }

    public static ICommand createAddParameterDefinitionCommand(String definitionName, ObjectNode from) {
        return new AddParameterDefinitionCommand(definitionName, from);
    }

    /**
     * Creates a command to add a new reusable example definition to the document.
     * @param definitionName the name of the example definition
     * @param from the source object for the example definition
     * @return the command
     */
    public static ICommand createAddExampleDefinitionCommand(String definitionName, ObjectNode from) {
        return new AddExampleDefinitionCommand(definitionName, from);
    }
  
    /**
     * Creates a command to add a header definition to the document.
     * @param definitionName the name of the header definition
     * @param from the source object for the header definition
     * @return the command
     */
    public static ICommand createAddHeaderDefinitionCommand(String definitionName, ObjectNode from) {
        return new AddHeaderDefinitionCommand(definitionName, from);
    }

    /**
     * Creates a command to delete a reusable parameter definition.
     * @param definitionName the name of the parameter definition to delete
     * @return the command
     */
    public static ICommand createDeleteParameterDefinitionCommand(String definitionName) {
        return new DeleteParameterDefinitionCommand(definitionName);
    }

    /**
     * Creates a command to delete a reusable header definition.
     * @param definitionName the name of the header definition to delete
     * @return the command
     */
    public static ICommand createDeleteHeaderDefinitionCommand(String definitionName) {
        return new DeleteHeaderDefinitionCommand(definitionName);
    }

    /**
     * Creates a command to add a request body definition to the document.
     * @param definitionName the name of the request body definition
     * @param from the source object for the request body definition
     * @return the command
     */
    public static ICommand createAddRequestBodyDefinitionCommand(String definitionName, ObjectNode from) {
        return new AddRequestBodyDefinitionCommand(definitionName, from);
    }

    /**
     * Creates a command to delete a reusable request body definition.
     * @param definitionName the name of the request body definition to delete
     * @return the command
     */
    public static ICommand createDeleteRequestBodyDefinitionCommand(String definitionName) {
        return new DeleteRequestBodyDefinitionCommand(definitionName);
    }

    /**
     * Creates a command to delete a reusable example definition.
     * @param definitionName the name of the example definition to delete
     * @return the command
     */
    public static ICommand createDeleteExampleDefinitionCommand(String definitionName) {
        return new DeleteExampleDefinitionCommand(definitionName);
    }

    public static ICommand createAddSchemaDefinitionCommand(String definitionName, ObjectNode from) {
        return new AddSchemaDefinitionCommand(definitionName, from);
    }

    /**
     * Creates a command to add a new property to a schema definition.
     * @param schemaDefinitionName the name of the schema definition
     * @param propertyName the name of the property to add
     * @param propertySchema the JSON schema for the property
     * @return the command
     */
    public static ICommand createAddSchemaPropertyCommand(String schemaDefinitionName,
            String propertyName, ObjectNode propertySchema) {
        return new AddSchemaPropertyCommand(schemaDefinitionName, propertyName, propertySchema);
    }

    /**
     * Creates a command to delete a single property from a schema definition.
     * @param schemaDefinitionName the name of the schema definition
     * @param propertyName the name of the property to delete
     * @return the command
     */
    public static ICommand createDeleteSchemaPropertyCommand(String schemaDefinitionName,
            String propertyName) {
        return new DeleteSchemaPropertyCommand(schemaDefinitionName, propertyName);
    }

    public static ICommand createAddDocumentSecurityRequirementCommand(
            OpenApiDocument document, SecurityRequirement requirement) {
        return new AddSecurityRequirementCommand((SecurityRequirementsParent) document, requirement);
    }
    public static ICommand createAddOperationSecurityRequirementCommand(
            OpenApiOperation operation, SecurityRequirement requirement) {
        return new AddSecurityRequirementCommand((SecurityRequirementsParent) operation, requirement);
    }
    public static ICommand createAddServerSecurityRequirementCommand(
            AsyncApiServer server, SecurityRequirement requirement) {
        return new AddSecurityRequirementCommand((SecurityRequirementsParent) server, requirement);
    }

    public static <T> ICommand createChangePropertyCommand(Node node, String property, T newValue) {
        return new ChangePropertyCommand<T>(node, property, newValue);
    }

    public static ICommand createAddChannelItemCommand(String channelItemName, ObjectNode from) {
        return new AddChannelItemCommand(channelItemName, from);
    }

    public static ICommand createAddPathItemCommand(String pathItemName, ObjectNode from) {
        return new AddPathItemCommand(pathItemName, from);
    }

    public static ICommand createChangeTitleCommand(String newTitle) {
        return new ChangeTitleCommand(newTitle);
    }

    public static ICommand createChangeDescriptionCommand(String newDescription) {
        return new ChangeDescriptionCommand(newDescription);
    }

    public static ICommand createChangeVersionCommand(String newVersion) {
        return new ChangeVersionCommand(newVersion);
    }

    public static ICommand createChangeContactCommand(String name, String email, String url) {
        return new ChangeContactCommand(name, email, url);
    }

    public static ICommand createChangeLicenseCommand(String name, String url) {
        return new ChangeLicenseCommand(name, url);
    }

    /**
     * Creates a command to set external documentation on a node.
     * @param parent the parent node (Document, Tag, Operation, or Schema)
     * @param url the external documentation URL
     * @param description an optional description
     * @return the command
     */
    public static ICommand createSetExternalDocsCommand(Node parent, String url, String description) {
        return new SetExternalDocsCommand(parent, url, description);
    }

    public static ICommand createDeleteLicenseCommand(Info info) {
        return new DeleteLicenseCommand(info);
    }

    public static ICommand createDeleteContactCommand(Info info) {
        return new DeleteContactCommand(info);
    }

    public static ICommand createDeleteExtensionCommand(Extensible parent, String extensionName) {
        return new DeleteExtensionCommand(parent, extensionName);
    }

    public static ICommand createDeleteMediaTypeCommand(OpenApiMediaType mediaType) {
        return new DeleteMediaTypeCommand(mediaType);
    }

    public static ICommand createDeleteAllChildSchemasCommand(Schema parent, String type) {
        return new DeleteAllChildSchemasCommand(parent, type);
    }

    public static ICommand createDeleteAllMediaTypeExamplesCommand(OpenApiMediaType mediaType) {
        return new DeleteAllExamplesCommand((OpenApiExamplesParent) mediaType);
    }

    public static ICommand createDeleteAllParameterExamplesCommand(OpenApiParameter parameter) {
        return new DeleteAllExamplesCommand((OpenApiExamplesParent) parameter);
    }

    public static ICommand createDeleteAllHeaderExamplesCommand(OpenApiHeader header) {
        return new DeleteAllExamplesCommand((OpenApiExamplesParent) header);
    }

    public static ICommand createDeleteAllResponseHeadersCommand(OpenApiResponse header) {
        return new DeleteAllHeadersCommand((OpenApiHeadersParent) header);
    }
    public static ICommand createDeleteAllEncodingHeadersCommand(OpenApiEncoding header) {
        return new DeleteAllHeadersCommand((OpenApiHeadersParent) header);
    }

    public static ICommand createDeleteAllPathItemOperationsCommand(OpenApiPathItem pathItem) {
        return new DeleteAllOperationsCommand(pathItem);
    }

    public static ICommand createDeleteAllPathItemParametersCommand(OpenApiPathItem parent, String type) {
        return new DeleteAllParametersCommand((OpenApiParametersParent) parent, type);
    }

    public static ICommand createDeleteAllOperationParametersCommand(OpenApiOperation parent, String type) {
        return new DeleteAllParametersCommand((OpenApiParametersParent) parent, type);
    }

    public static ICommand createDeleteAllPropertiesCommand(Schema schema) {
        return new DeleteAllPropertiesCommand(schema);
    }

    public static ICommand createDeleteAllResponsesCommand(OpenApiOperation operation) {
        return new DeleteAllResponsesCommand(operation);
    }

    public static ICommand createDeleteAllServerSecurityRequirementsCommand(AsyncApiServer server) {
        return new DeleteAllSecurityRequirementsCommand(server);
    }
    public static ICommand createDeleteAllOperationSecurityRequirementsCommand(OpenApiOperation operation) {
        return new DeleteAllSecurityRequirementsCommand(operation);
    }
    public static ICommand createDeleteAllDocumentSecurityRequirementsCommand(OpenApiDocument document) {
        return new DeleteAllSecurityRequirementsCommand(document);
    }

    public static ICommand createDeleteAllSecuritySchemesCommand() {
        return new DeleteAllSecuritySchemesCommand();
    }

    public static ICommand createDeleteAllTagsCommand() {
        return new DeleteAllTagsCommand();
    }

    public static ICommand createDeleteAllDocumentServersCommand(OpenApiDocument document) {
        return new DeleteAllServersCommand((OpenApiServersParent) document);
    }
    public static ICommand createDeleteAllPathItemServersCommand(OpenApiPathItem pathItem) {
        return new DeleteAllServersCommand((OpenApiServersParent) pathItem);
    }
    public static ICommand createDeleteAllOperationServersCommand(OpenApiOperation operation) {
        return new DeleteAllServersCommand((OpenApiServersParent) operation);
    }

    public static ICommand createReplacePathItemCommand(OpenApiPathItem old, OpenApiPathItem replacement) {
        return new ReplacePathItemCommand(old, replacement);
    }

    public static ICommand createReplaceOperationCommand(OpenApiOperation old,
                                                               OpenApiOperation replacement) {
        return new ReplaceOperationCommand(old, replacement);
    }

    public static ICommand createReplaceSchemaDefinitionCommand(OpenApiSchema old, OpenApiSchema replacement) {
        return new ReplaceSchemaDefinitionCommand(old, replacement);
    }

    public static ICommand createReplaceResponseDefinitionCommand(OpenApiResponse old, OpenApiResponse replacement) {
        return new ReplaceResponseDefinitionCommand(old, replacement);
    }

    public static ICommand createReplaceSecurityRequirementCommand(SecurityRequirement old,
                                                                         SecurityRequirement replacement) {
        return new ReplaceSecurityRequirementCommand(old, replacement);
    }

    // --- Extension commands ---

    public static ICommand createAddExtensionCommand(Extensible parent, String name, JsonNode value) {
        return new AddExtensionCommand(parent, name, value);
    }

    public static ICommand createChangeExtensionCommand(Extensible parent, String name, JsonNode newValue) {
        return new ChangeExtensionCommand(parent, name, newValue);
    }

    // --- Tag commands ---

    public static ICommand createAddTagCommand(String tagName, String tagDescription) {
        return new AddTagCommand(tagName, tagDescription);
    }

    public static ICommand createDeleteTagCommand(String tagName) {
        return new DeleteTagCommand(tagName);
    }

    public static ICommand createRenameTagCommand(String oldName, String newName) {
        return new RenameTagCommand(oldName, newName);
    }

    public static ICommand createRefactorSchemaDefinitionCommand(String oldName, String newName) {
        return new RefactorSchemaDefinitionCommand(oldName, newName);
    }

    public static ICommand createRefactorResponseDefinitionCommand(String oldName, String newName) {
        return new RefactorResponseDefinitionCommand(oldName, newName);
    }

    public static ICommand createRefactorParameterDefinitionCommand(String oldName, String newName) {
        return new RefactorParameterDefinitionCommand(oldName, newName);
    }

    public static ICommand createRefactorHeaderDefinitionCommand(String oldName, String newName) {
        return new RefactorHeaderDefinitionCommand(oldName, newName);
    }

    public static ICommand createRefactorCallbackDefinitionCommand(String oldName, String newName) {
        return new RefactorCallbackDefinitionCommand(oldName, newName);
    }

    public static ICommand createRefactorExampleDefinitionCommand(String oldName, String newName) {
        return new RefactorExampleDefinitionCommand(oldName, newName);
    }

    public static ICommand createRefactorLinkDefinitionCommand(String oldName, String newName) {
        return new RefactorLinkDefinitionCommand(oldName, newName);
    }

    public static ICommand createRefactorRequestBodyDefinitionCommand(String oldName, String newName) {
        return new RefactorRequestBodyDefinitionCommand(oldName, newName);
    }

    // --- Server commands ---

    public static ICommand createAddServerCommand(OpenApiServersParent parent, String serverUrl,
                                                  String serverDescription) {
        return new AddServerCommand(parent, serverUrl, serverDescription);
    }

    public static ICommand createDeleteServerCommand(OpenApiServersParent parent, String serverUrl) {
        return new DeleteServerCommand(parent, serverUrl);
    }

    public static ICommand createAddServerCommand(AsyncApiDocument document, String serverName,
                                                  String serverUrl, String serverDescription) {
        return new AddServerCommand(document, serverName, serverUrl, serverDescription);
    }

    public static ICommand createDeleteServerCommand(AsyncApiDocument document, String serverName) {
        return new DeleteServerCommand(document, serverName);
    }

    public static ICommand createDeleteAllServersCommand(AsyncApiDocument document) {
        return new DeleteAllServersCommand(document);
    }

    // --- Server Variable commands ---

    public static ICommand createAddServerVariableCommand(OpenApiServer server,
                                                          String variableName, String defaultValue,
                                                          String description, List<String> enumValues) {
        return new AddServerVariableCommand(server, variableName, defaultValue, description, enumValues);
    }

    public static ICommand createDeleteServerVariableCommand(OpenApiServer server,
                                                             String variableName) {
        return new DeleteServerVariableCommand(server, variableName);
    }

    // --- Path commands ---

    public static ICommand createCreatePathCommand(String pathName) {
        return new CreatePathCommand(pathName);
    }

    public static ICommand createDeletePathCommand(String pathName) {
        return new DeletePathCommand(pathName);
    }

    // --- Operation commands ---

    public static ICommand createCreateOperationCommand(OpenApiPathItem pathItem, String method) {
        return new CreateOperationCommand(pathItem, method);
    }

    public static ICommand createDeleteOperationCommand(OpenApiPathItem pathItem, String method) {
        return new DeleteOperationCommand(pathItem, method);
    }

    // --- Parameter commands ---

    public static ICommand createAddParameterCommand(OpenApiParametersParent parent, String parameterName,
                                                     String parameterLocation, String parameterDescription,
                                                     boolean parameterRequired, String parameterType) {
        return new AddParameterCommand(parent, parameterName, parameterLocation, parameterDescription,
                parameterRequired, parameterType);
    }

    public static ICommand createDeleteParameterCommand(OpenApiParametersParent parent, String parameterName,
                                                        String parameterLocation) {
        return new DeleteParameterCommand(parent, parameterName, parameterLocation);
    }

    // --- Request body commands ---

    public static ICommand createAddRequestBodyCommand(OpenApi30Operation operation) {
        return new AddRequestBodyCommand(operation);
    }

    public static ICommand createAddRequestBodyCommand(OpenApi31Operation operation) {
        return new AddRequestBodyCommand(operation);
    }

    public static ICommand createDeleteRequestBodyCommand(OpenApi30Operation operation) {
        return new DeleteRequestBodyCommand(operation);
    }

    public static ICommand createDeleteRequestBodyCommand(OpenApi31Operation operation) {
        return new DeleteRequestBodyCommand(operation);
    }

    // --- Response commands ---

    public static ICommand createAddResponseCommand(OpenApiOperation operation, String statusCode,
                                                    String description) {
        return new AddResponseCommand(operation, statusCode, description);
    }

    public static ICommand createDeleteResponseCommand(OpenApiOperation operation, String statusCode) {
        return new DeleteResponseCommand(operation, statusCode);
    }

    // --- Response header commands ---

    public static ICommand createAddResponseHeaderCommand(OpenApiHeadersParent response, String headerName,
                                                          String description, String schemaType,
                                                          String schemaRef) {
        return new AddResponseHeaderCommand(response, headerName, description, schemaType, schemaRef);
    }

    public static ICommand createDeleteResponseHeaderCommand(OpenApiHeadersParent response,
                                                             String headerName) {
        return new DeleteResponseHeaderCommand(response, headerName);
    }

    // --- Link commands ---

    /**
     * Creates a command to add a link to a response or components.
     * @param parent the parent node (response or components)
     * @param linkName the name of the link
     * @param from the link definition as a JSON object
     * @return the command
     */
    public static ICommand createAddLinkCommand(Node parent, String linkName, ObjectNode from) {
        return new AddLinkCommand(parent, linkName, from);
    }

    /**
     * Creates a command to delete a link from a response or components.
     * @param parent the parent node (response or components)
     * @param linkName the name of the link to delete
     * @return the command
     */
    public static ICommand createDeleteLinkCommand(Node parent, String linkName) {
        return new DeleteLinkCommand(parent, linkName);
    }

    // --- Callback commands ---

    /**
     * Creates a command to add a callback to an operation or components.
     * @param parent the parent node (operation or components)
     * @param callbackName the name of the callback
     * @param from the callback definition as a JSON object
     * @return the command
     */
    public static ICommand createAddCallbackCommand(Node parent, String callbackName, ObjectNode from) {
        return new AddCallbackCommand(parent, callbackName, from);
    }

    /**
     * Creates a command to delete a callback from an operation or components.
     * @param parent the parent node (operation or components)
     * @param callbackName the name of the callback to delete
     * @return the command
     */
    public static ICommand createDeleteCallbackCommand(Node parent, String callbackName) {
        return new DeleteCallbackCommand(parent, callbackName);
    }

    // --- Media type commands ---

    public static ICommand createAddMediaTypeCommand(Node parent, String mediaTypeName) {
        return new AddMediaTypeCommand(parent, mediaTypeName);
    }

    public static ICommand createChangeMediaTypeSchemaCommand(OpenApiMediaType mediaType, String schemaRef,
                                                              String schemaType) {
        return new ChangeMediaTypeSchemaCommand(mediaType, schemaRef, schemaType);
    }

    // --- Schema commands ---

    public static ICommand createCreateSchemaCommand(String schemaName) {
        return new CreateSchemaCommand(schemaName);
    }

    public static ICommand createDeleteSchemaCommand(String schemaName) {
        return new DeleteSchemaCommand(schemaName);
    }

    // --- Security scheme commands ---

    public static ICommand createAddSecuritySchemeCommand(String schemeName, ObjectNode schemeObj) {
        return new AddSecuritySchemeCommand(schemeName, schemeObj);
    }

    public static ICommand createUpdateSecuritySchemeCommand(String schemeName, ObjectNode newSchemeObj) {
        return new UpdateSecuritySchemeCommand(schemeName, newSchemeObj);
    }

    public static ICommand createDeleteSecuritySchemeCommand(String schemeName) {
        return new DeleteSecuritySchemeCommand(schemeName);
    }

    // --- Security requirement commands ---

    public static ICommand createAddOperationSecurityRequirementCommand2(
            SecurityRequirementsParent operation, SecurityRequirement requirement) {
        return new AddOperationSecurityRequirementCommand(operation, requirement);
    }

    public static ICommand createDeleteOperationSecurityRequirementCommand2(
            SecurityRequirementsParent operation, int index) {
        return new DeleteOperationSecurityRequirementCommand(operation, index);
    }

    public static ICommand createDeleteSecurityRequirementCommand(int index) {
        return new DeleteSecurityRequirementCommand(index);
    }

    // --- Node commands ---

    public static ICommand createUpdateNodeCommand(Node node, ObjectNode newContent) {
        return new UpdateNodeCommand(node, newContent);
    }

    public static ICommand createEnsureChildNodeCommand(NodePath parentPath, String childPropertyName) {
        return new EnsureChildNodeCommand(parentPath, childPropertyName);
    }

}
