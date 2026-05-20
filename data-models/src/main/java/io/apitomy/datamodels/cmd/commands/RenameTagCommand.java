package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiTag;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.List;

/**
 * A command used to rename a tag.
 * @author eric.wittmann@gmail.com
 */
public class RenameTagCommand extends AbstractCommand {

    public String _oldName;
    public String _newName;

    public boolean _tagRenamed;

    public RenameTagCommand() {
    }

    public RenameTagCommand(String oldName, String newName) {
        this._oldName = oldName;
        this._newName = newName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[RenameTagCommand] Executing.");
        this._tagRenamed = false;

        if (ModelTypeUtil.isOpenApiModel(document)) {
            executeForOpenApi((OpenApiDocument) document);
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            executeForAsyncApi2(document);
        }
    }

    private void executeForOpenApi(OpenApiDocument doc) {
        List<OpenApiTag> tags = (List<OpenApiTag>) doc.getTags();
        if (this.isNullOrUndefined(tags)) {
            return;
        }

        // Check if new name already exists
        for (OpenApiTag tag : tags) {
            if (this._newName.equals(tag.getName())) {
                return;
            }
        }

        // Find and rename the tag
        for (OpenApiTag tag : tags) {
            if (this._oldName.equals(tag.getName())) {
                tag.setName(this._newName);
                this._tagRenamed = true;
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void executeForAsyncApi2(Document document) {
        List<? extends Tag> tags = (List<? extends Tag>) NodeUtil.getNodeProperty(document, "tags");
        if (this.isNullOrUndefined(tags)) {
            return;
        }

        // Check if new name already exists
        for (Tag tag : tags) {
            if (this._newName.equals(tag.getName())) {
                return;
            }
        }

        // Find and rename the tag
        for (Tag tag : tags) {
            if (this._oldName.equals(tag.getName())) {
                tag.setName(this._newName);
                this._tagRenamed = true;
                return;
            }
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[RenameTagCommand] Reverting.");
        if (!this._tagRenamed) {
            return;
        }

        if (ModelTypeUtil.isOpenApiModel(document)) {
            undoForOpenApi((OpenApiDocument) document);
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            undoForAsyncApi2(document);
        }
    }

    private void undoForOpenApi(OpenApiDocument doc) {
        List<OpenApiTag> tags = (List<OpenApiTag>) doc.getTags();
        if (this.isNullOrUndefined(tags)) {
            return;
        }

        for (OpenApiTag tag : tags) {
            if (this._newName.equals(tag.getName())) {
                tag.setName(this._oldName);
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void undoForAsyncApi2(Document document) {
        List<? extends Tag> tags = (List<? extends Tag>) NodeUtil.getNodeProperty(document, "tags");
        if (this.isNullOrUndefined(tags)) {
            return;
        }

        for (Tag tag : tags) {
            if (this._newName.equals(tag.getName())) {
                tag.setName(this._oldName);
                return;
            }
        }
    }

}
