package io.apitomy.umg.base.visitors;

public class TraversalStep {

    private final TraversalStepType type;
    private final Object value;

    public static TraversalStep fromNodeProperty(String propertyName) {
        return new TraversalStep(TraversalStepType.property, propertyName);
    }

    public static TraversalStep fromListIndex(int index) {
        return new TraversalStep(TraversalStepType.arrayIndex, index);
    }

    public static TraversalStep fromMapIndex(String key) {
        return new TraversalStep(TraversalStepType.mapIndex, key);
    }

    private TraversalStep(TraversalStepType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public TraversalStepType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

}
