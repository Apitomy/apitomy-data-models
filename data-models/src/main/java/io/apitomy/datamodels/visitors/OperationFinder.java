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

package io.apitomy.datamodels.visitors;

import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.openapi.OpenApiOperation;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * Finds the first operation.
 * @author eric.wittmann@gmail.com
 */
public class OperationFinder extends CombinedVisitorAdapter {

    private String operationId;

    public Operation found;

    /**
     * Constructor.
     */
    public OperationFinder() {
        this.operationId = null;
    }

    /**
     * Constructor.
     * @param operationId
     */
    public OperationFinder(String operationId) {
        this.operationId = operationId;
    }

    @Override
    public void visitOperation(Operation node) {
        if (this.operationId == null || NodeUtil.equals(this.operationId, ((OpenApiOperation) node).getOperationId())) {
            if (found == null) {
                this.found = node;
            }
        }
    }

    /**
     * Returns true if an operation was found.
     */
    public boolean isFound() {
        return this.found != null;
    }

}