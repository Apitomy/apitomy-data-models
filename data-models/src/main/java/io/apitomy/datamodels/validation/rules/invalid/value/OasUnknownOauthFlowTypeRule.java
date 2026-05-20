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

import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20SecurityScheme;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Unknown OAuth Flow Type rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnknownOauthFlowTypeRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnknownOauthFlowTypeRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitSecurityScheme(io.apitomy.datamodels.models.SecurityScheme)
     */
    @Override
    public void visitSecurityScheme(SecurityScheme node) {
        OpenApi20SecurityScheme scheme = (OpenApi20SecurityScheme) node;
        if (hasValue(scheme.getFlow())) {
            this.reportIfInvalid(isValidEnumItem(scheme.getFlow(), array("implicit", "password", "application", "accessCode")),
                    node, "flow", map());
        }
    }

}