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

import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Header;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Unknown Header Type rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnknownHeaderTypeRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnknownHeaderTypeRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitHeader(io.apitomy.datamodels.models.openapi.OpenApiHeader)
     */
    @Override
    public void visitHeader(OpenApiHeader node) {
        OpenApi20Header header = (OpenApi20Header) node;
        if (hasValue(header.getType())) {
            this.reportIfInvalid(isValidEnumItem(header.getType(), array("string", "number", "integer", "boolean", "array")),
                    node, "type", map());
        }
    }

}