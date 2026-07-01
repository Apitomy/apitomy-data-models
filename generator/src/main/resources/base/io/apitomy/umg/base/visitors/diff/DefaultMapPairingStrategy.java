package io.apitomy.umg.base.visitors.diff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default map pairing strategy — pairs entries by key.
 */
public class DefaultMapPairingStrategy<T> implements MapPairingStrategy<DefaultPairingKey, T> {

    @Override
    public CollectionDiff<DefaultPairingKey, T> pair(Map<String, T> original, Map<String, T> updated) {
        return CollectionDiff.pairByKey(toPairingKeyMap(original), toPairingKeyMap(updated));
    }

    private static <T> Map<DefaultPairingKey, T> toPairingKeyMap(Map<String, T>  map) {
        if (map == null) return new LinkedHashMap<>();
        Map<DefaultPairingKey, T> res = new LinkedHashMap<>();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            res.put(new DefaultPairingKey(entry.getKey()), entry.getValue());
        }
        return res;
    }
}
