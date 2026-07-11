package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to add a new header definition in a document.  Source for the new
 * definition must be provided.  This source will be converted to an openapi
 * header definition object and then added to the data model.
 * @author eric.wittmann@gmail.com
 */
public class AddHeaderDefinitionCommand extends AbstractCommand {

    public boolean _defExisted;
    public String _newDefinitionName;
    public ObjectNode _newDefinitionObj;
    public boolean _nullDefinitionsParent;

    private transient AddHeaderDefinitionCommandHelper _helper;

    public AddHeaderDefinitionCommand() {
    }

    public AddHeaderDefinitionCommand(String definitionName, ObjectNode obj) {
        this._newDefinitionName = definitionName;
        this._newDefinitionObj = obj;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddHeaderDefinitionCommand] Executing.");
        this._helper = createHelper(document);

        OpenApiDocument doc = (OpenApiDocument) document;

        // Do nothing if the definition already exists.
        if (this._helper.defExists(doc)) {
            LoggerUtil.info("[AddHeaderDefinitionCommand] Definition with name %s already exists.", this._newDefinitionName);
            this._defExisted = true;
            return;
        }

        this._nullDefinitionsParent = this._helper.prepareDocumentForDef(doc);

        OpenApiHeader definition = this._helper.createHeaderDefinition(doc);
        this._helper.addDefinition(doc, definition);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddHeaderDefinitionCommand] Reverting.");
        if (this._defExisted) {
            return;
        }

        this._helper = createHelper(document);

        OpenApiDocument doc = (OpenApiDocument) document;
        this._helper.removeDefinition(doc);
    }

    private AddHeaderDefinitionCommandHelper createHelper(Document document) {
        if (ModelTypeUtil.isOpenApi30Model(document)) {
            return new OpenApi30Helper();
        }
        if (ModelTypeUtil.isOpenApi31Model(document)) {
            return new OpenApi31Helper();
        }
        throw new RuntimeException("Unsupported model type: " + document.root().modelType());
    }

    private interface AddHeaderDefinitionCommandHelper {

        boolean defExists(OpenApiDocument document);
        boolean prepareDocumentForDef(OpenApiDocument document);
        OpenApiHeader createHeaderDefinition(OpenApiDocument document);
        void addDefinition(OpenApiDocument document, OpenApiHeader definition);
        void removeDefinition(OpenApiDocument document);

    }

    private class OpenApi30Helper implements AddHeaderDefinitionCommandHelper {
        @Override
        public boolean defExists(OpenApiDocument document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc30.getComponents().getHeaders().get(_newDefinitionName));
        }

        @Override
        public boolean prepareDocumentForDef(OpenApiDocument document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents())) {
                doc30.setComponents(doc30.createComponents());
                return true;
            }
            return false;
        }

        @Override
        public OpenApiHeader createHeaderDefinition(OpenApiDocument document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            OpenApiHeader definition = doc30.getComponents().createHeader();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(OpenApiDocument document, OpenApiHeader definition) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            doc30.getComponents().addHeader(_newDefinitionName, definition);
        }

        @Override
        public void removeDefinition(OpenApiDocument document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (_nullDefinitionsParent) {
                doc30.setComponents(null);
            } else {
                doc30.getComponents().removeHeader(_newDefinitionName);
            }
        }
    }

    private class OpenApi31Helper implements AddHeaderDefinitionCommandHelper {
        @Override
        public boolean defExists(OpenApiDocument document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (isNullOrUndefined(doc31.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc31.getComponents().getHeaders().get(_newDefinitionName));
        }

        @Override
        public boolean prepareDocumentForDef(OpenApiDocument document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (isNullOrUndefined(doc31.getComponents())) {
                doc31.setComponents(doc31.createComponents());
                return true;
            }
            return false;
        }

        @Override
        public OpenApiHeader createHeaderDefinition(OpenApiDocument document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            OpenApiHeader definition = doc31.getComponents().createHeader();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(OpenApiDocument document, OpenApiHeader definition) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            doc31.getComponents().addHeader(_newDefinitionName, definition);
        }

        @Override
        public void removeDefinition(OpenApiDocument document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (_nullDefinitionsParent) {
                doc31.setComponents(null);
            } else {
                doc31.getComponents().removeHeader(_newDefinitionName);
            }
        }
    }

}
