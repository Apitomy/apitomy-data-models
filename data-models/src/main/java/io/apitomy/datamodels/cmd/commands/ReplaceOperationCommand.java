package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to replace an operation with a newer version.
 * @author eric.wittmann@gmail.com
 */
public class ReplaceOperationCommand extends AbstractReplaceNodeCommand<OpenApiOperation> {
    
    public ReplaceOperationCommand() {
    }

    public ReplaceOperationCommand(OpenApiOperation old, OpenApiOperation replacement) {
        super(old, replacement);
    }

    @Override
    protected void replaceNode(Node parent, OpenApiOperation newNode) {
        OpenApiPathItem pathItem = (OpenApiPathItem) parent;
        String method = this._nodePath.getLastSegment().getValue();
        NodeUtil.setProperty(pathItem, method, newNode);
    }

    @Override
    protected OpenApiOperation readNode(Node parent, ObjectNode node) {
        OpenApiPathItem pathItem = (OpenApiPathItem) parent;
        OpenApiOperation operation = pathItem.createOperation();
        Library.readNode(node, operation);
        return operation;
    }

}
