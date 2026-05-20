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

package io.apitomy.datamodels.validation.rules.other;

import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.asyncapi.v2x.AsyncApi2xOperation;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the Operation ID Uniqueness validation rule for both OpenAPI and AsyncAPI.
 * @author eric.wittmann@gmail.com
 */
public class OasOperationIdUniquenessValidationRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasOperationIdUniquenessValidationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    private Map<String, List<Operation>> indexedOperations = new HashMap<>();

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitOperation(io.apitomy.datamodels.models.Operation)
     */
    @Override
    public void visitOperation(Operation node) {
        String operationId = getOperationId(node);
        if (hasValue(operationId)) {
            List<Operation> dupes = this.indexedOperations.get(operationId);
            if (hasValue(dupes)) {
                this.reportIfInvalid(dupes.size() > 1, dupes.get(0), "operationId", map("operationId", operationId));
                this.report(node, "operationId", map("operationId", operationId));
                dupes.add(node);
            } else {
                dupes = new ArrayList<>();
                dupes.add(node);
                this.indexedOperations.put(operationId, dupes);
            }
        }
    }

    /**
     * Gets the operation ID from an operation node (works for both OpenAPI and AsyncAPI 2.x).
     */
    private String getOperationId(Operation node) {
        if (ModelTypeUtil.isOpenApiModel(node)) {
            return ((OpenApiOperation) node).getOperationId();
        }
        if (ModelTypeUtil.isAsyncApi2Model(node)) {
            return ((AsyncApi2xOperation) node).getOperationId();
        }
        return null;
    }

}
