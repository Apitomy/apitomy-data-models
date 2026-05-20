package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.asyncapi.AsyncApiComponents;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.models.asyncapi.AsyncApiSecurityScheme;
import io.apitomy.datamodels.models.openapi.OpenApiSecurityScheme;
import io.apitomy.datamodels.models.openapi.v2x.OpenApi2xSecurityDefinitions;
import io.apitomy.datamodels.models.openapi.v2x.OpenApi2xSecurityScheme;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to add a new security scheme to a document.
 * Takes a serialized security scheme as input, which allows the caller
 * to fully configure the scheme before adding it.
 * @author eric.wittmann@gmail.com
 */
public class AddSecuritySchemeCommand extends AbstractCommand {

    public String _schemeName;
    public ObjectNode _schemeObj;

    public boolean _schemeCreated;
    public boolean _nullParent;

    public AddSecuritySchemeCommand() {
    }

    public AddSecuritySchemeCommand(String schemeName, ObjectNode schemeObj) {
        this._schemeName = schemeName;
        this._schemeObj = schemeObj;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddSecuritySchemeCommand] Executing.");
        this._schemeCreated = false;
        this._nullParent = false;

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            executeForOpenApi20((OpenApi20Document) document);
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            executeForOpenApi3((OpenApi3xDocument) document);
        } else if (ModelTypeUtil.isAsyncApiModel(document)) {
            executeForAsyncApi((AsyncApiDocument) document);
        }
    }

    private void executeForOpenApi20(OpenApi20Document doc) {
        OpenApi2xSecurityDefinitions definitions = doc.getSecurityDefinitions();
        if (this.isNullOrUndefined(definitions)) {
            definitions = doc.createSecurityDefinitions();
            doc.setSecurityDefinitions(definitions);
            this._nullParent = true;
        }

        // Check if scheme already exists
        if (!this.isNullOrUndefined(definitions.getItem(this._schemeName))) {
            return;
        }

        OpenApi2xSecurityScheme newScheme = (OpenApi2xSecurityScheme) definitions.createSecurityScheme();
        Library.readNode(this._schemeObj, newScheme);
        definitions.addItem(this._schemeName, newScheme);
        this._schemeCreated = true;
    }

    private void executeForOpenApi3(OpenApi3xDocument doc) {
        OpenApi3xComponents components = doc.getComponents();
        if (this.isNullOrUndefined(components)) {
            components = doc.createComponents();
            doc.setComponents(components);
            this._nullParent = true;
        }

        // Check if scheme already exists
        if (!this.isNullOrUndefined(components.getSecuritySchemes()) &&
                components.getSecuritySchemes().containsKey(this._schemeName)) {
            return;
        }

        OpenApiSecurityScheme newScheme = components.createSecurityScheme();
        Library.readNode(this._schemeObj, newScheme);
        components.addSecurityScheme(this._schemeName, newScheme);
        this._schemeCreated = true;
    }

    private void executeForAsyncApi(AsyncApiDocument doc) {
        AsyncApiComponents components = doc.getComponents();
        if (this.isNullOrUndefined(components)) {
            components = doc.createComponents();
            doc.setComponents(components);
            this._nullParent = true;
        }

        // Check if scheme already exists
        if (!this.isNullOrUndefined(components.getSecuritySchemes()) &&
                components.getSecuritySchemes().containsKey(this._schemeName)) {
            return;
        }

        AsyncApiSecurityScheme newScheme = components.createSecurityScheme();
        Library.readNode(this._schemeObj, newScheme);
        components.addSecurityScheme(this._schemeName, newScheme);
        this._schemeCreated = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddSecuritySchemeCommand] Reverting.");
        if (!this._schemeCreated) {
            return;
        }

        if (ModelTypeUtil.isOpenApi2Model(document)) {
            OpenApi20Document doc = (OpenApi20Document) document;
            if (this._nullParent) {
                doc.setSecurityDefinitions(null);
            } else {
                OpenApi2xSecurityDefinitions definitions = doc.getSecurityDefinitions();
                if (!this.isNullOrUndefined(definitions)) {
                    definitions.removeItem(this._schemeName);
                }
            }
        } else if (ModelTypeUtil.isOpenApi3Model(document)) {
            OpenApi3xDocument doc = (OpenApi3xDocument) document;
            if (this._nullParent) {
                doc.setComponents(null);
            } else {
                OpenApi3xComponents components = doc.getComponents();
                if (!this.isNullOrUndefined(components)) {
                    components.removeSecurityScheme(this._schemeName);
                }
            }
        } else if (ModelTypeUtil.isAsyncApiModel(document)) {
            AsyncApiDocument doc = (AsyncApiDocument) document;
            if (this._nullParent) {
                doc.setComponents(null);
            } else {
                AsyncApiComponents components = doc.getComponents();
                if (!this.isNullOrUndefined(components)) {
                    components.removeSecurityScheme(this._schemeName);
                }
            }
        }
    }

}
