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
import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: SRV-010
 * Validates that servers can only have tags starting in AsyncAPI 2.5.
 * The tags property was added to servers in AsyncAPI 2.5, so using tags
 * in earlier versions is invalid.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaServerTagsOnlyIn25PlusRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaServerTagsOnlyIn25PlusRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitServer(Server node) {
        ModelType modelType = node.root().modelType();

        // Check if this is AsyncAPI 2.0-2.4 (before servers gained tags)
        if (modelType == ModelType.ASYNCAPI20 ||
            modelType == ModelType.ASYNCAPI21 ||
            modelType == ModelType.ASYNCAPI22 ||
            modelType == ModelType.ASYNCAPI23 ||
            modelType == ModelType.ASYNCAPI24) {

            // Check if the server has a tags property
            if (node.getExtraProperty("tags") != null) {
                this.reportIf(true, node, "tags", map());
            }
        }
    }

}
