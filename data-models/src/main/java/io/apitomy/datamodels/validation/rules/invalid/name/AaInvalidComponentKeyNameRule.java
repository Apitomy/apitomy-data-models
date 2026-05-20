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

package io.apitomy.datamodels.validation.rules.invalid.name;

import io.apitomy.datamodels.models.Components;
import io.apitomy.datamodels.models.asyncapi.AsyncApiComponents;
import io.apitomy.datamodels.models.asyncapi.v2x.AsyncApi2xComponents;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xComponents;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.Map;

/**
 * Implements the Invalid Component Key Name Pattern rule for AsyncAPI.
 * Rule: COMP-001
 * Component keys must match the regex pattern ^[a-zA-Z0-9\.\-_]+$
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidComponentKeyNameRule extends OasInvalidPropertyNameRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaInvalidComponentKeyNameRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitComponents(io.apitomy.datamodels.models.Components)
     */
    @Override
    public void visitComponents(Components node) {
        // Validate AsyncAPI-specific component keys
        if (ModelTypeUtil.isAsyncApiModel(node)) {
            AsyncApiComponents asyncApiComponents = (AsyncApiComponents) node;
            validateMapKeys(asyncApiComponents.getParameters(), node, "parameters");
            validateMapKeys(asyncApiComponents.getSecuritySchemes(), node, "securitySchemes");
            validateMapKeys(asyncApiComponents.getMessages(), node, "messages");
            validateMapKeys(asyncApiComponents.getCorrelationIds(), node, "correlationIds");
            validateMapKeys(asyncApiComponents.getMessageTraits(), node, "messageTraits");
            validateMapKeys(asyncApiComponents.getOperationTraits(), node, "operationTraits");
            validateMapKeys(asyncApiComponents.getServerBindings(), node, "serverBindings");
            validateMapKeys(asyncApiComponents.getChannelBindings(), node, "channelBindings");
            validateMapKeys(asyncApiComponents.getOperationBindings(), node, "operationBindings");
            validateMapKeys(asyncApiComponents.getMessageBindings(), node, "messageBindings");
        }

        // Validate AsyncAPI 2.x-specific component keys
        if (ModelTypeUtil.isAsyncApi2Model(node)) {
            validateMapKeys(((AsyncApi2xComponents) node).getSchemas(), node, "schemas");
        }

        // Validate AsyncAPI 3.x-specific component keys
        if (ModelTypeUtil.isAsyncApi3Model(node)) {
            AsyncApi3xComponents asyncApi3xComponents = (AsyncApi3xComponents) node;
            validateMapKeys(asyncApi3xComponents.getSchemas(), node, "schemas");
            validateMapKeys(asyncApi3xComponents.getServers(), node, "servers");
            validateMapKeys(asyncApi3xComponents.getChannels(), node, "channels");
            validateMapKeys(asyncApi3xComponents.getOperations(), node, "operations");
            validateMapKeys(asyncApi3xComponents.getServerVariables(), node, "serverVariables");
            validateMapKeys(asyncApi3xComponents.getReplies(), node, "replies");
            validateMapKeys(asyncApi3xComponents.getReplyAddresses(), node, "replyAddresses");
            validateMapKeys(asyncApi3xComponents.getExternalDocs(), node, "externalDocs");
            validateMapKeys(asyncApi3xComponents.getTags(), node, "tags");
        }
    }

    /**
     * Validates all keys in a map against the component name pattern.
     *
     * @param map the map to validate
     * @param node the Components node (for error reporting)
     * @param propertyName the name of the property containing this map
     */
    private void validateMapKeys(Map<String, ?> map, Components node, String propertyName) {
        if (map != null) {
            for (String key : map.keySet()) {
                if (!isValidDefinitionName(key)) {
                    this.report(node, propertyName + "[" + key + "]", map("key", key));
                }
            }
        }
    }

}
