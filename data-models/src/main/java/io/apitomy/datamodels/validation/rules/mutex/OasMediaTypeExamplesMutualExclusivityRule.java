package io.apitomy.datamodels.validation.rules.mutex;

import io.apitomy.datamodels.models.openapi.OpenApiExamplesParent;
import io.apitomy.datamodels.models.openapi.OpenApiMediaType;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Media Type Example/Examples Mutual Exclusivity Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasMediaTypeExamplesMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasMediaTypeExamplesMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitMediaType(io.apitomy.datamodels.models.openapi.OpenApiMediaType)
     */
    @Override
    public void visitMediaType(OpenApiMediaType node) {
        OpenApiExamplesParent examplesParent = (OpenApiExamplesParent) node;
        this.reportIf(hasValue(node.getExample()) && examplesParent.getExamples() != null && !examplesParent.getExamples().isEmpty(), node, "example", map());
    }

}
