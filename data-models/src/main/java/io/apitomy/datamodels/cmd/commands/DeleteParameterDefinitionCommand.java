package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.asyncapi.AsyncApiComponents;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.models.asyncapi.AsyncApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xParameter;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A command used to delete a single reusable parameter definition from a document.
 * @author eric.wittmann@gmail.com
 */
public class DeleteParameterDefinitionCommand extends AbstractCommand {

    public String _definitionName;

    public ObjectNode _oldDefinition;
    public int _oldIndex;

    public DeleteParameterDefinitionCommand() {
    }

    public DeleteParameterDefinitionCommand(String definitionName) {
        this._definitionName = definitionName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteParameterDefinitionCommand] Executing.");
        this._oldDefinition = null;

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi20Document doc = (OpenApi20Document) document;
            if (this.isNullOrUndefined(doc.getParameters())) {
                return;
            }
            OpenApiParameter param = doc.getParameters().getItem(this._definitionName);
            if (!this.isNullOrUndefined(param)) {
                this._oldDefinition = Library.writeNode(param);
                this._oldIndex = doc.getParameters().getItemNames().indexOf(this._definitionName);
                doc.getParameters().removeItem(this._definitionName);
            }
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getParameters())) {
                return;
            }
            OpenApi3xParameter param = (OpenApi3xParameter) components.getParameters().get(this._definitionName);
            if (!this.isNullOrUndefined(param)) {
                this._oldDefinition = Library.writeNode((Node) param);
                List<String> paramNames = new ArrayList<>(components.getParameters().keySet());
                this._oldIndex = paramNames.indexOf(this._definitionName);
                components.removeParameter(this._definitionName);
            }
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getParameters())) {
                return;
            }
            Parameter param = components.getParameters().get(this._definitionName);
            if (!this.isNullOrUndefined(param)) {
                this._oldDefinition = Library.writeNode((Node) param);
                List<String> paramNames = new ArrayList<>(components.getParameters().keySet());
                this._oldIndex = paramNames.indexOf(this._definitionName);
                components.removeParameter(this._definitionName);
            }
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteParameterDefinitionCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldDefinition)) {
            return;
        }

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi20Document doc = (OpenApi20Document) document;
            if (this.isNullOrUndefined(doc.getParameters())) {
                doc.setParameters(doc.createParameterDefinitions());
            }
            OpenApiParameter param = doc.getParameters().createParameter();
            Library.readNode(this._oldDefinition, param);
            doc.getParameters().insertItem(this._definitionName, param, this._oldIndex);
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            if (this.isNullOrUndefined(components)) {
                components = ((OpenApi3xDocument) document).createComponents();
                ((OpenApi3xDocument) document).setComponents(components);
            }
            OpenApi3xParameter param = (OpenApi3xParameter) components.createParameter();
            Library.readNode(this._oldDefinition, param);
            components.insertParameter(this._definitionName, param, this._oldIndex);
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            if (this.isNullOrUndefined(components)) {
                components = ((AsyncApiDocument) document).createComponents();
                ((AsyncApiDocument) document).setComponents(components);
            }
            AsyncApiParameter param = components.createParameter();
            Library.readNode(this._oldDefinition, param);
            components.insertParameter(this._definitionName, param, this._oldIndex);
        }
    }

}
