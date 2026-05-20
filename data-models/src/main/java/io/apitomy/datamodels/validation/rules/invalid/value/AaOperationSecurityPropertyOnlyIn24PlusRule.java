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
import io.apitomy.datamodels.models.asyncapi.v2x.v20.AsyncApi20Operation;
import io.apitomy.datamodels.models.asyncapi.v2x.v21.AsyncApi21Operation;
import io.apitomy.datamodels.models.asyncapi.v2x.v22.AsyncApi22Operation;
import io.apitomy.datamodels.models.asyncapi.v2x.v23.AsyncApi23Operation;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that the operation security property is only used in AsyncAPI 2.4+.
 * The security property was added in AsyncAPI 2.4.
 * @author eric.wittmann@gmail.com
 */
public class AaOperationSecurityPropertyOnlyIn24PlusRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaOperationSecurityPropertyOnlyIn24PlusRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitOperation(io.apitomy.datamodels.models.Operation)
     */
    @Override
    public void visitOperation(Operation node) {
        // Check if this is AsyncAPI 2.0-2.3
        if (node instanceof AsyncApi20Operation ||
            node instanceof AsyncApi21Operation ||
            node instanceof AsyncApi22Operation ||
            node instanceof AsyncApi23Operation) {

            // In 2.0-2.3, the security property doesn't exist on the interface,
            // but it might be present as an extra property
            if (node.getExtraProperty("security") != null) {
                this.reportIf(true, node, "security", map());
            }
        }
    }

}
