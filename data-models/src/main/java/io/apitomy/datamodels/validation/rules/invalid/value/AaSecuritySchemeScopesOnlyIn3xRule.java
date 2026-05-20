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

import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AASS-002
 * Validates that the 'scopes' property is only used in security schemes in AsyncAPI 3.x.
 * In AsyncAPI 2.x, scopes are defined within individual OAuth flows, not at the security scheme level.
 * In AsyncAPI 3.x, scopes can be defined directly on the security scheme.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaSecuritySchemeScopesOnlyIn3xRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaSecuritySchemeScopesOnlyIn3xRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitSecurityScheme(SecurityScheme node) {
        if (!ModelTypeUtil.isAsyncApi3Model(node)) {
            Object scopes = node.getExtraProperty("scopes");
            if (hasValue(scopes)) {
                this.reportIf(true, node, "scopes", map("version", ModelTypeUtil.getVersion(node.root().modelType())));
            }
        }
    }

}
