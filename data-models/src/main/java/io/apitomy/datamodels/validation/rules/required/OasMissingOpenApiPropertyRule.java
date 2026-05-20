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

package io.apitomy.datamodels.validation.rules.required;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasMissingOpenApiPropertyRule extends RequiredPropertyValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasMissingOpenApiPropertyRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitDocument(Document node) {
        ModelType mt = node.root().modelType();
        if (mt == ModelType.OPENAPI20) {
            this.requireProperty(node, "swagger", map());
        } else if (mt == ModelType.OPENAPI30 || mt == ModelType.OPENAPI31) {
            this.requireProperty(node, "openapi", map());
        } else if (ModelTypeUtil.isAsyncApiModel(node)) {
            this.requireProperty(node, "asyncapi", map());
        }
    }

}
