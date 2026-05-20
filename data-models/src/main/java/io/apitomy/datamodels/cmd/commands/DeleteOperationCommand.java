package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Operation;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30PathItem;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Operation;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31PathItem;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to delete an operation from a path item.
 * @author eric.wittmann@gmail.com
 */
public class DeleteOperationCommand extends AbstractCommand {

    public NodePath _pathItemPath;
    public String _method;

    public ObjectNode _oldOperation;

    public DeleteOperationCommand() {
    }

    public DeleteOperationCommand(OpenApiPathItem pathItem, String method) {
        this._pathItemPath = NodePathUtil.createNodePath(pathItem);
        this._method = method.toLowerCase();
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteOperationCommand] Executing.");
        this._oldOperation = null;

        OpenApiPathItem pathItem = (OpenApiPathItem) NodePathUtil.resolveNodePath(this._pathItemPath, document);
        if (this.isNullOrUndefined(pathItem)) {
            return;
        }

        OpenApiOperation operation = (OpenApiOperation) NodeUtil.getProperty(pathItem, this._method);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        // Save the operation for undo
        this._oldOperation = Library.writeNode(operation);

        // Remove the operation
        NodeUtil.setProperty(pathItem, this._method, null);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteOperationCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldOperation)) {
            return;
        }

        OpenApiPathItem pathItem = (OpenApiPathItem) NodePathUtil.resolveNodePath(this._pathItemPath, document);
        if (this.isNullOrUndefined(pathItem)) {
            return;
        }

        OpenApiOperation operation = pathItem.createOperation();
        Library.readNode(this._oldOperation, operation);
        setOperation(pathItem, operation, this._method);
    }

    /**
     * Sets the operation on the path item for the given HTTP method.
     */
    private void setOperation(OpenApiPathItem parent, OpenApiOperation operation, String method) {
        switch (method) {
            case "get":
                parent.setGet(operation);
                break;
            case "put":
                parent.setPut(operation);
                break;
            case "post":
                parent.setPost(operation);
                break;
            case "delete":
                parent.setDelete(operation);
                break;
            case "head":
                parent.setHead(operation);
                break;
            case "patch":
                parent.setPatch(operation);
                break;
            case "options":
                parent.setOptions(operation);
                break;
            case "trace":
                if (ModelTypeUtil.isOpenApi30Model(parent)) {
                    ((OpenApi30PathItem) parent).setTrace((OpenApi30Operation) operation);
                } else if (ModelTypeUtil.isOpenApi31Model(parent)) {
                    ((OpenApi31PathItem) parent).setTrace((OpenApi31Operation) operation);
                }
                break;
        }
    }

}
