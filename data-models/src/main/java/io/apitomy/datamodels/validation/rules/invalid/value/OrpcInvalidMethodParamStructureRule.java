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

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openrpc.OpenRpcMethod;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that the OpenRPC Method paramStructure value is valid.
 * @author eric.wittmann@gmail.com
 */
public class OrpcInvalidMethodParamStructureRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OrpcInvalidMethodParamStructureRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitMethod(OpenRpcMethod node) {
        if (hasValue(node.getParamStructure())) {
            String ps = node.getParamStructure();
            boolean valid = "by-name".equals(ps) || "by-position".equals(ps) || "either".equals(ps);
            this.reportIfInvalid(valid, (Node) node, "paramStructure", map("paramStructure", ps));
        }
    }

}
