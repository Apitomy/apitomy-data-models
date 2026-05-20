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

import io.apitomy.datamodels.models.asyncapi.AsyncApiChannelItem;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: CHAN-008
 * Validates that the 'messages' property only exists in AsyncAPI 3.x channels.
 * In AsyncAPI 2.x, channels use subscribe/publish operations instead of messages.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaChannelMessagesOnlyIn3xRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaChannelMessagesOnlyIn3xRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitChannelItem(AsyncApiChannelItem node) {
        // In AsyncAPI 2.x, channels should not have a messages property
        // This would appear as an extra property if someone incorrectly added it
        if (node.getExtraProperty("messages") != null) {
            this.reportIf(true, node, "messages", map());
        }
    }

}
