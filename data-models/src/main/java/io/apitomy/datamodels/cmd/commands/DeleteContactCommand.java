package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Contact;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Info;

/**
 * A command used to delete the contact.
 * @author eric.wittmann@gmail.com
 */
public class DeleteContactCommand extends DeleteNodeCommand<Contact> {

    public DeleteContactCommand() {
    }

    public DeleteContactCommand(Info info) {
        super("contact", info);
    }

    /**
     * @see DeleteNodeCommand#readNode(Document, ObjectNode)
     */
    @Override
    protected Contact readNode(Document doc, ObjectNode node) {
        Info info = (Info) io.apitomy.datamodels.util.NodeUtil.getProperty(doc, "info");
        Contact contact = info.createContact();
        Library.readNode(node, contact);
        return contact;
    }

}
