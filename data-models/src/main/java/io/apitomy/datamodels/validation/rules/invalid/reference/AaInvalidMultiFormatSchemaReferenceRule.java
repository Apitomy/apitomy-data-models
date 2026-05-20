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

import io.apitomy.datamodels.models.asyncapi.AsyncApiMultiFormatSchema;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AAMFS-003
 * Validates that a MultiFormatSchema's $ref property points to a valid schema definition in components.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidMultiFormatSchemaReferenceRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaInvalidMultiFormatSchemaReferenceRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitMultiFormatSchema(AsyncApiMultiFormatSchema node) {
        if (node instanceof AsyncApiReferenceable) {
            String ref = ((AsyncApiReferenceable) node).get$ref();
            if (hasValue(ref)) {
                this.reportIfInvalid(ReferenceUtil.canResolveRef(ref, node), node, "$ref", map());
            }
        }
    }

}
