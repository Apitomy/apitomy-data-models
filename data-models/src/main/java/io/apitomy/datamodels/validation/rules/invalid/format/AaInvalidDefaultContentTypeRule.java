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

import java.util.List;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: AAD-006
 * Validates that the AsyncAPI document defaultContentType property is a valid MIME type if provided.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidDefaultContentTypeRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaInvalidDefaultContentTypeRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitDocument(Document node) {
        if (ModelTypeUtil.isAsyncApiModel(node)) {
            AsyncApiDocument doc = (AsyncApiDocument) node;
            if (hasValue(doc.getDefaultContentType())) {
                // Default content type should be a valid MIME type
                this.reportIfInvalid(isValidMimeType(List.of(doc.getDefaultContentType())),
                    node, "defaultContentType", map());
            }
        }
    }

}
