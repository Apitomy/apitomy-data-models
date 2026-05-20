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

package io.apitomy.datamodels.validation.rules.invalid.reference;

import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.asyncapi.AsyncApiCorrelationID;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Correlation ID Reference rule for AsyncAPI.
 * Correlation ID references must point to valid correlation ID definitions.
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidCorrelationIdReferenceRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaInvalidCorrelationIdReferenceRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitMessage(io.apitomy.datamodels.models.asyncapi.AsyncApiMessage)
     */
    @Override
    public void visitMessage(AsyncApiMessage node) {
        // Skip if message itself is a reference
        if (node instanceof AsyncApiReferenceable && hasValue(((AsyncApiReferenceable) node).get$ref())) {
            return;
        }

        // Check correlationId reference
        AsyncApiCorrelationID correlationId = node.getCorrelationId();
        if (hasValue(correlationId) && correlationId instanceof Referenceable) {
            String ref = ((Referenceable) correlationId).get$ref();
            if (hasValue(ref)) {
                this.reportIfInvalid(ReferenceUtil.canResolveRef(ref, correlationId), node, "correlationId", map());
            }
        }
    }

}
