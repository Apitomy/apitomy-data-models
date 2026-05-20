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
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessageTrait;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AAMTRT-002
 * Validates that message traits only use messageId in AsyncAPI 2.4-2.6.
 * The messageId property was added to message traits in AsyncAPI 2.4 and removed in 3.0,
 * so it should not be used in versions 2.0, 2.1, 2.2, 2.3, or 3.0.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaMessageTraitMessageIdIn24To26Rule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaMessageTraitMessageIdIn24To26Rule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitMessageTrait(AsyncApiMessageTrait node) {
        ModelType modelType = node.root().modelType();

        // Check if this is AsyncAPI 2.0, 2.1, 2.2, or 2.3 (before messageId was added)
        if (modelType == ModelType.ASYNCAPI20 ||
            modelType == ModelType.ASYNCAPI21 ||
            modelType == ModelType.ASYNCAPI22 ||
            modelType == ModelType.ASYNCAPI23) {

            // Check if the message trait has a messageId property
            if (hasValue(node.getExtraProperty("messageId"))) {
                this.reportIf(true, node, "messageId", map());
            }
        }
    }

}
