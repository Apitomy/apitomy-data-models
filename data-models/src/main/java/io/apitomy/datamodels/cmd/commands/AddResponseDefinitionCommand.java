package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Response;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Response;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Response;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to add a new response definition in a document.  Source for the new
 * definition must be provided.  This source will be converted to an openapi
 * response definition object and then added to the data model.
 * @author eric.wittmann@gmail.com
 */
public class AddResponseDefinitionCommand extends AbstractCommand {

    public boolean _defExisted;
    public String _newDefinitionName;
    public ObjectNode _newDefinitionObj;
    public boolean _nullDefinitionsParent;

    private transient AddResponseDefinitionCommandHelper _helper;
    
    public AddResponseDefinitionCommand() {
    }

    public AddResponseDefinitionCommand(String definitionName, ObjectNode obj) {
        this._newDefinitionName = definitionName;
        this._newDefinitionObj = obj;
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddResponseDefinitionCommand] Executing.");
        this._helper = createHelper(document);

        OpenApiDocument doc = (OpenApiDocument) document;

        // Do nothing if the definition already exists.
        if (this._helper.defExists(doc)) {
            LoggerUtil.info("[AddResponseDefinitionCommand] Definition with name %s already exists.", this._newDefinitionName);
            this._defExisted = true;
            return;
        }

        this._nullDefinitionsParent = this._helper.prepareDocumentForDef(doc);

        OpenApiResponse definition = this._helper.createResponseDefinition(doc);
        this._helper.addDefinition(doc, definition);
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddResponseDefinitionCommand] Reverting.");
        if (this._defExisted) {
            return;
        }

        this._helper = createHelper(document);

        OpenApiDocument doc = (OpenApiDocument) document;
        this._helper.removeDefinition(doc);
    }

    private AddResponseDefinitionCommandHelper createHelper(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            return new OpenApi20Helper();
        }
        if (ModelTypeUtil.isOpenApi30Model(document)) {
            return new OpenApi30Helper();
        }
        if (ModelTypeUtil.isOpenApi31Model(document)) {
            return new OpenApi31Helper();
        }
        throw new RuntimeException("Unsupported model type: " + document.root().modelType());
    }

    private interface AddResponseDefinitionCommandHelper {

        boolean defExists(OpenApiDocument document);
        boolean prepareDocumentForDef(OpenApiDocument document);
        OpenApiResponse createResponseDefinition(OpenApiDocument document);
        void addDefinition(OpenApiDocument document, OpenApiResponse definition);
        void removeDefinition(OpenApiDocument document);

    }

    private class OpenApi20Helper implements AddResponseDefinitionCommandHelper {
        @Override
        public boolean defExists(OpenApiDocument document) {
            OpenApi20Document doc = (OpenApi20Document) document;
            if (!isNullOrUndefined(doc.getResponses())) {
                return !isNullOrUndefined(doc.getResponses().getItem(_newDefinitionName));
            }
            return false;
        }

        @Override
        public boolean prepareDocumentForDef(OpenApiDocument document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (isNullOrUndefined(doc20.getResponses())) {
                doc20.setResponses(doc20.createResponseDefinitions());
                return true;
            }
            return false;
        }

        @Override
        public OpenApiResponse createResponseDefinition(OpenApiDocument document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            OpenApiResponse definition = doc20.getResponses().createResponse();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(OpenApiDocument document, OpenApiResponse definition) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            OpenApi20Response def20 = (OpenApi20Response) definition;
            doc20.getResponses().addItem(_newDefinitionName, def20);
        }

        @Override
        public void removeDefinition(OpenApiDocument document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (_nullDefinitionsParent) {
                doc20.setResponses(null);
            } else {
                doc20.getResponses().removeItem(_newDefinitionName);
            }
        }
    }

    private class OpenApi30Helper implements AddResponseDefinitionCommandHelper {
        @Override
        public boolean defExists(OpenApiDocument document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc30.getComponents().getResponses().get(_newDefinitionName));
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
        public OpenApiResponse createResponseDefinition(OpenApiDocument document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            OpenApi30Response definition = (OpenApi30Response) doc30.getComponents().createResponse();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(OpenApiDocument document, OpenApiResponse definition) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            doc30.getComponents().addResponse(_newDefinitionName, definition);
        }

        @Override
        public void removeDefinition(OpenApiDocument document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (_nullDefinitionsParent) {
                doc30.setComponents(null);
            } else {
                doc30.getComponents().removeResponse(_newDefinitionName);
            }
        }
    }

    private class OpenApi31Helper implements AddResponseDefinitionCommandHelper {
        @Override
        public boolean defExists(OpenApiDocument document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (isNullOrUndefined(doc31.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc31.getComponents().getResponses().get(_newDefinitionName));
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
        public OpenApiResponse createResponseDefinition(OpenApiDocument document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            OpenApi31Response definition = (OpenApi31Response) doc31.getComponents().createResponse();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(OpenApiDocument document, OpenApiResponse definition) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            doc31.getComponents().addResponse(_newDefinitionName, definition);
        }

        @Override
        public void removeDefinition(OpenApiDocument document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (_nullDefinitionsParent) {
                doc31.setComponents(null);
            } else {
                doc31.getComponents().removeResponse(_newDefinitionName);
            }
        }
    }

}
