package io.apitomy.umg.base.visitors.diff;

import java.util.List;

/**
 * Strategy for pairing entries from two lists.
 */
public interface ListPairingStrategy<V> {

    CollectionDiff<Integer, V> pair(List<V> original, List<V> updated);
}
