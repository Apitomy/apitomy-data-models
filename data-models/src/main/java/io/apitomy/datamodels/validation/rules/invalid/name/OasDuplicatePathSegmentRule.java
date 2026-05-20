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

package io.apitomy.datamodels.validation.rules.invalid.name;

import java.util.List;

import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.util.ValidationUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Duplicate Path Segment Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasDuplicatePathSegmentRule extends OasInvalidPropertyNameRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasDuplicatePathSegmentRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitPathItem(io.apitomy.datamodels.models.openapi.OpenApiPathItem)
     */
    @Override
    public void visitPathItem(OpenApiPathItem node) {
        String pathTemplate = getPathTemplate(node);
        if (isDefined(pathTemplate) && isPathWellFormed(pathTemplate)) {
            List<PathSegment> pathSegments = getPathSegments(pathTemplate);
            List<String> duplicateParameters = findDuplicateParametersInPath(pathSegments);
            if (duplicateParameters.size() > 0) {
                this.reportPathError(node, map("path", pathTemplate, "duplicates", ValidationUtil.join(", ", duplicateParameters)));
            }
        }
    }

}
