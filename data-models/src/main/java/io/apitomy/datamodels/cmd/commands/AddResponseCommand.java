package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.OpenApiResponses;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to add a response to an operation.
 * @author eric.wittmann@gmail.com
 */
public class AddResponseCommand extends AbstractCommand {

    public NodePath _operationPath;
    public String _statusCode;
    public String _description;

    public boolean _created;
    public boolean _responsesCreated;

    public AddResponseCommand() {
    }

    public AddResponseCommand(OpenApiOperation operation, String statusCode, String description) {
        this._operationPath = NodePathUtil.createNodePath(operation);
        this._statusCode = statusCode;
        this._description = description;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddResponseCommand] Executing.");
        this._created = false;
        this._responsesCreated = false;

        OpenApiOperation operation = (OpenApiOperation) NodePathUtil.resolveNodePath(this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        // Get or create the responses container
        OpenApiResponses responses = operation.getResponses();
        if (this.isNullOrUndefined(responses)) {
            responses = operation.createResponses();
            operation.setResponses(responses);
            this._responsesCreated = true;
        }

        // Check if response already exists
        if (!this.isNullOrUndefined(responses.getItem(this._statusCode))) {
            return;
        }

        // Create and add the response
        OpenApiResponse response = responses.createResponse();
        response.setDescription(this._description);
        responses.addItem(this._statusCode, response);
        this._created = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddResponseCommand] Reverting.");
        if (!this._created) {
            return;
        }

        OpenApiOperation operation = (OpenApiOperation) NodePathUtil.resolveNodePath(this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        if (this._responsesCreated) {
            operation.setResponses(null);
        } else {
            OpenApiResponses responses = operation.getResponses();
            if (!this.isNullOrUndefined(responses)) {
                responses.removeItem(this._statusCode);
            }
        }
    }

}
