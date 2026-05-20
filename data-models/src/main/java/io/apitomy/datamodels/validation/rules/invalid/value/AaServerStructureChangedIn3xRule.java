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

import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: SRV-012
 * Validates that AsyncAPI 3.x servers use the new structure.
 * In AsyncAPI 2.x, servers used a single 'url' property.
 * In AsyncAPI 3.x, this was replaced with separate 'host', 'pathname', and 'protocol' properties.
 * This rule detects when the old 'url' property is used in a 3.x document.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaServerStructureChangedIn3xRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaServerStructureChangedIn3xRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitServer(Server node) {
        if (ModelTypeUtil.isAsyncApi3Model(node)) {
            // Check if the server has the old 'url' property
            if (node.getExtraProperty("url") != null) {
                this.reportIf(true, node, "url", map());
            }
        }
    }

}
