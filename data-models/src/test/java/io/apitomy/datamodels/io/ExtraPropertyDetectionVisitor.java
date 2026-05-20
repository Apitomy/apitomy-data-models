/*
 * Copyright 2022 Red Hat
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

package io.apitomy.datamodels.io;

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.visitors.AllNodeVisitor;

/**
 * @author eric.wittmann@gmail.com
 */
public class ExtraPropertyDetectionVisitor extends AllNodeVisitor {

    public List<String> extraProperties = new ArrayList<>();

    public int getExtraPropertyCount() {
        return extraProperties.size();
    }

    /**
     * @see io.apitomy.datamodels.visitors.AllNodeVisitor#visitNode(io.apitomy.datamodels.Node)
     */
    @Override
    protected void visitNode(Node node) {
        if (node.getExtraPropertyNames().size() > 0) {
            node.getExtraPropertyNames().forEach(pname -> {
                extraProperties.add(node.getClass().getSimpleName() + "::" + pname);
            });
        }
    }

}
