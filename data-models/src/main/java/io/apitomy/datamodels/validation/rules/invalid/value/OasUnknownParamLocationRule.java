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

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Unknown Parameter Location rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnknownParamLocationRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnknownParamLocationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        OpenApiParameter param = (OpenApiParameter) node;
        if (hasValue(param.getIn())) {
            if (node.root().modelType() == ModelType.OPENAPI20) {
                this.reportIfInvalid(isValidEnumItem(param.getIn(), array( "query", "header", "path", "formData", "body" )),
                        node, "in", map("options", "query, header, path, formData, body"));
            } else {
                this.reportIfInvalid(isValidEnumItem(param.getIn(), array( "query", "header", "path", "cookie" )),
                        node, "in", map("options", "query, header, path, cookie"));
            }
        }
    }

}
