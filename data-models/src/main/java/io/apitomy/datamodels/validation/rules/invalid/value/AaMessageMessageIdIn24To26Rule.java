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
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AAM-014
 * Validates that messages only use messageId in AsyncAPI 2.4-2.6.
 * The messageId property was added to messages in AsyncAPI 2.4 and removed in 3.0,
 * so it should not be used in versions 2.0, 2.1, 2.2, 2.3, or 3.0.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaMessageMessageIdIn24To26Rule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaMessageMessageIdIn24To26Rule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitMessage(AsyncApiMessage node) {
        ModelType modelType = node.root().modelType();

        // Check if this is AsyncAPI 2.0, 2.1, 2.2, or 2.3 (before messageId was added)
        if (modelType == ModelType.ASYNCAPI20 ||
            modelType == ModelType.ASYNCAPI21 ||
            modelType == ModelType.ASYNCAPI22 ||
            modelType == ModelType.ASYNCAPI23) {

            // Check if the message has a messageId property
            if (hasValue(node.getExtraProperty("messageId"))) {
                this.reportIf(true, node, "messageId", map());
            }
        }
    }

}
