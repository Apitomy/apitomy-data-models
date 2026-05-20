package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiRequestBody;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A command used to delete a single reusable request body definition from a document.
 * @author eric.wittmann@gmail.com
 */
public class DeleteRequestBodyDefinitionCommand extends AbstractCommand {

    public String _definitionName;

    public ObjectNode _oldDefinition;
    public int _oldIndex;

    public DeleteRequestBodyDefinitionCommand() {
    }

    public DeleteRequestBodyDefinitionCommand(String definitionName) {
        this._definitionName = definitionName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteRequestBodyDefinitionCommand] Executing.");
        this._oldDefinition = null;

        OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
        if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getRequestBodies())) {
            return;
        }
        OpenApiRequestBody requestBody = components.getRequestBodies().get(this._definitionName);
        if (!this.isNullOrUndefined(requestBody)) {
            this._oldDefinition = Library.writeNode((Node) requestBody);
            List<String> requestBodyNames = new ArrayList<>(components.getRequestBodies().keySet());
            this._oldIndex = requestBodyNames.indexOf(this._definitionName);
            components.removeRequestBody(this._definitionName);
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteRequestBodyDefinitionCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldDefinition)) {
            return;
        }

        OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
        if (this.isNullOrUndefined(components)) {
            components = ((OpenApi3xDocument) document).createComponents();
            ((OpenApi3xDocument) document).setComponents(components);
        }
        OpenApiRequestBody requestBody = components.createRequestBody();
        Library.readNode(this._oldDefinition, requestBody);
        components.insertRequestBody(this._definitionName, requestBody, this._oldIndex);
    }

}
