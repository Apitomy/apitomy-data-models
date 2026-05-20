package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiExample;
import io.apitomy.datamodels.models.openapi.OpenApiExamplesParent;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A command used to delete a single mediaType from an operation.
 * @author eric.wittmann@gmail.com
 */
public class DeleteAllExamplesCommand extends AbstractCommand {

    public NodePath _parentPath;

    public Map<String, ObjectNode> _oldExamples;

    public DeleteAllExamplesCommand() {
    }

    public DeleteAllExamplesCommand(OpenApiExamplesParent parent) {
        this._parentPath = NodePathUtil.createNodePath((Node) parent);
    }

    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteAllExamplesCommand] Executing.");

        if (this.isNullOrUndefined(document)) {
            LoggerUtil.debug("[DeleteAllExamplesCommand] Could not execute the command, invalid argument.");
            return;
        }

        if (this.isNullOrUndefined(this._parentPath)) {
            LoggerUtil.debug("[DeleteAllExamplesCommand] Could not execute the command, problem when unmarshalling.");
            return;
        }

        OpenApiExamplesParent parent = (OpenApiExamplesParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            LoggerUtil.debug("[DeleteAllExamplesCommand] Parent node not found.");
            return;
        }

        // Save the examples to enable undo
        this._oldExamples = new LinkedHashMap<>();
        parent.getExamples().keySet().forEach(k -> {
            OpenApiExample n = parent.getExamples().get(k);
            this._oldExamples.put(k, Library.writeNode(n));
        });

        parent.clearExamples();
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        if (this.isNullOrUndefined(document)) {
            LoggerUtil.debug("[DeleteAllExamplesCommand] Could not revert the command, invalid argument.");
            return;
        }

        if (this.isNullOrUndefined(this._parentPath)) {
            LoggerUtil.debug("[DeleteAllExamplesCommand] Could not revert the command, problem when unmarshalling.");
            return;
        }

        LoggerUtil.info("[DeleteAllExamplesCommand] Reverting.");

        OpenApiExamplesParent parent = (OpenApiExamplesParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            LoggerUtil.info("[DeleteAllExamplesCommand] No parent node found.");
            return;
        }

        if (this.isNullOrUndefined(this._oldExamples)) {
            LoggerUtil.info("[DeleteAllExamplesCommand] Could not revert. Previous data is not available.");
            return;
        }

        for (String k : this._oldExamples.keySet()) {
            OpenApiExample example = parent.createExample();
            Library.readNode(this._oldExamples.get(k), (Node) example);
            parent.addExample(k, example);
        }
    }

}
