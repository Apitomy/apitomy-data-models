package io.apitomy.datamodels.validation.rules.mutex;

import io.apitomy.datamodels.models.Example;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32Example;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Example SerializedValue Mutual Exclusivity Rule.
 * In OpenAPI 3.2, if serializedValue is present, value and externalValue MUST be absent.
 */
public class OasExampleSerializedValueMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasExampleSerializedValueMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitExample(Example node) {
        OpenApi32Example example = (OpenApi32Example) node;
        if (hasValue(example.getSerializedValue())) {
            this.reportIf(hasValue(example.getValue()), example, "serializedValue",
                    map("otherProperty", "value"));
            this.reportIf(hasValue(example.getExternalValue()), example, "serializedValue",
                    map("otherProperty", "externalValue"));
        }
    }

}
