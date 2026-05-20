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

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;
import io.apitomy.datamodels.visitors.ConsumesProducesFinder;

/**
 * Implements the Form Data Parameter Not Allowed rule.
 * @author eric.wittmann@gmail.com
 */
public class OasFormDataParamNotAllowedRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasFormDataParamNotAllowedRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        OpenApiParameter param = (OpenApiParameter) node;
        if (equals(param.getIn(), "formData")) {
            ConsumesProducesFinder finder = new ConsumesProducesFinder();
            VisitorUtil.visitTree(node, finder, TraverserDirection.up);

            List<String> consumes = finder.consumes;
            if (!hasValue(consumes)) {
                consumes = new ArrayList<>();
            }
            boolean valid = consumes.indexOf("application/x-www-form-urlencoded") >= 0 || consumes.indexOf("multipart/form-data") >= 0;
            this.reportIfInvalid(valid, node, "consumes", map());
        }
    }

}