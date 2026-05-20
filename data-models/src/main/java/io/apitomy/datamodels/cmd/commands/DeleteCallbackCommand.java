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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A command used to delete a callback from an operation or from components/callbacks.
 * @author eric.wittmann@gmail.com
 */
public class DeleteCallbackCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _callbackName;

    public ObjectNode _oldCallback;
    public int _oldIndex;

    public DeleteCallbackCommand() {
    }

    public DeleteCallbackCommand(Node parent, String callbackName) {
        this._parentPath = NodePathUtil.createNodePath(parent);
        this._callbackName = callbackName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteCallbackCommand] Executing.");
        this._oldCallback = null;

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (parent instanceof OpenApi3xOperation) {
            OpenApi3xOperation operation3x = (OpenApi3xOperation) parent;
            Map<String, OpenApi3xCallback> callbacks = operation3x.getCallbacks();
            if (this.isNullOrUndefined(callbacks) || !callbacks.containsKey(this._callbackName)) {
                return;
            }
            OpenApi3xCallback callback = callbacks.get(this._callbackName);
            this._oldCallback = Library.writeNode(callback);
            List<String> callbackNames = new ArrayList<>(callbacks.keySet());
            this._oldIndex = callbackNames.indexOf(this._callbackName);
            operation3x.removeCallback(this._callbackName);
        } else if (parent instanceof OpenApi3xComponents) {
            OpenApi3xComponents components3x = (OpenApi3xComponents) parent;
            Map<String, OpenApiCallback> callbacks = components3x.getCallbacks();
            if (this.isNullOrUndefined(callbacks) || !callbacks.containsKey(this._callbackName)) {
                return;
            }
            OpenApiCallback callback = callbacks.get(this._callbackName);
            this._oldCallback = Library.writeNode(callback);
            List<String> callbackNames = new ArrayList<>(callbacks.keySet());
            this._oldIndex = callbackNames.indexOf(this._callbackName);
            components3x.removeCallback(this._callbackName);
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteCallbackCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldCallback)) {
            return;
        }

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (parent instanceof OpenApi3xOperation) {
            OpenApi3xOperation operation3x = (OpenApi3xOperation) parent;
            OpenApi3xCallback callback = operation3x.createCallback();
            Library.readNode(this._oldCallback, callback);
            operation3x.insertCallback(this._callbackName, callback, this._oldIndex);
        } else if (parent instanceof OpenApi3xComponents) {
            OpenApi3xComponents components3x = (OpenApi3xComponents) parent;
            OpenApiCallback callback = components3x.createCallback();
            Library.readNode(this._oldCallback, callback);
            components3x.insertCallback(this._callbackName, callback, this._oldIndex);
        }
    }

}
