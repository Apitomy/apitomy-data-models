package io.apitomy.umg.base.visitors.diff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default list pairing strategy — pairs entries by index.
 */
public class IndexPairingStrategy<V> implements ListPairingStrategy<DefaultPairingKey, V> {

    @Override
    public CollectionDiff<DefaultPairingKey, V> pair(List<V> original, List<V> updated) {
        return CollectionDiff.pairByKey(toPairingKeyMap(original), toPairingKeyMap(updated));
    }

    private static <V> Map<DefaultPairingKey, V> toPairingKeyMap(List<V> list) {
        if (list == null) return new LinkedHashMap<>();
        Map<DefaultPairingKey, V> res = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            res.put(new DefaultPairingKey(i), list.get(i));
        }
        return res;
    }
}
