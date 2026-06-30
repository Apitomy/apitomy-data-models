package io.test.synthetic.visitors.diff;

/**
 * Default provider: key-based pairing for maps, index-based for lists.
 */
public class DefaultPairingStrategyProvider implements PairingStrategyProvider {

	@Override
	public <V> MapPairingStrategy<String, V> getMapStrategy(String propertyName) {
		return new KeyPairingStrategy<>();
	}

	@Override
	public <V> ListPairingStrategy<Integer, V> getListStrategy(String propertyName) {
		return new IndexPairingStrategy<>();
	}
}
