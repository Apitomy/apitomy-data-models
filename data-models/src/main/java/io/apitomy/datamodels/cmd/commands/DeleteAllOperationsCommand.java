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

import java.util.HashMap;
import java.util.Map;

/**
 * A command used to delete all operations from a path
 * @author eric.wittmann@gmail.com
 */
public class DeleteAllOperationsCommand extends AbstractCommand {
    
    private static final String[] ALL_METHODS = new String[] {
        "get", "put", "post", "delete", "head", "patch", "options", "trace"
    };

    public NodePath _parentPath;
    public Map<String, ObjectNode> _oldOperations;
    
    public DeleteAllOperationsCommand() {
    }

    public DeleteAllOperationsCommand(OpenApiPathItem parent) {
        this._parentPath = NodePathUtil.createNodePath(parent);
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteAllOperationsCommand] Executing.");
        this._oldOperations = new HashMap<>();

        OpenApiPathItem parent = (OpenApiPathItem) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        // Save the old operations (if any)
        for (String method : ALL_METHODS) {
            OpenApiOperation oldOp = (OpenApiOperation) NodeUtil.getProperty(parent, method);
            if (!this.isNullOrUndefined(oldOp)) {
                this._oldOperations.put(method, Library.writeNode(oldOp));
                NodeUtil.setProperty(parent, method, null);
            }
        }
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteAllOperationsCommand] Reverting.");
        if (this._oldOperations == null || this._oldOperations.isEmpty()) {
            return;
        }

        OpenApiPathItem parent = (OpenApiPathItem) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        for (String method : this._oldOperations.keySet()) {
            OpenApiOperation operation = parent.createOperation();
            Library.readNode(this._oldOperations.get(method), operation);
            setOperation(parent, operation, method);
        }
    }

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
