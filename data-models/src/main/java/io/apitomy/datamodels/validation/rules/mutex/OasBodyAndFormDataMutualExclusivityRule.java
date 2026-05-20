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

package io.apitomy.datamodels.validation.rules.mutex;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiParametersParent;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.List;

/**
 * Implements the Body and Form Data Mutual Exclusivity Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasBodyAndFormDataMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasBodyAndFormDataMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    private void visitParameterParent(OpenApiParametersParent paramParent) {
        @SuppressWarnings("unchecked")
        List<OpenApiParameter> parameters = paramParent.getParameters();
        if (hasValue(parameters)) {
            boolean hasBodyParam = false;
            boolean hasFormDataParam = false;
            for (OpenApiParameter param : parameters) {
                if (equals(param.getIn(), "body")) {
                    hasBodyParam = true;
                }
                if (equals(param.getIn(), "formData")) {
                    hasFormDataParam = true;
                }
            }
            this.reportIf(hasBodyParam && hasFormDataParam, (Node) paramParent, "in", map());
        }
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitOperation(io.apitomy.datamodels.models.Operation)
     */
    @Override
    public void visitOperation(Operation node) {
        visitParameterParent((OpenApiParametersParent) node);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitPathItem(io.apitomy.datamodels.models.openapi.OpenApiPathItem)
     */
    @Override
    public void visitPathItem(OpenApiPathItem node) {
        visitParameterParent((OpenApiParametersParent) node);
    }

}
