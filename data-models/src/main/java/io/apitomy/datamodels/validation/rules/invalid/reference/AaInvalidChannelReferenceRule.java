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

import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannel;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannelItem;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: CHAN-003
 * Validates that Channel $ref property points to a valid channel definition in components.
 * Applies to both AsyncAPI 2.x (AsyncApiChannelItem) and 3.x (AsyncApi3xChannel).
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidChannelReferenceRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaInvalidChannelReferenceRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitChannelItem(AsyncApiChannelItem node) {
        if (node instanceof Referenceable) {
            String ref = ((Referenceable) node).get$ref();
            if (hasValue(ref)) {
                this.reportIfInvalid(ReferenceUtil.canResolveRef(ref, node), node, "$ref", map());
            }
        }
    }

    @Override
    public void visitChannel(AsyncApiChannel node) {
        String ref = ((Referenceable) node).get$ref();
        if (hasValue(ref)) {
            this.reportIfInvalid(ReferenceUtil.canResolveRef(ref, node), node, "$ref", map());
        }
    }

}
