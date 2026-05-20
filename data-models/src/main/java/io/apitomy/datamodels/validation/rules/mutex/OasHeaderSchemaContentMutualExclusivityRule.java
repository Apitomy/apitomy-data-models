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

import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Header;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30MediaType;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Header;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.Map;

/**
 * Implements the Header Schema/Content Mutual Exclusivity Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasHeaderSchemaContentMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasHeaderSchemaContentMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    private boolean hasContent(Map<String, ?> content) {
        return isDefined(content) && !content.isEmpty();
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitHeader(io.apitomy.datamodels.models.openapi.OpenApiHeader)
     */
    @Override
    public void visitHeader(OpenApiHeader node) {
        Object schema = null;
        Map<String, ?> content = null;

        if (ModelTypeUtil.isOpenApi30Model(node)) {
            schema = ((OpenApi30Header) node).getSchema();
            content = ((OpenApi30Header) node).getContent();
        } else if (ModelTypeUtil.isOpenApi31Model(node)) {
            schema = ((OpenApi31Header) node).getSchema();
            content = ((OpenApi31Header) node).getContent();
        }

        this.reportIf(hasValue(schema) && hasContent(content), node, "schema", map());
    }
}
