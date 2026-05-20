package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to modify the version of a document.
 * @author eric.wittmann@gmail.com
 */
public class ChangeVersionCommand extends AbstractCommand {

    public String _newVersion;

    public String _oldVersion;
    public boolean _nullInfo;

    /**
     * Constructor.
     */
    public ChangeVersionCommand() {
    }

    /**
     * Constructor.
     * @param newVersion
     */
    public ChangeVersionCommand(String newVersion) {
        this._newVersion = newVersion;
    }

    @Override
    public void execute(Document document) {
        LoggerUtil.info("[ChangeVersionCommand] Executing.");
        if (ModelTypeUtil.isJsonSchemaModel(document)) {
            return;
        }
        Info info = (Info) NodeUtil.getProperty(document, "info");
        if (NodeUtil.isNullOrUndefined(info)) {
            Info newInfo = (Info) NodeUtil.invokeMethod(document, "createInfo");
            NodeUtil.setProperty(document, "info", newInfo);
            this._nullInfo = true;
            this._oldVersion = null;
            info = newInfo;
        } else {
            this._oldVersion = info.getVersion();
        }
        info.setVersion(this._newVersion);
    }

    @Override
    public void undo(Document document) {
        LoggerUtil.info("[ChangeVersionCommand] Reverting.");
        if (ModelTypeUtil.isJsonSchemaModel(document)) {
            return;
        }
        if (this._nullInfo) {
            NodeUtil.setProperty(document, "info", null);
        } else {
            Info info = (Info) NodeUtil.getProperty(document, "info");
            info.setVersion(this._oldVersion);
        }
    }

}
