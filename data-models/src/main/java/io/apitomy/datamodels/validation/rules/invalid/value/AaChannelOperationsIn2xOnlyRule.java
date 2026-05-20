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

import io.apitomy.datamodels.models.asyncapi.AsyncApiChannel;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: CHAN-009
 * Validates that subscribe/publish operations only exist in AsyncAPI 2.x channels.
 * In AsyncAPI 3.x, channels no longer have subscribe/publish operations.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaChannelOperationsIn2xOnlyRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaChannelOperationsIn2xOnlyRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitChannel(AsyncApiChannel node) {
        // In AsyncAPI 3.x, channels should not have subscribe/publish operations
        // These would appear as extra properties if someone incorrectly added them
        if (node.getExtraProperty("subscribe") != null) {
            this.reportIf(true, node, "subscribe", map("property", "subscribe"));
        }
        if (node.getExtraProperty("publish") != null) {
            this.reportIf(true, node, "publish", map("property", "publish"));
        }
    }

}
