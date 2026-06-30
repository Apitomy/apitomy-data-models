package io.apitomy.umg.base.visitors.diff;


/**
 * Base class for generated per-spec-version DiffVisitors. Generated subclasses
 * add typed, field-specific methods for each entity and property.
 *
 * <p>The traverser auto-recurses into entity fields and matched collection pairs.
 */
public abstract class DiffVisitor {

    /**
     * Returns the pairing strategy for a collection field, or null to use the default
     * (key-based for maps, index-based for lists).
     */
    public PairingStrategy<?, ?> getPairingStrategy(String propertyName) {
        return null;
    }
}
