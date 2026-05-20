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

import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.OpenApiXML;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;
import io.apitomy.datamodels.validation.rules.invalid.type.OasInvalidPropertyTypeValidationRule;

/**
 * Implements the Unexpected XML Wrapping rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnexpectedXmlWrappingRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnexpectedXmlWrappingRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitXML(io.apitomy.datamodels.models.openapi.OpenApiXML)
     */
    @Override
    public void visitXML(OpenApiXML node) {
        if (hasValue(node.isWrapped())) {
            OasInvalidPropertyTypeValidationRule.getTypes((Schema) node.parent(), (types, allowedTypes) ->
                this.reportIfInvalid(types.contains("array"), node, "wrapped", map()));
        }
    }

}