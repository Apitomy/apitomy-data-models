package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiPaths;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to delete a path item from a document.
 * @author eric.wittmann@gmail.com
 */
public class DeletePathCommand extends AbstractCommand {

    public String _pathName;

    public ObjectNode _oldPathItem;
    public int _oldIndex;

    public DeletePathCommand() {
    }

    public DeletePathCommand(String pathName) {
        this._pathName = pathName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeletePathCommand] Executing.");
        this._oldPathItem = null;

        OpenApiDocument doc = (OpenApiDocument) document;
        OpenApiPaths paths = doc.getPaths();
        if (this.isNullOrUndefined(paths)) {
            return;
        }

        OpenApiPathItem pathItem = paths.getItem(this._pathName);
        if (this.isNullOrUndefined(pathItem)) {
            return;
        }

        // Save the path item and its index for undo
        this._oldPathItem = Library.writeNode(pathItem);
        this._oldIndex = paths.getItemNames().indexOf(this._pathName);

        // Remove the path item
        paths.removeItem(this._pathName);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeletePathCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldPathItem)) {
            return;
        }

        OpenApiDocument doc = (OpenApiDocument) document;
        OpenApiPaths paths = doc.getPaths();
        if (this.isNullOrUndefined(paths)) {
            paths = doc.createPaths();
            doc.setPaths(paths);
        }

        OpenApiPathItem newPathItem = paths.createPathItem();
        Library.readNode(this._oldPathItem, newPathItem);
        paths.insertItem(this._pathName, newPathItem, this._oldIndex);
    }

}
