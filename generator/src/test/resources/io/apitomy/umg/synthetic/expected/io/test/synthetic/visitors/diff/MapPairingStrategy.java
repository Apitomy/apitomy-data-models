package io.test.synthetic.visitors.diff;

import java.util.Map;

/**
 * Strategy for pairing entries from two maps.
 */
public interface MapPairingStrategy<V> {

	CollectionDiff<String, V> pair(Map<String, V> original, Map<String, V> updated);
}
