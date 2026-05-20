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

package io.apitomy.datamodels.validation.rules.required;

import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: SRV-005
 * Validates that a Server has a 'host' property in AsyncAPI 3.x.
 * In AsyncAPI 3.x, the 'host' property replaces the 'url' property used in 2.x.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaMissingServerHostRule extends RequiredPropertyValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaMissingServerHostRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitServer(Server node) {
        if (ModelTypeUtil.isAsyncApi3Model(node)) {
            if (hasValue(((Referenceable) node).get$ref())) {
                return;
            }
            this.requireProperty(node, "host", map());
        }
    }

}
