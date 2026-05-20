package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to update a node with new content. This is typically used
 * when a source editor modifies a node's content directly as JSON/YAML.
 * The command saves the old content and replaces it with the new content.
 * @author eric.wittmann@gmail.com
 */
public class UpdateNodeCommand extends AbstractCommand {

    public NodePath _nodePath;
    public ObjectNode _newContent;

    public ObjectNode _oldContent;

    public UpdateNodeCommand() {
    }

    public UpdateNodeCommand(Node node, ObjectNode newContent) {
        this._nodePath = NodePathUtil.createNodePath(node);
        this._newContent = newContent;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[UpdateNodeCommand] Executing.");
        this._oldContent = null;

        Node node = NodePathUtil.resolveNodePath(this._nodePath, document);
        if (this.isNullOrUndefined(node)) {
            return;
        }

        // Save the old content
        this._oldContent = Library.writeNode(node);

        // Apply the new content to the node
        Library.readNode(this._newContent, node);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[UpdateNodeCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldContent)) {
            return;
        }

        Node node = NodePathUtil.resolveNodePath(this._nodePath, document);
        if (this.isNullOrUndefined(node)) {
            return;
        }

        // Restore the old content
        Library.readNode(this._oldContent, node);
    }

}
