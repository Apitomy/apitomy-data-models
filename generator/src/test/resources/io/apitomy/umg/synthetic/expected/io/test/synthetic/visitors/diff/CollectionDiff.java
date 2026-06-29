package io.test.synthetic.visitors.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of pairing two collections. Contains entries that were added, removed,
 * or matched between the original and updated collections.
 *
 * @param <K>
 *            the key type (String for maps, Integer for lists)
 * @param <V>
 *            the value type
 */
public class CollectionDiff<K, V> {

	private final List<Entry<K, V>> added;
	private final List<Entry<K, V>> removed;
	private final List<MatchedPair<K, V>> matched;

	public CollectionDiff(List<Entry<K, V>> added, List<Entry<K, V>> removed, List<MatchedPair<K, V>> matched) {
		this.added = added;
		this.removed = removed;
		this.matched = matched;
	}

	public List<Entry<K, V>> getAdded() {
		return added;
	}

	public List<Entry<K, V>> getRemoved() {
		return removed;
	}

	public List<MatchedPair<K, V>> getMatched() {
		return matched;
	}

	public boolean hasChanges() {
		return !added.isEmpty() || !removed.isEmpty();
	}

	public static class Entry<K, V> {
		private final K key;
		private final V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}
		public V getValue() {
			return value;
		}
	}

	public static class MatchedPair<K, V> {
		private final K key;
		private final V original;
		private final V updated;

		public MatchedPair(K key, V original, V updated) {
			this.key = key;
			this.original = original;
			this.updated = updated;
		}

		public K getKey() {
			return key;
		}
		public V getOriginal() {
			return original;
		}
		public V getUpdated() {
			return updated;
		}
	}
}
