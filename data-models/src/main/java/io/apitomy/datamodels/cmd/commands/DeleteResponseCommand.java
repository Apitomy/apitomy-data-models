package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.OpenApiResponses;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to delete a response from an operation.
 * @author eric.wittmann@gmail.com
 */
public class DeleteResponseCommand extends AbstractCommand {

    public NodePath _operationPath;
    public String _statusCode;

    public ObjectNode _oldResponse;
    public int _oldIndex;

    public DeleteResponseCommand() {
    }

    public DeleteResponseCommand(OpenApiOperation operation, String statusCode) {
        this._operationPath = NodePathUtil.createNodePath(operation);
        this._statusCode = statusCode;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteResponseCommand] Executing.");
        this._oldResponse = null;

        OpenApiOperation operation = (OpenApiOperation) NodePathUtil.resolveNodePath(this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        OpenApiResponses responses = operation.getResponses();
        if (this.isNullOrUndefined(responses)) {
            return;
        }

        OpenApiResponse response = responses.getItem(this._statusCode);
        if (this.isNullOrUndefined(response)) {
            return;
        }

        // Save the response and its index for undo
        this._oldResponse = Library.writeNode(response);
        this._oldIndex = responses.getItemNames().indexOf(this._statusCode);

        // Remove the response
        responses.removeItem(this._statusCode);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteResponseCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldResponse)) {
            return;
        }

        OpenApiOperation operation = (OpenApiOperation) NodePathUtil.resolveNodePath(this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        OpenApiResponses responses = operation.getResponses();
        if (this.isNullOrUndefined(responses)) {
            return;
        }

        OpenApiResponse newResponse = responses.createResponse();
        Library.readNode(this._oldResponse, newResponse);
        responses.insertItem(this._statusCode, newResponse, this._oldIndex);
    }

}
