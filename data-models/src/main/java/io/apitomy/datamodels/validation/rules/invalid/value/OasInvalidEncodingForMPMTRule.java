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

import io.apitomy.datamodels.models.openapi.OpenApiMediaType;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasInvalidEncodingForMPMTRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidEncodingForMPMTRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitMediaType(io.apitomy.datamodels.models.openapi.OpenApiMediaType)
     */
    @Override
    public void visitMediaType(OpenApiMediaType node) {
        if (isDefined(node.getEncoding()) && node.getEncoding().size() > 0) {
            String mediaTypeName = getMappedNodeName(node);
            this.reportIfInvalid(isValidMultipartType(mediaTypeName), node, "encoding", map("name", mediaTypeName));
        }
    }

}
