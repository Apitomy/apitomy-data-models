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

import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Parameter Structure Changed in 3.x rule for AsyncAPI.
 * In AsyncAPI 3.x, parameters no longer have a schema property.
 * Instead, they have direct properties like enum, default, and examples.
 * @author eric.wittmann@gmail.com
 */
public class AaParameterStructureChangedIn3xRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaParameterStructureChangedIn3xRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitParameter(io.apitomy.datamodels.models.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        if (ModelTypeUtil.isAsyncApi3Model(node)) {
            // In AsyncAPI 3.x, parameters should not have a schema property
            Object schema = node.getExtraProperty("schema");
            if (hasValue(schema)) {
                this.reportIfInvalid(false, node, "schema", map());
            }
        }
    }

}
