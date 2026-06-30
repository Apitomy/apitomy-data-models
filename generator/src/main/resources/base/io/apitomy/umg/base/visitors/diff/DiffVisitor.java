package io.apitomy.umg.base.visitors.diff;


/**
 * Base class for generated per-spec-version DiffVisitors. Generated subclasses
 * add typed, field-specific methods for each entity and property.
 */
public abstract class DiffVisitor {

    /**
     * Returns the map pairing strategy for a map field, or null to use the default
     * (key-based pairing).
     */
    public MapPairingStrategy<?, ?> getMapPairingStrategy(String propertyName) {
        return null;
    }

    /**
     * Returns the list pairing strategy for a list field, or null to use the default
     * (index-based pairing).
     */
    public ListPairingStrategy<?, ?> getListPairingStrategy(String propertyName) {
        return null;
    }
}
