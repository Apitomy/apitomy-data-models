package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.models.asyncapi.AsyncApiServer;
import io.apitomy.datamodels.models.asyncapi.AsyncApiServers;
import io.apitomy.datamodels.models.openapi.OpenApiServer;
import io.apitomy.datamodels.models.openapi.OpenApiServersParent;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

import java.util.List;

/**
 * A command used to delete a server from any node that supports servers.
 * @author eric.wittmann@gmail.com
 */
public class DeleteServerCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _serverUrl;
    public String _serverName;

    public ObjectNode _oldServer;
    public int _serverIndex;

    public DeleteServerCommand() {
    }

    public DeleteServerCommand(OpenApiServersParent parent, String serverUrl) {
        this._parentPath = NodePathUtil.createNodePath((Node) parent);
        this._serverUrl = serverUrl;
    }

    public DeleteServerCommand(AsyncApiDocument document, String serverName) {
        this._parentPath = Library.createNodePath(document);
        this._serverName = serverName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteServerCommand] Executing.");
        this._oldServer = null;
        this._serverIndex = -1;

        if (ModelTypeUtil.isAsyncApiModel(document)) {
            executeForAsyncApi((AsyncApiDocument) document);
        } else {
            executeForOpenApi(document);
        }
    }

    private void executeForOpenApi(Document document) {
        OpenApiServersParent parent = (OpenApiServersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        List<Server> servers = parent.getServers();
        if (this.isNullOrUndefined(servers)) {
            return;
        }

        int idx = 0;
        for (Server server : servers) {
            if (this._serverUrl.equals(((OpenApiServer) server).getUrl())) {
                this._oldServer = Library.writeNode(server);
                this._serverIndex = idx;
                parent.removeServer(server);
                return;
            }
            idx++;
        }
    }

    private void executeForAsyncApi(AsyncApiDocument doc) {
        AsyncApiServers servers = doc.getServers();
        if (this.isNullOrUndefined(servers)) {
            return;
        }

        AsyncApiServer server = servers.getItem(this._serverName);
        if (this.isNullOrUndefined(server)) {
            return;
        }

        this._oldServer = Library.writeNode(server);
        this._serverIndex = servers.getItemNames().indexOf(this._serverName);
        servers.removeItem(this._serverName);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteServerCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldServer)) {
            return;
        }

        if (ModelTypeUtil.isAsyncApiModel(document)) {
            undoForAsyncApi((AsyncApiDocument) document);
        } else {
            undoForOpenApi(document);
        }
    }

    private void undoForOpenApi(Document document) {
        if (this._serverIndex < 0) {
            return;
        }

        OpenApiServersParent parent = (OpenApiServersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        OpenApiServer newServer = (OpenApiServer) parent.createServer();
        Library.readNode(this._oldServer, newServer);
        parent.insertServer(newServer, this._serverIndex);
    }

    private void undoForAsyncApi(AsyncApiDocument doc) {
        AsyncApiServers servers = doc.getServers();
        if (this.isNullOrUndefined(servers)) {
            servers = doc.createServers();
            doc.setServers(servers);
        }

        AsyncApiServer newServer = servers.createServer();
        Library.readNode(this._oldServer, newServer);
        servers.insertItem(this._serverName, newServer, this._serverIndex);
    }

}
