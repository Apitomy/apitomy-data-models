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

package io.apitomy.datamodels.validation.rules.other;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openrpc.OpenRpcDocument;
import io.apitomy.datamodels.models.openrpc.OpenRpcMethod;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Duplicate Method Name validation rule.
 * @author eric.wittmann@gmail.com
 */
public class OrpcDuplicateMethodNameRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OrpcDuplicateMethodNameRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitDocument(Document node) {
        if (ModelTypeUtil.isOpenRpcModel(node)) {
            OpenRpcDocument doc = (OpenRpcDocument) node;
            List<? extends OpenRpcMethod> methods = doc.getMethods();
            if (methods != null) {
                Set<String> names = new HashSet<>();
                for (OpenRpcMethod method : methods) {
                    if (hasValue(method.getName())) {
                        if (!names.add(method.getName())) {
                            this.report((Node) method, "name", map("methodName", method.getName()));
                        }
                    }
                }
            }
        }
    }

}
