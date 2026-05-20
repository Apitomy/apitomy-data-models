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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.models.openapi.OpenApiPaths;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Identical Path Template Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasIdenticalPathTemplateRule extends OasInvalidPropertyNameRule {

    Map<String, List<PathSegment>> indexedPathTemplates = new HashMap<>();

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasIdenticalPathTemplateRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Utility function to find other paths that are semantically similar to the path that is being checked against.
     * Two paths that differ only in formal parameter name are considered identical.
     * For example, paths /test/{var1} and /test/{var2} are identical.
     * See OAS 3 Specification's Path Templates section for more details.
     *
     * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#path-templating-matching
     *
     * @param pathToCheck
     * @param pathIndex
     */
    private List<String> findIdenticalPaths(String pathToCheck) {
        List<String> identicalPaths = new ArrayList<>();
        List<PathSegment> pathSegments = indexedPathTemplates.get(pathToCheck);
        for (String checkAgainst : indexedPathTemplates.keySet()) {
            if (equals(checkAgainst, pathToCheck)) { continue; }
            boolean segmentsIdentical = true;
            List<PathSegment> pathSegmentsToCheckAgainst = indexedPathTemplates.get(checkAgainst);
            if (pathSegments.size() != pathSegmentsToCheckAgainst.size()) {
                segmentsIdentical = false;
            } else {
                int idx = 0;
                for (PathSegment pathSegment : pathSegments) {
                    segmentsIdentical = segmentsIdentical && this.isSegmentIdentical(pathSegment, pathSegmentsToCheckAgainst.get(idx));
                    idx++;
                }
            }
            if (segmentsIdentical) {
                identicalPaths.add(checkAgainst);
            }
        }
        return identicalPaths;
    }

    /**
     * Utility function to test the equality of two path segments.
     * Segments are considered equal if they have same prefixes (if any) and same "normalized name".
     *
     * @param segment1
     * @param segment2
     * @return {boolean}
     */
    private boolean isSegmentIdentical(PathSegment segment1, PathSegment segment2) {
        if (equals(segment1.prefix, segment2.prefix)) {
            if (!hasValue(segment1.normalizedName) && !hasValue(segment2.normalizedName))
            {
                return true;
            }
            if ( (!hasValue(segment1.normalizedName) && hasValue(segment2.normalizedName)) ||
                    (hasValue(segment1.normalizedName) && !hasValue(segment2.normalizedName)) )
            {
                return false;
            }
            return equals(segment1.normalizedName, segment2.normalizedName);
        }
        return false;
    }

    @Override
    public void visitPaths(OpenApiPaths node) {
        // First index the path items.
        node.getItemNames().forEach(pathTemplate -> {
            if (isPathWellFormed(pathTemplate)) {
                List<PathSegment> pathSegments = getPathSegments(pathTemplate);
                indexedPathTemplates.put(pathTemplate, pathSegments);
            }
        });

        node.getItemNames().forEach(pathTemplate -> {
            if (isPathWellFormed(pathTemplate)) {
                List<String> identicalPaths = findIdenticalPaths(pathTemplate);
                if (identicalPaths.size() > 0) {
                    this.reportPathError(node.getItem(pathTemplate), map("path", pathTemplate));
                }
            }
        });
    }
}
