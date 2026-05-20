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

package io.apitomy.datamodels.validation.rules.required;

import io.apitomy.datamodels.models.asyncapi.AsyncApiChannel;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: CHAN-001
 * Validates that AsyncAPI 3.x Channel entities have the required 'address' property.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaMissingChannelAddressRule extends RequiredPropertyValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaMissingChannelAddressRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitChannel(AsyncApiChannel node) {
        this.requireProperty(node, "address", map());
    }

}
