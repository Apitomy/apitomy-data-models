package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiExample;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.models.openrpc.OpenRpcComponents;
import io.apitomy.datamodels.models.openrpc.OpenRpcDocument;
import io.apitomy.datamodels.models.openrpc.OpenRpcExample;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A command used to delete a single reusable example definition from a document.
 * @author eric.wittmann@gmail.com
 */
public class DeleteExampleDefinitionCommand extends AbstractCommand {

    public String _definitionName;

    public ObjectNode _oldDefinition;
    public int _oldIndex;

    public DeleteExampleDefinitionCommand() {
    }

    public DeleteExampleDefinitionCommand(String definitionName) {
        this._definitionName = definitionName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteExampleDefinitionCommand] Executing.");
        this._oldDefinition = null;

        if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getExamples())) {
                return;
            }
            OpenApiExample example = components.getExamples().get(this._definitionName);
            if (!this.isNullOrUndefined(example)) {
                this._oldDefinition = Library.writeNode((Node) example);
                List<String> exampleNames = new ArrayList<>(components.getExamples().keySet());
                this._oldIndex = exampleNames.indexOf(this._definitionName);
                components.removeExample(this._definitionName);
            }
        } else if (ModelTypeUtil.isOpenRpcModel(document)) {
            OpenRpcComponents components = ((OpenRpcDocument) document).getComponents();
            if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getExamples())) {
                return;
            }
            OpenRpcExample example = components.getExamples().get(this._definitionName);
            if (!this.isNullOrUndefined(example)) {
                this._oldDefinition = Library.writeNode((Node) example);
                List<String> exampleNames = new ArrayList<>(components.getExamples().keySet());
                this._oldIndex = exampleNames.indexOf(this._definitionName);
                components.removeExample(this._definitionName);
            }
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteExampleDefinitionCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldDefinition)) {
            return;
        }

        if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            if (this.isNullOrUndefined(components)) {
                components = ((OpenApi3xDocument) document).createComponents();
                ((OpenApi3xDocument) document).setComponents(components);
            }
            OpenApiExample example = components.createExample();
            Library.readNode(this._oldDefinition, example);
            components.insertExample(this._definitionName, example, this._oldIndex);
        } else if (ModelTypeUtil.isOpenRpcModel(document)) {
            OpenRpcDocument doc = (OpenRpcDocument) document;
            OpenRpcComponents components = doc.getComponents();
            if (this.isNullOrUndefined(components)) {
                components = doc.createComponents();
                doc.setComponents(components);
            }
            OpenRpcExample example = components.createExample();
            Library.readNode(this._oldDefinition, example);
            components.insertExample(this._definitionName, example, this._oldIndex);
        }
    }

}
