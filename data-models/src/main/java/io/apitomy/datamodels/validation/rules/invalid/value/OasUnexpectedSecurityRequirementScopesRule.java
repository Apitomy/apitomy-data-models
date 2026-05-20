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

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.models.SecurityRequirement;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Scopes;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20SecurityDefinitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20SecurityScheme;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Unexpected Security Requirement Scope(s) rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnexpectedSecurityRequirementScopesRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnexpectedSecurityRequirementScopesRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Returns true if the given required scopes are all actually defined by the security definition.
     * @param requiredScopes
     * @param definedScopes
     */
    protected boolean isValidScopes(List<String> requiredScopes, OpenApi20Scopes definedScopes) {
        boolean rval = true;
        List<String> dscopes = new ArrayList<>();
        if (hasValue(definedScopes)) {
            dscopes = definedScopes.getItemNames();
        }
        for (String requiredScope : requiredScopes) {
            if (dscopes.indexOf(requiredScope) == -1) {
                rval = false;
            }
        }
        return rval;
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitSecurityRequirement(io.apitomy.datamodels.models.SecurityRequirement)
     */
    @Override
    public void visitSecurityRequirement(SecurityRequirement node) {
        List<String> snames = node.getItemNames();
        snames.forEach( sname -> {
            OpenApi20SecurityDefinitions sdefs = (OpenApi20SecurityDefinitions) ((OpenApi20Document) node.root()).getSecurityDefinitions();
            if (hasValue(sdefs)) {
                OpenApi20SecurityScheme scheme = (OpenApi20SecurityScheme) sdefs.getItem(sname);
                if (hasValue(scheme)) {
                    if (equals(scheme.getType(), "oauth2")) {
                        OpenApi20Scopes definedScopes = (OpenApi20Scopes) scheme.getScopes();
                        List<String> requiredScopes = node.getItem(sname);
                        this.reportIfInvalid(isValidScopes(requiredScopes, definedScopes), node, null, map("sname", sname));
                    }
                }
            }
        });
    }
}