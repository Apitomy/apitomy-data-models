package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiPaths;

/**
 * A command used to replace a path item with a newer version.
 * @author eric.wittmann@gmail.com
 */
public class ReplacePathItemCommand extends AbstractReplaceNodeCommand<OpenApiPathItem> {

    public ReplacePathItemCommand() {
    }
    
    public ReplacePathItemCommand(OpenApiPathItem old, OpenApiPathItem replacement) {
        super(old, replacement);
    }

    @Override
    protected void replaceNode(Node parent, OpenApiPathItem newNode) {
        OpenApiPaths paths = (OpenApiPaths) parent;
        String pathName = this._nodePath.getLastSegment().getValue();

        paths.addItem(pathName, newNode);
    }

    @Override
    protected OpenApiPathItem readNode(Node parent, ObjectNode node) {
        OpenApiPaths paths = (OpenApiPaths) parent;
        OpenApiPathItem pathItem = paths.createPathItem();
        Library.readNode(node, pathItem);
        return pathItem;
    }

}
