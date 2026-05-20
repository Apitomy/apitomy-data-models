package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xOperation;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xRequestBody;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Operation;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30RequestBody;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Operation;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31RequestBody;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to add a request body to an operation (OpenAPI 3.0+ only).
 * @author eric.wittmann@gmail.com
 */
public class AddRequestBodyCommand extends AbstractCommand {

    public NodePath _operationPath;

    public boolean _created;

    public AddRequestBodyCommand() {
    }

    public AddRequestBodyCommand(OpenApi30Operation operation) {
        this._operationPath = NodePathUtil.createNodePath(operation);
    }

    public AddRequestBodyCommand(OpenApi31Operation operation) {
        this._operationPath = NodePathUtil.createNodePath(operation);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddRequestBodyCommand] Executing.");
        this._created = false;

        Object operation = NodePathUtil.resolveNodePath(this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        // Don't create if one already exists
        Object existing = NodeUtil.getProperty(operation, "requestBody");
        if (!this.isNullOrUndefined(existing)) {
            return;
        }

        OpenApi3xOperation op3x = (OpenApi3xOperation) operation;
        OpenApi3xRequestBody requestBody = op3x.createRequestBody();
        op3x.setRequestBody(requestBody);
        this._created = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddRequestBodyCommand] Reverting.");
        if (!this._created) {
            return;
        }

        Object operation = NodePathUtil.resolveNodePath(this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        ((OpenApi3xOperation) operation).setRequestBody(null);
    }

}
