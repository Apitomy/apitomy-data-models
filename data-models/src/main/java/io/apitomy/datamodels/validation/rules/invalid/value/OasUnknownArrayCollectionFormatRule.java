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

import io.apitomy.datamodels.models.openapi.OpenApiItems;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Items;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Unknown Array Collection Format rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnknownArrayCollectionFormatRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnknownArrayCollectionFormatRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitItems(OpenApiItems node) {
        if (hasValue(node.getCollectionFormat())) {
            this.reportIfInvalid(isValidEnumItem(node.getCollectionFormat(), array("csv", "ssv", "tsv", "pipes")), node,
                    "collectionFormat", map());
        }
    }

}