package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Extensible;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.Map;

/**
 * A command used to add a vendor extension to any extensible node.
 * @author eric.wittmann@gmail.com
 */
public class AddExtensionCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _name;
    public JsonNode _value;

    public boolean _existed;
    public JsonNode _oldValue;

    public AddExtensionCommand() {
    }

    public AddExtensionCommand(Extensible parent, String name, JsonNode value) {
        this._parentPath = NodePathUtil.createNodePath((Node) parent);
        this._name = name;
        this._value = value;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddExtensionCommand] Executing.");
        this._existed = false;
        this._oldValue = null;

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        Extensible extensible = (Extensible) parent;
        Map<String, JsonNode> extensions = extensible.getExtensions();
        if (!this.isNullOrUndefined(extensions) && extensions.containsKey(this._name)) {
            this._existed = true;
            this._oldValue = extensions.get(this._name);
        }
        extensible.addExtension(this._name, this._value);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddExtensionCommand] Reverting.");

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        Extensible extensible = (Extensible) parent;
        if (this._existed) {
            extensible.addExtension(this._name, this._oldValue);
        } else {
            extensible.removeExtension(this._name);
        }
    }

}
