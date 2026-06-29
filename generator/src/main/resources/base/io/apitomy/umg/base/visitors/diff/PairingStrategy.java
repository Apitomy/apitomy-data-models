package io.apitomy.umg.base.visitors.diff;

/**
 * Strategy for pairing elements from two collections to produce a {@link CollectionDiff}.
 *
 * @param <K> the key type (String for maps, Integer for lists)
 * @param <V> the value type
 */
public interface PairingStrategy<K, V> {

    CollectionDiff<K, V> pair(java.util.Map<K, V> original, java.util.Map<K, V> updated);
}
