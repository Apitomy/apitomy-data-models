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

package io.apitomy.datamodels.validation.rules.invalid.format;

import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Parameter Description Rule.
 * Validates that parameter descriptions are valid GitHub-flavored markdown.
 * This rule applies to both OpenAPI and AsyncAPI specifications.
 *
 * Rule Codes:
 * - PAR-010: OpenAPI 2.0, 3.0, 3.1
 * - AAPARAM-003: AsyncAPI 2.0-2.6, 3.0
 *
 * @author eric.wittmann@gmail.com
 */
public class InvalidParameterDescriptionRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public InvalidParameterDescriptionRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitParameter(Parameter node) {
        if (hasValue(node.getDescription())) {
            this.reportIfInvalid(isValidGFM(node.getDescription()), node, "description", map());
        }
    }

}
