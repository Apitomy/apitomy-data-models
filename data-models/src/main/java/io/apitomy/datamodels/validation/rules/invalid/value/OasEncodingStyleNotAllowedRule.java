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

import io.apitomy.datamodels.models.openapi.OpenApiEncoding;
import io.apitomy.datamodels.models.openapi.OpenApiMediaType;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Encoding Style Not Allowed rule.
 * @author eric.wittmann@gmail.com
 */
public class OasEncodingStyleNotAllowedRule extends AbstractInvalidPropertyValueRule {

    private String mediaTypeName;

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasEncodingStyleNotAllowedRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitMediaType(OpenApiMediaType node) {
        mediaTypeName = getMappedNodeName(node);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitEncoding(io.apitomy.datamodels.models.openapi.OpenApiEncoding)
     */
    @Override
    public void visitEncoding(OpenApiEncoding node) {
        if (hasValue(node.getStyle())) {
            this.reportIfInvalid(mediaTypeName.indexOf("application/x-www-form-urlencoded") == 0, node,
                    "style", map("name", mediaTypeName));
        }
    }

}
