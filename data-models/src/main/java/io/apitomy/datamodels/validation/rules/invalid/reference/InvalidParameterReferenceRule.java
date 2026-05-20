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

package io.apitomy.datamodels.validation.rules.invalid.reference;

import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Parameter Reference Rule.
 * Validates that parameter $ref properties point to valid parameter definitions.
 * This rule applies to both OpenAPI and AsyncAPI specifications.
 *
 * Rule Codes:
 * - PAR-018: OpenAPI 2.0, 3.0, 3.1
 *
 * @author eric.wittmann@gmail.com
 */
public class InvalidParameterReferenceRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public InvalidParameterReferenceRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitParameter(Parameter node) {
        String ref = null;

        // Handle both OpenAPI (Referenceable) and AsyncAPI (AsyncApiReferenceable)
        if (node instanceof AsyncApiReferenceable) {
            ref = ((AsyncApiReferenceable) node).get$ref();
        } else if (node instanceof Referenceable) {
            ref = ((Referenceable) node).get$ref();
        }

        if (hasValue(ref)) {
            this.reportIfInvalid(ReferenceUtil.canResolveRef(ref, node), node, "$ref", map());
        }
    }

}
