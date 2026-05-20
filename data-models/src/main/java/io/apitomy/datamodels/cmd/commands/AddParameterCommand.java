package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiParametersParent;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Schema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.models.union.StringUnionValueImpl;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

import java.util.List;

/**
 * A command used to add a new parameter to a path item or operation.
 * @author eric.wittmann@gmail.com
 */
public class AddParameterCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _parameterName;
    public String _parameterLocation;
    public String _parameterDescription;
    public boolean _parameterRequired;
    public String _parameterType;

    public boolean _parameterCreated;

    public AddParameterCommand() {
    }

    public AddParameterCommand(OpenApiParametersParent parent, String parameterName,
                               String parameterLocation, String parameterDescription,
                               boolean parameterRequired, String parameterType) {
        this._parentPath = NodePathUtil.createNodePath((Node) parent);
        this._parameterName = parameterName;
        this._parameterLocation = parameterLocation;
        this._parameterDescription = parameterDescription;
        this._parameterRequired = parameterRequired;
        this._parameterType = parameterType;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddParameterCommand] Executing.");
        this._parameterCreated = false;

        OpenApiParametersParent parent = (OpenApiParametersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        // Check if parameter already exists
        List<? extends OpenApiParameter> existingParams = (List<? extends OpenApiParameter>) parent.getParameters();
        if (!this.isNullOrUndefined(existingParams)) {
            for (OpenApiParameter param : existingParams) {
                if (this._parameterName.equals(param.getName()) &&
                        this._parameterLocation.equals(param.getIn())) {
                    return;
                }
            }
        }

        // Create new parameter
        OpenApiParameter newParameter = parent.createParameter();
        newParameter.setName(this._parameterName);
        newParameter.setIn(this._parameterLocation);

        if (!this.isNullOrUndefined(this._parameterDescription)) {
            newParameter.setDescription(this._parameterDescription);
        }

        newParameter.setRequired(this._parameterRequired);

        // Create and set schema with type
        if (!this.isNullOrUndefined(this._parameterType)) {
            OpenApiSchema schema = newParameter.createSchema();
            if (ModelTypeUtil.isOpenApi2Model(document)) {
                ((OpenApi20Schema) schema).setType(this._parameterType);
            } else if (ModelTypeUtil.isOpenApi30Model(document)) {
                ((OpenApi30Schema) schema).setType(this._parameterType);
            } else if (ModelTypeUtil.isOpenApi31Model(document)) {
                ((OpenApi31Schema) schema).setType(new StringUnionValueImpl(this._parameterType));
            }
            newParameter.setSchema(schema);
        }

        parent.addParameter(newParameter);
        this._parameterCreated = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddParameterCommand] Reverting.");
        if (!this._parameterCreated) {
            return;
        }

        OpenApiParametersParent parent = (OpenApiParametersParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        List<? extends OpenApiParameter> parameters = (List<? extends OpenApiParameter>) parent.getParameters();
        if (this.isNullOrUndefined(parameters)) {
            return;
        }

        for (OpenApiParameter param : parameters) {
            if (this._parameterName.equals(param.getName()) &&
                    this._parameterLocation.equals(param.getIn())) {
                parent.removeParameter(param);
                return;
            }
        }
    }

}
