package io.apitomy.umg.base.visitors.diff;

/**
 * Provides pairing strategies for collection fields during diff traversal.
 * Pass a custom provider to the DiffTraverser constructor to control how
 * map and list fields are paired.
 */
public interface PairingStrategyProvider<P extends PairingKey> {

    <V> MapPairingStrategy<P, V> getMapStrategy(String propertyName);

    <V> ListPairingStrategy<P, V> getListStrategy(String propertyName);
}
