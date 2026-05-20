package io.apitomy.datamodels.refs;

import java.util.Stack;

public class ReferenceContext {

    private final Stack<Reference> refStack;

    public ReferenceContext() {
        this.refStack = new Stack<>();
    }

    private ReferenceContext(Stack<Reference> fromStack) {
        this.refStack = new Stack<>();
        this.refStack.addAll(fromStack);
    }

    public ReferenceContext append(Reference reference) {
        ReferenceContext newContext = new ReferenceContext(refStack);
        newContext.refStack.add(reference);
        return newContext;
    }

    public Reference canonicalizeRef(Reference reference) {
        // TODO handle a sequence of relative local file paths, in case the sequence of refs is something like:
        //   design.json has a $ref == ./common/types.json#/components/schemas/MySchema
        //   types.json has a $ref == ./examples.json#/components/examples/MyExample
        //
        // In the above scenario, the canonical form of the $ref from types.json should probably be:
        //   ./types/examples.json#/components/examples/MyExample
        //
        // This is because it will be resolved relative to design.json not relative to types.json

        if (refStack.isEmpty() || reference.getAbsPart() != null) {
            return reference;
        } else {
            return new Reference(absolutePartFromStack(), reference.getRelPart());
        }
    }

    private String absolutePartFromStack() {
        for (int idx = this.refStack.size() - 1; idx >= 0; idx--) {
            Reference reference = this.refStack.get(idx);
            String abs = reference.getAbsPart();
            if (abs != null) {
                return abs;
            }
        }
        return null;
    }

}
