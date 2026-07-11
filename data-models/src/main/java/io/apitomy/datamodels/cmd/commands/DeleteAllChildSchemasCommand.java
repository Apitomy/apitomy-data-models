package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.asyncapi.AsyncApiSchema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A command used to delete all child schemas from a schema.
 * @author eric.wittmann@gmail.com
 */
public class DeleteAllChildSchemasCommand extends AbstractSchemaInhCommand {

    public NodePath _schemaPath;
    public String _childSchemaType;

    public List<ObjectNode> _oldSchemas;

    public DeleteAllChildSchemasCommand() {
    }

    public DeleteAllChildSchemasCommand(Schema parent, String type) {
        this._schemaPath = NodePathUtil.createNodePath((Node) parent);
        this._childSchemaType = type;
    }
    
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteAllChildSchemasCommand] Executing.");
        this._oldSchemas = new ArrayList<>();

        Schema schema = (Schema) NodePathUtil.resolveNodePath(this._schemaPath, document);
        if (this.isNullOrUndefined(schema)) {
            return;
        }

        // Pull the schemas out (if allOf, anyOf, or oneOf)
        List<? extends Schema> schemas = new ArrayList<>();
        if (NodeUtil.equals(TYPE_ALL_OF, this._childSchemaType)) {
            schemas = new LinkedList<>(schema.getAllOf());
            for (int i = 0; i < schemas.size(); i++) {
                Schema s = schemas.get(i);
                if (has$Ref(s)) {
                    schema.removeAllOf(s);
                    addOldSchema(s);
                }
            }
            if (schema.getAllOf().isEmpty()) {
                NodeUtil.setProperty(schema, TYPE_ALL_OF, null);
            }
        }
        if (NodeUtil.equals(TYPE_ANY_OF, this._childSchemaType)) {
            schemas = new LinkedList<>(getAnyOfSchemas(schema));
            for (int i = 0; i < schemas.size(); i++) {
                Schema s = schemas.get(i);
                if (has$Ref(s)) {
                    removeAnyOfSchema(schema, s);
                    addOldSchema(s);
                }
            }
            if (getAnyOfSchemas(schema).isEmpty()) {
                NodeUtil.setProperty(schema, TYPE_ANY_OF, null);
            }
        }
        if (NodeUtil.equals(TYPE_ONE_OF, this._childSchemaType)) {
            schemas = new LinkedList<>(getOneOfSchemas(schema));
            for (int i = 0; i < schemas.size(); i++) {
                Schema s = schemas.get(i);
                if (has$Ref(s)) {
                    removeOneOfSchema(schema, s);
                    addOldSchema(s);
                }
            }
            if (getOneOfSchemas(schema).isEmpty()) {
                NodeUtil.setProperty(schema, TYPE_ONE_OF, null);
            }
        }
    }

    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteAllChildSchemasCommand] Reverting.");

        if (this.isNullOrUndefined(this._oldSchemas) || this._oldSchemas.size() == 0) {
            return;
        }

        Schema schema = (Schema) NodePathUtil.resolveNodePath (this._schemaPath, document);
        if (this.isNullOrUndefined(schema)) {
            return;
        }

        // TODO: remember the index of each schema so we can restore them to the right positions.
        this.copySchemaJsTo(_oldSchemas, schema, _childSchemaType);
    }

    private void addOldSchema(Schema schema) {
        if (this.isNullOrUndefined(schema)) {
            return;
        }
        this._oldSchemas.add(Library.writeNode(schema));
    }

    private List<? extends Schema> getAnyOfSchemas(Schema schema) {
        if (ModelTypeUtil.isOpenApi30Model(schema)) {
            OpenApi30Schema schema30 = (OpenApi30Schema) schema;
            return schema30.getAnyOf();
        } else if (ModelTypeUtil.isOpenApi31Model(schema)) {
            OpenApi31Schema schema31 = (OpenApi31Schema) schema;
            return schema31.getAnyOf();
        } else if (ModelTypeUtil.isAsyncApiModel(schema)) {
            AsyncApiSchema s = (AsyncApiSchema) schema;
            return s.getAnyOf();
        }
        return null;
    }

    private List<? extends Schema> getOneOfSchemas(Schema schema) {
        if (ModelTypeUtil.isOpenApi30Model(schema)) {
            OpenApi30Schema schema30 = (OpenApi30Schema) schema;
            return schema30.getOneOf();
        } else if (ModelTypeUtil.isOpenApi31Model(schema)) {
            OpenApi31Schema schema31 = (OpenApi31Schema) schema;
            return schema31.getOneOf();
        } else if (ModelTypeUtil.isAsyncApiModel(schema)) {
            AsyncApiSchema s = (AsyncApiSchema) schema;
            return s.getOneOf();
        }
        return null;
    }

    private static void removeAnyOfSchema(Schema parent, Schema child) {
        if (ModelTypeUtil.isOpenApi30Model(parent)) {
            OpenApi30Schema schema30 = (OpenApi30Schema) parent;
            schema30.removeAnyOf((OpenApi30Schema) child);
        } else if (ModelTypeUtil.isOpenApi31Model(parent)) {
            OpenApi31Schema schema31 = (OpenApi31Schema) parent;
            schema31.removeAnyOf((OpenApi31Schema) child);
        } else if (ModelTypeUtil.isAsyncApiModel(parent)) {
            AsyncApiSchema schema = (AsyncApiSchema) parent;
            schema.removeAnyOf((AsyncApiSchema) child);
        }
    }

    private static void removeOneOfSchema(Schema parent, Schema child) {
        if (ModelTypeUtil.isOpenApi30Model(parent)) {
            OpenApi30Schema schema30 = (OpenApi30Schema) parent;
            schema30.removeOneOf((OpenApi30Schema) child);
        } else if (ModelTypeUtil.isOpenApi31Model(parent)) {
            OpenApi31Schema schema31 = (OpenApi31Schema) parent;
            schema31.removeOneOf((OpenApi31Schema) child);
        } else if (ModelTypeUtil.isAsyncApiModel(parent)) {
            AsyncApiSchema schema = (AsyncApiSchema) parent;
            schema.removeOneOf((AsyncApiSchema) child);
        }
    }

}
