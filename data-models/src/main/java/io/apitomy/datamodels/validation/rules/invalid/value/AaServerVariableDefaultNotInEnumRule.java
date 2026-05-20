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

import io.apitomy.datamodels.models.ServerVariable;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.List;

/**
 * Implements the SVAR-004 rule: Server Variable Default Not in Enum.
 * Validates that if a server variable has both a default value and an enum,
 * the default value must be one of the enum values.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaServerVariableDefaultNotInEnumRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaServerVariableDefaultNotInEnumRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitServerVariable(ServerVariable)
     */
    @Override
    public void visitServerVariable(ServerVariable node) {
        String defaultValue = node.getDefault();
        List<String> enumValues = node.getEnum();

        if (hasValue(defaultValue) && hasValue(enumValues)) {
            boolean isValid = enumValues.contains(defaultValue);
            this.reportIfInvalid(isValid, node, "default", map());
        }
    }

}
