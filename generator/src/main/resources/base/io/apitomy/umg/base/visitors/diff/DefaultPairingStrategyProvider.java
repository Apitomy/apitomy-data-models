package io.apitomy.umg.base.visitors.diff;

/**
 * Default provider: key-based pairing for maps, index-based for lists.
 */
public class DefaultPairingStrategyProvider implements PairingStrategyProvider<DefaultPairingKey> {

    @Override
    public <T> MapPairingStrategy<DefaultPairingKey, T> getMapStrategy(String propertyName) {
        return new DefaultMapPairingStrategy<>();
    }

    @Override
    public <T> ListPairingStrategy<DefaultPairingKey, T> getListStrategy(String propertyName) {
        return new DefaultListPairingStrategy<>();
    }
}
