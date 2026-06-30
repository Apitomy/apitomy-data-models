package io.apitomy.umg.base.visitors.diff;

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
 * @param <K> the key type (String for maps, Integer for lists)
 * @param <V> the value type
 */
public class CollectionDiff<K, V> {

    private final List<Entry<K, V>> added;
    private final List<Entry<K, V>> removed;
    private final List<MatchedPair<K, V>> matched;

    public CollectionDiff(List<Entry<K, V>> added, List<Entry<K, V>> removed,
            List<MatchedPair<K, V>> matched) {
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

    /**
     * Pairs two maps by key. Entries with matching keys are paired;
     * keys in only one map are classified as added or removed.
     */
    public static <K, V> CollectionDiff<K, V> pairByKey(Map<K, V> original, Map<K, V> updated) {
        Map<K, V> orig = original != null ? original : new LinkedHashMap<>();
        Map<K, V> upd = updated != null ? updated : new LinkedHashMap<>();

        Set<K> allKeys = new LinkedHashSet<>();
        allKeys.addAll(orig.keySet());
        allKeys.addAll(upd.keySet());

        List<Entry<K, V>> added = new ArrayList<>();
        List<Entry<K, V>> removed = new ArrayList<>();
        List<MatchedPair<K, V>> matched = new ArrayList<>();

        for (K key : allKeys) {
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

    public static class Entry<K, V> {
        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() { return key; }
        public V getValue() { return value; }
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

        public K getKey() { return key; }
        public V getOriginal() { return original; }
        public V getUpdated() { return updated; }
    }
}
