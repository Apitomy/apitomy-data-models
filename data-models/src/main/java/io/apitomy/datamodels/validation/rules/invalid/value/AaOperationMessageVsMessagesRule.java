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

import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that operations use the correct message property for their version.
 * AsyncAPI 2.x uses singular 'message', while 3.0 uses plural 'messages'.
 * @author eric.wittmann@gmail.com
 */
public class AaOperationMessageVsMessagesRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaOperationMessageVsMessagesRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitOperation(io.apitomy.datamodels.models.Operation)
     */
    @Override
    public void visitOperation(Operation node) {
        // AsyncAPI 2.x should use 'message', not 'messages'
        if (ModelTypeUtil.isAsyncApi2Model(node)) {
            if (node.getExtraProperty("messages") != null) {
                this.reportIf(true, node, "messages", map());
            }
        }

        // AsyncAPI 3.x should use 'messages', not 'message'
        if (ModelTypeUtil.isAsyncApi3Model(node)) {
            if (node.getExtraProperty("message") != null) {
                this.reportIf(true, node, "message", map());
            }
        }
    }

}
