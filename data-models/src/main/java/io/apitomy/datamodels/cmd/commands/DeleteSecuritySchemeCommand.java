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
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A command used to delete a single security scheme from a document.
 * @author eric.wittmann@gmail.com
 */
public class DeleteSecuritySchemeCommand extends AbstractCommand {

    public String _schemeName;

    public ObjectNode _oldScheme;
    public int _oldIndex;

    public DeleteSecuritySchemeCommand() {
    }

    public DeleteSecuritySchemeCommand(String schemeName) {
        this._schemeName = schemeName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteSecuritySchemeCommand] Executing.");
        this._oldScheme = null;

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi2xSecurityDefinitions defs = ((OpenApi20Document) document).getSecurityDefinitions();
            if (this.isNullOrUndefined(defs)) {
                return;
            }
            OpenApiSecurityScheme scheme = defs.getItem(this._schemeName);
            if (!this.isNullOrUndefined(scheme)) {
                this._oldScheme = Library.writeNode(scheme);
                this._oldIndex = defs.getItemNames().indexOf(this._schemeName);
                defs.removeItem(this._schemeName);
            }
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getSecuritySchemes())) {
                return;
            }
            SecurityScheme scheme = components.getSecuritySchemes().get(this._schemeName);
            if (!this.isNullOrUndefined(scheme)) {
                this._oldScheme = Library.writeNode((Node) scheme);
                List<String> schemeNames = new ArrayList<>(components.getSecuritySchemes().keySet());
                this._oldIndex = schemeNames.indexOf(this._schemeName);
                components.removeSecurityScheme(this._schemeName);
            }
        } else if (ModelTypeUtil.isAsyncApiModel(document)) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            if (this.isNullOrUndefined(components) || this.isNullOrUndefined(components.getSecuritySchemes())) {
                return;
            }
            SecurityScheme scheme = components.getSecuritySchemes().get(this._schemeName);
            if (!this.isNullOrUndefined(scheme)) {
                this._oldScheme = Library.writeNode((Node) scheme);
                List<String> schemeNames = new ArrayList<>(components.getSecuritySchemes().keySet());
                this._oldIndex = schemeNames.indexOf(this._schemeName);
                components.removeSecurityScheme(this._schemeName);
            }
        }
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteSecuritySchemeCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldScheme)) {
            return;
        }

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi2xSecurityDefinitions defs = ((OpenApi20Document) document).getSecurityDefinitions();
            if (this.isNullOrUndefined(defs)) {
                defs = ((OpenApi20Document) document).createSecurityDefinitions();
                ((OpenApi20Document) document).setSecurityDefinitions(defs);
            }
            OpenApiSecurityScheme scheme = defs.createSecurityScheme();
            Library.readNode(this._oldScheme, scheme);
            defs.insertItem(this._schemeName, scheme, this._oldIndex);
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xComponents components = ((OpenApi3xDocument) document).getComponents();
            if (this.isNullOrUndefined(components)) {
                components = ((OpenApi3xDocument) document).createComponents();
                ((OpenApi3xDocument) document).setComponents(components);
            }
            OpenApiSecurityScheme scheme = components.createSecurityScheme();
            Library.readNode(this._oldScheme, scheme);
            components.insertSecurityScheme(this._schemeName, scheme, this._oldIndex);
        } else if (ModelTypeUtil.isAsyncApiModel(document)) {
            AsyncApiComponents components = ((AsyncApiDocument) document).getComponents();
            if (this.isNullOrUndefined(components)) {
                components = ((AsyncApiDocument) document).createComponents();
                ((AsyncApiDocument) document).setComponents(components);
            }
            AsyncApiSecurityScheme scheme = components.createSecurityScheme();
            Library.readNode(this._oldScheme, scheme);
            components.insertSecurityScheme(this._schemeName, scheme, this._oldIndex);
        }
    }

}
