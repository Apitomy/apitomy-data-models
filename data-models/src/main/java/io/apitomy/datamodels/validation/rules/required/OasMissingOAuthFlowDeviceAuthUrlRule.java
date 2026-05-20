/*
 * Copyright 2024 Red Hat
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

import io.apitomy.datamodels.models.OAuthFlow;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that the Device Authorization OAuth flow has a deviceAuthorizationUrl.
 *
 * @author eric.wittmann@gmail.com
 */
public class OasMissingOAuthFlowDeviceAuthUrlRule extends RequiredPropertyValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasMissingOAuthFlowDeviceAuthUrlRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitOAuthFlow(io.apitomy.datamodels.models.OAuthFlow)
     */
    @Override
    public void visitOAuthFlow(OAuthFlow node) {
        String flowType = getMappedNodeName(node);
        if ("deviceAuthorization".equals(flowType)) {
            this.requireProperty(node, "deviceAuthorizationUrl", map());
        }
    }

}
