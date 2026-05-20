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

package io.apitomy.datamodels.validation.rules.invalid.name;

import io.apitomy.datamodels.models.openapi.OpenApiRequestBody;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Request Body Definition Name Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasInvalidRequestBodyDefinitionNameRule extends OasInvalidPropertyNameRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidRequestBodyDefinitionNameRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitRequestBody(io.apitomy.datamodels.models.openapi.OpenApiRequestBody)
     */
    @Override
    public void visitRequestBody(OpenApiRequestBody node) {
        if (isDefinition(node)) {
            String name = getDefinitionName(node);
            this.reportIfInvalid(isValidDefinitionName(name), node, "name", map());
        }
    }

}
