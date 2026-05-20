package io.apitomy.datamodels.visitors;

import io.apitomy.datamodels.models.Components;
import io.apitomy.datamodels.models.openapi.OpenApiDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiParameterDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiResponseDefinitions;
import io.apitomy.datamodels.models.openapi.OpenApiSecurityDefinitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Definitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20ParameterDefinitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20ResponseDefinitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20SecurityDefinitions;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;

public class DefinitionDetectionVisitor extends CombinedVisitorAdapter {

    private boolean isDefinitionParent = false;

    public boolean isDefinitionParentDetected() {
        return isDefinitionParent;
    }

    @Override
    public void visitDefinitions(OpenApiDefinitions node) {
        this.isDefinitionParent = true;
    }

    @Override
    public void visitParameterDefinitions(OpenApiParameterDefinitions node) {
        this.isDefinitionParent = true;
    }

    @Override
    public void visitResponseDefinitions(OpenApiResponseDefinitions node) {
        this.isDefinitionParent = true;
    }

    @Override
    public void visitSecurityDefinitions(OpenApiSecurityDefinitions node) {
        this.isDefinitionParent = true;
    }

    @Override
    public void visitComponents(Components node) {
        this.isDefinitionParent = true;
    }

}
