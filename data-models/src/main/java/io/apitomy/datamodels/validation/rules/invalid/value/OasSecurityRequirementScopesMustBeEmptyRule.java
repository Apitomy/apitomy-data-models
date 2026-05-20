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

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.SecurityRequirement;
import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Security Requirement Scopes Must Be Empty rule.
 * @author eric.wittmann@gmail.com
 */
public class OasSecurityRequirementScopesMustBeEmptyRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasSecurityRequirementScopesMustBeEmptyRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    private SecurityScheme findSecurityScheme(OpenApiDocument document, String schemeName) {
        if (document.root().modelType() == ModelType.OPENAPI20) {
            OpenApi20Document doc20 = (OpenApi20Document) document;
            if (hasValue(doc20.getSecurityDefinitions())) {
                return doc20.getSecurityDefinitions().getItem(schemeName);
            }
        } else if (document.root().modelType() == ModelType.OPENAPI30) {
            OpenApi30Document doc30 = (OpenApi30Document) document;
            if (hasValue(doc30.getComponents()) && hasValue(doc30.getComponents().getSecuritySchemes())) {
                return doc30.getComponents().getSecuritySchemes().get(schemeName);
            }
        } else if (document.root().modelType() == ModelType.OPENAPI31) {
            OpenApi31Document doc31 = (OpenApi31Document) document;
            if (hasValue(doc31.getComponents()) && hasValue(doc31.getComponents().getSecuritySchemes())) {
                return doc31.getComponents().getSecuritySchemes().get(schemeName);
            }
        }

        return null;
    }

    @Override
    public void visitSecurityRequirement(SecurityRequirement node) {
        List<String> allowedTypes = new ArrayList<>();
        allowedTypes.add("oauth2");
        String options = "\"oauth2\"";
        if (node.root().modelType() == ModelType.OPENAPI30 || node.root().modelType() == ModelType.OPENAPI31) {
            allowedTypes.add("openIdConnect");
            options = "\"oauth2\" or \"openIdConnect\"";
        }
        List<String> snames = node.getItemNames();
        for (String sname : snames) {
            SecurityScheme scheme = findSecurityScheme((OpenApiDocument) node.root(), sname);
            if (hasValue(scheme)) {
                if (allowedTypes.indexOf(scheme.getType()) == -1) {
                    List<String> scopes = getScopes(node, sname);
                    this.reportIfInvalid(hasValue(scopes) && scopes.size() == 0, node, null, map("sname", sname, "options", options));
                }
            }
        };
    }

    private List<String> getScopes(SecurityRequirement node, String sname) {
        return node.getItem(sname);
    }

}