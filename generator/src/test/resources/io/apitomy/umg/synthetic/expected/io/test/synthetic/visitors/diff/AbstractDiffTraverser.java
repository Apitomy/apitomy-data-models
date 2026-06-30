package io.test.synthetic.visitors.diff;

import io.test.synthetic.Node;
import java.util.List;
import java.util.Map;

/**
 * Base class for generated diff traversers. Provides shared collection pairing
 * logic. Subclasses (generated per spec version) provide entity-specific field
 * iteration and call typed visitor methods.
 *
 * @param <V>
 *            the spec-version-specific DiffVisitor subclass
 */
public class AbstractDiffTraverser<V extends DiffVisitor> {

	protected final V visitor;

	protected AbstractDiffTraverser(V visitor) {
		this.visitor = visitor;
	}

	@SuppressWarnings("unchecked")
	protected <T> CollectionDiff<String, T> pairMap(String propertyName, Map<String, T> original,
			Map<String, T> updated) {
		MapPairingStrategy<String, T> strategy = (MapPairingStrategy<String, T>) visitor
				.getMapPairingStrategy(propertyName);
		if (strategy == null) {
			strategy = new KeyPairingStrategy<>();
		}
		return strategy.pair(original, updated);
	}

	@SuppressWarnings("unchecked")
	protected <T> CollectionDiff<Integer, T> pairList(String propertyName, List<T> original, List<T> updated) {
		ListPairingStrategy<Integer, T> strategy = (ListPairingStrategy<Integer, T>) visitor
				.getListPairingStrategy(propertyName);
		if (strategy == null) {
			strategy = new IndexPairingStrategy<>();
		}
		return strategy.pair(original, updated);
	}

	/**
	 * Dispatches to the correct entity-specific traverse method. Generated
	 * subclasses override this with type-based dispatch.
	 */
	protected void traverseNode(Node original, Node updated) {
	}
}
