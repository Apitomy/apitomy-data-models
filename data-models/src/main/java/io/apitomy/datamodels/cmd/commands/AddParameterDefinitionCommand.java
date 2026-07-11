package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.asyncapi.AsyncApiComponents;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.models.asyncapi.AsyncApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Parameter;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Parameter;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Parameter;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to add a new parameter definition in a document.  Source for the new
 * definition must be provided.  This source will be converted to an openapi
 * parameter definition object and then added to the data model.
 * @author eric.wittmann@gmail.com
 */
public class AddParameterDefinitionCommand extends AbstractCommand {

    public boolean _defExisted;
    public String _newDefinitionName;
    public ObjectNode _newDefinitionObj;
    public boolean _nullDefinitionsParent;

    private transient AddParameterDefinitionCommandHelper _helper;

    public AddParameterDefinitionCommand() {
    }

    public AddParameterDefinitionCommand(String definitionName, ObjectNode obj) {
        this._newDefinitionName = definitionName;
        this._newDefinitionObj = obj;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddParameterDefinitionCommand] Executing.");
        this._helper = createHelper(document);

        // Do nothing if the definition already exists.
        if (this._helper.defExists(document)) {
            LoggerUtil.info("[AddParameterDefinitionCommand] Definition with name %s already exists.", this._newDefinitionName);
            this._defExisted = true;
            return;
        }

        this._nullDefinitionsParent = this._helper.prepareDocumentForDef(document);

        Parameter definition = this._helper.createParameterDefinition(document);
        this._helper.addDefinition(document, definition);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddParameterDefinitionCommand] Reverting.");
        if (this._defExisted) {
            return;
        }

        this._helper = createHelper(document);

        this._helper.removeDefinition(document);
    }

    private AddParameterDefinitionCommandHelper createHelper(Document document) {
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
        throw new RuntimeException("Unsupported model type: " + document.root().modelType());
    }

    private interface AddParameterDefinitionCommandHelper {

        boolean defExists(Document document);
        boolean prepareDocumentForDef(Document document);
        Parameter createParameterDefinition(Document document);
        void addDefinition(Document document, Parameter definition);
        void removeDefinition(Document document);

    }

    private class OpenApi20Helper implements AddParameterDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenApi20Document doc = (OpenApi20Document) document;
            if (!isNullOrUndefined(doc.getParameters())) {
                return !isNullOrUndefined(doc.getParameters().getItem(_newDefinitionName));
            }
            return false;
        }

        @Override
        public boolean prepareDocumentForDef(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (isNullOrUndefined(doc20.getParameters())) {
                doc20.setParameters(doc20.createParameterDefinitions());
                return true;
            }
            return false;
        }

        @Override
        public Parameter createParameterDefinition(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            OpenApiParameter definition = doc20.getParameters().createParameter();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Parameter definition) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            OpenApi20Parameter def20 = (OpenApi20Parameter) definition;
            doc20.getParameters().addItem(_newDefinitionName, def20);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (_nullDefinitionsParent) {
                doc20.setParameters(null);
            } else {
                doc20.getParameters().removeItem(_newDefinitionName);
            }
        }
    }

    private class OpenApi30Helper implements AddParameterDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc30.getComponents().getParameters().get(_newDefinitionName));
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
        public Parameter createParameterDefinition(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            OpenApi30Parameter definition = (OpenApi30Parameter) doc30.getComponents().createParameter();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Parameter definition) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            doc30.getComponents().addParameter(_newDefinitionName, (OpenApiParameter) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (_nullDefinitionsParent) {
                doc30.setComponents(null);
            } else {
                doc30.getComponents().removeParameter(_newDefinitionName);
            }
        }
    }

    private class OpenApi31Helper implements AddParameterDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (isNullOrUndefined(doc31.getComponents())) {
                return false;
            }
            return !isNullOrUndefined(doc31.getComponents().getParameters().get(_newDefinitionName));
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
        public Parameter createParameterDefinition(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            OpenApi31Parameter definition = (OpenApi31Parameter) doc31.getComponents().createParameter();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Parameter definition) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            doc31.getComponents().addParameter(_newDefinitionName, (OpenApiParameter) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (_nullDefinitionsParent) {
                doc31.setComponents(null);
            } else {
                doc31.getComponents().removeParameter(_newDefinitionName);
            }
        }
    }

    private class AsyncApi2Helper implements AddParameterDefinitionCommandHelper {
        @Override
        public boolean defExists(Document document) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            if (isNullOrUndefined(components)) {
                return false;
            }
            return !isNullOrUndefined(components.getParameters().get(_newDefinitionName));
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
        public Parameter createParameterDefinition(Document document) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            AsyncApiParameter definition = components.createParameter();
            Library.readNode(_newDefinitionObj, definition);
            return definition;
        }

        @Override
        public void addDefinition(Document document, Parameter definition) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            components.addParameter(_newDefinitionName, (AsyncApiParameter) definition);
        }

        @Override
        public void removeDefinition(Document document) {
            AsyncApiDocument doc = (AsyncApiDocument) document;
            if (_nullDefinitionsParent) {
                doc.setComponents(null);
            } else {
                AsyncApiComponents components = doc.getComponents();
                if (!isNullOrUndefined(components)) {
                    components.removeParameter(_newDefinitionName);
                }
            }
        }
    }

}
