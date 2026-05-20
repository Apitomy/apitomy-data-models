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

package io.apitomy.datamodels.validation.rules.invalid.type;

import java.util.List;

import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Schema Array Items rule.
 * @author eric.wittmann@gmail.com
 */
public class OasInvalidSchemaArrayItemsRule extends OasInvalidPropertyTypeValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidSchemaArrayItemsRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitSchema(Schema node) {
        Schema items = (Schema) NodeUtil.getNodeProperty(node, "items");

        if (isDefined(items)) {
            getTypes(node, (types, allowedTypes) -> {
                List<String> allowedItemTypes = List.of("array", "null");

                for (String type : types) {
                    if (!allowedItemTypes.contains(type)) {
                        this.report(node, "items", map());
                        break;
                    }
                }
            });
        }
    }

}
