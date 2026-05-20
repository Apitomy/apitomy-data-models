package io.apitomy.datamodels.cmd.commands;

import java.util.ArrayList;
import java.util.Map;

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.deref.AllReferenceableNodeVisitor;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.openapi.OpenApiCallback;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to rename a callback definition and update all $ref references.
 * @author eric.wittmann@gmail.com
 */
public class RefactorCallbackDefinitionCommand extends AbstractCommand {

    public String _oldName;
    public String _newName;

    public boolean _callbackRenamed;

    public RefactorCallbackDefinitionCommand() {
    }

    public RefactorCallbackDefinitionCommand(String oldName, String newName) {
        this._oldName = oldName;
        this._newName = newName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[RefactorCallbackDefinitionCommand] Executing.");
        this._callbackRenamed = false;

        if (ModelTypeUtil.isOpenApi30Model(document)) {
            renameInOas30((OpenApi30Document) document, _oldName, _newName);
        } else if (ModelTypeUtil.isOpenApi31Model(document)) {
            renameInOas31((OpenApi31Document) document, _oldName, _newName);
        } else {
            return;
        }

        if (this._callbackRenamed) {
            String oldRef = "#/components/callbacks/" + _oldName;
            String newRef = "#/components/callbacks/" + _newName;
            updateRefs(document, oldRef, newRef);
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[RefactorCallbackDefinitionCommand] Reverting.");
        if (!this._callbackRenamed) {
            return;
        }

        if (ModelTypeUtil.isOpenApi30Model(document)) {
            renameInOas30((OpenApi30Document) document, _newName, _oldName);
        } else if (ModelTypeUtil.isOpenApi31Model(document)) {
            renameInOas31((OpenApi31Document) document, _newName, _oldName);
        }

        String oldRef = "#/components/callbacks/" + _oldName;
        String newRef = "#/components/callbacks/" + _newName;
        updateRefs(document, newRef, oldRef);
    }

    private void renameInOas30(OpenApi30Document doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getComponents())) {
            return;
        }
        OpenApiCallback callback = doc.getComponents().getCallbacks().get(fromName);
        if (isNullOrUndefined(callback)) {
            return;
        }
        if (!isNullOrUndefined(doc.getComponents().getCallbacks().get(toName))) {
            return;
        }
        int index = indexOfKey(doc.getComponents().getCallbacks(), fromName);
        doc.getComponents().removeCallback(fromName);
        doc.getComponents().insertCallback(toName, callback, index);
        this._callbackRenamed = true;
    }

    private void renameInOas31(OpenApi31Document doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getComponents())) {
            return;
        }
        OpenApiCallback callback = doc.getComponents().getCallbacks().get(fromName);
        if (isNullOrUndefined(callback)) {
            return;
        }
        if (!isNullOrUndefined(doc.getComponents().getCallbacks().get(toName))) {
            return;
        }
        int index = indexOfKey(doc.getComponents().getCallbacks(), fromName);
        doc.getComponents().removeCallback(fromName);
        doc.getComponents().insertCallback(toName, callback, index);
        this._callbackRenamed = true;
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
