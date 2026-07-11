package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

import java.util.ArrayList;

/**
 * A command used to delete a single property from a schema definition in a document.
 * @author eric.wittmann@gmail.com
 */
public class DeleteSchemaPropertyCommand extends AbstractCommand {

    public String _schemaDefinitionName;
    public String _propertyName;
    public ObjectNode _oldProperty;
    public boolean _oldRequired;

    private transient DeleteSchemaPropertyCommandHelper _helper;

    public DeleteSchemaPropertyCommand() {
    }

    public DeleteSchemaPropertyCommand(String schemaDefinitionName, String propertyName) {
        this._schemaDefinitionName = schemaDefinitionName;
        this._propertyName = propertyName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteSchemaPropertyCommand] Executing.");
        this._helper = createHelper(document);

        Schema schema = this._helper.resolveSchema(document);
        if (this.isNullOrUndefined(schema)) {
            LoggerUtil.info("[DeleteSchemaPropertyCommand] Schema definition '%s' not found.", this._schemaDefinitionName);
            return;
        }

        // Do nothing if the property does not exist.
        if (this.isNullOrUndefined(schema.getProperties()) || !schema.getProperties().containsKey(this._propertyName)) {
            LoggerUtil.info("[DeleteSchemaPropertyCommand] Property '%s' not found on schema '%s'.", this._propertyName, this._schemaDefinitionName);
            return;
        }

        // Serialize the property for undo.
        Schema propertySchema = schema.getProperties().get(this._propertyName);
        this._oldProperty = Library.writeNode(propertySchema);

        // Check if the property is in the required array.
        this._oldRequired = schema.getRequired() != null && schema.getRequired().contains(this._propertyName);

        // Remove the property.
        schema.removeProperty(this._propertyName);

        // Remove from required if present.
        if (this._oldRequired) {
            schema.getRequired().remove(this._propertyName);
            if (schema.getRequired().isEmpty()) {
                schema.setRequired(null);
            }
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteSchemaPropertyCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldProperty)) {
            return;
        }

        this._helper = createHelper(document);
        Schema schema = this._helper.resolveSchema(document);
        if (this.isNullOrUndefined(schema)) {
            return;
        }

        // Re-add the property.
        Schema restoredSchema = schema.createSchema();
        Library.readNode(this._oldProperty, restoredSchema);
        schema.addProperty(this._propertyName, restoredSchema);

        // Restore required if it was there.
        if (this._oldRequired) {
            if (schema.getRequired() == null) {
                schema.setRequired(new ArrayList<>());
            }
            if (!schema.getRequired().contains(this._propertyName)) {
                schema.getRequired().add(this._propertyName);
            }
        }
    }

    private DeleteSchemaPropertyCommandHelper createHelper(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            return new OpenApi20Helper();
        }
        if (ModelTypeUtil.isOpenApi30Model(document)) {
            return new OpenApi30Helper();
        }
        if (ModelTypeUtil.isOpenApi31Model(document)) {
            return new OpenApi31Helper();
        }
        throw new RuntimeException("Unsupported model type: " + document.root().modelType());
    }

    private interface DeleteSchemaPropertyCommandHelper {
        Schema resolveSchema(Document document);
    }

    private class OpenApi20Helper implements DeleteSchemaPropertyCommandHelper {
        @Override
        public Schema resolveSchema(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (isNullOrUndefined(doc20.getDefinitions())) {
                return null;
            }
            return doc20.getDefinitions().getItem(_schemaDefinitionName);
        }
    }

    private class OpenApi30Helper implements DeleteSchemaPropertyCommandHelper {
        @Override
        public Schema resolveSchema(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents()) || isNullOrUndefined(doc30.getComponents().getSchemas())) {
                return null;
            }
            return doc30.getComponents().getSchemas().get(_schemaDefinitionName);
        }
    }

    private class OpenApi31Helper implements DeleteSchemaPropertyCommandHelper {
        @Override
        public Schema resolveSchema(Document document) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (isNullOrUndefined(doc31.getComponents()) || isNullOrUndefined(doc31.getComponents().getSchemas())) {
                return null;
            }
            return doc31.getComponents().getSchemas().get(_schemaDefinitionName);
        }
    }

}
