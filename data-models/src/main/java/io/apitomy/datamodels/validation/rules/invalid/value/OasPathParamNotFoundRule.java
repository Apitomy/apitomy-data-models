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

import java.util.List;

import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Path Parameter Not Found rule.
 * @author eric.wittmann@gmail.com
 */
public class OasPathParamNotFoundRule extends AbstractInvalidPropertyValueRule {

    private String pathItemTemplate;

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasPathParamNotFoundRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitPathItem(io.apitomy.datamodels.models.openapi.OpenApiPathItem)
     */
    @Override
    public void visitPathItem(OpenApiPathItem node) {
        this.pathItemTemplate = getPathTemplate(node);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        if (!isDefinition(node)) {
            OpenApiParameter resolvedParam = (OpenApiParameter) ReferenceUtil.resolveNodeRef(node);
            if (hasValue(resolvedParam) && equals(resolvedParam.getIn(), "path")) {
                // Note: parent may be an operation *or* a path-item.
                List<PathSegment> pathSegs = getPathSegments(pathItemTemplate);
                this.reportIf(!hasPathParamSegment(pathSegs, resolvedParam.getName()), node, "name", map("name", resolvedParam.getName()));
            }
        }
    }

    protected boolean hasPathParamSegment(List<PathSegment> segments, String paramName) {
        for (PathSegment seg : segments) {
            if (equals(seg.formalName, paramName)) {
                return true;
            }
        }
        return false;
    }

}
