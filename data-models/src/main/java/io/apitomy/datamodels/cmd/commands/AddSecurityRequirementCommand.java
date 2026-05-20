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
 * A command used to create a new security requirement in a document.
 * @author eric.wittmann@gmail.com
 */
public class AddSecurityRequirementCommand extends AbstractCommand {

    public NodePath _parentPath;
    public ObjectNode _requirement;

    public boolean _added;
    
    public AddSecurityRequirementCommand() {
    }

    public AddSecurityRequirementCommand(SecurityRequirementsParent parent, SecurityRequirement requirement) {
        this._parentPath = Library.createNodePath((Node) parent);
        this._requirement = Library.writeNode(requirement);
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddSecurityRequirementCommand] Executing.");
        this._added = false;

        SecurityRequirementsParent parent = (SecurityRequirementsParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }
        SecurityRequirement requirement = parent.createSecurityRequirement();
        Library.readNode(this._requirement, requirement);
        parent.addSecurity(requirement);
        this._added = true;
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddSecurityRequirementCommand] Reverting.");
        if (!this._added) {
            return;
        }

        SecurityRequirementsParent parent = (SecurityRequirementsParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        List<SecurityRequirement> security = parent.getSecurity();
        if (this.isNullOrUndefined(security)) {
            LoggerUtil.info("[AddSecurityRequirementCommand] Security requirement not found, skipping undo.");
            return;
        }

        SecurityRequirement requirement = parent.createSecurityRequirement();
        Library.readNode(this._requirement, requirement);

        int idx = this.indexOfRequirement(security, requirement);
        if (idx != -1) {
            security.remove(idx);
        }
    }

    protected int indexOfRequirement(List<SecurityRequirement> requirements, SecurityRequirement requirement) {
        int idx = 0;
        for (SecurityRequirement r : requirements) {
            if (this.isEqual(r, requirement)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    protected boolean isEqual(SecurityRequirement req1, SecurityRequirement req2) {
        List<String> names1 = req1.getItemNames();
        List<String> names2 = req2.getItemNames();
        if (names1.size() != names2.size()) {
            return false;
        }
        boolean rval = true;
        for (String name1 : names1) {
            if (names2.indexOf(name1) == -1 || !areScopesEqual(req1.getItem(name1), req2.getItem(name1))) {
                rval = false;
            }
        }
        return rval;
    }

    protected boolean areScopesEqual(List<String> scopes1, List<String> scopes2) {
        if(scopes1.size() != scopes2.size()) {
            return false;
        }
        boolean rval = true;
        for (String scope1 : scopes1) {
            if (scopes2.indexOf(scope1) == -1) {
                rval = false;
            }
        }
        return rval;
    }

}
