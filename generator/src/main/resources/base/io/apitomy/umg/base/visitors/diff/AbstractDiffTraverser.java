package io.apitomy.umg.base.visitors.diff;

import java.util.List;
import java.util.Map;

import io.apitomy.umg.base.Any;
import io.apitomy.umg.base.Node;
import io.apitomy.umg.base.union.Union;

/**
 * Base class for generated diff traversers. Provides shared collection diffing logic.
 * Subclasses (generated per spec version) provide entity-specific field iteration.
 */
public class AbstractDiffTraverser {

    protected final DiffVisitor visitor;

    protected AbstractDiffTraverser(DiffVisitor visitor) {
        this.visitor = visitor;
    }

    @SuppressWarnings("unchecked")
    protected <V> void diffMap(String propertyName, Map<String, V> original, Map<String, V> updated) {
        PairingStrategy<String, V> strategy =
                (PairingStrategy<String, V>) visitor.getPairingStrategy(propertyName);
        if (strategy == null) {
            strategy = new KeyPairingStrategy<>();
        }

        CollectionDiff<String, V> diff = strategy.pair(
                original != null ? original : Map.of(),
                updated != null ? updated : Map.of());
        visitor.diffMap(propertyName, diff);

        for (CollectionDiff.MatchedPair<String, V> pair : diff.getMatched()) {
            recurseIntoPair(pair.getOriginal(), pair.getUpdated());
        }
    }

    @SuppressWarnings("unchecked")
    protected <V> void diffList(String propertyName, List<V> original, List<V> updated) {
        PairingStrategy<Integer, V> strategy =
                (PairingStrategy<Integer, V>) visitor.getPairingStrategy(propertyName);
        if (strategy == null) {
            strategy = new IndexPairingStrategy<>();
        }

        CollectionDiff<Integer, V> diff = strategy.pair(
                IndexPairingStrategy.toIndexMap(original),
                IndexPairingStrategy.toIndexMap(updated));
        visitor.diffList(propertyName, diff);

        for (CollectionDiff.MatchedPair<Integer, V> pair : diff.getMatched()) {
            recurseIntoPair(pair.getOriginal(), pair.getUpdated());
        }
    }

    protected void diffEntityField(String propertyName, Node original, Node updated) {
        visitor.diffEntity(propertyName, original, updated);
        if (original != null && updated != null) {
            traverseNode(original, updated);
        }
    }

    protected void diffUnionField(String propertyName, Union original, Union updated) {
        visitor.diffUnion(propertyName, original, updated);
    }

    private void recurseIntoPair(Object original, Object updated) {
        if (original instanceof Node && updated instanceof Node) {
            traverseNode((Node) original, (Node) updated);
        } else if (original instanceof Union && updated instanceof Union) {
            // Union pairs in collections — visitor handles dispatch
            visitor.diffUnion(null, (Union) original, (Union) updated);
        }
    }

    /**
     * Dispatches to the correct entity-specific traverse method.
     * Generated subclasses override this with type-based dispatch.
     */
    protected void traverseNode(Node original, Node updated) {
    }
}
