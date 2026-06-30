package io.test.synthetic.visitors.diff;

/**
 * Provides pairing strategies for collection fields during diff traversal. Pass
 * a custom provider to the DiffTraverser constructor to control how map and
 * list fields are paired.
 */
public interface PairingStrategyProvider {

	<V> MapPairingStrategy<String, V> getMapStrategy(String propertyName);

	<V> ListPairingStrategy<Integer, V> getListStrategy(String propertyName);
}
