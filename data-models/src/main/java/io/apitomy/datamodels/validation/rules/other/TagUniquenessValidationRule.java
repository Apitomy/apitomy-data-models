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

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannel;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessageTrait;
import io.apitomy.datamodels.models.asyncapi.AsyncApiOperation;
import io.apitomy.datamodels.models.asyncapi.AsyncApiOperationTrait;
import io.apitomy.datamodels.models.asyncapi.v2x.AsyncApi2xDocument;
import io.apitomy.datamodels.models.asyncapi.v2x.v25.AsyncApi25Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v26.AsyncApi26Server;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xInfo;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xServer;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

import java.util.List;

/**
 * Implements the Tag Name Uniqueness validation rule.  Ensures that no two sibling
 * tags (tags belonging to the same parent node) share the same name.
 * @author eric.wittmann@gmail.com
 */
public class TagUniquenessValidationRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public TagUniquenessValidationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.AllNodeVisitor#visitTag(io.apitomy.datamodels.models.Tag)
     */
    @Override
    public void visitTag(Tag node) {
        List<? extends Tag> tags = getTagsFromParent(node.parent());
        if (tags == null || tags.size() <= 1) {
            return;
        }
        int tcount = 0;
        for (Tag tag : tags) {
            if (equals(tag.getName(), node.getName())) {
                tcount++;
            }
        }
        this.reportIf(tcount > 1, node, node.getName(), map("tagName", node.getName()));
    }

    /**
     * Returns the list of sibling tags from the given parent node.
     * @param parent the parent node that owns the tag list
     * @return the list of tags, or null if the parent type is not recognized
     */
    private List<? extends Tag> getTagsFromParent(Node parent) {
        // OpenAPI
        if (parent instanceof OpenApiDocument) {
            return ((OpenApiDocument) parent).getTags();
        }
        // AsyncAPI 2.x document-level tags
        if (parent instanceof AsyncApi2xDocument) {
            return ((AsyncApi2xDocument) parent).getTags();
        }
        // AsyncAPI 3.x info-level tags
        if (parent instanceof AsyncApi3xInfo) {
            return ((AsyncApi3xInfo) parent).getTags();
        }
        // AsyncAPI operation tags (all versions)
        if (parent instanceof AsyncApiOperation) {
            return ((AsyncApiOperation) parent).getTags();
        }
        // AsyncAPI message tags (all versions)
        if (parent instanceof AsyncApiMessage) {
            return ((AsyncApiMessage) parent).getTags();
        }
        // AsyncAPI message trait tags (all versions)
        if (parent instanceof AsyncApiMessageTrait) {
            return ((AsyncApiMessageTrait) parent).getTags();
        }
        // AsyncAPI operation trait tags (all versions)
        if (parent instanceof AsyncApiOperationTrait) {
            return ((AsyncApiOperationTrait) parent).getTags();
        }
        // AsyncAPI channel tags (3.x)
        if (parent instanceof AsyncApiChannel) {
            return ((AsyncApiChannel) parent).getTags();
        }
        // AsyncAPI server tags (version-specific)
        if (parent instanceof AsyncApi3xServer) {
            return ((AsyncApi3xServer) parent).getTags();
        }
        if (parent instanceof AsyncApi25Server) {
            return ((AsyncApi25Server) parent).getTags();
        }
        if (parent instanceof AsyncApi26Server) {
            return ((AsyncApi26Server) parent).getTags();
        }
        return null;
    }

}
