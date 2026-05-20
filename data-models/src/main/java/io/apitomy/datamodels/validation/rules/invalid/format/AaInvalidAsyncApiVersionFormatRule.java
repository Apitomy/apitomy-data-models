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

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.RegexUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AAD-004
 * Validates that the AsyncAPI version property matches the expected pattern.
 * Valid formats: "2.0.0", "2.1.0", "2.2.0", "2.3.0", "2.4.0", "2.5.0", "2.6.0", "3.0.0"
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidAsyncApiVersionFormatRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaInvalidAsyncApiVersionFormatRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitDocument(Document node) {
        if (ModelTypeUtil.isAsyncApiModel(node)) {
            AsyncApiDocument doc = (AsyncApiDocument) node;
            if (hasValue(doc.getAsyncapi())) {
                String version = doc.getAsyncapi();
                // AsyncAPI version should match pattern: major.minor.patch
                // Valid examples: 2.0.0, 2.1.0, 2.6.0, 3.0.0
                boolean isValid = RegexUtil.matches(version, "^\\d+\\.\\d+\\.\\d+$");
                this.reportIfInvalid(isValid, node, "asyncapi",
                    map("version", version));
            }
        }
    }

}
