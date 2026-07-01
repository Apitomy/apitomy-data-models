package io.apitomy.umg.base.visitors.diff;

import java.util.List;
import java.util.Map;

import io.apitomy.umg.base.Any;
import io.apitomy.umg.base.visitors.TraversalContext;
import io.apitomy.umg.base.visitors.TraversalContextImpl;

/**
 * Base class for generated diff traversers. Provides shared collection pairing logic
 * and path tracking via two TraversalContext instances (one per side).
 *
 * @param <P> the pairing key type
 * @param <V> the spec-version-specific DiffVisitor subclass
 */
public class AbstractDiffTraverser<P, V> {

    protected final V visitor;
    protected final PairingStrategyProvider<P> pairingProvider;
    protected final TraversalContextImpl originalContext;
    protected final TraversalContextImpl updatedContext;

    @SuppressWarnings("unchecked")
    protected AbstractDiffTraverser(V visitor) {
        this.visitor = visitor;
        this.pairingProvider = (PairingStrategyProvider<P>) new DefaultPairingStrategyProvider();
        this.originalContext = new TraversalContextImpl();
        this.updatedContext = new TraversalContextImpl();
    }

    @SuppressWarnings("unchecked")
    protected AbstractDiffTraverser(V visitor, PairingStrategyProvider<P> pairingProvider) {
        this.visitor = visitor;
        this.pairingProvider = pairingProvider;
        this.originalContext = new TraversalContextImpl();
        this.updatedContext = new TraversalContextImpl();
    }

    public TraversalContext getOriginalContext() {
        return originalContext;
    }

    public TraversalContext getUpdatedContext() {
        return updatedContext;
    }

    /**
     * Push a property name onto both contexts (shared by both sides).
     */
    protected void pushProperty(String propertyName) {
        originalContext.pushProperty(propertyName);
        updatedContext.pushProperty(propertyName);
    }

    /**
     * Push collection element positions onto the respective contexts.
     */
    protected void pushElement(PairingKey key) {
        if (key.getOriginalKey() != null) {
            originalContext.pushMapIndex(key.getOriginalKey());
        } else if (key.getOriginalIndex() != null) {
            originalContext.pushListIndex(key.getOriginalIndex());
        }
        if (key.getUpdatedKey() != null) {
            updatedContext.pushMapIndex(key.getUpdatedKey());
        } else if (key.getUpdatedIndex() != null) {
            updatedContext.pushListIndex(key.getUpdatedIndex());
        }
    }

    /**
     * Pop from both contexts.
     */
    protected void pop() {
        originalContext.pop();
        updatedContext.pop();
    }

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
