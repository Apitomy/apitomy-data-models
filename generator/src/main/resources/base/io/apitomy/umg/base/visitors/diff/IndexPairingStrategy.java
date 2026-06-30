package io.apitomy.umg.base.visitors.diff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default list pairing strategy — pairs entries by index.
 */
public class IndexPairingStrategy<V> implements ListPairingStrategy<V> {

    @Override
    public CollectionDiff<Integer, V> pair(List<V> original, List<V> updated) {
        return CollectionDiff.pairByKey(toIndexMap(original), toIndexMap(updated));
    }

    private static <V> Map<Integer, V> toIndexMap(List<V> list) {
        if (list == null) return new LinkedHashMap<>();
        Map<Integer, V> map = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            map.put(i, list.get(i));
        }
        return map;
    }
}
