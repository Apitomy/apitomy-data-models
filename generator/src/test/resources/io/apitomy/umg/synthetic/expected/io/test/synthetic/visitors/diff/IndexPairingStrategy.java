package io.test.synthetic.visitors.diff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pairs list entries by index. Items at the same index are paired; extra items
 * in the longer list are added/removed.
 */
public class IndexPairingStrategy<V> implements PairingStrategy<Integer, V> {

	@Override
	public CollectionDiff<Integer, V> pair(Map<Integer, V> original, Map<Integer, V> updated) {
		Map<Integer, V> orig = original != null ? original : new LinkedHashMap<>();
		Map<Integer, V> upd = updated != null ? updated : new LinkedHashMap<>();

		int maxIndex = Math.max(orig.size(), upd.size());

		List<CollectionDiff.Entry<Integer, V>> added = new ArrayList<>();
		List<CollectionDiff.Entry<Integer, V>> removed = new ArrayList<>();
		List<CollectionDiff.MatchedPair<Integer, V>> matched = new ArrayList<>();

		for (int i = 0; i < maxIndex; i++) {
			boolean inOrig = orig.containsKey(i);
			boolean inUpd = upd.containsKey(i);
			if (inOrig && inUpd) {
				matched.add(new CollectionDiff.MatchedPair<>(i, orig.get(i), upd.get(i)));
			} else if (inUpd) {
				added.add(new CollectionDiff.Entry<>(i, upd.get(i)));
			} else {
				removed.add(new CollectionDiff.Entry<>(i, orig.get(i)));
			}
		}

		return new CollectionDiff<>(added, removed, matched);
	}

	/**
	 * Converts a list to an index-keyed map for use with this strategy.
	 */
	public static <V> Map<Integer, V> toIndexMap(List<V> list) {
		if (list == null)
			return new LinkedHashMap<>();
		Map<Integer, V> map = new LinkedHashMap<>();
		for (int i = 0; i < list.size(); i++) {
			map.put(i, list.get(i));
		}
		return map;
	}
}
