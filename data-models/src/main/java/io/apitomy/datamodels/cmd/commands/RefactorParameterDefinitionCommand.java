package io.apitomy.datamodels.cmd.commands;

import java.util.ArrayList;
import java.util.Map;

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.deref.AllReferenceableNodeVisitor;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.asyncapi.AsyncApiComponents;
import io.apitomy.datamodels.models.asyncapi.AsyncApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Parameter;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to rename a parameter definition and update all $ref references.
 * @author eric.wittmann@gmail.com
 */
public class RefactorParameterDefinitionCommand extends AbstractCommand {

    public String _oldName;
    public String _newName;

    public boolean _parameterRenamed;

    public RefactorParameterDefinitionCommand() {
    }

    public RefactorParameterDefinitionCommand(String oldName, String newName) {
        this._oldName = oldName;
        this._newName = newName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[RefactorParameterDefinitionCommand] Executing.");
        this._parameterRenamed = false;

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            renameInOas20((OpenApi20Document) document, _oldName, _newName);
        } else if (ModelTypeUtil.isOpenApi30Model(document)) {
            renameInOas30((OpenApi30Document) document, _oldName, _newName);
        } else if (ModelTypeUtil.isOpenApi31Model(document)) {
            renameInOas31((OpenApi31Document) document, _oldName, _newName);
        } else if (ModelTypeUtil.isAsyncApi2Model(document)) {
            renameInAsyncApi2((AsyncApiDocument) document, _oldName, _newName);
        } else {
            return;
        }

        if (this._parameterRenamed) {
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
        LoggerUtil.info("[RefactorParameterDefinitionCommand] Reverting.");
        if (!this._parameterRenamed) {
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
        }

        String oldRef = getRefPrefix(document) + _oldName;
        String newRef = getRefPrefix(document) + _newName;
        updateRefs(document, newRef, oldRef);
    }

    private void renameInOas20(OpenApi20Document doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getParameters())) {
            return;
        }
        OpenApi20Parameter parameter = (OpenApi20Parameter) doc.getParameters().getItem(fromName);
        if (isNullOrUndefined(parameter)) {
            return;
        }
        if (!isNullOrUndefined(doc.getParameters().getItem(toName))) {
            return;
        }
        int index = doc.getParameters().getItemNames().indexOf(fromName);
        doc.getParameters().removeItem(fromName);
        doc.getParameters().insertItem(toName, parameter, index);
        this._parameterRenamed = true;
    }

    private void renameInOas30(OpenApi30Document doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getComponents())) {
            return;
        }
        OpenApiParameter parameter = doc.getComponents().getParameters().get(fromName);
        if (isNullOrUndefined(parameter)) {
            return;
        }
        if (!isNullOrUndefined(doc.getComponents().getParameters().get(toName))) {
            return;
        }
        int index = indexOfKey(doc.getComponents().getParameters(), fromName);
        doc.getComponents().removeParameter(fromName);
        doc.getComponents().insertParameter(toName, parameter, index);
        this._parameterRenamed = true;
    }

    private void renameInOas31(OpenApi31Document doc, String fromName, String toName) {
        if (isNullOrUndefined(doc.getComponents())) {
            return;
        }
        OpenApiParameter parameter = doc.getComponents().getParameters().get(fromName);
        if (isNullOrUndefined(parameter)) {
            return;
        }
        if (!isNullOrUndefined(doc.getComponents().getParameters().get(toName))) {
            return;
        }
        int index = indexOfKey(doc.getComponents().getParameters(), fromName);
        doc.getComponents().removeParameter(fromName);
        doc.getComponents().insertParameter(toName, parameter, index);
        this._parameterRenamed = true;
    }

    private void renameInAsyncApi2(AsyncApiDocument doc, String fromName, String toName) {
        AsyncApiComponents components = doc.getComponents();
        if (isNullOrUndefined(components)) {
            return;
        }
        AsyncApiParameter parameter = components.getParameters().get(fromName);
        if (isNullOrUndefined(parameter)) {
            return;
        }
        if (!isNullOrUndefined(components.getParameters().get(toName))) {
            return;
        }
        int index = indexOfKey(components.getParameters(), fromName);
        components.removeParameter(fromName);
        components.insertParameter(toName, parameter, index);
        this._parameterRenamed = true;
    }

    private String getRefPrefix(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            return "#/parameters/";
        }
        return "#/components/parameters/";
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
