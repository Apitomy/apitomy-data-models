package io.apitomy.datamodels.validation.rules.mutex;

import io.apitomy.datamodels.models.openapi.OpenApiXML;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32XML;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the XML nodeType vs attribute/wrapped Mutual Exclusivity Rule.
 * In OpenAPI 3.2, if nodeType is present, attribute and wrapped MUST NOT be present.
 */
public class OasXmlNodeTypeMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasXmlNodeTypeMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitXML(OpenApiXML node) {
        OpenApi32XML xml = (OpenApi32XML) node;
        if (hasValue(xml.getNodeType())) {
            this.reportIf(hasValue(xml.isAttribute()), xml, "attribute",
                    map("otherProperty", "attribute"));
            this.reportIf(hasValue(xml.isWrapped()), xml, "wrapped",
                    map("otherProperty", "wrapped"));
        }
    }

}
