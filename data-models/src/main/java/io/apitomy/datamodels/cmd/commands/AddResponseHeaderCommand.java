package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.OpenApiHeadersParent;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xHeader;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xSchema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.models.union.StringUnionValueImpl;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

import java.util.Map;

/**
 * A command used to add a header to a response.
 * @author eric.wittmann@gmail.com
 */
public class AddResponseHeaderCommand extends AbstractCommand {

    public NodePath _responsePath;
    public String _headerName;
    public String _description;
    public String _schemaType;
    public String _schemaRef;

    public boolean _created;

    public AddResponseHeaderCommand() {
    }

    public AddResponseHeaderCommand(OpenApiHeadersParent response, String headerName,
                                    String description, String schemaType, String schemaRef) {
        this._responsePath = NodePathUtil.createNodePath((Node) response);
        this._headerName = headerName;
        this._description = description;
        this._schemaType = schemaType;
        this._schemaRef = schemaRef;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddResponseHeaderCommand] Executing.");
        this._created = false;

        OpenApiHeadersParent response = (OpenApiHeadersParent) NodePathUtil.resolveNodePath(this._responsePath, document);
        if (this.isNullOrUndefined(response)) {
            return;
        }

        // Check if header already exists
        Map<String, ? extends OpenApiHeader> headers = (Map<String, ? extends OpenApiHeader>) response.getHeaders();
        if (!this.isNullOrUndefined(headers) && headers.containsKey(this._headerName)) {
            return;
        }

        // Create the header
        OpenApiHeader header = response.createHeader();
        header.setDescription(this._description);

        // Set schema if provided (version-specific: createSchema/setSchema not on base OpenApiHeader)
        if (!this.isNullOrUndefined(this._schemaType) || !this.isNullOrUndefined(this._schemaRef)) {
            OpenApi3xHeader header3x = (OpenApi3xHeader) header;
            OpenApi3xSchema schema = header3x.createSchema();
            if (!this.isNullOrUndefined(this._schemaRef)) {
                ((Referenceable) schema).set$ref(this._schemaRef);
            } else if (!this.isNullOrUndefined(this._schemaType)) {
                if (ModelTypeUtil.isOpenApi30Model(schema)) {
                    ((OpenApi30Schema) schema).setType(this._schemaType);
                } else {
                    ((OpenApi31Schema) schema).setType(new StringUnionValueImpl(this._schemaType));
                }
            }
            header3x.setSchema(schema);
        }

        response.addHeader(this._headerName, header);
        this._created = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddResponseHeaderCommand] Reverting.");
        if (!this._created) {
            return;
        }

        OpenApiHeadersParent response = (OpenApiHeadersParent) NodePathUtil.resolveNodePath(this._responsePath, document);
        if (this.isNullOrUndefined(response)) {
            return;
        }

        response.removeHeader(this._headerName);
    }

}
