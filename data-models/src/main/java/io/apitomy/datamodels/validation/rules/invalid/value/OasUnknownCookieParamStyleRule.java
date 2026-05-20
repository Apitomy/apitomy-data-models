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

import java.util.Map;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasUnknownCookieParamStyleRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnknownCookieParamStyleRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        String style = null;
        String _in = null;
        if (ModelTypeUtil.isOpenApi30Model(node)) {
            style = ((OpenApi30Parameter) node).getStyle();
            _in = ((OpenApi30Parameter) node).getIn();
        } else if (ModelTypeUtil.isOpenApi31Model(node)) {
            style = ((OpenApi31Parameter) node).getStyle();
            _in = ((OpenApi31Parameter) node).getIn();
        }
        if (hasValue(style)) {
            if (equals(_in, "cookie")) {
                this.reportIfInvalid(isValidEnumItem(style, array("form")), node, "style",
                        map("style", style));
            }
        }
    }

}