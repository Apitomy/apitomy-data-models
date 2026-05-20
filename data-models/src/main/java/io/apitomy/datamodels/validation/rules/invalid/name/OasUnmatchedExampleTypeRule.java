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

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.models.Example;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Example;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Operation;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;
import io.apitomy.datamodels.visitors.OperationFinder;

/**
 * Implements the Unmatched Example Type Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnmatchedExampleTypeRule extends OasInvalidPropertyNameRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnmatchedExampleTypeRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Finds the parent operation for the given node.
     * @param example
     */
    private static Operation findParentOperation(Node node) {
        OperationFinder finder = new OperationFinder();
        VisitorUtil.visitTree(node, finder, TraverserDirection.up);
        return finder.found;
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitExample(io.apitomy.datamodels.models.Example)
     */
    @Override
    public void visitExample(Example node) {
        OpenApi20Example example20 = (OpenApi20Example) node;
        OpenApi20Document doc = (OpenApi20Document) example20.root();
        List<String> produces = doc.getProduces();
        OpenApi20Operation operation = (OpenApi20Operation) findParentOperation(example20);
        if (isDefined(operation)) {
            if (hasValue(operation.getProduces())) {
                produces = operation.getProduces();
            }
            if (!hasValue(produces)) {
                produces = new ArrayList<>();
            }

            List<String> ctypes = example20.getItemNames();
            for (String ct : ctypes) {
                this.reportIfInvalid(produces.indexOf(ct) != -1, example20, "produces", map("contentType", ct));
            }
        }
    }

}
