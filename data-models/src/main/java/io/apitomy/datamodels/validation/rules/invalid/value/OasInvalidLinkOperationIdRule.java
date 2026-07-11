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

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.models.Link;
import io.apitomy.datamodels.models.openapi.OpenApiLink;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;
import io.apitomy.datamodels.visitors.OperationFinder;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasInvalidLinkOperationIdRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidLinkOperationIdRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitLink(io.apitomy.datamodels.models.Link)
     */
    @Override
    public void visitLink(Link node) {
        OpenApiLink oaiLink = (OpenApiLink) node;
        if (hasValue(oaiLink.getOperationId())) {
            OperationFinder finder = new OperationFinder(oaiLink.getOperationId());
            VisitorUtil.visitTree(node.root(), finder, TraverserDirection.down);
            this.reportIfInvalid(finder.isFound(), node, "operationId", map());
        }
    }

}