package io.apitomy.umg.base.visitors.diff;

import java.util.List;
import java.util.Map;

import io.apitomy.umg.base.Any;

/**
 * Base class for generated diff traversers. Provides shared collection pairing logic.
 * Subclasses (generated per spec version) provide entity-specific field iteration
 * and call typed visitor methods.
 *
 * @param <P> the pairing key type
 * @param <V> the spec-version-specific DiffVisitor subclass
 */
public class AbstractDiffTraverser<P, V> {

    protected final V visitor;
    protected final PairingStrategyProvider<P> pairingProvider;

    @SuppressWarnings("unchecked")
    protected AbstractDiffTraverser(V visitor) {
        this.visitor = visitor;
        this.pairingProvider = (PairingStrategyProvider<P>) new DefaultPairingStrategyProvider();
    }

    protected AbstractDiffTraverser(V visitor, PairingStrategyProvider<P> pairingProvider) {
        this.visitor = visitor;
        this.pairingProvider = pairingProvider;
    }

    /**
     * Public entry point — accepts Any (Node or Union).
     * Generated subclasses override this with type-based dispatch
     * to union traverse methods and entity traverse methods.
     */
    public void traverse(Any original, Any updated) {
    }

    protected <T> CollectionDiff<P, T> pairMap(String propertyName, Map<String, T> original, Map<String, T> updated) {
        MapPairingStrategy<P, T> strategy = pairingProvider.getMapStrategy(propertyName);
        return strategy.pair(original, updated);
    }

    protected <T> CollectionDiff<P, T> pairList(String propertyName, List<T> original, List<T> updated) {
        ListPairingStrategy<P, T> strategy = pairingProvider.getListStrategy(propertyName);
        return strategy.pair(original, updated);
    }

}
