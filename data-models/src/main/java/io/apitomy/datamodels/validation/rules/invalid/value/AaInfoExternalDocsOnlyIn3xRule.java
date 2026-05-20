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

import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: INF-006
 * Validates that the 'externalDocs' property in Info only appears in AsyncAPI 3.x documents.
 * The 'externalDocs' property is not valid in AsyncAPI 2.x Info objects.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInfoExternalDocsOnlyIn3xRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaInfoExternalDocsOnlyIn3xRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitInfo(Info node) {
        if (ModelTypeUtil.isAsyncApi2Model(node)) {
            // Check if 'externalDocs' property exists in Info (as an extra property)
            this.reportIfInvalid(!hasValue(node.getExtraProperty("externalDocs")), node, "externalDocs", map());
        }
    }

}
