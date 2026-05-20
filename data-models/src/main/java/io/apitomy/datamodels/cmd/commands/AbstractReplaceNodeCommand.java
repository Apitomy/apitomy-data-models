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
 * Base class for commands that replace entire nodes.
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractReplaceNodeCommand<T extends Node> extends AbstractCommand {
    
    public NodePath _nodePath;
    public ObjectNode _new;

    public ObjectNode _old;
    
    public AbstractReplaceNodeCommand() {
    }

    public AbstractReplaceNodeCommand(T old, T replacement) {
        this._nodePath = Library.createNodePath(old);
        this._new = Library.writeNode(replacement);
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[ReplaceNodeCommand] Executing.");
        this._old = null;

        @SuppressWarnings("unchecked")
        T oldNode = (T) NodePathUtil.resolveNodePath(this._nodePath, document);
        if (this.isNullOrUndefined(oldNode)) {
            return;
        }

        this._old = Library.writeNode(oldNode);
        T newNode = this.readNode(oldNode.parent(), this._new);
        this.replaceNode(oldNode.parent(), newNode);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[ReplaceNodeCommand] Reverting.");
        if (this.isNullOrUndefined(this._old)) {
            return;
        }

        @SuppressWarnings("unchecked")
        T node = (T) NodePathUtil.resolveNodePath(this._nodePath, document);
        if (this.isNullOrUndefined(node)) {
            return;
        }

        T restoreNode = this.readNode(node.parent(), this._old);
        this.replaceNode(node.parent(), restoreNode);
    }

    /**
     * Replaces the old node entry with the new node in the data model, without changing the order of the nodes.
     */
    protected abstract void replaceNode(Node parent, T newNode);

    /**
     * Unmarshalls a node into the appropriate type.
     */
    protected abstract T readNode(Node parent, ObjectNode node);

}
