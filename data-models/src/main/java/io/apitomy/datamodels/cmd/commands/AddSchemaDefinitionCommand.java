package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.asyncapi.AsyncApiComponents;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Schema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.models.openrpc.OpenRpcDocument;
import io.apitomy.datamodels.models.openrpc.OpenRpcSchema;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.Map;

/**
 * A command used to add a new definition in a document.  Source for the new
 * definition must be provided.  This source will be converted to an OAS
 * definition object and then added to the data model.
 * @author eric.wittmann@gmail.com
 */
public class AddSchemaDefinitionCommand extends AbstractCommand {

    public boolean _defExisted;
    public String _newDefinitionName;
    public ObjectNode _newDefinitionObj;
    public boolean _nullDefinitionsParent;

    private transient AddSchemaDefinitionCommandHelper _helper;

    public AddSchemaDefinitionCommand() {
    }

    public AddSchemaDefinitionCommand(String definitionName, ObjectNode obj) {
        this._newDefinitionName = definitionName;
        this._newDefinitionObj = obj;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddSchemaDefinitionCommand] Executing.");
        this._helper = createHelper(document);

        // Do nothing if the definition already exists.
        if (this._helper.defExists(document)) {
            LoggerUtil.info("[AddSchemaDefinitionCommand] Definition with name %s already exists.", this._newDefinitionName);
            this._defExisted = true;
            return;
        }

        this._nullDefinitionsParent = this._helper.prepareDocumentForDef(document);

        Schema definition = this._helper.createSchemaDefinition(document);
        this._helper.addDefinition(document, definition);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddSchemaDefinitionCommand] Reverting.");
        if (this._defExisted) {
            return;
        }

        this._helper.removeDefinition(document);
    }

    private AddSchemaDefinitionCommandHelper createHelper(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            return new OpenApi20Helper();
        }
        if (ModelTypeUtil.isOpenApi30Model(document)) {
            return new OpenApi30Helper();
        }
        if (ModelTypeUtil.isOpenApi31Model(document)) {
            return new OpenApi31Helper();
        }
        if (ModelTypeUtil.isAsyncApi2Model(document)) {
            return new AsyncApi2Helper();
        }
        if (ModelTypeUtil.isOpenRpcModel(document)) {
            return new OpenRpcHelper();
        }
        throw new RuntimeException("Unsupported model type: " + document.root().modelType());
    }

    private interface AddSchemaDefinitionCommandHelper {
        boolean defExists(Document document);

        boolean prepareDocumentForDef(Document document);

        Schema createSchemaDefinition(Document document);

        void addDefinition(Document document, Schema definition);

        void removeDefinition(Document document);
    }

    private class OpenApi20Helper implements AddSchemaDefinitionCommandHelper {

        @Override
        public boolean defExists(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (isNullOrUndefined(doc20.getDefinitions())) {
                return false;
            }
            return !isNullOrUndefined(doc20.getDefinitions().getItem(_newDefinitionName));
        }

        @Override
        public boolean prepareDocumentForDef(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (isNullOrUndefined(doc20.getDefinitions())) {
                doc20.setDefinitions(doc20.createDefinitions());
                return true;
            }
            return false;
        }

        @Override
        public Schema createSchemaDefinition(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            OpenApiSchema definition = doc20.getDefinitions().createSchema();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Schema definition) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            OpenApi20Schema def20 = (OpenApi20Schema) definition;
            doc20.getDefinitions().addItem(_newDefinitionName, def20);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (_nullDefinitionsParent) {
                doc20.setDefinitions(null);
            } else {
                doc20.getDefinitions().removeItem(_newDefinitionName);
            }
        }
    }

    private class OpenApi30Helper implements AddSchemaDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc30.getComponents().getSchemas().get(_newDefinitionName));
        }

        @Override
        public boolean prepareDocumentForDef(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents())) {
                doc30.setComponents(doc30.createComponents());
                return true;
            }
            return false;
        }

        @Override
        public Schema createSchemaDefinition(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            OpenApi30Schema definition = (OpenApi30Schema) doc30.getComponents().createSchema();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Schema definition) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            doc30.getComponents().addSchema(_newDefinitionName, (OpenApiSchema) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (_nullDefinitionsParent) {
                doc30.setComponents(null);
            } else {
                doc30.getComponents().removeSchema(_newDefinitionName);
            }
        }
    }

    private class OpenApi31Helper implements AddSchemaDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (isNullOrUndefined(doc31.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc31.getComponents().getSchemas().get(_newDefinitionName));
        }

        @Override
        public boolean prepareDocumentForDef(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (isNullOrUndefined(doc31.getComponents())) {
                doc31.setComponents(doc31.createComponents());
                return true;
            }
            return false;
        }

        @Override
        public Schema createSchemaDefinition(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            OpenApi31Schema definition = (OpenApi31Schema) doc31.getComponents().createSchema();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Schema definition) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            doc31.getComponents().addSchema(_newDefinitionName, (OpenApiSchema) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (_nullDefinitionsParent) {
                doc31.setComponents(null);
            } else {
                doc31.getComponents().removeSchema(_newDefinitionName);
            }
        }
    }

    private class OpenRpcHelper implements AddSchemaDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            if (isNullOrUndefined(doc.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc.getComponents().getSchemas().get(_newDefinitionName));
        }

        @Override
        public boolean prepareDocumentForDef(Document document) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            if (isNullOrUndefined(doc.getComponents())) {
                doc.setComponents(doc.createComponents());
                return true;
            }
            return false;
        }

        @Override
        public Schema createSchemaDefinition(Document document) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            OpenRpcSchema definition = doc.getComponents().createSchema();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Schema definition) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            doc.getComponents().addSchema(_newDefinitionName, (OpenRpcSchema) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            if (_nullDefinitionsParent) {
                doc.setComponents(null);
            } else {
                doc.getComponents().removeSchema(_newDefinitionName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private class AsyncApi2Helper implements AddSchemaDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            if (isNullOrUndefined(components)) {
                return false;
            }
            Map<String, ?> schemas = (Map<String, ?>) NodeUtil.getNodeProperty(components, "schemas");
            return !isNullOrUndefined(schemas) && schemas.containsKey(_newDefinitionName);
        }

        @Override
        public boolean prepareDocumentForDef(Document document) {
            AsyncApiDocument doc = (AsyncApiDocument) document;
            if (isNullOrUndefined(doc.getComponents())) {
                doc.setComponents(doc.createComponents());
                return true;
            }
            return false;
        }

        @Override
        public Schema createSchemaDefinition(Document document) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            Schema definition = (Schema) NodeUtil.invokeMethod(components, "createSchema");
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Schema definition) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            NodeUtil.invokeMethod(components, "addSchema", _newDefinitionName, definition);
        }

        @Override
        public void removeDefinition(Document document) {
            AsyncApiDocument doc = (AsyncApiDocument) document;
            if (_nullDefinitionsParent) {
                doc.setComponents(null);
            } else {
                AsyncApiComponents components = doc.getComponents();
                if (!isNullOrUndefined(components)) {
                    NodeUtil.invokeMethod(components, "removeSchema", _newDefinitionName);
                }
            }
        }
    }

}
