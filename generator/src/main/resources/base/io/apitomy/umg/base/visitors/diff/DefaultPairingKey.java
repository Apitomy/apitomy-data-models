package io.apitomy.umg.base.visitors.diff;


/**
 * Default pairing key for index-based (lists) and key-based (maps) pairing.
 * For the default strategies, original and updated positions are the same
 * (same index or same key).
 */
public class DefaultPairingKey implements PairingKey {

    private final Integer index;
    private final String key;

    public DefaultPairingKey(int index) {
        this.index = index;
        this.key = null;
    }

    public DefaultPairingKey(String key) {
        this.index = null;
        this.key = key;
    }

    @Override
    public Integer getOriginalIndex() {
        return index;
    }

    @Override
    public Integer getUpdatedIndex() {
        return index;
    }

    @Override
    public String getOriginalKey() {
        return key;
    }

    @Override
    public String getUpdatedKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultPairingKey)) return false;
        DefaultPairingKey that = (DefaultPairingKey) o;
        return (index == null ? that.index == null : index.equals(that.index))
                && (key == null ? that.key == null : key.equals(that.key));
    }

    @Override
    public int hashCode() {
        int result = index == null ? 0 : index.hashCode();
        result = 31 * result + (key == null ? 0 : key.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return key != null ? key : String.valueOf(index);
    }
}
