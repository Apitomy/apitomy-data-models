package io.apitomy.umg.base.visitors.diff;

import java.util.Map;

/**
 * Default map pairing strategy — pairs entries by key.
 */
public class KeyPairingStrategy<V> implements MapPairingStrategy<String, V> {

    @Override
    public CollectionDiff<String, V> pair(Map<String, V> original, Map<String, V> updated) {
        return CollectionDiff.pairByKey(original, updated);
    }
}
