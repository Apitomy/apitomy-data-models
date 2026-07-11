package io.apitomy.datamodels.deref;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.ExternalDocumentation;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.models.ServerVariable;
import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannel;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannelBindings;
import io.apitomy.datamodels.models.asyncapi.AsyncApiCorrelationID;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessageBindings;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessageTrait;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMultiFormatSchema;
import io.apitomy.datamodels.models.asyncapi.AsyncApiOperationBindings;
import io.apitomy.datamodels.models.asyncapi.AsyncApiOperationReply;
import io.apitomy.datamodels.models.asyncapi.AsyncApiOperationReplyAddress;
import io.apitomy.datamodels.models.asyncapi.AsyncApiOperationTrait;
import io.apitomy.datamodels.models.asyncapi.AsyncApiParameter;
import io.apitomy.datamodels.models.asyncapi.AsyncApiSecurityScheme;
import io.apitomy.datamodels.models.asyncapi.AsyncApiServerBindings;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xChannel;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xComponents;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xDocument;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xExternalDocumentation;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xMultiFormatSchema;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xOperation;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xOperationReply;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xOperationReplyAddress;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xSchema;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xServer;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xServerVariable;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xTag;
import io.apitomy.datamodels.models.union.AnyUnionValueImpl;
import io.apitomy.datamodels.models.union.MultiFormatSchemaSchemaUnion;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.util.RegexUtil;

import java.util.List;
import java.util.Map;

/**
 * Node importer for AsyncAPI 3.x documents. Handles dereferencing and importing external
 * references into the AsyncAPI 3.x components section.
 */
public class AsyncApi3NodeImporter extends ReferencedNodeImporter {

    public AsyncApi3NodeImporter(Document doc, Node nodeWithUnresolvedRef, String ref, boolean shouldInline) {
        super(doc, nodeWithUnresolvedRef, ref, shouldInline);
    }

    @Override
    protected void setPathToImportedNode(Node importedNode, String type, String name) {
        setPathToImportedNode(importedNode, "#/components/" + type + "/" + name);
    }

    @Override
    public void visitChannelBindings(AsyncApiChannelBindings node) {
        String componentType = "channelBindings";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedChannelBinding"), getComponentNames(components.getChannelBindings()));
            components.addChannelBinding(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitChannel(AsyncApiChannel node) {
        String componentType = "channels";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedChannel"), getComponentNames(components.getChannels()));
            components.addChannel(name, (AsyncApi3xChannel) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitCorrelationID(AsyncApiCorrelationID node) {
        String componentType = "correlationIds";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedCorrelationID"), getComponentNames(components.getCorrelationIds()));
            components.addCorrelationId(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitExternalDocumentation(ExternalDocumentation node) {
        String componentType = "externalDocs";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            Map<String, ?> externalDocs = ((AsyncApi3xComponents) components).getExternalDocs();
            String name = generateNodeName(getNameHintFromRef("ImportedExternalDocs"), getComponentNames(externalDocs));
            ((AsyncApi3xComponents) components).addExternalDoc(name, (AsyncApi3xExternalDocumentation) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitMessage(AsyncApiMessage node) {
        String componentType = "messages";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedMessage"), getComponentNames(components.getMessages()));
            components.addMessage(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitMessageBindings(AsyncApiMessageBindings node) {
        String componentType = "messageBindings";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedMessageBinding"), getComponentNames(components.getMessageBindings()));
            components.addMessageBinding(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitMessageTrait(AsyncApiMessageTrait node) {
        String componentType = "messageTraits";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedMessageTrait"), getComponentNames(components.getMessageTraits()));
            components.addMessageTrait(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitMultiFormatSchema(AsyncApiMultiFormatSchema node) {
        String componentType = "schemas";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedSchema"), getComponentNames(components.getSchemas()));
            // AsyncAPI 3.0 schemas are a union type, use addSchema() method
            components.addSchema(name, (AsyncApi3xMultiFormatSchema) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitOperation(Operation node) {
        String componentType = "operations";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedOperation"), getComponentNames(components.getOperations()));
            components.addOperation(name, (AsyncApi3xOperation) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitOperationBindings(AsyncApiOperationBindings node) {
        String componentType = "operationBindings";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedOperationBinding"), getComponentNames(components.getOperationBindings()));
            components.addOperationBinding(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitOperationReply(AsyncApiOperationReply node) {
        String componentType = "replies";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedReply"), getComponentNames(components.getReplies()));
            components.addReply(name, (AsyncApi3xOperationReply) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitOperationReplyAddress(AsyncApiOperationReplyAddress node) {
        String componentType = "replyAddresses";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedReplyAddress"), getComponentNames(components.getReplyAddresses()));
            components.addReplyAddress(name, (AsyncApi3xOperationReplyAddress) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitOperationTrait(AsyncApiOperationTrait node) {
        String componentType = "operationTraits";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedOperationTrait"), getComponentNames(components.getOperationTraits()));
            components.addOperationTrait(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitParameter(Parameter node) {
        String componentType = "parameters";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedParameter"), getComponentNames(components.getParameters()));
            components.addParameter(name, (AsyncApiParameter) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitSchema(Schema node) {
        // For AsyncAPI 3.0, we need to detect if this is a non-JSON schema and wrap it in a MultiFormatSchema
        String componentType = "schemas";
        if (shouldInline()) {
            // Check if this is a non-JSON schema that needs wrapping
            if (isAvroSchema(node)) {
                AsyncApi3xMultiFormatSchema multiFormatSchema = wrapInMultiFormatSchema(node, "avro");
                inlineComponent(componentType, multiFormatSchema);
            } else if (isProtobufSchema(node)) {
                AsyncApi3xMultiFormatSchema multiFormatSchema = wrapInMultiFormatSchema(node, "protobuf");
                inlineComponent(componentType, multiFormatSchema);
            } else {
                inlineComponent(componentType, node);
            }
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();

            // Determine the best name hint for this schema
            String nameHint;
            if (isAvroSchema(node)) {
                // For Avro schemas, prefer the name from the schema itself
                String avroName = extractAvroSchemaName(node);
                if (avroName != null) {
                    nameHint = avroName;
                } else {
                    // Fall back to extracting from the reference URL
                    nameHint = getNameHintFromRef("ImportedSchema");
                }
            } else if (isProtobufSchema(node)) {
                // For Protobuf schemas, prefer the name from the schema itself
                String protobufName = extractProtobufSchemaName(node);
                if (protobufName != null) {
                    nameHint = protobufName;
                } else {
                    // Fall back to extracting from the reference URL
                    nameHint = getNameHintFromRef("ImportedSchema");
                }
            } else {
                // For JSON schemas, extract from the reference URL
                nameHint = getNameHintFromRef("ImportedSchema");
            }

            String name = generateNodeName(nameHint, getComponentNames(components.getSchemas()));

            // Check if this is a non-JSON schema that needs wrapping
            if (isAvroSchema(node)) {
                AsyncApi3xMultiFormatSchema multiFormatSchema = wrapInMultiFormatSchema(node, "avro");
                components.addSchema(name, multiFormatSchema);
                multiFormatSchema.attach(components);
                setPathToImportedNode(multiFormatSchema, componentType, name);
            } else if (isProtobufSchema(node)) {
                AsyncApi3xMultiFormatSchema multiFormatSchema = wrapInMultiFormatSchema(node, "protobuf");
                components.addSchema(name, multiFormatSchema);
                multiFormatSchema.attach(components);
                setPathToImportedNode(multiFormatSchema, componentType, name);
            } else {
                // Cast to MultiFormatSchemaSchemaUnion (AsyncApi3xSchema implements this)
                components.addSchema(name, (MultiFormatSchemaSchemaUnion) node);
                node.attach(components);
                setPathToImportedNode(node, componentType, name);
            }
        }
    }

    @Override
    public void visitSecurityScheme(SecurityScheme node) {
        String componentType = "securitySchemes";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedSecurityScheme"), getComponentNames(components.getSecuritySchemes()));
            components.addSecurityScheme(name, (AsyncApiSecurityScheme) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitServer(Server node) {
        String componentType = "servers";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedServer"), getComponentNames(components.getServers()));
            components.addServer(name, (AsyncApi3xServer) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitServerBindings(AsyncApiServerBindings node) {
        String componentType = "serverBindings";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedServerBinding"), getComponentNames(components.getServerBindings()));
            components.addServerBinding(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitServerVariable(ServerVariable node) {
        String componentType = "serverVariables";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedServerVariable"), getComponentNames(components.getServerVariables()));
            components.addServerVariable(name, (AsyncApi3xServerVariable) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitTag(Tag node) {
        String componentType = "tags";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            AsyncApi3xComponents components = ensureAsyncApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedTag"), getComponentNames(components.getTags()));
            components.addTag(name, (AsyncApi3xTag) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void importJson(JsonNode jsonNode, String mediaType) {
        // Handle JSON content that isn't an OpenAPI/AsyncAPI document
        // This is used for Avro JSON schemas or other JSON-based formats
        String componentType = "schemas";
        AsyncApi3xComponents components = ensureAsyncApiComponents();

        // Determine name hint from reference URL
        String nameHint = getNameHintFromRef("ImportedSchema");

        // Try to extract a better name from the JSON if it's an Avro schema
        if (mediaType != null && mediaType.contains("avro")) {
            String avroName = extractAvroNameFromJson(jsonNode);
            if (avroName != null) {
                nameHint = avroName;
            }
        }

        String name = generateNodeName(nameHint, getComponentNames(components.getSchemas()));

        // Create a MultiFormatSchema to wrap the JSON content
        AsyncApi3xMultiFormatSchema multiFormatSchema = wrapJsonInMultiFormatSchema(jsonNode, mediaType);

        // Add to components
        components.addSchema(name, multiFormatSchema);
        multiFormatSchema.attach(components);
        setPathToImportedNode(multiFormatSchema, componentType, name);
    }

    @Override
    public void importText(String textContent, String mediaType) {
        // Handle text content (Protobuf, GraphQL, Avro text, etc.)
        String componentType = "schemas";
        AsyncApi3xComponents components = ensureAsyncApiComponents();

        // Determine name hint - try to extract from content based on media type
        String nameHint = getNameHintFromRef("ImportedSchema");

        if (mediaType != null && mediaType.contains("protobuf")) {
            String protobufName = extractProtobufNameFromText(textContent);
            if (protobufName != null) {
                nameHint = protobufName;
            }
        }

        String name = generateNodeName(nameHint, getComponentNames(components.getSchemas()));

        // Create a MultiFormatSchema to wrap the text content
        AsyncApi3xMultiFormatSchema multiFormatSchema = wrapTextInMultiFormatSchema(textContent, mediaType);

        // Add to components
        components.addSchema(name, multiFormatSchema);
        multiFormatSchema.attach(components);
        setPathToImportedNode(multiFormatSchema, componentType, name);
    }

    /**
     * Ensures that the AsyncAPI 3.0 document has a components section, creating it if necessary.
     *
     * @return the AsyncApi3xComponents object
     */
    private AsyncApi3xComponents ensureAsyncApiComponents() {
        AsyncApi3xDocument doc = (AsyncApi3xDocument) getDoc();
        if (doc.getComponents() == null) {
            doc.setComponents(doc.createComponents());
        }
        return (AsyncApi3xComponents) doc.getComponents();
    }

    /**
     * Detects if a schema is an Apache Avro schema by looking for the "type": "record" pattern.
     *
     * @param schemaNode the schema node to check
     * @return true if this appears to be an Avro schema
     */
    private boolean isAvroSchema(Schema schemaNode) {
        try {
            ObjectNode json = Library.writeNode(schemaNode);
            String type = JsonUtil.getStringProperty(json, "type");

            // Avro schemas have "type": "record", "enum", "fixed", etc.
            // JSON Schema has "type": "object", "array", "string", etc.
            if (type != null) {
                return "record".equals(type) || "enum".equals(type) || "fixed".equals(type);
            }

            return false;
        } catch (Exception e) {
            // If we can't determine, assume it's JSON Schema
            return false;
        }
    }

    /**
     * Extracts the name from an Avro schema. Avro schemas have a "name" field that identifies
     * the type. Optionally, they may also have a "namespace" field for fully qualified names.
     *
     * @param schemaNode the Avro schema node
     * @return the name from the schema, or null if not found
     */
    private String extractAvroSchemaName(Schema schemaNode) {
        try {
            ObjectNode json = Library.writeNode(schemaNode);
            String name = JsonUtil.getStringProperty(json, "name");

            if (!NodeUtil.isNullOrUndefined(name)) {
                return name;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Detects if a schema is a Protocol Buffers schema by looking for protobuf content in the "x-text-content" field.
     *
     * @param schemaNode the schema node to check
     * @return true if this appears to be a Protobuf schema
     */
    private boolean isProtobufSchema(Schema schemaNode) {
        try {
            // Protobuf schemas have an "x-text-content" property with the .proto file text
            ObjectNode json = Library.writeNode(schemaNode);
            String protoContent = JsonUtil.getStringProperty(json, "x-text-content");
            return !NodeUtil.isNullOrUndefined(protoContent);
        } catch (Exception e) {
            // If we can't determine, assume it's JSON Schema
            return false;
        }
    }

    /**
     * Extracts the name from a Protobuf schema. Parses the .proto text content to find
     * the message name.
     *
     * @param schemaNode the Protobuf schema node
     * @return the name from the first message definition, or null if not found
     */
    private String extractProtobufSchemaName(Schema schemaNode) {
        try {
            ObjectNode json = Library.writeNode(schemaNode);
            String protoContent = JsonUtil.getStringProperty(json, "x-text-content");

            if (!NodeUtil.isNullOrUndefined(protoContent)) {
                // Simple regex to extract message name from "message MessageName {"
                List<String[]> matches = RegexUtil.findMatches(protoContent, "message\\s+([A-Za-z0-9_]+)\\s*\\{");
                LoggerUtil.debug(">>>> --------------");
                LoggerUtil.debug(">>>> MATCHES: ", matches);
                LoggerUtil.debug(">>>> --------------");
                if (!matches.isEmpty()) {
                    return matches.get(0)[1];
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the raw Protobuf content from a schema node.
     *
     * @param schemaNode the Protobuf schema node
     * @return the .proto file content as a string, or null if not found
     */
    private String extractProtobufContent(Schema schemaNode) {
        try {
            ObjectNode json = Library.writeNode(schemaNode);
            String protoContent = JsonUtil.getStringProperty(json, "x-text-content");
            if (!NodeUtil.isNullOrUndefined(protoContent)) {
                return protoContent;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Wraps a schema (Avro, Protobuf, etc.) in an AsyncAPI 3.0 Multi-Format Schema Object.
     *
     * @param schemaNode the schema to wrap
     * @param schemaType the type of schema ("avro", "protobuf", etc.)
     * @return a MultiFormatSchema containing the wrapped schema
     */
    private AsyncApi3xMultiFormatSchema wrapInMultiFormatSchema(Schema schemaNode, String schemaType) {
        AsyncApi3xComponents components = ensureAsyncApiComponents();
        AsyncApi3xMultiFormatSchema multiFormatSchema = components.createMultiFormatSchema();

        // Set the schema format based on the type
        switch (schemaType) {
            case "avro":
                multiFormatSchema.setSchemaFormat("application/vnd.apache.avro+json;version=1.12.0");
                break;
            case "protobuf":
                multiFormatSchema.setSchemaFormat("application/vnd.google.protobuf;version=3");
                break;
            default:
                multiFormatSchema.setSchemaFormat("application/schema+json");
                break;
        }

        // For Protobuf, extract the raw .proto content and set it as a TextNode
        // For other formats, convert the schema node to JSON
        if ("protobuf".equals(schemaType)) {
            String protobufContent = extractProtobufContent(schemaNode);
            if (protobufContent != null) {
                // The schema property in MultiFormatSchema is an AnySchemaUnion, which can be
                // either a Schema object or any JsonNode. For Protobuf, we use a TextNode
                // wrapped in an AnyUnionValueImpl.
                JsonNode protobufTextNode = JsonUtil.textNode(protobufContent);
                AnyUnionValueImpl schemaValue = new AnyUnionValueImpl(protobufTextNode);

                // Set it as the schema
                multiFormatSchema.setSchema(schemaValue);
            } else {
                // Fallback to normal schema handling
                ObjectNode schemaJson = Library.writeNode(schemaNode);
                AsyncApi3xSchema schema = components.createSchema();
                Library.readNode(schemaJson, schema);
                multiFormatSchema.setSchema(schema);
            }
        } else {
            // Convert the schema node to JSON and set it as the schema property
            ObjectNode schemaJson = Library.writeNode(schemaNode);
            // Set the schema by creating an AsyncApi3xSchema and populating it from the JSON
            AsyncApi3xSchema schema = components.createSchema();
            Library.readNode(schemaJson, schema);
            multiFormatSchema.setSchema(schema);
        }

        return multiFormatSchema;
    }

    /**
     * Extracts the name from an Avro schema in JSON format.
     *
     * @param jsonNode the Avro schema JSON
     * @return the name from the schema, or null if not found
     */
    private String extractAvroNameFromJson(JsonNode jsonNode) {
        try {
            if (JsonUtil.isObject(jsonNode)) {
                String name = JsonUtil.getStringProperty((ObjectNode) jsonNode, "name");
                if (!NodeUtil.isNullOrUndefined(name)) {
                    return name;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the name from a Protobuf schema text.
     *
     * @param textContent the .proto file content
     * @return the name from the first message definition, or null if not found
     */
    private String extractProtobufNameFromText(String textContent) {
        try {
            if (!NodeUtil.isNullOrUndefined(textContent)) {
                // Simple regex to extract message name from "message MessageName {"
                List<String[]> matches = RegexUtil.findMatches(textContent, "message\\s+([A-Za-z0-9_]+)\\s*\\{");
                if (!matches.isEmpty()) {
                    return matches.get(0)[1];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Wraps JSON content in an AsyncAPI 3.0 Multi-Format Schema Object.
     *
     * @param jsonNode the JSON content to wrap
     * @param mediaType the media type of the content
     * @return a MultiFormatSchema containing the wrapped JSON
     */
    private AsyncApi3xMultiFormatSchema wrapJsonInMultiFormatSchema(JsonNode jsonNode, String mediaType) {
        AsyncApi3xComponents components = ensureAsyncApiComponents();
        AsyncApi3xMultiFormatSchema multiFormatSchema = components.createMultiFormatSchema();

        // Set the schema format based on media type
        if (mediaType != null && !mediaType.isEmpty()) {
            multiFormatSchema.setSchemaFormat(mediaType);
        } else {
            multiFormatSchema.setSchemaFormat("application/schema+json");
        }

        // Wrap the JsonNode in an AnyUnionValueImpl
        AnyUnionValueImpl schemaValue = new AnyUnionValueImpl(jsonNode);
        multiFormatSchema.setSchema(schemaValue);

        return multiFormatSchema;
    }

    /**
     * Wraps text content in an AsyncAPI 3.0 Multi-Format Schema Object.
     *
     * @param textContent the text content to wrap
     * @param mediaType the media type of the content
     * @return a MultiFormatSchema containing the wrapped text
     */
    private AsyncApi3xMultiFormatSchema wrapTextInMultiFormatSchema(String textContent, String mediaType) {
        AsyncApi3xComponents components = ensureAsyncApiComponents();
        AsyncApi3xMultiFormatSchema multiFormatSchema = components.createMultiFormatSchema();

        // Set the schema format based on media type
        if (mediaType != null && !mediaType.isEmpty()) {
            multiFormatSchema.setSchemaFormat(mediaType);
        } else {
            multiFormatSchema.setSchemaFormat("text/plain");
        }

        // Wrap the text in a TextNode and then in an AnyUnionValueImpl
        JsonNode textNode = JsonUtil.textNode(textContent);
        AnyUnionValueImpl schemaValue = new AnyUnionValueImpl(textNode);
        multiFormatSchema.setSchema(schemaValue);

        return multiFormatSchema;
    }

}
