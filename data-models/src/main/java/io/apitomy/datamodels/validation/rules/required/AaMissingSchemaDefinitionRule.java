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

import io.apitomy.datamodels.models.asyncapi.AsyncApiMultiFormatSchema;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Missing Schema Definition rule for AsyncAPI 3.x MultiFormatSchema.
 * Rule: AAMFS-002
 * A multi-format schema must have a schema property defined (unless it has a $ref).
 * @author eric.wittmann@gmail.com
 */
public class AaMissingSchemaDefinitionRule extends RequiredPropertyValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaMissingSchemaDefinitionRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitMultiFormatSchema(AsyncApiMultiFormatSchema node) {
        // Skip if it's a reference
        if (node instanceof AsyncApiReferenceable && hasValue(((AsyncApiReferenceable) node).get$ref())) {
            return;
        }
        this.requireProperty(node, "schema", map());
    }

}
