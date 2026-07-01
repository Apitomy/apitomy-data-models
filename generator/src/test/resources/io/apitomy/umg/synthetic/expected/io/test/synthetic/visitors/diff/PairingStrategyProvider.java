package io.test.synthetic.visitors.diff;

/**
 * Provides pairing strategies for collection fields during diff traversal. Pass
 * a custom provider to the DiffTraverser constructor to control how map and
 * list fields are paired.
 */
public interface PairingStrategyProvider<P extends PairingKey> {

	<T> MapPairingStrategy<P, T> getMapStrategy(String propertyName);

	<T> ListPairingStrategy<P, T> getListStrategy(String propertyName);
}
