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

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasMissingPathParamDefinitionRule extends AbstractInvalidPropertyValueRule {

    private List<String> pathItemsWithError = new ArrayList<>();
    private String pathItemTemplate;

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasMissingPathParamDefinitionRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Returns true if the list of parameters includes a path param with the given name.
     * @param paramName
     * @param parameters
     */
    private boolean hasPathParameter(String paramName, List<OpenApiParameter> parameters) {
        for (OpenApiParameter parameter : parameters) {
            if (equals(parameter.getIn(), "path") && equals(parameter.getName(), paramName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitPathItem(io.apitomy.datamodels.models.openapi.OpenApiPathItem)
     */
    @Override
    public void visitPathItem(OpenApiPathItem node) {
        pathItemTemplate = getPathTemplate(node);
        if (!isPathWellFormed(pathItemTemplate)) {
            this.pathItemsWithError.add(pathItemTemplate);
        }
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitOperation(io.apitomy.datamodels.models.Operation)
     */
    @Override
    public void visitOperation(Operation node) {
        String method = getOperationMethod(node);

        // Perform operation level checks only if there are no issues at the pathItem level.
        if (this.pathItemsWithError.indexOf(pathItemTemplate) != -1) {
            return;
        }

        // Check parameters are unique within operation
        List<OpenApiParameter> mergedParameters = mergeParameters((OpenApiOperation) node);
        List<PathSegment> pathSegs = getPathSegments(pathItemTemplate);
        // Report all the path segments that don't have an associated parameter definition
        for (PathSegment pathSeg : pathSegs) {
            if (!hasValue(pathSeg.formalName)) { continue; }
            boolean valid = hasPathParameter(pathSeg.formalName, mergedParameters);
            this.reportIfInvalid(valid, node,null, map(
                    "param", pathSeg.formalName,
                    "path", pathItemTemplate,
                    "method", method.toUpperCase()
                    ));
        }
    }

}