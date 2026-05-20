package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.models.License;

/**
 * A command used to delete the license.
 *
 * @author eric.wittmann@gmail.com
 */
public class DeleteLicenseCommand extends DeleteNodeCommand<License> {

    public DeleteLicenseCommand() {
    }

    public DeleteLicenseCommand(Info info) {
        super("license", info);
    }

    /**
     * @see io.apitomy.datamodels.cmd.commands.DeleteNodeCommand#readNode(Document, ObjectNode)
     */
    @Override
    protected License readNode(Document doc, ObjectNode node) {
        Info info = (Info) io.apitomy.datamodels.util.NodeUtil.getProperty(doc, "info");
        License license = info.createLicense();
        Library.readNode(node, license);
        return license;
    }

}
