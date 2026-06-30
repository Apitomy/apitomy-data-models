package io.apitomy.umg.base.visitors.diff;

import java.util.List;
import java.util.Map;

import io.apitomy.umg.base.Node;

/**
 * Base class for generated diff traversers. Provides shared collection pairing logic.
 * Subclasses (generated per spec version) provide entity-specific field iteration
 * and call typed visitor methods.
 *
 * @param <V> the spec-version-specific DiffVisitor subclass
 */
public class AbstractDiffTraverser<P, V extends DiffVisitor> {

    protected final V visitor;
    protected final PairingStrategyProvider<P> pairingProvider;

    @SuppressWarnings("unchecked")
    protected AbstractDiffTraverser(V visitor) {
        this(visitor, (PairingStrategyProvider<P>) (PairingStrategyProvider<?>) new DefaultPairingStrategyProvider());
    }

    protected AbstractDiffTraverser(V visitor, PairingStrategyProvider<P> pairingProvider) {
        this.visitor = visitor;
        this.pairingProvider = pairingProvider;
    }

    protected <T> CollectionDiff<P, T> pairMap(String propertyName, Map<String, T> original, Map<String, T> updated) {
        MapPairingStrategy<P, T> strategy = pairingProvider.getMapStrategy(propertyName);
        return strategy.pair(original, updated);
    }

    protected <T> CollectionDiff<P, T> pairList(String propertyName, List<T> original, List<T> updated) {
        ListPairingStrategy<P, T> strategy = pairingProvider.getListStrategy(propertyName);
        return strategy.pair(original, updated);
    }

    /**
     * Dispatches to the correct entity-specific traverse method.
     * Generated subclasses override this with type-based dispatch.
     */
    protected void traverseNode(Node original, Node updated) {
    }
}
