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

package io.apitomy.datamodels.validation.rules.mutex;

import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.models.asyncapi.v2x.v20.AsyncApi20Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v21.AsyncApi21Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v22.AsyncApi22Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v23.AsyncApi23Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v24.AsyncApi24Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v25.AsyncApi25Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v26.AsyncApi26Message;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.List;

/**
 * Implements the AAM-009 rule: Message OneOf Mutually Exclusive.
 * When a message has oneOf, it should not have payload, headers, or other message properties.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaMessageOneOfMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaMessageOneOfMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitMessage(io.apitomy.datamodels.models.asyncapi.AsyncApiMessage)
     */
    @Override
    public void visitMessage(AsyncApiMessage node) {
        List<? extends AsyncApiMessage> oneOf = getOneOf(node);

        if (hasValue(oneOf)) {
            // When oneOf is present, check for mutually exclusive properties
            this.reportIf(hasValue(NodeUtil.getNodeProperty(node, "payload")), node, "oneOf", map("property", "payload"));
            this.reportIf(hasValue(NodeUtil.getNodeProperty(node, "headers")), node, "oneOf", map("property", "headers"));
            this.reportIf(hasValue(node.getCorrelationId()), node, "oneOf", map("property", "correlationId"));
            this.reportIf(hasValue(node.getTraits()), node, "oneOf", map("property", "traits"));
        }
    }

    /**
     * Gets the oneOf list from a message based on version.
     */
    private List<? extends AsyncApiMessage> getOneOf(AsyncApiMessage message) {
        if (message instanceof AsyncApi20Message) {
            return ((AsyncApi20Message) message).getOneOf();
        } else if (message instanceof AsyncApi21Message) {
            return ((AsyncApi21Message) message).getOneOf();
        } else if (message instanceof AsyncApi22Message) {
            return ((AsyncApi22Message) message).getOneOf();
        } else if (message instanceof AsyncApi23Message) {
            return ((AsyncApi23Message) message).getOneOf();
        } else if (message instanceof AsyncApi24Message) {
            return ((AsyncApi24Message) message).getOneOf();
        } else if (message instanceof AsyncApi25Message) {
            return ((AsyncApi25Message) message).getOneOf();
        } else if (message instanceof AsyncApi26Message) {
            return ((AsyncApi26Message) message).getOneOf();
        }
        return null;
    }

}
