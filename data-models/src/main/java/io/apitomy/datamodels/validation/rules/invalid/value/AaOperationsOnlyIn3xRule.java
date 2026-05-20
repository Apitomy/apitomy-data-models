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

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AAD-007
 * Validates that the 'operations' property only appears in AsyncAPI 3.x documents.
 * The 'operations' property is not valid in AsyncAPI 2.x versions.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaOperationsOnlyIn3xRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaOperationsOnlyIn3xRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitDocument(Document node) {
        if (ModelTypeUtil.isAsyncApiModel(node)) {
            // Check if 'operations' property exists (as an extra property)
            if (node.getExtraProperty("operations") != null) {
                // If operations exists, document must be AsyncAPI 3.x
                boolean isAsyncApi3x = ModelTypeUtil.isAsyncApi3Model(node);
                this.reportIfInvalid(isAsyncApi3x, node, "operations",
                    map("version", ((AsyncApiDocument) node).getAsyncapi()));
            }
        }
    }

}
