package io.apitomy.datamodels.validation.rules.mutex;

import io.apitomy.datamodels.models.Example;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32Example;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Example DataValue/Value Mutual Exclusivity Rule.
 * In OpenAPI 3.2, if dataValue is present, value MUST be absent.
 */
public class OasExampleDataValueMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasExampleDataValueMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitExample(Example node) {
        OpenApi32Example example = (OpenApi32Example) node;
        this.reportIf(hasValue(example.getDataValue()) && hasValue(example.getValue()), example, "dataValue", map());
    }

}
