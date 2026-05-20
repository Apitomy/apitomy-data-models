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

import java.util.List;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Operation;
import io.apitomy.datamodels.models.openapi.v2x.v20.visitors.OpenApi20VisitorAdapter;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * Finds the first path item.
 * @author eric.wittmann@gmail.com
 */
public class ConsumesProducesFinder extends OpenApi20VisitorAdapter {

    public List<String> consumes;
    public List<String> produces;

    @Override
    public void visitDocument(Document node) {
        OpenApi20Document doc = (OpenApi20Document) node;
        if (NodeUtil.isNullOrUndefined(this.consumes)) {
            this.consumes = doc.getConsumes();
        }
        if (NodeUtil.isNullOrUndefined(this.produces)) {
            this.produces = doc.getProduces();
        }
    }

    @Override
    public void visitOperation(Operation node) {
        consumes = ((OpenApi20Operation) node).getConsumes();
        produces = ((OpenApi20Operation) node).getProduces();
    }

}