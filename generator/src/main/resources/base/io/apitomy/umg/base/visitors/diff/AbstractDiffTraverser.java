package io.apitomy.umg.base.visitors.diff;

import java.util.List;
import java.util.Map;

import io.apitomy.umg.base.Node;

/**
 * Base class for generated diff traversers. Provides shared collection diffing logic.
 * Subclasses (generated per spec version) provide entity-specific field iteration
 * and call typed visitor methods.
 *
 * @param <V> the spec-version-specific DiffVisitor subclass
 */
public class AbstractDiffTraverser<V extends DiffVisitor> {

    protected final V visitor;

    protected AbstractDiffTraverser(V visitor) {
        this.visitor = visitor;
    }

    @SuppressWarnings("unchecked")
    protected <T> CollectionDiff<String, T> pairMap(String propertyName, Map<String, T> original, Map<String, T> updated) {
        PairingStrategy<String, T> strategy =
                (PairingStrategy<String, T>) visitor.getPairingStrategy(propertyName);
        if (strategy == null) {
            strategy = new KeyPairingStrategy<>();
        }
        return strategy.pair(
                original != null ? original : Map.of(),
                updated != null ? updated : Map.of());
    }

    @SuppressWarnings("unchecked")
    protected <T> CollectionDiff<Integer, T> pairList(String propertyName, List<T> original, List<T> updated) {
        PairingStrategy<Integer, T> strategy =
                (PairingStrategy<Integer, T>) visitor.getPairingStrategy(propertyName);
        if (strategy == null) {
            strategy = new IndexPairingStrategy<>();
        }
        return strategy.pair(
                IndexPairingStrategy.toIndexMap(original),
                IndexPairingStrategy.toIndexMap(updated));
    }

    /**
     * Dispatches to the correct entity-specific traverse method.
     * Generated subclasses override this with type-based dispatch.
     */
    protected void traverseNode(Node original, Node updated) {
    }
}
