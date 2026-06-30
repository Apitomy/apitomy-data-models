package io.test.synthetic.visitors.diff;

/**
 * Default provider: key-based pairing for maps, index-based for lists.
 */
public class DefaultPairingStrategyProvider implements PairingStrategyProvider<DefaultPairingKey> {

	@Override
	public <V> MapPairingStrategy<DefaultPairingKey, V> getMapStrategy(String propertyName) {
		return new KeyPairingStrategy<>();
	}

	@Override
	public <V> ListPairingStrategy<DefaultPairingKey, V> getListStrategy(String propertyName) {
		return new IndexPairingStrategy<>();
	}
}
