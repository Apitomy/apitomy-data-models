package io.apitomy.datamodels.validation.rules.invalid.value;

import io.apitomy.datamodels.models.openapi.OpenApiXML;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32XML;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that the XML nodeType field is one of the allowed values:
 * element, attribute, text, cdata, none.
 */
public class OasInvalidXmlNodeTypeRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidXmlNodeTypeRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitXML(OpenApiXML node) {
        OpenApi32XML xml = (OpenApi32XML) node;
        String nodeType = xml.getNodeType();
        if (hasValue(nodeType)) {
            this.reportIfInvalid(
                    isValidEnumItem(nodeType, array("element", "attribute", "text", "cdata", "none")),
                    node, "nodeType", map("nodeType", nodeType));
        }
    }

}
