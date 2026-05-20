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

import java.util.Map;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Base class for all Required Property rules.
 * @author eric.wittmann@gmail.com
 */
public abstract class RequiredPropertyValidationRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public RequiredPropertyValidationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Called when a required property is missing.
     * @param node
     * @param propertyName
     * @param messageParams
     */
    protected void requireProperty(Node node, String propertyName, Map<String, String> messageParams) {
        Object propertyValue = NodeUtil.getProperty(node, propertyName);
        if (!isDefined(propertyValue)) {
            this.report(node, propertyName, messageParams);
        }
    }

    /**
     * Called when a conditionally required property is missing.
     * @param node
     * @param propertyName
     * @param dependentPropertyName
     * @param dependentPropertyExpectedValue
     * @param messageParams
     */
    protected void requirePropertyWhen(Node node, String propertyName, String dependentPropertyName,
            Object dependentPropertyExpectedValue, Map<String, String> messageParams) {
        Object dependentPropertyActualValue = NodeUtil.getProperty(node, dependentPropertyName);
        if (NodeUtil.equals(dependentPropertyActualValue, dependentPropertyExpectedValue)) {
            this.requireProperty(node, propertyName, messageParams);
        }
    }

}
