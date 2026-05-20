package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.models.asyncapi.AsyncApiComponents;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.models.asyncapi.AsyncApiSecurityScheme;
import io.apitomy.datamodels.models.openapi.OpenApiSecurityScheme;
import io.apitomy.datamodels.models.openapi.v2x.OpenApi2xSecurityDefinitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20SecurityScheme;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to update (replace) an existing security scheme in a document.
 * Takes the scheme name and a new serialized security scheme to replace the existing one.
 * @author eric.wittmann@gmail.com
 */
public class UpdateSecuritySchemeCommand extends AbstractCommand {

    public String _schemeName;
    public ObjectNode _newSchemeObj;

    public ObjectNode _oldSchemeObj;

    public UpdateSecuritySchemeCommand() {
    }

    public UpdateSecuritySchemeCommand(String schemeName, ObjectNode newSchemeObj) {
        this._schemeName = schemeName;
        this._newSchemeObj = newSchemeObj;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[UpdateSecuritySchemeCommand] Executing.");
        this._oldSchemeObj = null;

        SecurityScheme existingScheme = findScheme(document);
        if (this.isNullOrUndefined(existingScheme)) {
            return;
        }

        // Save old state for undo
        this._oldSchemeObj = Library.writeNode((Node) existingScheme);

        // Remove the old scheme and add a new one with the same name
        removeScheme(document);
        addScheme(document);
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[UpdateSecuritySchemeCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldSchemeObj)) {
            return;
        }

        // Remove the updated scheme and restore the old one
        removeScheme(document);
        restoreScheme(document);
    }

    private SecurityScheme findScheme(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi2xSecurityDefinitions defs = ((OpenApi20Document) document).getSecurityDefinitions();
            return this.isNullOrUndefined(defs) ? null : defs.getItem(this._schemeName);
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getSecuritySchemes())) {
                return null;
            }
            return components.getSecuritySchemes().get(this._schemeName);
        } else if (ModelTypeUtil.isAsyncApiModel(document)) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getSecuritySchemes())) {
                return null;
            }
            return components.getSecuritySchemes().get(this._schemeName);
        }
        return null;
    }

    private void removeScheme(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi2xSecurityDefinitions defs = ((OpenApi20Document) document).getSecurityDefinitions();
            if (!this.isNullOrUndefined(defs)) {
                defs.removeItem(this._schemeName);
            }
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            if (!this.isNullOrUndefined(components)) {
                components.removeSecurityScheme(this._schemeName);
            }
        } else if (ModelTypeUtil.isAsyncApiModel(document)) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            if (!this.isNullOrUndefined(components)) {
                components.removeSecurityScheme(this._schemeName);
            }
        }
    }

    private void addScheme(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi2xSecurityDefinitions defs = ((OpenApi20Document) document).getSecurityDefinitions();
            OpenApi20SecurityScheme newScheme = (OpenApi20SecurityScheme) defs.createSecurityScheme();
            Library.readNode(this._newSchemeObj, newScheme);
            defs.addItem(this._schemeName, newScheme);
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            OpenApiSecurityScheme newScheme = components.createSecurityScheme();
            Library.readNode(this._newSchemeObj, newScheme);
            components.addSecurityScheme(this._schemeName, newScheme);
        } else if (ModelTypeUtil.isAsyncApiModel(document)) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            AsyncApiSecurityScheme newScheme = components.createSecurityScheme();
            Library.readNode(this._newSchemeObj, newScheme);
            components.addSecurityScheme(this._schemeName, newScheme);
        }
    }

    private void restoreScheme(Document document) {
        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi2xSecurityDefinitions defs = ((OpenApi20Document) document).getSecurityDefinitions();
            OpenApi20SecurityScheme oldScheme = (OpenApi20SecurityScheme) defs.createSecurityScheme();
            Library.readNode(this._oldSchemeObj, oldScheme);
            defs.addItem(this._schemeName, oldScheme);
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            OpenApiSecurityScheme oldScheme = components.createSecurityScheme();
            Library.readNode(this._oldSchemeObj, oldScheme);
            components.addSecurityScheme(this._schemeName, oldScheme);
        } else if (ModelTypeUtil.isAsyncApiModel(document)) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            AsyncApiSecurityScheme oldScheme = components.createSecurityScheme();
            Library.readNode(this._oldSchemeObj, oldScheme);
            components.addSecurityScheme(this._schemeName, oldScheme);
        }
    }

}
