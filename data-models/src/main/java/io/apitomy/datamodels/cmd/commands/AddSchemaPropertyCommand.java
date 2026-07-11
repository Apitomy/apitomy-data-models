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

/**
 * A command used to add a new property to a schema definition in a document.
 * @author eric.wittmann@gmail.com
 */
public class AddSchemaPropertyCommand extends AbstractCommand {

    public String _schemaDefinitionName;
    public String _propertyName;
    public ObjectNode _propertySchema;
    public boolean _propertyExisted;
    public boolean _nullProperties;

    private transient AddSchemaPropertyCommandHelper _helper;

    public AddSchemaPropertyCommand() {
    }

    public AddSchemaPropertyCommand(String schemaDefinitionName, String propertyName, ObjectNode propertySchema) {
        this._schemaDefinitionName = schemaDefinitionName;
        this._propertyName = propertyName;
        this._propertySchema = propertySchema;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddSchemaPropertyCommand] Executing.");
        this._helper = createHelper(document);

        Schema schema = this._helper.resolveSchema(document);
        if (this.isNullOrUndefined(schema)) {
            LoggerUtil.info("[AddSchemaPropertyCommand] Schema definition '%s' not found.", this._schemaDefinitionName);
            return;
        }

        // Do nothing if the property already exists.
        if (!this.isNullOrUndefined(schema.getProperties()) && schema.getProperties().containsKey(this._propertyName)) {
            LoggerUtil.info("[AddSchemaPropertyCommand] Property '%s' already exists on schema '%s'.", this._propertyName, this._schemaDefinitionName);
            this._propertyExisted = true;
            return;
        }

        this._nullProperties = this.isNullOrUndefined(schema.getProperties());

        Schema newPropSchema = schema.createSchema();
        Library.readNode(this._propertySchema, newPropSchema);
        schema.addProperty(this._propertyName, newPropSchema);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddSchemaPropertyCommand] Reverting.");
        if (this._propertyExisted) {
            return;
        }

        this._helper = createHelper(document);
        Schema schema = this._helper.resolveSchema(document);
        if (this.isNullOrUndefined(schema)) {
            return;
        }

        schema.removeProperty(this._propertyName);
        if (this._nullProperties) {
            schema.clearProperties();
        }
    }

    private AddSchemaPropertyCommandHelper createHelper(Document document) {
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

    private interface AddSchemaPropertyCommandHelper {
        Schema resolveSchema(Document document);
    }

    private class OpenApi20Helper implements AddSchemaPropertyCommandHelper {
        @Override
        public Schema resolveSchema(Document document) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (isNullOrUndefined(doc20.getDefinitions())) {
                return null;
            }
            return doc20.getDefinitions().getItem(_schemaDefinitionName);
        }
    }

    private class OpenApi30Helper implements AddSchemaPropertyCommandHelper {
        @Override
        public Schema resolveSchema(Document document) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (isNullOrUndefined(doc30.getComponents()) || isNullOrUndefined(doc30.getComponents().getSchemas())) {
                return null;
            }
            return doc30.getComponents().getSchemas().get(_schemaDefinitionName);
        }
    }

    private class OpenApi31Helper implements AddSchemaPropertyCommandHelper {
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
