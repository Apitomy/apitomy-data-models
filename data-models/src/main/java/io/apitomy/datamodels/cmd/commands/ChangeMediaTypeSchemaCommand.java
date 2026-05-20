package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openapi.OpenApiMediaType;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.models.union.StringUnionValueImpl;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to set or change the schema on a media type entry.
 * @author eric.wittmann@gmail.com
 */
public class ChangeMediaTypeSchemaCommand extends AbstractCommand {

    public NodePath _mediaTypePath;
    public String _schemaRef;
    public String _schemaType;

    public ObjectNode _oldSchema;
    public boolean _hadSchema;

    public ChangeMediaTypeSchemaCommand() {
    }

    public ChangeMediaTypeSchemaCommand(OpenApiMediaType mediaType, String schemaRef, String schemaType) {
        this._mediaTypePath = NodePathUtil.createNodePath(mediaType);
        this._schemaRef = schemaRef;
        this._schemaType = schemaType;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[ChangeMediaTypeSchemaCommand] Executing.");
        this._oldSchema = null;
        this._hadSchema = false;

        OpenApiMediaType mediaType = (OpenApiMediaType) NodePathUtil.resolveNodePath(this._mediaTypePath, document);
        if (this.isNullOrUndefined(mediaType)) {
            return;
        }

        // Save old schema for undo
        OpenApiSchema oldSchema = mediaType.getSchema();
        if (!this.isNullOrUndefined(oldSchema)) {
            this._oldSchema = Library.writeNode(oldSchema);
            this._hadSchema = true;
        }

        // Create new schema and set properties (version-specific: set$ref/setType not on base OpenApiSchema)
        if (!this.isNullOrUndefined(this._schemaRef) || !this.isNullOrUndefined(this._schemaType)) {
            OpenApiSchema newSchema = mediaType.createSchema();
            if (ModelTypeUtil.isOpenApi30Model(document)) {
                OpenApi30Schema schema30 = (OpenApi30Schema) newSchema;
                if (!this.isNullOrUndefined(this._schemaRef)) {
                    schema30.set$ref(this._schemaRef);
                } else if (!this.isNullOrUndefined(this._schemaType)) {
                    schema30.setType(this._schemaType);
                }
            } else if (ModelTypeUtil.isOpenApi31Model(document)) {
                OpenApi31Schema schema31 = (OpenApi31Schema) newSchema;
                if (!this.isNullOrUndefined(this._schemaRef)) {
                    schema31.set$ref(this._schemaRef);
                } else if (!this.isNullOrUndefined(this._schemaType)) {
                    schema31.setType(new StringUnionValueImpl(this._schemaType));
                }
            }
            mediaType.setSchema(newSchema);
        } else {
            // Clear schema
            mediaType.setSchema(null);
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[ChangeMediaTypeSchemaCommand] Reverting.");

        OpenApiMediaType mediaType = (OpenApiMediaType) NodePathUtil.resolveNodePath(this._mediaTypePath, document);
        if (this.isNullOrUndefined(mediaType)) {
            return;
        }

        if (this._hadSchema && !this.isNullOrUndefined(this._oldSchema)) {
            OpenApiSchema restoredSchema = mediaType.createSchema();
            Library.readNode(this._oldSchema, restoredSchema);
            mediaType.setSchema(restoredSchema);
        } else {
            mediaType.setSchema(null);
        }
    }

}
