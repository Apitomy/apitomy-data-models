package io.test.synthetic.visitors.diff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Result of pairing two collections. Contains entries that were added, removed,
 * or matched between the original and updated collections.
 *
 * @param <P>
 *            the pairing key type (String for maps, Integer for lists)
 * @param <T>
 *            the value type
 */
public class CollectionDiff<P, T> {

	private final List<Entry<P, T>> added;
	private final List<Entry<P, T>> removed;
	private final List<MatchedPair<P, T>> matched;

	public CollectionDiff(List<Entry<P, T>> added, List<Entry<P, T>> removed, List<MatchedPair<P, T>> matched) {
		this.added = added;
		this.removed = removed;
		this.matched = matched;
	}

	public List<Entry<P, T>> getAdded() {
		return added;
	}

	public List<Entry<P, T>> getRemoved() {
		return removed;
	}

	public List<MatchedPair<P, T>> getMatched() {
		return matched;
	}

	public boolean hasChanges() {
		return !added.isEmpty() || !removed.isEmpty();
	}

	/**
	 * Pairs two maps by key. Entries with matching keys are paired; keys in only
	 * one map are classified as added or removed.
	 */
	public static <P, T> CollectionDiff<P, T> pairByKey(Map<P, T> original, Map<P, T> updated) {
		Map<P, T> orig = original != null ? original : new LinkedHashMap<>();
		Map<P, T> upd = updated != null ? updated : new LinkedHashMap<>();

		Set<P> allKeys = new LinkedHashSet<>();
		allKeys.addAll(orig.keySet());
		allKeys.addAll(upd.keySet());

		List<Entry<P, T>> added = new ArrayList<>();
		List<Entry<P, T>> removed = new ArrayList<>();
		List<MatchedPair<P, T>> matched = new ArrayList<>();

		for (P key : allKeys) {
			boolean inOrig = orig.containsKey(key);
			boolean inUpd = upd.containsKey(key);
			if (inOrig && inUpd) {
				matched.add(new MatchedPair<>(key, orig.get(key), upd.get(key)));
			} else if (inUpd) {
				added.add(new Entry<>(key, upd.get(key)));
			} else {
				removed.add(new Entry<>(key, orig.get(key)));
			}
		}

		return new CollectionDiff<>(added, removed, matched);
	}

	public static class Entry<P, T> {
		private final P key;
		private final T value;

		public Entry(P key, T value) {
			this.key = key;
			this.value = value;
		}

		public P getKey() {
			return key;
		}
		public T getValue() {
			return value;
		}
	}

	public static class MatchedPair<P, T> {
		private final P key;
		private final T original;
		private final T updated;

		public MatchedPair(P key, T original, T updated) {
			this.key = key;
			this.original = original;
			this.updated = updated;
		}

		public P getKey() {
			return key;
		}
		public T getOriginal() {
			return original;
		}
		public T getUpdated() {
			return updated;
		}
	}
}
