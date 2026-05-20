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
 * A command used to add a new tag to a document.
 * @author eric.wittmann@gmail.com
 */
public class AddTagCommand extends AbstractCommand {

    public String _tagName;
    public String _tagDescription;

    public boolean _tagCreated;

    public AddTagCommand() {
    }

    public AddTagCommand(String tagName, String tagDescription) {
        this._tagName = tagName;
        this._tagDescription = tagDescription;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddTagCommand] Executing.");
        this._tagCreated = false;

        if (ModelTypeUtil.isOpenApiModel(document)) {
            executeForOpenApi((OpenApiDocument) document);
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            executeForAsyncApi2(document);
        }
    }

    private void executeForOpenApi(OpenApiDocument doc) {
        // Check if tag already exists
        List<OpenApiTag> existingTags = (List<OpenApiTag>) doc.getTags();
        if (!this.isNullOrUndefined(existingTags)) {
            for (OpenApiTag tag : existingTags) {
                if (this._tagName.equals(tag.getName())) {
                    return;
                }
            }
        }

        // Create new tag
        OpenApiTag newTag = doc.createTag();
        newTag.setName(this._tagName);
        if (!this.isNullOrUndefined(this._tagDescription) && !this._tagDescription.isEmpty()) {
            newTag.setDescription(this._tagDescription);
        }

        doc.addTag(newTag);
        this._tagCreated = true;
    }

    @SuppressWarnings("unchecked")
    private void executeForAsyncApi2(Document document) {
        // Check if tag already exists
        List<? extends Tag> existingTags = (List<? extends Tag>) NodeUtil.getNodeProperty(document, "tags");
        if (!this.isNullOrUndefined(existingTags)) {
            for (Tag tag : existingTags) {
                if (this._tagName.equals(tag.getName())) {
                    return;
                }
            }
        }

        // Create new tag
        Tag newTag = (Tag) NodeUtil.invokeMethod(document, "createTag");
        newTag.setName(this._tagName);
        if (!this.isNullOrUndefined(this._tagDescription) && !this._tagDescription.isEmpty()) {
            newTag.setDescription(this._tagDescription);
        }

        NodeUtil.invokeMethod(document, "addTag", newTag);
        this._tagCreated = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddTagCommand] Reverting.");
        if (!this._tagCreated) {
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
            if (this._tagName.equals(tag.getName())) {
                doc.removeTag(tag);
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
            if (this._tagName.equals(tag.getName())) {
                NodeUtil.invokeMethod(document, "removeTag", tag);
                return;
            }
        }
    }

}
