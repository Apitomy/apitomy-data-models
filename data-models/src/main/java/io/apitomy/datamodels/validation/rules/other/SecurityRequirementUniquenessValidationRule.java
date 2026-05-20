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

package io.apitomy.datamodels.validation.rules.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.apitomy.datamodels.models.SecurityRequirement;
import io.apitomy.datamodels.models.SecurityRequirementsParent;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * @author cfoskin@redhat.com
 */
public class SecurityRequirementUniquenessValidationRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public SecurityRequirementUniquenessValidationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @param currentSecurityRequirement
     * @param otherSecurityRequirement
     * @return returns true if the two security requirements are equal
     */
    private static boolean isEqualTo(SecurityRequirement currentSecurityRequirement,
            SecurityRequirement otherSecurityRequirement) {
        // compare security requirement names
        List<String> currentSecurityRequirementNames = currentSecurityRequirement.getItemNames();
        List<String> otherSecurityRequirementNames = otherSecurityRequirement.getItemNames();
        Collections.sort(currentSecurityRequirementNames);
        Collections.sort(otherSecurityRequirementNames);

        if (!currentSecurityRequirementNames.equals(otherSecurityRequirementNames)) {
            return false;
        }

        // get and compare the scopes for each of the sec req names that are the same
        for (String secReqName : currentSecurityRequirementNames) {
            List<String> secReqScopes = currentSecurityRequirement.getItem(secReqName);
            List<String> otherSecReqScopes = otherSecurityRequirement.getItem(secReqName);
            Collections.sort(secReqScopes);
            Collections.sort(otherSecReqScopes);

            if (!secReqScopes.equals(otherSecReqScopes)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param currentSecurityRequirement
     * @param allRequirements
     * @return true if duplicates are found
     */
    private static boolean checkForDuplicates(SecurityRequirement currentSecurityRequirement,
            List<? extends SecurityRequirement> allRequirements) {
        ArrayList<SecurityRequirement> clonedList = new ArrayList<SecurityRequirement>(allRequirements);
        clonedList.remove(currentSecurityRequirement); // remove the current one to examine siblings

        for (SecurityRequirement otherSecurityRequirement : clonedList) {
            if (isEqualTo(currentSecurityRequirement, otherSecurityRequirement)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitSecurityRequirement(io.apitomy.datamodels.models.SecurityRequirement)
     */
    @Override
    public void visitSecurityRequirement(SecurityRequirement node) {
        SecurityRequirementsParent parent = (SecurityRequirementsParent) node.parent();
        List<? extends SecurityRequirement> securityRequirements = parent.getSecurity();

        if (securityRequirements.size() <= 1) {
            return;
        }

        if (checkForDuplicates(node, securityRequirements)) {
            this.report(node, "security", map("securityReq", node.getItemNames().toString()));
        }
    }
}