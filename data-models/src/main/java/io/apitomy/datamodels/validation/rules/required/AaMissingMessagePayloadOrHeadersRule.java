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

import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apitomy.datamodels.models.asyncapi.v2x.AsyncApi2xMessage;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xMessage;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Missing Message Payload or Headers rule for AsyncAPI.
 * Messages should have either payload or headers defined.
 * @author eric.wittmann@gmail.com
 */
public class AaMissingMessagePayloadOrHeadersRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaMissingMessagePayloadOrHeadersRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitMessage(io.apitomy.datamodels.models.asyncapi.AsyncApiMessage)
     */
    @Override
    public void visitMessage(AsyncApiMessage node) {
        // Skip if it's a reference
        if (node instanceof AsyncApiReferenceable && hasValue(((AsyncApiReferenceable) node).get$ref())) {
            return;
        }

        // Check for payload or headers based on version
        Object payload = null;
        Object headers = null;

        if (ModelTypeUtil.isAsyncApi2Model(node)) {
            payload = ((AsyncApi2xMessage) node).getPayload();
            headers = ((AsyncApi2xMessage) node).getHeaders();
        } else if (ModelTypeUtil.isAsyncApi3Model(node)) {
            payload = ((AsyncApi3xMessage) node).getPayload();
            headers = ((AsyncApi3xMessage) node).getHeaders();
        }

        // Message should have at least one of payload or headers
        if (!hasValue(payload) && !hasValue(headers)) {
            this.report(node, null, map("property", "payload or headers"));
        }
    }

}
