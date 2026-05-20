package io.apitomy.umg.base.visitors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Context used during traversal of a model. This gives insight into where in
 * the traversal of a model a visitor/traverser might be. The traverser
 * maintains this context and optionally makes it available to the visitor (only
 * if the visitor implements the TraversingVisitor interface.
 *
 * @author eric.wittmann@gmail.com
 */
public class TraversalContextImpl implements TraversalContext {

    private final Stack<TraversalStep> stack = new Stack<>();

    public void pushProperty(String propertyName) {
        this.stack.push(TraversalStep.fromNodeProperty(propertyName));
    }
    public void pushListIndex(int index) {
        this.stack.push(TraversalStep.fromListIndex(index));
    }
    public void pushMapIndex(String key) {
        this.stack.push(TraversalStep.fromMapIndex(key));
    }

    public void pop() {
        this.stack.pop();
    }

    public TraversalStep peek() {
        return this.stack.peek();
    }

    @Override
    public TraversalStep getMostRecentStep() {
        return this.stack.peek();
    }

    @Override
    public List<TraversalStep> getAllSteps() {
        TraversalStep[] steps = this.stack.toArray(new TraversalStep[this.stack.size()]);
        return Collections.unmodifiableList(Arrays.asList(steps));
    }

    @Override
    public String getMostRecentPropertyStep() {
        for (int idx = stack.size() - 1; idx >= 0; idx--) {
            TraversalStep step = stack.get(idx);
            if (step.getType() == TraversalStepType.property) {
                return (String) step.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean containsStep(TraversalStepType type, Object value) {
        for (TraversalStep step : stack) {
            if (step.getType() == type && value.equals(step.getValue())) {
                return true;
            }
        }
        return false;
    }

}
