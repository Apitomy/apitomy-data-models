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
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid API Scheme rule.
 * @author eric.wittmann@gmail.com
 */
public class OasInvalidApiSchemeRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidApiSchemeRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitDocument(io.apitomy.datamodels.models.Document)
     */
    @Override
    public void visitDocument(Document node) {
        OpenApi20Document doc = (OpenApi20Document) node;
        if (hasValue(doc.getSchemes())) {
            doc.getSchemes().forEach(scheme -> {
                this.reportIfInvalid(isValidEnumItem(scheme, array("http", "https", "ws", "wss")), node,
                        "schemes", map("scheme", scheme));
            });
        }
    }

}
