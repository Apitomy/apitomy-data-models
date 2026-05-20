package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiParametersParent;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.List;

/**
 * A command used to delete a parameter from a path item or operation.
 * @author eric.wittmann@gmail.com
 */
public class DeleteParameterCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _parameterName;
    public String _parameterLocation;

    public ObjectNode _oldParameter;
    public int _oldIndex;

    public DeleteParameterCommand() {
    }

    public DeleteParameterCommand(OpenApiParametersParent parent, String parameterName,
                                  String parameterLocation) {
        this._parentPath = NodePathUtil.createNodePath((Node) parent);
        this._parameterName = parameterName;
        this._parameterLocation = parameterLocation;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteParameterCommand] Executing.");
        this._oldParameter = null;

        OpenApiParametersParent parent = (OpenApiParametersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        List<? extends OpenApiParameter> parameters = (List<? extends OpenApiParameter>) parent.getParameters();
        if (this.isNullOrUndefined(parameters)) {
            return;
        }

        for (int idx = 0; idx < parameters.size(); idx++) {
            OpenApiParameter param = parameters.get(idx);
            if (this._parameterName.equals(param.getName()) &&
                    this._parameterLocation.equals(param.getIn())) {
                this._oldParameter = Library.writeNode(param);
                this._oldIndex = idx;
                parent.removeParameter(param);
                return;
            }
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteParameterCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldParameter)) {
            return;
        }

        OpenApiParametersParent parent = (OpenApiParametersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        OpenApiParameter newParameter = parent.createParameter();
        Library.readNode(this._oldParameter, newParameter);
        parent.insertParameter(newParameter, this._oldIndex);
    }

}
