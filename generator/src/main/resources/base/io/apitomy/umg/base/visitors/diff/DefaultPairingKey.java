package io.apitomy.umg.base.visitors.diff;

import java.util.Objects;

/**
 * Default provider: key-based pairing for maps, index-based for lists.
 */
public class DefaultPairingKey {

    private Type type;
    private Integer index;
    private String key;

    public DefaultPairingKey(int index) {
        type = Type.INDEX;
        this.index = index;
    }

    public DefaultPairingKey(String key) {
        type = Type.KEY;
        this.key = key;
    }

    public Type getType() {
        return type;
    }

    public Integer getIndex() {
        return index;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultPairingKey)) return false;
        DefaultPairingKey that = (DefaultPairingKey) o;
        return type == that.type && Objects.equals(index, that.index) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, index, key);
    }

    public enum Type {
        INDEX,
        KEY
    }
}
