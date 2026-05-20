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

import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AATAG-001
 * Validates that tag references point to valid tags in components.
 * In AsyncAPI 3.x, tags can be referenced using $ref.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidTagReferenceRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaInvalidTagReferenceRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitTag(Tag node) {
        if (ModelTypeUtil.isAsyncApi3Model(node)) {
            if (node instanceof AsyncApiReferenceable) {
                String ref = ((AsyncApiReferenceable) node).get$ref();
                if (hasValue(ref)) {
                    this.reportIfInvalid(ReferenceUtil.canResolveRef(ref, node), node, "$ref", map());
                }
            }
        }
    }

}
