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

package io.apitomy.datamodels.validation.rules.invalid.reference;

import io.apitomy.datamodels.models.asyncapi.AsyncApiOperationReply;
import io.apitomy.datamodels.models.asyncapi.AsyncApiReference;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.List;

/**
 * Implements the Invalid Reply Message References rule for AsyncAPI 3.x.
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidReplyMessageReferencesRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaInvalidReplyMessageReferencesRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitOperationReply(AsyncApiOperationReply node) {
        List<AsyncApiReference> messages = node.getMessages();
        if (hasValue(messages)) {
            for (AsyncApiReference messageRef : messages) {
                if (hasValue(messageRef)) {
                    String ref = messageRef.get$ref();
                    if (hasValue(ref)) {
                        this.reportIfInvalid(ReferenceUtil.canResolveRef(ref, messageRef), node, "messages", map());
                    }
                }
            }
        }
    }
}
