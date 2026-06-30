package io.test.synthetic.visitors.diff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Strategy for pairing elements from two collections to produce a
 * {@link CollectionDiff}.
 *
 * @param <K>
 *            the key type (String for maps, Integer for lists)
 * @param <V>
 *            the value type
 */
public interface PairingStrategy<K, V> {

	CollectionDiff<K, V> pair(Map<K, V> original, Map<K, V> updated);

	static <K, V> CollectionDiff<K, V> pairByKey(Map<K, V> original, Map<K, V> updated) {
		Map<K, V> orig = original != null ? original : new LinkedHashMap<>();
		Map<K, V> upd = updated != null ? updated : new LinkedHashMap<>();

		Set<K> allKeys = new LinkedHashSet<>();
		allKeys.addAll(orig.keySet());
		allKeys.addAll(upd.keySet());

		List<CollectionDiff.Entry<K, V>> added = new ArrayList<>();
		List<CollectionDiff.Entry<K, V>> removed = new ArrayList<>();
		List<CollectionDiff.MatchedPair<K, V>> matched = new ArrayList<>();

		for (K key : allKeys) {
			boolean inOrig = orig.containsKey(key);
			boolean inUpd = upd.containsKey(key);
			if (inOrig && inUpd) {
				matched.add(new CollectionDiff.MatchedPair<>(key, orig.get(key), upd.get(key)));
			} else if (inUpd) {
				added.add(new CollectionDiff.Entry<>(key, upd.get(key)));
			} else {
				removed.add(new CollectionDiff.Entry<>(key, orig.get(key)));
			}
		}

		return new CollectionDiff<>(added, removed, matched);
	}
}
