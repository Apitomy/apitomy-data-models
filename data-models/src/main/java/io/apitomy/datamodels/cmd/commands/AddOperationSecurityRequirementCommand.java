package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.SecurityRequirement;
import io.apitomy.datamodels.models.SecurityRequirementsParent;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.List;

/**
 * A command used to add a security requirement to an operation.
 * @author eric.wittmann@gmail.com
 */
public class AddOperationSecurityRequirementCommand extends AbstractCommand {

    public NodePath _operationPath;
    public ObjectNode _requirement;

    public boolean _added;

    public AddOperationSecurityRequirementCommand() {
    }

    public AddOperationSecurityRequirementCommand(SecurityRequirementsParent operation,
                                                  SecurityRequirement requirement) {
        this._operationPath = NodePathUtil.createNodePath((Node) operation);
        this._requirement = Library.writeNode(requirement);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddOperationSecurityRequirementCommand] Executing.");
        this._added = false;

        SecurityRequirementsParent operation = (SecurityRequirementsParent) NodePathUtil.resolveNodePath(
                this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        SecurityRequirement requirement = operation.createSecurityRequirement();
        Library.readNode(this._requirement, requirement);
        operation.addSecurity(requirement);
        this._added = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddOperationSecurityRequirementCommand] Reverting.");
        if (!this._added) {
            return;
        }

        SecurityRequirementsParent operation = (SecurityRequirementsParent) NodePathUtil.resolveNodePath(
                this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        List<SecurityRequirement> security = (List<SecurityRequirement>) operation.getSecurity();
        if (this.isNullOrUndefined(security) || security.isEmpty()) {
            return;
        }

        // Remove the last added requirement
        SecurityRequirement last = security.get(security.size() - 1);
        operation.removeSecurity(last);
    }

}
