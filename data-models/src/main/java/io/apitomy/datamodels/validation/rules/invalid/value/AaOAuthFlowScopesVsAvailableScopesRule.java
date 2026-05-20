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
import io.apitomy.datamodels.models.OAuthFlow;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AAFLOW-001
 * Validates that OAuth flows use the correct property name for scopes based on AsyncAPI version.
 * In AsyncAPI 2.x, OAuth flows use the 'scopes' property.
 * In AsyncAPI 3.x, OAuth flows use the 'availableScopes' property.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaOAuthFlowScopesVsAvailableScopesRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaOAuthFlowScopesVsAvailableScopesRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitOAuthFlow(OAuthFlow node) {
        if (ModelTypeUtil.isAsyncApiModel(node)) {
            ModelType modelType = node.root().modelType();

            // In AsyncAPI 2.x, should use 'scopes', not 'availableScopes'
            if (ModelTypeUtil.isAsyncApi2Model(node)) {
                Object availableScopes = node.getExtraProperty("availableScopes");
                if (hasValue(availableScopes)) {
                    this.reportIf(true, node, "availableScopes", map());
                }
            }

            // In AsyncAPI 3.x, should use 'availableScopes', not 'scopes'
            if (ModelTypeUtil.isAsyncApi3Model(node)) {
                Object scopes = node.getExtraProperty("scopes");
                if (hasValue(scopes)) {
                    this.reportIf(true, node, "scopes", map());
                }
            }
        }
    }

}
