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

package io.apitomy.datamodels.validation.rules.invalid.format;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.models.asyncapi.AsyncApiSecurityScheme;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xSecurityScheme;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30SecurityScheme;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31SecurityScheme;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid OpenId Connect URL Rule
 * @author eric.wittmann@gmail.com
 */
public class OasInvalidOpenIDConnectUrlRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidOpenIDConnectUrlRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitSecurityScheme(SecurityScheme node) {
        if (ModelTypeUtil.isOpenApi3Model(node)) {
            OpenApi3xSecurityScheme scheme = (OpenApi3xSecurityScheme) node;
            if (hasValue(scheme.getOpenIdConnectUrl())) {
                this.reportIfInvalid(isValidUrl(scheme.getOpenIdConnectUrl()), scheme, "openIdConnectUrl", map());
            }
        } else {
            AsyncApiSecurityScheme scheme = (AsyncApiSecurityScheme) node;
            if (hasValue(scheme.getOpenIdConnectUrl())) {
                this.reportIfInvalid(isValidUrl(scheme.getOpenIdConnectUrl()), scheme, "openIdConnectUrl", map());
            }
        }

    }

}
