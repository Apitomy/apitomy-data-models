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

import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiParametersParent;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.List;

/**
 * Implements the Body Parameter Uniqueness validation rule (can only have 1 body param).
 * @author eric.wittmann@gmail.com
 */
public class OasBodyParameterUniquenessValidationRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasBodyParameterUniquenessValidationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        OpenApiParameter param = (OpenApiParameter) node;
        if (equals(param.getIn(), "body")) {
            List<OpenApiParameter> params = ((OpenApiParametersParent) node.parent()).getParameters();
            int count = countBodyParams(params);
            this.reportIf(count > 1, node, "in", map());
        }
    }

    /**
     * Counts the # of body style params.
     */
    private int countBodyParams(List<OpenApiParameter> params) {
        int count = 0;
        if (isDefined(params)) {
            for (OpenApiParameter parameter : params) {
                if (equals(parameter.getIn(), "body")) {
                    count++;
                }
            }
        }
        return count;
    }

}
