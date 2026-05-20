package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiCallback;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xCallback;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xOperation;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to add a callback to an operation or to components/callbacks.
 * @author eric.wittmann@gmail.com
 */
public class AddCallbackCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _callbackName;
    public ObjectNode _from;

    public boolean _created;

    public AddCallbackCommand() {
    }

    public AddCallbackCommand(Node parent, String callbackName, ObjectNode from) {
        this._parentPath = NodePathUtil.createNodePath(parent);
        this._callbackName = callbackName;
        this._from = from;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddCallbackCommand] Executing.");
        this._created = false;

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (parent instanceof OpenApi3xOperation) {
            OpenApi3xOperation operation3x = (OpenApi3xOperation) parent;
            if (!this.isNullOrUndefined(operation3x.getCallbacks()) && operation3x.getCallbacks().containsKey(this._callbackName)) {
                return;
            }
            OpenApi3xCallback callback = operation3x.createCallback();
            Library.readNode(this._from, callback);
            operation3x.addCallback(this._callbackName, callback);
            this._created = true;
        } else if (parent instanceof OpenApi3xComponents) {
            OpenApi3xComponents components3x = (OpenApi3xComponents) parent;
            if (!this.isNullOrUndefined(components3x.getCallbacks()) && components3x.getCallbacks().containsKey(this._callbackName)) {
                return;
            }
            OpenApiCallback callback = components3x.createCallback();
            Library.readNode(this._from, callback);
            components3x.addCallback(this._callbackName, callback);
            this._created = true;
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddCallbackCommand] Reverting.");
        if (!this._created) {
            return;
        }

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (parent instanceof OpenApi3xOperation) {
            ((OpenApi3xOperation) parent).removeCallback(this._callbackName);
        } else if (parent instanceof OpenApi3xComponents) {
            ((OpenApi3xComponents) parent).removeCallback(this._callbackName);
        }
    }

}
