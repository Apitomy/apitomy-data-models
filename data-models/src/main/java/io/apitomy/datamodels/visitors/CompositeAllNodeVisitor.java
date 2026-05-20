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

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Visitable;
import io.apitomy.datamodels.models.visitors.AllNodeVisitor;
import io.apitomy.datamodels.models.visitors.Visitor;

/**
 * A composite visitor base class useful for any data model type.
 *
 * @author eric.wittmann@gmail.com
 * @author jsenko@redhat.com
 */
public class CompositeAllNodeVisitor extends AllNodeVisitor {

    private List<Visitor> visitors = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param visitors
     */
    public CompositeAllNodeVisitor(List<Visitor> visitors) {
        this.visitors = visitors;
    }

    /**
     * Adds a visitor.
     *
     * @param visitor
     */
    public void addVisitor(Visitor visitor) {
        this.visitors.add(visitor);
    }

    /**
     * Adds multiple visitors.
     *
     * @param visitors
     */
    public void addVisitors(List<? extends Visitor> visitors) {
        this.visitors.addAll(visitors);
    }

    /**
     * Gets the list of visitors.
     */
    public List<? extends Visitor> getVisitors() {
        return this.visitors;
    }

    /**
     * Make the node accept all of the visitors.
     *
     * @param node
     */
    protected void acceptAll(Visitable node) {
        this.visitors.forEach(visitor -> {
            node.accept(visitor);
        });
    }

    @Override
    protected void visitNode(Node node) {
        acceptAll(node);
    }
}
