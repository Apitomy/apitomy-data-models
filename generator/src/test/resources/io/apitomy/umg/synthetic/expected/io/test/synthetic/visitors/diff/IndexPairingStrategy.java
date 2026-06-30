package io.test.synthetic.visitors.diff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pairs list entries by index. Converts lists to index-keyed maps, then
 * delegates to the shared pairing logic.
 */
public class IndexPairingStrategy<V> implements PairingStrategy<Integer, V> {

	@Override
	public CollectionDiff<Integer, V> pair(Map<Integer, V> original, Map<Integer, V> updated) {
		return PairingStrategy.pairByKey(original, updated);
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
