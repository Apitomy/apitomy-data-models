package io.apitomy.umg.base.visitors.diff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default map pairing strategy — pairs entries by key.
 */
public class DefaultMapPairingStrategy<V> implements MapPairingStrategy<DefaultPairingKey, V> {

    @Override
    public CollectionDiff<DefaultPairingKey, V> pair(Map<String, V> original, Map<String, V> updated) {
        return CollectionDiff.pairByKey(toPairingKeyMap(original), toPairingKeyMap(updated));
    }

    private static <V> Map<DefaultPairingKey, V> toPairingKeyMap(Map<String, V>  map) {
        if (map == null) return new LinkedHashMap<>();
        Map<DefaultPairingKey, V> res = new LinkedHashMap<>();
        for (Map.Entry<String, V> entry : map.entrySet()) {
            res.put(new DefaultPairingKey(entry.getKey()), entry.getValue());
        }
        return res;
    }
}
