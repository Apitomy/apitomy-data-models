package io.test.synthetic.visitors.diff;

import java.util.Map;

/**
 * Strategy for pairing entries from two maps.
 *
 * @param <P>
 *            the pairing key type (e.g., String for key-based pairing)
 * @param <T>
 *            the value type
 */
public interface MapPairingStrategy<P extends PairingKey, T> {

	CollectionDiff<P, T> pair(Map<String, T> original, Map<String, T> updated);
}
