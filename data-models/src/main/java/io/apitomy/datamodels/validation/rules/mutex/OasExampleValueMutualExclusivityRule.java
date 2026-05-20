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

import io.apitomy.datamodels.models.Example;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xExample;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Example;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Example Value/External Value Mutual Exclusivity Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasExampleValueMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasExampleValueMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitExample(io.apitomy.datamodels.models.Example)
     */
    @Override
    public void visitExample(Example node) {
        OpenApi3xExample example30 = (OpenApi3xExample) node;
        this.reportIf(hasValue(example30.getValue()) && hasValue(example30.getExternalValue()), example30, "value", map());
    }

}
