package io.test.synthetic.visitors.diff;

import java.util.Map;

/**
 * Strategy for pairing entries from two maps.
 *
 * @param <P>
 *            the pairing key type (e.g., String for key-based pairing)
 * @param <V>
 *            the value type
 */
public interface MapPairingStrategy<P, V> {

	CollectionDiff<P, V> pair(Map<String, V> original, Map<String, V> updated);
}
