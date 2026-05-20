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

package io.apitomy.datamodels.validation.rules.mutex;

import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Parameter;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Parameter;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.Map;

/**
 * Implements the Parameter Schema/Content Mutual Exclusivity Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasParameterSchemaContentMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasParameterSchemaContentMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    private boolean hasContent(OpenApiParameter contentParent) {
        Map<String, ?> content = null;
        if (ModelTypeUtil.isOpenApi30Model(contentParent)) {
            content = ((OpenApi30Parameter) contentParent).getContent();
        } else if (ModelTypeUtil.isOpenApi31Model(contentParent)) {
            content = ((OpenApi31Parameter) contentParent).getContent();
        }

        if (!isDefined(content)) {
            return false;
        }
        return !content.isEmpty();
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        OpenApiParameter parameter = (OpenApiParameter) node;
        this.reportIf(hasValue(parameter.getSchema()) && hasContent(parameter), node, "schema", map());
    }

}
