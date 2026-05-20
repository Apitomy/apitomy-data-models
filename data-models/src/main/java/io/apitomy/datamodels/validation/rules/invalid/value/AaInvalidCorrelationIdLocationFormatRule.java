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

import io.apitomy.datamodels.models.asyncapi.AsyncApiCorrelationID;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AACID-002
 * Validates that correlation ID location is a valid runtime expression.
 * Runtime expressions in AsyncAPI must start with '$'.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidCorrelationIdLocationFormatRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaInvalidCorrelationIdLocationFormatRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitCorrelationID(AsyncApiCorrelationID node) {
        // Skip if it's a reference
        if (hasRef(node)) {
            return;
        }

        // Validate location if present
        String location = node.getLocation();
        if (hasValue(location)) {
            // Runtime expressions must start with $
            boolean isValid = location.startsWith("$");
            this.reportIfInvalid(isValid, node, "location", map());
        }
    }

}
