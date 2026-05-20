package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.OpenApiHeadersParent;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A command used to delete all headers from a document.
 * @author vvilerio
 */
public class DeleteAllHeadersCommand extends AbstractCommand {

    public NodePath _parentPath;

    public Map<String, ObjectNode> _oldHeaders;

    public DeleteAllHeadersCommand() {
    }

    public DeleteAllHeadersCommand(OpenApiHeadersParent parent) {
        this._parentPath = NodePathUtil.createNodePath((Node) parent);
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteAllHeadersCommand] Executing.");

        OpenApiHeadersParent parent = (OpenApiHeadersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        Map<String, ? extends Node> headers = parent.getHeaders();
        if (this.isNullOrUndefined(headers) || headers.size() == 0) {
            return;
        }

        // Preserve insertion order with LinkedHashMap
        this._oldHeaders = new LinkedHashMap<>();

        // Save the headers we're about to delete for later undo
        headers.keySet().forEach(name -> {
            Node header = headers.get(name);
            this._oldHeaders.put(name, Library.writeNode(header));
        });

        parent.clearHeaders();
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteAllHeaders] Reverting.");

        if (this.isNullOrUndefined(this._oldHeaders) || this._oldHeaders.size() == 0) {
            return;
        }

        OpenApiHeadersParent parent = (OpenApiHeadersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        for (String k : this._oldHeaders.keySet()) {
            OpenApiHeader header = parent.createHeader();
            Library.readNode(this._oldHeaders.get(k), header);
            parent.addHeader(k, header);
        }

    }

}
