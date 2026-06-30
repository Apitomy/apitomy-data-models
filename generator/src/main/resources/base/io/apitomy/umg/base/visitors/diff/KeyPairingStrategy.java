package io.apitomy.umg.base.visitors.diff;

import java.util.Map;

/**
 * Pairs map entries by key.
 */
public class KeyPairingStrategy<V> implements PairingStrategy<String, V> {

    @Override
    public CollectionDiff<String, V> pair(Map<String, V> original, Map<String, V> updated) {
        return PairingStrategy.pairByKey(original, updated);
    }
}
