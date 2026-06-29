package io.test.synthetic.visitors.diff;

import io.test.synthetic.Node;
import io.test.synthetic.union.Union;

/**
 * Visitor for paired tree traversal. The generated DiffTraverser walks two
 * trees in parallel, calling these methods for each field. Subclasses override
 * the methods they care about.
 *
 * <p>
 * The traverser auto-recurses into entity fields and matched collection pairs.
 * Override {@link #visitEntityPair} to skip recursion for specific entities.
 */
public abstract class DiffVisitor {

	/**
	 * Called before traversing fields of an entity pair. Return false to skip
	 * field-level traversal of this pair.
	 */
	public boolean visitEntityPair(Node original, Node updated) {
		return true;
	}

	/**
	 * Called for each primitive-typed field (String, Boolean, Number, etc.).
	 */
	public void diffPrimitive(String propertyName, Object original, Object updated) {
	}

	/**
	 * Called for each entity-typed field. The traverser auto-recurses into both
	 * entities after this call (if both are non-null).
	 */
	public void diffEntity(String propertyName, Node original, Node updated) {
	}

	/**
	 * Called for each union-typed field.
	 */
	public void diffUnion(String propertyName, Union original, Union updated) {
	}

	/**
	 * Called for each list-typed field with the pairing result. The traverser
	 * auto-recurses into matched entity/union pairs after this call.
	 */
	public void diffList(String propertyName, CollectionDiff<Integer, ?> diff) {
	}

	/**
	 * Called for each map-typed field with the pairing result. The traverser
	 * auto-recurses into matched entity/union pairs after this call.
	 */
	public void diffMap(String propertyName, CollectionDiff<String, ?> diff) {
	}

	/**
	 * Returns the pairing strategy for a collection field, or null to use the
	 * default (key-based for maps, index-based for lists).
	 */
	public PairingStrategy<?, ?> getPairingStrategy(String propertyName) {
		return null;
	}
}
