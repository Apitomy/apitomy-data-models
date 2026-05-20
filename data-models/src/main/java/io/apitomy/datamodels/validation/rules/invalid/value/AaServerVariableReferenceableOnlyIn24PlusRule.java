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

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.ServerVariable;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: SVAR-006
 * Validates that server variables can only use $ref starting in AsyncAPI 2.4.
 * Server variables gained the Referenceable trait in AsyncAPI 2.4, so using $ref
 * in versions 2.0, 2.1, 2.2, or 2.3 is invalid.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaServerVariableReferenceableOnlyIn24PlusRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaServerVariableReferenceableOnlyIn24PlusRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitServerVariable(ServerVariable node) {
        ModelType modelType = node.root().modelType();

        // Check if this is AsyncAPI 2.0, 2.1, 2.2, or 2.3 (before server variables became referenceable)
        if (modelType == ModelType.ASYNCAPI20 ||
            modelType == ModelType.ASYNCAPI21 ||
            modelType == ModelType.ASYNCAPI22 ||
            modelType == ModelType.ASYNCAPI23) {

            // Check if the server variable has a $ref property
            if (node.getExtraProperty("$ref") != null) {
                this.reportIf(true, node, "$ref", map());
            }
        }
    }

}
