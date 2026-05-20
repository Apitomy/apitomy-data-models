package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.models.ServerVariable;
import io.apitomy.datamodels.models.openapi.OpenApiServer;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.List;
import java.util.Map;

/**
 * A command used to add a variable to a server.
 * @author eric.wittmann@gmail.com
 */
public class AddServerVariableCommand extends AbstractCommand {

    public NodePath _serverPath;
    public String _variableName;
    public String _defaultValue;
    public String _description;
    public List<String> _enumValues;

    public boolean _created;

    public AddServerVariableCommand() {
    }

    public AddServerVariableCommand(OpenApiServer server, String variableName,
                                    String defaultValue, String description,
                                    List<String> enumValues) {
        this._serverPath = NodePathUtil.createNodePath((Node) server);
        this._variableName = variableName;
        this._defaultValue = defaultValue;
        this._description = description;
        this._enumValues = enumValues;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddServerVariableCommand] Executing.");
        this._created = false;

        Server server = (Server) NodePathUtil.resolveNodePath(this._serverPath, document);
        if (this.isNullOrUndefined(server)) {
            return;
        }

        // Check if variable already exists
        Map<String, ? extends ServerVariable> variables = (Map<String, ? extends ServerVariable>) server.getVariables();
        if (!this.isNullOrUndefined(variables) && variables.containsKey(this._variableName)) {
            return;
        }

        // Create the variable
        ServerVariable variable = server.createServerVariable();
        if (!this.isNullOrUndefined(this._defaultValue)) {
            variable.setDefault(this._defaultValue);
        }
        if (!this.isNullOrUndefined(this._description)) {
            variable.setDescription(this._description);
        }
        if (!this.isNullOrUndefined(this._enumValues)) {
            variable.setEnum(this._enumValues);
        }

        server.addVariable(this._variableName, variable);
        this._created = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddServerVariableCommand] Reverting.");
        if (!this._created) {
            return;
        }

        Server server = (Server) NodePathUtil.resolveNodePath(this._serverPath, document);
        if (this.isNullOrUndefined(server)) {
            return;
        }

        server.removeVariable(this._variableName);
    }

}
