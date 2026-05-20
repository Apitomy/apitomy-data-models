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
import io.apitomy.datamodels.util.NodeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A command used to delete all security requirements from a document or operation.
 * @author eric.wittmann@gmail.com
 */
public class DeleteAllSecurityRequirementsCommand extends AbstractCommand {

    public NodePath _parentPath;
    public List<ObjectNode> _oldSecurityRequirements;
    
    public DeleteAllSecurityRequirementsCommand() {
    }

    public DeleteAllSecurityRequirementsCommand(Node parent) {
        this._parentPath = Library.createNodePath(parent);
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteAllSecurityRequirementsCommand] Executing.");
        this._oldSecurityRequirements = new ArrayList<>();

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        // Save the old security-requirements (if any)
        List<? extends SecurityRequirement> requirements = (List<? extends SecurityRequirement>) NodeUtil.getProperty(parent, "security");
        if (!this.isNullOrUndefined(requirements)) {
            requirements.forEach( req -> {
                this._oldSecurityRequirements.add(Library.writeNode(req));
            });
        }

        clearSecurityRequirements((SecurityRequirementsParent) parent);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteAllSecurityRequirementsCommand] Reverting.");
        if (NodeUtil.isDefined(this._oldSecurityRequirements) && this._oldSecurityRequirements.isEmpty()) {
            return;
        }

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        List<? extends SecurityRequirement> requirements = (List<? extends SecurityRequirement>) NodeUtil.getProperty(parent, "security");
        if (this.isNullOrUndefined(requirements)) {
            requirements = new ArrayList<>();
            NodeUtil.setProperty(parent, "security", requirements);
        }
        for (ObjectNode oldSecurityRequirement : this._oldSecurityRequirements) {
            SecurityRequirement requirement = createSecurityRequirement((SecurityRequirementsParent) parent);
            Library.readNode(oldSecurityRequirement, requirement);
            addSecurityRequirement((SecurityRequirementsParent) parent, requirement);
        }
    }

    private void clearSecurityRequirements(SecurityRequirementsParent parent) {
        parent.clearSecurity();
    }

    private SecurityRequirement createSecurityRequirement(SecurityRequirementsParent parent) {
        return parent.createSecurityRequirement();
    }

    private void addSecurityRequirement(SecurityRequirementsParent parent, SecurityRequirement requirement) {
        parent.addSecurity(requirement);
    }

}
