package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Example;
import io.apitomy.datamodels.models.openapi.OpenApiExample;
import io.apitomy.datamodels.models.openrpc.OpenRpcComponents;
import io.apitomy.datamodels.models.openrpc.OpenRpcDocument;
import io.apitomy.datamodels.models.openrpc.OpenRpcExample;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Example;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Example;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to add a new example definition in a document.  Source for the new
 * definition must be provided.  This source will be converted to an example
 * definition object and then added to the data model.
 * @author eric.wittmann@gmail.com
 */
public class AddExampleDefinitionCommand extends AbstractCommand {

    public boolean _defExisted;
    public String _newDefinitionName;
    public ObjectNode _newDefinitionObj;
    public boolean _nullDefinitionsParent;

    private transient AddExampleDefinitionCommandHelper _helper;

    public AddExampleDefinitionCommand() {
    }

    public AddExampleDefinitionCommand(String definitionName, ObjectNode obj) {
        this._newDefinitionName = definitionName;
        this._newDefinitionObj = obj;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddExampleDefinitionCommand] Executing.");
        this._helper = createHelper(document);

        // Do nothing if the definition already exists.
        if (this._helper.defExists(document)) {
            LoggerUtil.info("[AddExampleDefinitionCommand] Definition with name %s already exists.", this._newDefinitionName);
            this._defExisted = true;
            return;
        }

        this._nullDefinitionsParent = this._helper.prepareDocumentForDef(document);

        Example definition = this._helper.createExampleDefinition(document);
        this._helper.addDefinition(document, definition);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddExampleDefinitionCommand] Reverting.");
        if (this._defExisted) {
            return;
        }

        this._helper = createHelper(document);
        this._helper.removeDefinition(document);
    }

    private AddExampleDefinitionCommandHelper createHelper(Document document) {
        if (ModelTypeUtil.isOpenApi30Model(document)) {
            return new OpenApi30Helper();
        }
        if (ModelTypeUtil.isOpenApi31Model(document)) {
            return new OpenApi31Helper();
        }
        if (ModelTypeUtil.isOpenRpcModel(document)) {
            return new OpenRpcHelper();
        }
        throw new RuntimeException("Unsupported model type: " + document.root().modelType());
    }

    private interface AddExampleDefinitionCommandHelper {

        boolean defExists(Document document);
        boolean prepareDocumentForDef(Document document);
        Example createExampleDefinition(Document document);
        void addDefinition(Document document, Example definition);
        void removeDefinition(Document document);

    }

    private class OpenApi30Helper implements AddExampleDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc30.getComponents().getExamples().get(_newDefinitionName));
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
        public Example createExampleDefinition(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            OpenApi30Example definition = (OpenApi30Example) doc30.getComponents().createExample();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Example definition) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            doc30.getComponents().addExample(_newDefinitionName, (OpenApiExample) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (_nullDefinitionsParent) {
                doc30.setComponents(null);
            } else {
                doc30.getComponents().removeExample(_newDefinitionName);
            }
        }
    }

    private class OpenApi31Helper implements AddExampleDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (isNullOrUndefined(doc31.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc31.getComponents().getExamples().get(_newDefinitionName));
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
        public Example createExampleDefinition(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            OpenApi31Example definition = (OpenApi31Example) doc31.getComponents().createExample();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Example definition) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            doc31.getComponents().addExample(_newDefinitionName, (OpenApiExample) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (_nullDefinitionsParent) {
                doc31.setComponents(null);
            } else {
                doc31.getComponents().removeExample(_newDefinitionName);
            }
        }
    }

    private class OpenRpcHelper implements AddExampleDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            if (isNullOrUndefined(doc.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc.getComponents().getExamples().get(_newDefinitionName));
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
        public Example createExampleDefinition(Document document) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            OpenRpcExample definition = doc.getComponents().createExample();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Example definition) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            doc.getComponents().addExample(_newDefinitionName, (OpenRpcExample) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            if (_nullDefinitionsParent) {
                doc.setComponents(null);
            } else {
                doc.getComponents().removeExample(_newDefinitionName);
            }
        }
    }

}
