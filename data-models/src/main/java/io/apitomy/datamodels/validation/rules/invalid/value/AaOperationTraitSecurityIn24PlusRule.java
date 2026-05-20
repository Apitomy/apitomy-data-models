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

import io.apitomy.datamodels.models.asyncapi.AsyncApiOperationTrait;
import io.apitomy.datamodels.models.asyncapi.v2x.v20.AsyncApi20OperationTrait;
import io.apitomy.datamodels.models.asyncapi.v2x.v21.AsyncApi21OperationTrait;
import io.apitomy.datamodels.models.asyncapi.v2x.v22.AsyncApi22OperationTrait;
import io.apitomy.datamodels.models.asyncapi.v2x.v23.AsyncApi23OperationTrait;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that the operation trait security property is only used in AsyncAPI 2.4+.
 * The security property was added to operation traits in AsyncAPI 2.4.
 * @author eric.wittmann@gmail.com
 */
public class AaOperationTraitSecurityIn24PlusRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaOperationTraitSecurityIn24PlusRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitOperationTrait(io.apitomy.datamodels.models.asyncapi.AsyncApiOperationTrait)
     */
    @Override
    public void visitOperationTrait(AsyncApiOperationTrait node) {
        // Check if this is AsyncAPI 2.0-2.3
        if (node instanceof AsyncApi20OperationTrait ||
            node instanceof AsyncApi21OperationTrait ||
            node instanceof AsyncApi22OperationTrait ||
            node instanceof AsyncApi23OperationTrait) {

            // In 2.0-2.3, the security property doesn't exist on operation traits,
            // but it might be present as an extra property
            if (node.getExtraProperty("security") != null) {
                this.reportIf(true, node, "security", map());
            }
        }
    }

}
