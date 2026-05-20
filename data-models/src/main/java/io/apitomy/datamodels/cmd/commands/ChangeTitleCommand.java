package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to modify the title of a document.
 * @author eric.wittmann@gmail.com
 */
public class ChangeTitleCommand extends AbstractCommand {

    public String _newTitle;

    public String _oldTitle;
    public boolean _nullInfo;

    /**
     * Constructor.
     */
    public ChangeTitleCommand() {
    }

    /**
     * Constructor.
     * @param newTitle
     */
    public ChangeTitleCommand(String newTitle) {
        this._newTitle = newTitle;
    }

    @Override
    public void execute(Document document) {
        LoggerUtil.info("[ChangeTitleCommand] Executing.");
        if (ModelTypeUtil.isJsonSchemaModel(document)) {
            return;
        }
        Info info = (Info) NodeUtil.getProperty(document, "info");
        if (NodeUtil.isNullOrUndefined(info)) {
            Info newInfo = (Info) NodeUtil.invokeMethod(document, "createInfo");
            NodeUtil.setProperty(document, "info", newInfo);
            this._nullInfo = true;
            this._oldTitle = null;
            info = newInfo;
        } else {
            this._oldTitle = info.getTitle();
        }
        info.setTitle(this._newTitle);
    }

    @Override
    public void undo(Document document) {
        LoggerUtil.info("[ChangeTitleCommand] Reverting.");
        if (ModelTypeUtil.isJsonSchemaModel(document)) {
            return;
        }
        if (this._nullInfo) {
            NodeUtil.setProperty(document, "info", null);
        } else {
            Info info = (Info) NodeUtil.getProperty(document, "info");
            info.setTitle(this._oldTitle);
        }
    }

}
