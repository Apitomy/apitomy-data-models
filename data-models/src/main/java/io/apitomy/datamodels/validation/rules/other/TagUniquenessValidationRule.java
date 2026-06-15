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

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Info;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannel;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessage;
import io.apitomy.datamodels.models.asyncapi.AsyncApiMessageTrait;
import io.apitomy.datamodels.models.asyncapi.AsyncApiOperationTrait;
import io.apitomy.datamodels.models.asyncapi.AsyncApiOperation;
import io.apitomy.datamodels.models.asyncapi.v2x.AsyncApi2xDocument;
import io.apitomy.datamodels.models.asyncapi.v2x.v25.AsyncApi25Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v26.AsyncApi26Server;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xInfo;
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xServer;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
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
     * Returns the list of sibling tags from the given parent node by using the
     * visitor pattern to dispatch to the correct parent type.
     * @param parent the parent node that owns the tag list
     * @return the list of tags, or null if the parent type has no tags
     */
    private List<? extends Tag> getTagsFromParent(Node parent) {
        TagListExtractor extractor = new TagListExtractor();
        parent.accept(extractor);
        return extractor.tags;
    }

    /**
     * Visitor that extracts the tag list from any node type that can own tags.
     */
    private static class TagListExtractor extends CombinedVisitorAdapter {

        List<? extends Tag> tags;

        @Override
        public void visitDocument(Document node) {
            if (node instanceof OpenApiDocument) {
                this.tags = ((OpenApiDocument) node).getTags();
            } else if (node instanceof AsyncApi2xDocument) {
                this.tags = ((AsyncApi2xDocument) node).getTags();
            }
        }

        @Override
        public void visitInfo(Info node) {
            if (node instanceof AsyncApi3xInfo) {
                this.tags = ((AsyncApi3xInfo) node).getTags();
            }
        }

        @Override
        public void visitServer(Server node) {
            if (node instanceof AsyncApi3xServer) {
                this.tags = ((AsyncApi3xServer) node).getTags();
            } else if (node instanceof AsyncApi25Server) {
                this.tags = ((AsyncApi25Server) node).getTags();
            } else if (node instanceof AsyncApi26Server) {
                this.tags = ((AsyncApi26Server) node).getTags();
            }
        }

        @Override
        public void visitOperation(Operation node) {
            if (node instanceof AsyncApiOperation) {
                this.tags = ((AsyncApiOperation) node).getTags();
            }
        }

        @Override
        public void visitMessage(AsyncApiMessage node) {
            this.tags = node.getTags();
        }

        @Override
        public void visitMessageTrait(AsyncApiMessageTrait node) {
            this.tags = node.getTags();
        }

        @Override
        public void visitOperationTrait(AsyncApiOperationTrait node) {
            this.tags = node.getTags();
        }

        @Override
        public void visitChannel(AsyncApiChannel node) {
            this.tags = node.getTags();
        }
    }

}
