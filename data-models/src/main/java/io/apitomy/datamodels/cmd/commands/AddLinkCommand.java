package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiLink;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xLink;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xResponse;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Components;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Link;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Response;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Components;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Link;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Response;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to add a link to a response or to components/links.
 * @author eric.wittmann@gmail.com
 */
public class AddLinkCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _linkName;
    public ObjectNode _from;

    public boolean _created;

    public AddLinkCommand() {
    }

    public AddLinkCommand(Node parent, String linkName, ObjectNode from) {
        this._parentPath = NodePathUtil.createNodePath(parent);
        this._linkName = linkName;
        this._from = from;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddLinkCommand] Executing.");
        this._created = false;

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (parent instanceof OpenApi3xResponse) {
            OpenApi3xResponse response3x = (OpenApi3xResponse) parent;
            if (!this.isNullOrUndefined(response3x.getLinks()) && response3x.getLinks().containsKey(this._linkName)) {
                return;
            }
            OpenApi3xLink link = response3x.createLink();
            Library.readNode(this._from, link);
            response3x.addLink(this._linkName, link);
            this._created = true;
        } else if (parent instanceof OpenApi3xComponents) {
            OpenApi3xComponents components3x = (OpenApi3xComponents) parent;
            if (!this.isNullOrUndefined(components3x.getLinks()) && components3x.getLinks().containsKey(this._linkName)) {
                return;
            }
            OpenApiLink link = components3x.createLink();
            Library.readNode(this._from, link);
            components3x.addLink(this._linkName, link);
            this._created = true;
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddLinkCommand] Reverting.");
        if (!this._created) {
            return;
        }

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (parent instanceof OpenApi3xResponse) {
            ((OpenApi3xResponse) parent).removeLink(this._linkName);
        } else if (parent instanceof OpenApi3xComponents) {
            ((OpenApi3xComponents) parent).removeLink(this._linkName);
        }
    }

}
