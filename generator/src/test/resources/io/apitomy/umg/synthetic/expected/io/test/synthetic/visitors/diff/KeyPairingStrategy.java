package io.test.synthetic.visitors.diff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pairs map entries by key. Entries with matching keys are paired; keys present
 * in only one map are added/removed.
 */
public class KeyPairingStrategy<V> implements PairingStrategy<String, V> {

	@Override
	public CollectionDiff<String, V> pair(Map<String, V> original, Map<String, V> updated) {
		Map<String, V> orig = original != null ? original : new LinkedHashMap<>();
		Map<String, V> upd = updated != null ? updated : new LinkedHashMap<>();

		Set<String> allKeys = new LinkedHashSet<>();
		allKeys.addAll(orig.keySet());
		allKeys.addAll(upd.keySet());

		List<CollectionDiff.Entry<String, V>> added = new ArrayList<>();
		List<CollectionDiff.Entry<String, V>> removed = new ArrayList<>();
		List<CollectionDiff.MatchedPair<String, V>> matched = new ArrayList<>();

		for (String key : allKeys) {
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
