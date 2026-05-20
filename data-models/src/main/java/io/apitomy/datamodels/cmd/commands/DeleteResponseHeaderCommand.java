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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A command used to delete a header from a response.
 * @author eric.wittmann@gmail.com
 */
public class DeleteResponseHeaderCommand extends AbstractCommand {

    public NodePath _responsePath;
    public String _headerName;

    public ObjectNode _oldHeader;
    public int _oldIndex;

    public DeleteResponseHeaderCommand() {
    }

    public DeleteResponseHeaderCommand(OpenApiHeadersParent response, String headerName) {
        this._responsePath = NodePathUtil.createNodePath((Node) response);
        this._headerName = headerName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteResponseHeaderCommand] Executing.");
        this._oldHeader = null;

        OpenApiHeadersParent response = (OpenApiHeadersParent) NodePathUtil.resolveNodePath(this._responsePath, document);
        if (this.isNullOrUndefined(response)) {
            return;
        }

        Map<String, ? extends OpenApiHeader> headers = (Map<String, ? extends OpenApiHeader>) response.getHeaders();
        if (this.isNullOrUndefined(headers) || !headers.containsKey(this._headerName)) {
            return;
        }

        // Save the header and its index for undo
        OpenApiHeader header = headers.get(this._headerName);
        this._oldHeader = Library.writeNode(header);
        List<String> headerNames = new ArrayList<>(headers.keySet());
        this._oldIndex = headerNames.indexOf(this._headerName);

        // Remove the header
        response.removeHeader(this._headerName);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteResponseHeaderCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldHeader)) {
            return;
        }

        OpenApiHeadersParent response = (OpenApiHeadersParent) NodePathUtil.resolveNodePath(this._responsePath, document);
        if (this.isNullOrUndefined(response)) {
            return;
        }

        OpenApiHeader newHeader = response.createHeader();
        Library.readNode(this._oldHeader, newHeader);
        response.insertHeader(this._headerName, newHeader, this._oldIndex);
    }

}
