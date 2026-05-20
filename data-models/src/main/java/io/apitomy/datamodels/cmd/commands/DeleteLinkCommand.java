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
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A command used to delete a link from a response or from components/links.
 * @author eric.wittmann@gmail.com
 */
public class DeleteLinkCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _linkName;

    public ObjectNode _oldLink;
    public int _oldIndex;

    public DeleteLinkCommand() {
    }

    public DeleteLinkCommand(Node parent, String linkName) {
        this._parentPath = NodePathUtil.createNodePath(parent);
        this._linkName = linkName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteLinkCommand] Executing.");
        this._oldLink = null;

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (parent instanceof OpenApi3xResponse) {
            OpenApi3xResponse response3x = (OpenApi3xResponse) parent;
            Map<String, OpenApi3xLink> links = response3x.getLinks();
            if (this.isNullOrUndefined(links) || !links.containsKey(this._linkName)) {
                return;
            }
            OpenApi3xLink link = links.get(this._linkName);
            this._oldLink = Library.writeNode(link);
            List<String> linkNames = new ArrayList<>(links.keySet());
            this._oldIndex = linkNames.indexOf(this._linkName);
            response3x.removeLink(this._linkName);
        } else if (parent instanceof OpenApi3xComponents) {
            OpenApi3xComponents components3x = (OpenApi3xComponents) parent;
            Map<String, OpenApiLink> links = components3x.getLinks();
            if (this.isNullOrUndefined(links) || !links.containsKey(this._linkName)) {
                return;
            }
            OpenApiLink link = links.get(this._linkName);
            this._oldLink = Library.writeNode(link);
            List<String> linkNames = new ArrayList<>(links.keySet());
            this._oldIndex = linkNames.indexOf(this._linkName);
            components3x.removeLink(this._linkName);
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteLinkCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldLink)) {
            return;
        }

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (parent instanceof OpenApi3xResponse) {
            OpenApi3xResponse response3x = (OpenApi3xResponse) parent;
            OpenApi3xLink link = response3x.createLink();
            Library.readNode(this._oldLink, link);
            response3x.insertLink(this._linkName, link, this._oldIndex);
        } else if (parent instanceof OpenApi3xComponents) {
            OpenApi3xComponents components3x = (OpenApi3xComponents) parent;
            OpenApiLink link = components3x.createLink();
            Library.readNode(this._oldLink, link);
            components3x.insertLink(this._linkName, link, this._oldIndex);
        }
    }

}
