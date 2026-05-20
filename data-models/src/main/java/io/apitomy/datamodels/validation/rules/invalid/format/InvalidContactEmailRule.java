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

import io.apitomy.datamodels.models.Contact;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Contact Email Rule
 * @author eric.wittmann@gmail.com
 */
public class InvalidContactEmailRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public InvalidContactEmailRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitContact(io.apitomy.datamodels.models.Contact)
     */
    @Override
    public void visitContact(Contact node) {
        if (hasValue(node.getEmail())) {
            this.reportIfInvalid(isValidEmailAddress(node.getEmail()), node, "email", map());
        }
    }

}
