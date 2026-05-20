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

package io.apitomy.datamodels.validation.rules.invalid.format;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.openrpc.OpenRpcDocument;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.RegexUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that the OpenRPC version property matches the expected pattern.
 * @author eric.wittmann@gmail.com
 */
public class OrpcInvalidOpenRpcVersionFormatRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OrpcInvalidOpenRpcVersionFormatRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitDocument(Document node) {
        if (ModelTypeUtil.isOpenRpcModel(node)) {
            OpenRpcDocument doc = (OpenRpcDocument) node;
            if (hasValue(doc.getOpenrpc())) {
                String version = doc.getOpenrpc();
                boolean isValid = RegexUtil.matches(version, "^\\d+\\.\\d+\\.\\d+$");
                this.reportIfInvalid(isValid, node, "openrpc",
                    map("version", version));
            }
        }
    }

}
