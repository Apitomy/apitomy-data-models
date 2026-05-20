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

package io.apitomy.datamodels.validation.rules.invalid.reference;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.SecurityRequirement;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Security Requirement Name rule.
 * @author eric.wittmann@gmail.com
 */
public class OasInvalidSecurityRequirementNameRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidSecurityRequirementNameRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Returns true if the security requirement name is valid.  It does this by looking up a declared
     * security scheme definition in the document.  If no security scheme definition exists with the
     * given name, then it is invalid.
     * @param securityReqName
     * @param doc
     */
    private boolean isValidSecurityRequirementName(String securityReqName, Document doc) {
        ModelType dt = doc.root().modelType();
        if (dt == ModelType.OPENAPI20) {
            OpenApi20Document doc20 = (OpenApi20Document) doc;
            return hasValue(doc20.getSecurityDefinitions()) && isDefined(doc20.getSecurityDefinitions().getItem(securityReqName));
        } else if (dt == ModelType.OPENAPI30) {
            OpenApi30Document doc30 = (OpenApi30Document) doc;
            return hasValue(doc30.getComponents()) &&
                    isDefined(doc30.getComponents()) &&
                    isDefined(doc30.getComponents().getSecuritySchemes().get(securityReqName));
        } else if (dt == ModelType.OPENAPI31) {
            OpenApi31Document doc31 = (OpenApi31Document) doc;
            return hasValue(doc31.getComponents()) &&
                    isDefined(doc31.getComponents()) &&
                    isDefined(doc31.getComponents().getSecuritySchemes().get(securityReqName));
        } else if (dt == ModelType.ASYNCAPI20 || dt == ModelType.ASYNCAPI21 || dt == ModelType.ASYNCAPI22 || dt == ModelType.ASYNCAPI23 || dt == ModelType.ASYNCAPI24 || dt == ModelType.ASYNCAPI25 || dt == ModelType.ASYNCAPI26) {
            // TODO implement this
        }
        return false;
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitSecurityRequirement(io.apitomy.datamodels.models.SecurityRequirement)
     */
    @Override
    public void visitSecurityRequirement(SecurityRequirement node) {
        node.getItemNames().forEach( name -> {
            this.reportIfInvalid(isValidSecurityRequirementName(name, (Document) node.root()), node, null, map("name", name));
        });
    }

}
