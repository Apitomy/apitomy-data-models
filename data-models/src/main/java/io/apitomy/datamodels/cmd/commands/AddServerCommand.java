package io.apitomy.datamodels.cmd.commands;

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
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Server;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Server;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.util.RegexUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A command used to add a new server to any node that supports servers.
 * @author eric.wittmann@gmail.com
 */
public class AddServerCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _serverUrl;
    public String _serverDescription;
    public String _serverName;

    public boolean _serverCreated;
    public boolean _nullServersParent;

    public AddServerCommand() {
    }

    public AddServerCommand(OpenApiServersParent parent, String serverUrl, String serverDescription) {
        this._parentPath = NodePathUtil.createNodePath((Node) parent);
        this._serverUrl = serverUrl;
        this._serverDescription = serverDescription;
    }

    public AddServerCommand(AsyncApiDocument document, String serverName, String serverUrl,
                            String serverDescription) {
        this._parentPath = Library.createNodePath(document);
        this._serverName = serverName;
        this._serverUrl = serverUrl;
        this._serverDescription = serverDescription;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddServerCommand] Executing.");
        this._serverCreated = false;
        this._nullServersParent = false;

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

        // Check if server already exists
        List<Server> existingServers = parent.getServers();
        if (!this.isNullOrUndefined(existingServers)) {
            for (Server server : existingServers) {
                if (this._serverUrl.equals(((OpenApiServer) server).getUrl())) {
                    return;
                }
            }
        }

        // Create new server
        OpenApiServer newServer = (OpenApiServer) parent.createServer();
        newServer.setUrl(this._serverUrl);
        if (!this.isNullOrUndefined(this._serverDescription) && !this._serverDescription.isEmpty()) {
            newServer.setDescription(this._serverDescription);
        }

        // Extract and create server variables
        List<String> variableNames = extractVariables(this._serverUrl);
        for (String variableName : variableNames) {
            if (ModelTypeUtil.isOpenApi30Model((Node) parent)) {
                OpenApi30Server server30 = (OpenApi30Server) newServer;
                server30.addVariable(variableName, server30.createServerVariable());
            } else if (ModelTypeUtil.isOpenApi31Model((Node) parent)) {
                OpenApi31Server server31 = (OpenApi31Server) newServer;
                server31.addVariable(variableName, server31.createServerVariable());
            }
        }

        parent.addServer(newServer);
        this._serverCreated = true;
    }

    private void executeForAsyncApi(AsyncApiDocument doc) {
        AsyncApiServers servers = doc.getServers();
        if (this.isNullOrUndefined(servers)) {
            servers = doc.createServers();
            doc.setServers(servers);
            this._nullServersParent = true;
        }

        // Check if server with this name already exists
        if (!this.isNullOrUndefined(servers.getItem(this._serverName))) {
            return;
        }

        // Create new server
        AsyncApiServer newServer = servers.createServer();
        NodeUtil.setProperty(newServer, "url", this._serverUrl);
        if (!this.isNullOrUndefined(this._serverDescription) && !this._serverDescription.isEmpty()) {
            newServer.setDescription(this._serverDescription);
        }

        // Extract and create server variables
        List<String> variableNames = extractVariables(this._serverUrl);
        for (String variableName : variableNames) {
            newServer.addVariable(variableName, newServer.createServerVariable());
        }

        servers.addItem(this._serverName, newServer);
        this._serverCreated = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddServerCommand] Reverting.");
        if (!this._serverCreated) {
            return;
        }

        if (ModelTypeUtil.isAsyncApiModel(document)) {
            undoForAsyncApi((AsyncApiDocument) document);
        } else {
            undoForOpenApi(document);
        }
    }

    private void undoForOpenApi(Document document) {
        OpenApiServersParent parent = (OpenApiServersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        List<Server> servers = parent.getServers();
        if (this.isNullOrUndefined(servers)) {
            return;
        }

        for (Server server : servers) {
            if (this._serverUrl.equals(((OpenApiServer) server).getUrl())) {
                parent.removeServer(server);
                return;
            }
        }
    }

    private void undoForAsyncApi(AsyncApiDocument doc) {
        if (this._nullServersParent) {
            doc.setServers(null);
        } else {
            AsyncApiServers servers = doc.getServers();
            if (!this.isNullOrUndefined(servers)) {
                servers.removeItem(this._serverName);
            }
        }
    }

    /**
     * Extracts variable names from a server URL template.
     * For example, "http://{domain}.example.com:{port}/api" returns ["domain", "port"].
     */
    private static List<String> extractVariables(String url) {
        List<String> variables = new ArrayList<>();
        List<String[]> matches = RegexUtil.findMatches(url, "\\{([^}]+)\\}");
        for (String[] match : matches) {
            variables.add(match[1]);
        }
        return variables;
    }

}
