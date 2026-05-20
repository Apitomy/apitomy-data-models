package io.apitomy.datamodels.cmd;

import io.apitomy.datamodels.models.Document;

/**
 * All editor commands must implement this interface.
 * @author eric.wittmann@gmail.com
 */
public interface ICommand {

    /**
     * Called to execute the command against the given document.
     * @param document
     */
    public void execute(Document document);

    /**
     * Called to undo the command (restore the document to a previous state).
     * @param document
     */
    public void undo(Document document);
    
    /**
     * Returns the type of the command (i.e. the command's class name).
     */
    public String type();

}
