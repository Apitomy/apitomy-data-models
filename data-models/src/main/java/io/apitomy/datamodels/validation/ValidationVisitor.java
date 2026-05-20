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

package io.apitomy.datamodels.validation;

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.visitors.TraversalContext;
import io.apitomy.datamodels.models.visitors.TraversingVisitor;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.visitors.CompositeAllNodeVisitor;

/**
 * Base class for all validation visitors.
 * @author eric.wittmann@gmail.com
 */
public class ValidationVisitor extends CompositeAllNodeVisitor implements IValidationProblemReporter, TraversingVisitor {

    private List<ValidationProblem> problems = new ArrayList<>();
    private IValidationSeverityRegistry severityRegistry;

    /**
     * Constructor.
     * @param type
     */
    public ValidationVisitor(ModelType type) {
        super(new ArrayList<>());
        ValidationRuleSet ruleSet = ValidationRuleSet.instance;
        List<ValidationRule> rulesFor = ruleSet.getRulesFor(type);
        rulesFor.forEach(rule -> {
            rule.setReporter(this);
        });
        this.addVisitors(rulesFor);
    }

    @Override
    public void setTraversalContext(TraversalContext context) {
        this.getVisitors().forEach(visitor -> {
            if (visitor instanceof TraversingVisitor) {
                ((TraversingVisitor) visitor).setTraversalContext(context);
            }
        });
    }

    /**
     * Sets the severity registry.
     * @param severityRegistry
     */
    public void setSeverityRegistry(IValidationSeverityRegistry severityRegistry) {
        this.severityRegistry = severityRegistry;
    }

    /**
     * Accessor for the problems.
     */
    public List<ValidationProblem> getValidationProblems() {
        return this.problems;
    }

    @Override
    public void report(ValidationRuleMetaData ruleInfo, Node node, String property, String message) {
        ValidationProblemSeverity severity = this.severityRegistry.lookupSeverity(ruleInfo);
        if (severity == ValidationProblemSeverity.ignore) {
            return;
        }

        NodePath path = NodePathUtil.createNodePath(node);
        ValidationProblem problem = new ValidationProblem(ruleInfo.code, path, property, message, severity);

        // Include in the list of problems tracked by the validator.
        this.problems.add(problem);
    }

}
