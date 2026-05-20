package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xOperation;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xRequestBody;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Operation;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to delete a request body from an operation (OpenAPI 3.0+ only).
 * @author eric.wittmann@gmail.com
 */
public class DeleteRequestBodyCommand extends AbstractCommand {

    public NodePath _operationPath;

    public ObjectNode _oldRequestBody;

    public DeleteRequestBodyCommand() {
    }

    public DeleteRequestBodyCommand(OpenApi3xOperation operation) {
        this._operationPath = NodePathUtil.createNodePath(operation);
    }

    public DeleteRequestBodyCommand(OpenApi31Operation operation) {
        this._operationPath = NodePathUtil.createNodePath(operation);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteRequestBodyCommand] Executing.");
        this._oldRequestBody = null;

        Object operation = NodePathUtil.resolveNodePath(this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        Object requestBody = NodeUtil.getProperty(operation, "requestBody");
        if (this.isNullOrUndefined(requestBody)) {
            return;
        }

        // Save the request body for undo
        this._oldRequestBody = Library.writeNode((io.apitomy.datamodels.models.Node) requestBody);

        // Remove the request body
        if (operation instanceof OpenApi3xOperation) {
            ((OpenApi3xOperation) operation).setRequestBody(null);
        } else if (operation instanceof OpenApi31Operation) {
            ((OpenApi31Operation) operation).setRequestBody(null);
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteRequestBodyCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldRequestBody)) {
            return;
        }

        Object operation = NodePathUtil.resolveNodePath(this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        OpenApi3xOperation op3x = (OpenApi3xOperation) operation;
        OpenApi3xRequestBody requestBody = op3x.createRequestBody();
        Library.readNode(this._oldRequestBody, requestBody);
        op3x.setRequestBody(requestBody);
    }

}
