package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiPaths;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to create a new empty path item in a document.
 * @author eric.wittmann@gmail.com
 */
public class CreatePathCommand extends AbstractCommand {

    public String _pathName;

    public boolean _pathCreated;
    public boolean _pathsCreated;

    public CreatePathCommand() {
    }

    public CreatePathCommand(String pathName) {
        this._pathName = pathName.startsWith("/") ? pathName : "/" + pathName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[CreatePathCommand] Executing.");
        this._pathCreated = false;
        this._pathsCreated = false;

        OpenApiDocument doc = (OpenApiDocument) document;
        OpenApiPaths paths = doc.getPaths();

        // Create paths object if it doesn't exist
        if (this.isNullOrUndefined(paths)) {
            paths = doc.createPaths();
            doc.setPaths(paths);
            this._pathsCreated = true;
        }

        // Check if path already exists
        if (!this.isNullOrUndefined(paths.getItem(this._pathName))) {
            return;
        }

        // Create the new path item
        OpenApiPathItem newPathItem = paths.createPathItem();
        paths.addItem(this._pathName, newPathItem);
        this._pathCreated = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[CreatePathCommand] Reverting.");
        if (!this._pathCreated) {
            return;
        }

        OpenApiDocument doc = (OpenApiDocument) document;

        if (this._pathsCreated) {
            doc.setPaths(null);
        } else {
            OpenApiPaths paths = doc.getPaths();
            if (!this.isNullOrUndefined(paths)) {
                paths.removeItem(this._pathName);
            }
        }
    }

}
