package io.apitomy.datamodels.cmd.commands;

import java.util.ArrayList;
import java.util.Map;

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.deref.AllReferenceableNodeVisitor;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.asyncapi.AsyncApiComponents;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.models.openrpc.OpenRpcDocument;
import io.apitomy.datamodels.models.openrpc.OpenRpcSchema;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Schema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to rename a schema definition and update all $ref references.
 * @author eric.wittmann@gmail.com
 */
public class RefactorSchemaDefinitionCommand extends AbstractCommand {

    public String _oldName;
    public String _newName;

    public boolean _schemaRenamed;

    public RefactorSchemaDefinitionCommand() {
    }

    public RefactorSchemaDefinitionCommand(String oldName, String newName) {
        this._oldName = oldName;
        this._newName = newName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[RefactorSchemaDefinitionCommand] Executing.");
        this._schemaRenamed = false;

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            renameInOas20((OpenApi20Document) document, _oldName, _newName);
        } else if (ModelTypeUtil.isOpenApi30Model(document)) {
            renameInOas30((OpenApi30Document) document, _oldName, _newName);
        } else if (ModelTypeUtil.isOpenApi31Model(document)) {
            renameInOas31((OpenApi31Document) document, _oldName, _newName);
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            renameInAsyncApi2((AsyncApiDocument) document, _oldName, _newName);
        } else if (ModelTypeUtil.isOpenRpcModel(document)) {
            renameInOpenRpc((OpenRpcDocument) document, _oldName, _newName);
        } else {
            return;
        }

        if (this._schemaRenamed) {
            String oldRef = getRefPrefix(document) + _oldName;
            String newRef = getRefPrefix(document) + _newName;
            updateRefs(document, oldRef, newRef);
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[RefactorSchemaDefinitionCommand] Reverting.");
        if (!this._schemaRenamed) {
            return;
        }

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            renameInOas20((OpenApi20Document) document, _newName, _oldName);
        } else if (ModelTypeUtil.isOpenApi30Model(document)) {
            renameInOas30((OpenApi30Document) document, _newName, _oldName);
        } else if (ModelTypeUtil.isOpenApi31Model(document)) {
            renameInOas31((OpenApi31Document) document, _newName, _oldName);
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            renameInAsyncApi2((AsyncApiDocument) document, _newName, _oldName);
        } else if (ModelTypeUtil.isOpenRpcModel(document)) {
            renameInOpenRpc((OpenRpcDocument) document, _newName, _oldName);
        }

        String oldRef = getRefPrefix(document) + _oldName;
        String newRef = getRefPrefix(document) + _newName;
        updateRefs(document, newRef, oldRef);
    }

    private void renameInOas20(OpenApi20Document doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getDefinitions())) {
            return;
        }
        OpenApi20Schema schema = (OpenApi20Schema) doc.getDefinitions().getItem(fromName);
        if (isNullOrUndefined(schema)) {
            return;
        }
        if (!isNullOrUndefined(doc.getDefinitions().getItem(toName))) {
            return;
        }
        int index = doc.getDefinitions().getItemNames().indexOf(fromName);
        doc.getDefinitions().removeItem(fromName);
        doc.getDefinitions().insertItem(toName, schema, index);
        this._schemaRenamed = true;
    }

    private void renameInOas30(OpenApi30Document doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getComponents())) {
            return;
        }
        OpenApiSchema schema = (OpenApiSchema) doc.getComponents().getSchemas().get(fromName);
        if (isNullOrUndefined(schema)) {
            return;
        }
        if (!isNullOrUndefined(doc.getComponents().getSchemas().get(toName))) {
            return;
        }
        int index = indexOfKey(doc.getComponents().getSchemas(), fromName);
        doc.getComponents().removeSchema(fromName);
        doc.getComponents().insertSchema(toName, schema, index);
        this._schemaRenamed = true;
    }

    private void renameInOas31(OpenApi31Document doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getComponents())) {
            return;
        }
        OpenApiSchema schema = (OpenApiSchema) doc.getComponents().getSchemas().get(fromName);
        if (isNullOrUndefined(schema)) {
            return;
        }
        if (!isNullOrUndefined(doc.getComponents().getSchemas().get(toName))) {
            return;
        }
        int index = indexOfKey(doc.getComponents().getSchemas(), fromName);
        doc.getComponents().removeSchema(fromName);
        doc.getComponents().insertSchema(toName, schema, index);
        this._schemaRenamed = true;
    }

    @SuppressWarnings("unchecked")
    private void renameInAsyncApi2(AsyncApiDocument doc, String fromName, String toName) {
        AsyncApiComponents components = doc.getComponents();
        if (isNullOrUndefined(components)) {
            return;
        }
        Map<String, ? extends Schema> schemas = (Map<String, ? extends Schema>) NodeUtil.getNodeProperty(components, "schemas");
        if (isNullOrUndefined(schemas)) {
            return;
        }
        Schema schema = schemas.get(fromName);
        if (isNullOrUndefined(schema)) {
            return;
        }
        if (!isNullOrUndefined(schemas.get(toName))) {
            return;
        }
        int index = new ArrayList<>(schemas.keySet()).indexOf(fromName);
        NodeUtil.invokeMethod(components, "removeSchema", fromName);
        NodeUtil.invokeMethod(components, "insertSchema", toName, schema, index);
        this._schemaRenamed = true;
    }

    private void renameInOpenRpc(OpenRpcDocument doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getComponents())) {
            return;
        }
        OpenRpcSchema schema = doc.getComponents().getSchemas().get(fromName);
        if (isNullOrUndefined(schema)) {
            return;
        }
        if (!isNullOrUndefined(doc.getComponents().getSchemas().get(toName))) {
            return;
        }
        int index = indexOfKey(doc.getComponents().getSchemas(), fromName);
        doc.getComponents().removeSchema(fromName);
        doc.getComponents().insertSchema(toName, schema, index);
        this._schemaRenamed = true;
    }

    private String getRefPrefix(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            return "#/definitions/";
        }
        return "#/components/schemas/";
    }

    private void updateRefs(Document document, String oldRef, String newRef) {
        VisitorUtil.visitTree(document, new AllReferenceableNodeVisitor() {
            @Override
            protected void visitReferenceableNode(Referenceable refNode) {
                String ref = refNode.get$ref();
                if (oldRef.equals(ref)) {
                    refNode.set$ref(newRef);
                }
            }
        }, TraverserDirection.down);
    }

    /**
     * Returns the index of the given key in the map's iteration order.
     */
    private static int indexOfKey(Map<String, ?> map, String key) {
        return new ArrayList<>(map.keySet()).indexOf(key);
    }

}
