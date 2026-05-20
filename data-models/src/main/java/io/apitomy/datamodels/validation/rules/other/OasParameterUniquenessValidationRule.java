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

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiParametersParent;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Parameter Uniqueness validation rule.
 * @author eric.wittmann@gmail.com
 */
public class OasParameterUniquenessValidationRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasParameterUniquenessValidationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Given a 'in' and a 'name' for a parameter, return the # of parameters in the list
     * of parameters that match.
     */
    private int getParamCount(List<OpenApiParameter> params, String paramName, String paramIn) {
        int count = 0;
        for (OpenApiParameter param : params) {
            if (hasValue(param) && equals(param.getName(), paramName) && equals(param.getIn(), paramIn)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Validates that all parameter name and "in" combinations are unique
     */
    private void ensureUnique(OpenApiParametersParent paramsParent) {
        List<OpenApiParameter> params = paramsParent.getParameters();
        if (!hasValue(params)) {
            return;
        }

        // Must validate against resolved params in the case where we're using $ref
        List<OpenApiParameter> resolvedParams = new ArrayList<>(params.size());
        params.forEach(param -> {
            Node resolveNodeRef = ReferenceUtil.resolveNodeRef(param);
            if (resolveNodeRef instanceof OpenApiParameter) {
                resolvedParams.add((OpenApiParameter) resolveNodeRef);
            } else {
                resolvedParams.add(param);
            }
        });

        // Loop through the resolved params looking for duplicates.
        int idx = 0;
        for (OpenApiParameter resolvedParam : resolvedParams) {
            if (hasValue(resolvedParam) && hasValue(resolvedParam.getIn()) && hasValue(resolvedParam.getName()) && !equals(resolvedParam.getIn(), "body")) {
                int count = getParamCount(resolvedParams, resolvedParam.getName(), resolvedParam.getIn());
                if (count > 1) {
                    // Report the error on the original param - not the resolved param.
                    OpenApiParameter param = params.get(idx);
                    this.report(param, "in", map("paramIn", resolvedParam.getIn(), "paramName", resolvedParam.getName()));
                }
            }
            idx++;
        }
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitPathItem(io.apitomy.datamodels.models.openapi.OpenApiPathItem)
     */
    @Override
    public void visitPathItem(OpenApiPathItem node) {
        this.ensureUnique((OpenApiParametersParent) node);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitOperation(io.apitomy.datamodels.models.Operation)
     */
    @Override
    public void visitOperation(Operation node) {
        OpenApiOperation op = (OpenApiOperation) node;
        this.ensureUnique((OpenApiParametersParent) op);
    }

}
