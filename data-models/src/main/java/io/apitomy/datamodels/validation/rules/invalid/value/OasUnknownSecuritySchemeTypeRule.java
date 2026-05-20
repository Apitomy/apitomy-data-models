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
import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Unknown Security Scheme Type rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnknownSecuritySchemeTypeRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnknownSecuritySchemeTypeRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitSecurityScheme(io.apitomy.datamodels.models.SecurityScheme)
     */
    @Override
    public void visitSecurityScheme(SecurityScheme node) {
        if (hasValue(node.getType())) {
            if (node.root().modelType() == ModelType.OPENAPI20) {
                this.reportIfInvalid(isValidEnumItem(node.getType(), array("apiKey", "basic", "oauth2")), node,
                        "type", map("options", "basic, apiKey, oauth2"));
            } else {
                this.reportIfInvalid(isValidEnumItem(node.getType(), array("apiKey", "http", "oauth2", "openIdConnect")), node,
                        "type", map("options", "http, apiKey, oauth2, openIdConnect"));
            }
        }
    }

}