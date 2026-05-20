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

import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AAM-013
 * Validates that messages do not use messageId in AsyncAPI 3.x.
 * The messageId property was removed from messages in AsyncAPI 3.x,
 * so it should not be used.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaMessageNoMessageIdIn3xRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaMessageNoMessageIdIn3xRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitMessage(AsyncApiMessage node) {
        // Check if this is AsyncAPI 3.x (where messageId was removed)
        if (ModelTypeUtil.isAsyncApi3Model(node)) {
            // Check if the message has a messageId property
            if (hasValue(node.getExtraProperty("messageId"))) {
                this.reportIf(true, node, "messageId", map());
            }
        }
    }

}
