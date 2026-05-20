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

package io.apitomy.datamodels.validation.rules.required;

import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasPathParamsMustBeRequiredRule extends RequiredPropertyValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasPathParamsMustBeRequiredRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        if (hasValue(NodeUtil.getNodeProperty(node, "$ref"))) {
            return;
        }
        OpenApiParameter param = (OpenApiParameter) node;
        if (equals(param.getIn(), "path") && !equals(param.isRequired(), true)) {
            this.report(node, "required", map("name", param.getName()));
        }
    }

}
