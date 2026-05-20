/*
 * Copyright 2019 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apitomy.datamodels.validation.rules.invalid.value;

import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Operation;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Operation;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasUnexpectedRequestBodyRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnexpectedRequestBodyRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Returns true if the given operation is one of:  POST, PUT, OPTIONS
     * @param operation
     */
    protected boolean isValidRequestBodyOperation(OpenApiOperation operation) {
        String method = getOperationMethod(operation);
        return isValidEnumItem(method, array("put", "post", "options", "patch"));
    }

    protected boolean hasRequestBody(Operation node) {
        if (node instanceof OpenApi31Operation) {
            return hasValue(((OpenApi31Operation) node).getRequestBody());
        } else if (node instanceof OpenApi30Operation) {
            return hasValue(((OpenApi30Operation) node).getRequestBody());
        }
        return false;
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitOperation(io.apitomy.datamodels.models.Operation)
     */
    @Override
    public void visitOperation(Operation node) {
        if (hasRequestBody(node)) {
            OpenApiOperation operation = (OpenApiOperation) node;

            String method = getOperationMethod(operation);
            this.reportIfInvalid(isValidRequestBodyOperation(operation), operation,
                    "requestBody", map("method", method.toUpperCase()));
        }
    }

}