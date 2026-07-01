package io.apitomy.umg.base.visitors.diff;

/**
 * Represents a pairing between elements from original and updated collections.
 * Provides access to the original and updated positions as both indices and map keys.
 */
public interface PairingKey {

    /**
     * The index in the original list, or null if not applicable.
     */
    Integer getOriginalIndex();

    /**
     * The index in the updated list, or null if not applicable.
     */
    Integer getUpdatedIndex();

    /**
     * The key in the original map, or null if not applicable.
     */
    String getOriginalKey();

    /**
     * The key in the updated map, or null if not applicable.
     */
    String getUpdatedKey();

    /**
     * Returns the original position as a string (map key or index).
     */
    default String getOriginalPosition() {
        return getOriginalKey() != null ? getOriginalKey() : String.valueOf(getOriginalIndex());
    }

    /**
     * Returns the updated position as a string (map key or index).
     */
    default String getUpdatedPosition() {
        return getUpdatedKey() != null ? getUpdatedKey() : String.valueOf(getUpdatedIndex());
    }
}
