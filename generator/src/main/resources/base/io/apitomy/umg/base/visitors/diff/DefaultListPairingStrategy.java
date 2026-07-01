package io.apitomy.umg.base.visitors.diff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default list pairing strategy — pairs entries by index.
 */
public class DefaultListPairingStrategy<T> implements ListPairingStrategy<DefaultPairingKey, T> {

    @Override
    public CollectionDiff<DefaultPairingKey, T> pair(List<T> original, List<T> updated) {
        return CollectionDiff.pairByKey(toPairingKeyMap(original), toPairingKeyMap(updated));
    }

    private static <T> Map<DefaultPairingKey, T> toPairingKeyMap(List<T> list) {
        if (list == null) return new LinkedHashMap<>();
        Map<DefaultPairingKey, T> res = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            res.put(new DefaultPairingKey(i), list.get(i));
        }
        return res;
    }
}
