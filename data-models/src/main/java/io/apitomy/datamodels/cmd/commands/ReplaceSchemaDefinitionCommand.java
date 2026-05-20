package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.OpenApiComponents;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.openrpc.OpenRpcComponents;
import io.apitomy.datamodels.models.openrpc.OpenRpcSchema;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Definitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Schema;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to replace a definition schema with a newer version.
 * @author eric.wittmann@gmail.com
 */
public class ReplaceSchemaDefinitionCommand extends AbstractReplaceNodeCommand<Schema> {

    public ReplaceSchemaDefinitionCommand() {
    }

    public ReplaceSchemaDefinitionCommand(Schema old, Schema replacement) {
        super(old, replacement);
    }

    @Override
    protected void replaceNode(Node parent, Schema newNode) {
        String definitionName = this._nodePath.getLastSegment().getValue();
        if (ModelTypeUtil.isOpenApi2Model(parent)) {
            OpenApi20Definitions definitions = (OpenApi20Definitions) parent;
            definitions.addItem(definitionName, (OpenApi20Schema) newNode);
        } else if (ModelTypeUtil.isAsyncApi2Model(parent)) {
            NodeUtil.invokeMethod(parent, "addSchema", definitionName, newNode);
        } else if (ModelTypeUtil.isOpenRpcModel(parent)) {
            OpenRpcComponents components = (OpenRpcComponents) parent;
            components.addSchema(definitionName, (OpenRpcSchema) newNode);
        } else {
            OpenApiComponents components = (OpenApiComponents) parent;
            components.addSchema(definitionName, (OpenApiSchema) newNode);
        }
    }

    @Override
    protected Schema readNode(Node parent, ObjectNode node) {
        if (ModelTypeUtil.isOpenApi2Model(parent)) {
            OpenApi20Definitions definitions = (OpenApi20Definitions) parent;
            OpenApi20Schema definition = (OpenApi20Schema) definitions.createSchema();
            Library.readNode(node, definition);
            return definition;
        } else if (ModelTypeUtil.isAsyncApi2Model(parent)) {
            Schema schema = (Schema) NodeUtil.invokeMethod(parent, "createSchema");
            Library.readNode(node, schema);
            return schema;
        } else if (ModelTypeUtil.isOpenRpcModel(parent)) {
            OpenRpcComponents components = (OpenRpcComponents) parent;
            OpenRpcSchema schema = components.createSchema();
            Library.readNode(node, schema);
            return schema;
        } else {
            OpenApiComponents components = (OpenApiComponents) parent;
            Schema schema = components.createSchema();
            Library.readNode(node, schema);
            return schema;
        }
    }

}
