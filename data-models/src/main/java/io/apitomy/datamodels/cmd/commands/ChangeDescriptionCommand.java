package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to modify the description of a document.
 * @author eric.wittmann@gmail.com
 */
public class ChangeDescriptionCommand extends AbstractCommand {

    public String _newDescription;

    public String _oldDescription;
    public boolean _nullInfo;

    /**
     * Constructor.
     */
    public ChangeDescriptionCommand() {
    }

    /**
     * Constructor.
     * @param newDescription
     */
    public ChangeDescriptionCommand(String newDescription) {
        this._newDescription = newDescription;
    }

    @Override
    public void execute(Document document) {
        LoggerUtil.info("[ChangeDescriptionCommand] Executing.");
        if (ModelTypeUtil.isJsonSchemaModel(document)) {
            return;
        }
        Info info = (Info) NodeUtil.getProperty(document, "info");
        if (NodeUtil.isNullOrUndefined(info)) {
            Info newInfo = (Info) NodeUtil.invokeMethod(document, "createInfo");
            NodeUtil.setProperty(document, "info", newInfo);
            this._nullInfo = true;
            this._oldDescription = null;
            info = newInfo;
        } else {
            this._oldDescription = info.getDescription();
        }
        info.setDescription(this._newDescription);
    }

    @Override
    public void undo(Document document) {
        LoggerUtil.info("[ChangeDescriptionCommand] Reverting.");
        if (ModelTypeUtil.isJsonSchemaModel(document)) {
            return;
        }
        if (this._nullInfo) {
            NodeUtil.setProperty(document, "info", null);
        } else {
            Info info = (Info) NodeUtil.getProperty(document, "info");
            info.setDescription(this._oldDescription);
        }
    }

}
