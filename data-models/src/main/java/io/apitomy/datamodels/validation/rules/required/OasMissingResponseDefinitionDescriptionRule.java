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

import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasMissingResponseDefinitionDescriptionRule extends RequiredPropertyValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasMissingResponseDefinitionDescriptionRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitResponse(io.apitomy.datamodels.models.openapi.OpenApiResponse)
     */
    @Override
    public void visitResponse(OpenApiResponse node) {
        if (hasValue(NodeUtil.getProperty(node, "$ref"))) {
            return;
        }
        if (isDefinition(node)) {
            String responseName = getMappedNodeName(node);
            this.requireProperty(node, "description", map("name", responseName));
        }
    }

}