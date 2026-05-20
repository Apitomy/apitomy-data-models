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

import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apitomy.datamodels.models.asyncapi.v2x.v20.AsyncApi20Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v21.AsyncApi21Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v22.AsyncApi22Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v23.AsyncApi23Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v24.AsyncApi24Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v25.AsyncApi25Message;
import io.apitomy.datamodels.models.asyncapi.v2x.v26.AsyncApi26Message;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Message Schema Format rule for AsyncAPI.
 * Schema format must be a valid format identifier (e.g., "application/vnd.aai.asyncapi;version=2.0.0").
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidMessageSchemaFormatRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaInvalidMessageSchemaFormatRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitMessage(io.apitomy.datamodels.models.asyncapi.AsyncApiMessage)
     */
    @Override
    public void visitMessage(AsyncApiMessage node) {
        // Skip if it's a reference
        if (node instanceof AsyncApiReferenceable && hasValue(((AsyncApiReferenceable) node).get$ref())) {
            return;
        }

        // Get schemaFormat based on version (only in AsyncAPI 2.x)
        String schemaFormat = null;

        if (node instanceof AsyncApi20Message) {
            schemaFormat = ((AsyncApi20Message) node).getSchemaFormat();
        } else if (node instanceof AsyncApi21Message) {
            schemaFormat = ((AsyncApi21Message) node).getSchemaFormat();
        } else if (node instanceof AsyncApi22Message) {
            schemaFormat = ((AsyncApi22Message) node).getSchemaFormat();
        } else if (node instanceof AsyncApi23Message) {
            schemaFormat = ((AsyncApi23Message) node).getSchemaFormat();
        } else if (node instanceof AsyncApi24Message) {
            schemaFormat = ((AsyncApi24Message) node).getSchemaFormat();
        } else if (node instanceof AsyncApi25Message) {
            schemaFormat = ((AsyncApi25Message) node).getSchemaFormat();
        } else if (node instanceof AsyncApi26Message) {
            schemaFormat = ((AsyncApi26Message) node).getSchemaFormat();
        }

        if (hasValue(schemaFormat)) {
            // Schema format should follow a pattern like "type/subtype" or "type/subtype;params"
            // Common formats include:
            // - application/vnd.aai.asyncapi;version=2.0.0
            // - application/schema+json;version=draft-07
            boolean isValid = schemaFormat.contains("/");
            this.reportIfInvalid(isValid, node, "schemaFormat", map());
        }
    }

}
