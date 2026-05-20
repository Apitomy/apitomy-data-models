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

import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Header;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Parameter;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Header;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Parameter;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasAllowReservedNotAllowedForParamRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasAllowReservedNotAllowedForParamRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        OpenApiParameter param = (OpenApiParameter) node;

        Boolean isAllowReserved = null;
        if (ModelTypeUtil.isOpenApi30Model(node)) {
            isAllowReserved = ((OpenApi30Parameter) node).isAllowReserved();
        } else if (ModelTypeUtil.isOpenApi31Model(node)) {
            isAllowReserved = ((OpenApi31Parameter) node).isAllowReserved();
        }

        if (hasValue(isAllowReserved)) {
            this.reportIfInvalid(equals(param.getIn(), "query"), node, "allowReserved", map());
        }
    }

}