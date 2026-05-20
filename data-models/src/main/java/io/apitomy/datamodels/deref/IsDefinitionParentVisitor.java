package io.apitomy.datamodels.deref;

import io.apitomy.datamodels.models.Components;
import io.apitomy.datamodels.models.openapi.OpenApiDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiParameterDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiResponseDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiSecurityDefinitions;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;

/**
 * Visitor to determine if a Node is a "Components" node.  This includes the actual
 * Components entities from OpenAPI 3 and AsyncAPI 2.  It also includes all of the
 * Definitions entities from OpenAPI 2.
 *
 * @author eric.wittmann@gmail.com
 */
public class IsDefinitionParentVisitor extends CombinedVisitorAdapter {

    private boolean _isDefinitionParent = false;

    public boolean isDefinitionParent() {
        return this._isDefinitionParent;
    }

    @Override
    public void visitComponents(Components node) {
        this._isDefinitionParent = true;
    }

    @Override
    public void visitDefinitions(OpenApiDefinitions node) {
        this._isDefinitionParent = true;
    }

    @Override
    public void visitParameterDefinitions(OpenApiParameterDefinitions node) {
        this._isDefinitionParent = true;
    }

    @Override
    public void visitResponseDefinitions(OpenApiResponseDefinitions node) {
        this._isDefinitionParent = true;
    }

    @Override
    public void visitSecurityDefinitions(OpenApiSecurityDefinitions node) {
        this._isDefinitionParent = true;
    }

}
