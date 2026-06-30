package io.test.synthetic.visitors.diff;

import java.util.List;

/**
 * Strategy for pairing entries from two lists.
 *
 * @param <P>
 *            the pairing key type (e.g., Integer for index-based pairing)
 * @param <V>
 *            the value type
 */
public interface ListPairingStrategy<P, V> {

	CollectionDiff<P, V> pair(List<V> original, List<V> updated);
}
