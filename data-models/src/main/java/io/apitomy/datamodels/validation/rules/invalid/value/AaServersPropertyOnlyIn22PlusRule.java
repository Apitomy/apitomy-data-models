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
import io.apitomy.datamodels.models.asyncapi.v2x.v20.AsyncApi20ChannelItem;
import io.apitomy.datamodels.models.asyncapi.v2x.v21.AsyncApi21ChannelItem;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: CHAN-006
 * Validates that the 'servers' property in channels only exists starting in AsyncAPI 2.2.
 * In AsyncAPI 2.0 and 2.1, channels do not have a 'servers' property.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaServersPropertyOnlyIn22PlusRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaServersPropertyOnlyIn22PlusRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitChannelItem(AsyncApiChannelItem node) {
        // Check if this is a 2.0 or 2.1 channel that has servers property
        if (node instanceof AsyncApi20ChannelItem || node instanceof AsyncApi21ChannelItem) {
            // In 2.0 and 2.1, the servers property doesn't exist on the interface,
            // but it might be present as an extra property
            if (node.getExtraProperty("servers") != null) {
                this.reportIf(true, node, "servers", map());
            }
        }
    }

}
