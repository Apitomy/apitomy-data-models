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
 * A command used to delete a security requirement from an operation by index.
 * @author eric.wittmann@gmail.com
 */
public class DeleteOperationSecurityRequirementCommand extends AbstractCommand {

    public NodePath _operationPath;
    public int _index;

    public ObjectNode _oldRequirement;

    public DeleteOperationSecurityRequirementCommand() {
    }

    public DeleteOperationSecurityRequirementCommand(SecurityRequirementsParent operation, int index) {
        this._operationPath = NodePathUtil.createNodePath((Node) operation);
        this._index = index;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteOperationSecurityRequirementCommand] Executing.");
        this._oldRequirement = null;

        SecurityRequirementsParent operation = (SecurityRequirementsParent) NodePathUtil.resolveNodePath(
                this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        List<SecurityRequirement> security = (List<SecurityRequirement>) operation.getSecurity();
        if (this.isNullOrUndefined(security) || this._index < 0 || this._index >= security.size()) {
            return;
        }

        SecurityRequirement requirement = security.get(this._index);
        this._oldRequirement = Library.writeNode(requirement);
        operation.removeSecurity(requirement);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteOperationSecurityRequirementCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldRequirement)) {
            return;
        }

        SecurityRequirementsParent operation = (SecurityRequirementsParent) NodePathUtil.resolveNodePath(
                this._operationPath, document);
        if (this.isNullOrUndefined(operation)) {
            return;
        }

        SecurityRequirement requirement = operation.createSecurityRequirement();
        Library.readNode(this._oldRequirement, requirement);
        operation.insertSecurity(requirement, this._index);
    }

}
