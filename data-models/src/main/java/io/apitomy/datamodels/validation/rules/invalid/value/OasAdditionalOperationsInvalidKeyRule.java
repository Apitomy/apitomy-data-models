package io.apitomy.datamodels.validation.rules.invalid.value;

import java.util.Map;
import java.util.Set;

import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32Operation;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32PathItem;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that additionalOperations keys in a PathItem do not duplicate
 * the fixed HTTP method fields (get, put, post, delete, options, head, patch, trace, query).
 */
public class OasAdditionalOperationsInvalidKeyRule extends AbstractInvalidPropertyValueRule {

    private static final Set<String> RESERVED_METHODS = Set.of(
            "get", "put", "post", "delete", "options", "head", "patch", "trace", "query");

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasAdditionalOperationsInvalidKeyRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitPathItem(OpenApiPathItem node) {
        OpenApi32PathItem pathItem = (OpenApi32PathItem) node;
        Map<String, OpenApi32Operation> additionalOps = pathItem.getAdditionalOperations();
        if (hasValue(additionalOps)) {
            for (String key : additionalOps.keySet()) {
                this.reportIf(RESERVED_METHODS.contains(key.toLowerCase()), node,
                        "additionalOperations", map("method", key));
            }
        }
    }

}
