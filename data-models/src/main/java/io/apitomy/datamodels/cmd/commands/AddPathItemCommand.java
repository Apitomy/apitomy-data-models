package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to add a new pathItem in a document.  Source for the new
 * pathItem must be provided.  This source will be converted to an OAS
 * pathItem object and then added to the data model.
 * @author eric.wittmann@gmail.com
 */
public class AddPathItemCommand extends AbstractCommand {

    public boolean _pathItemExists;
    public String _newPathItemName;
    public ObjectNode _newPathItemObj;
    public boolean _nullPathItems;
    
    public AddPathItemCommand() {
    }

    public AddPathItemCommand(String pathItemName) {
        this._newPathItemName = pathItemName;
    }

    public AddPathItemCommand(String pathItemName, ObjectNode from) {
        this._newPathItemName = pathItemName;
        this._newPathItemObj = from;
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddPathItemCommand] Executing.");
        OpenApiDocument doc = (OpenApiDocument) document;
        if (this.isNullOrUndefined(doc.getPaths())) {
            doc.setPaths(doc.createPaths());
            this._nullPathItems = true;
        }

        if (!this.isNullOrUndefined(doc.getPaths().getItem(this._newPathItemName))) {
            LoggerUtil.info("[AddPathItemCommand] PathItem with name %s already exists.", this._newPathItemName);
            this._pathItemExists = true;
        } else {
            OpenApiPathItem pathItem = doc.getPaths().createPathItem();
            Library.readNode(this._newPathItemObj, pathItem);
            doc.getPaths().addItem(this._newPathItemName, pathItem);
            this._pathItemExists = false;
        }
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddPathItemCommand] Reverting.");
        if (this._pathItemExists) {
            return;
        }
        OpenApiDocument doc = (OpenApiDocument) document;
        if (this._nullPathItems) {
            doc.setPaths(null);
        } else {
            doc.getPaths().removeItem(this._newPathItemName);
        }
    }


}
