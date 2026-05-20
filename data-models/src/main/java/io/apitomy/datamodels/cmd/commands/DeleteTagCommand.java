package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
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
 * A command used to delete a single tag from a document.
 * @author eric.wittmann@gmail.com
 */
public class DeleteTagCommand extends AbstractCommand {

    public String _tagName;

    public ObjectNode _oldTag;
    public int _tagIndex;

    public DeleteTagCommand() {
    }

    public DeleteTagCommand(String tagName) {
        this._tagName = tagName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteTagCommand] Executing.");
        this._oldTag = null;
        this._tagIndex = -1;

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

        int idx = 0;
        for (OpenApiTag tag : tags) {
            if (this._tagName.equals(tag.getName())) {
                this._oldTag = Library.writeNode(tag);
                this._tagIndex = idx;
                doc.removeTag(tag);
                return;
            }
            idx++;
        }
    }

    @SuppressWarnings("unchecked")
    private void executeForAsyncApi2(Document document) {
        List<? extends Tag> tags = (List<? extends Tag>) NodeUtil.getNodeProperty(document, "tags");
        if (this.isNullOrUndefined(tags)) {
            return;
        }

        int idx = 0;
        for (Tag tag : tags) {
            if (this._tagName.equals(tag.getName())) {
                this._oldTag = Library.writeNode(tag);
                this._tagIndex = idx;
                NodeUtil.invokeMethod(document, "removeTag", tag);
                return;
            }
            idx++;
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteTagCommand] Reverting.");
        if (this._tagIndex < 0 || this.isNullOrUndefined(this._oldTag)) {
            return;
        }

        if (ModelTypeUtil.isOpenApiModel(document)) {
            undoForOpenApi((OpenApiDocument) document);
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            undoForAsyncApi2(document);
        }
    }

    private void undoForOpenApi(OpenApiDocument doc) {
        OpenApiTag newTag = doc.createTag();
        Library.readNode(this._oldTag, newTag);
        doc.insertTag(newTag, this._tagIndex);
    }

    private void undoForAsyncApi2(Document document) {
        Tag newTag = (Tag) NodeUtil.invokeMethod(document, "createTag");
        Library.readNode(this._oldTag, newTag);
        NodeUtil.invokeMethod(document, "insertTag", newTag, this._tagIndex);
    }

}
