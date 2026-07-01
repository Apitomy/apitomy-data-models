package io.apitomy.umg.base.visitors.diff;

import java.util.List;

/**
 * Strategy for pairing entries from two lists.
 *
 * @param <P> the pairing key type (e.g., Integer for index-based pairing)
 * @param <T> the value type
 */
public interface ListPairingStrategy<P extends PairingKey, T> {

    CollectionDiff<P, T> pair(List<T> original, List<T> updated);
}
