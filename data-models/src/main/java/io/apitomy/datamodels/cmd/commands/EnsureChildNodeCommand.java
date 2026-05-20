package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to ensure that a child node exists. The command creates
 * the child node and adds it if it does not already exist.
 * @author eric.wittmann@gmail.com
 */
public class EnsureChildNodeCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _childPropertyName;

    public boolean _parentExisted;
    public boolean _childExisted;

    public EnsureChildNodeCommand() {
    }

    public EnsureChildNodeCommand(NodePath parentPath, String childPropertyName) {
        this._parentPath = parentPath;
        this._childPropertyName = childPropertyName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[EnsureChildNodeCommand] Executing.");
        this._parentExisted = false;
        this._childExisted = false;

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        this._parentExisted = true;

        // Check if child already exists using getter
        Object existingChild = NodeUtil.getProperty(parent, this._childPropertyName);
        if (!this.isNullOrUndefined(existingChild)) {
            this._childExisted = true;
            return;
        }

        // Create the child using the "create" method naming convention
        String createMethodName = "create" + capitalize(this._childPropertyName);
        try {
            java.lang.reflect.Method createMethod = parent.getClass().getMethod(createMethodName);
            Object newChild = createMethod.invoke(parent);

            // Set the child using the setter
            NodeUtil.setProperty(parent, this._childPropertyName, newChild);
        } catch (Exception e) {
            LoggerUtil.warn("[EnsureChildNodeCommand] Failed to create child '%s' on parent: %s",
                    this._childPropertyName, e.getMessage());
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[EnsureChildNodeCommand] Reverting.");
        if (!this._parentExisted || this._childExisted) {
            return;
        }

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        // Remove the child we created by setting it to null
        NodeUtil.setProperty(parent, this._childPropertyName, null);
    }

    /**
     * Capitalizes the first character of a string.
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
