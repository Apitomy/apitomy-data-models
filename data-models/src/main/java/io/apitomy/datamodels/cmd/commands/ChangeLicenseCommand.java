package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to modify the license information of a document.
 * @author eric.wittmann@gmail.com
 */
public class ChangeLicenseCommand extends AbstractCommand {

    public String _newLicenseName;
    public String _newLicenseUrl;

    public ObjectNode _oldLicense;
    public boolean _nullInfo;

    public ChangeLicenseCommand() {
    }

    public ChangeLicenseCommand(String name, String url) {
        this._newLicenseName = name;
        this._newLicenseUrl = url;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[ChangeLicenseCommand] Executing.");
        if (ModelTypeUtil.isJsonSchemaModel(document)) {
            return;
        }
        this._oldLicense = null;
        this._nullInfo = false;

        Info info = (Info) NodeUtil.getProperty(document, "info");
        if (this.isNullOrUndefined(info)) {
            this._nullInfo = true;
            Info newInfo = (Info) NodeUtil.invokeMethod(document, "createInfo");
            NodeUtil.setProperty(document, "info", newInfo);
            this._oldLicense = null;
            info = newInfo;
        } else {
            this._oldLicense = null;
            if (!this.isNullOrUndefined(info.getLicense())) {
                this._oldLicense = Library.writeNode(info.getLicense());
            }
        }
        info.setLicense(info.createLicense());
        info.getLicense().setName(this._newLicenseName);
        info.getLicense().setUrl(this._newLicenseUrl);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[ChangeLicenseCommand] Reverting.");
        if (ModelTypeUtil.isJsonSchemaModel(document)) {
            return;
        }
        Info info = (Info) NodeUtil.getProperty(document, "info");
        if (this._nullInfo) {
            NodeUtil.setProperty(document, "info", null);
        } else if (NodeUtil.isDefined(this._oldLicense)) {
            info.setLicense(info.createLicense());
            Library.readNode(this._oldLicense, info.getLicense());
        } else {
            info.setLicense(null);
        }
    }

}
