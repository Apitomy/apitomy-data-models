package io.apitomy.umg.models.concept.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a primitive (built-in) type: string, boolean, number, integer, object, any.
 */
public enum PrimitiveType implements Type {

    STRING("string", String.class),
    BOOLEAN("boolean", Boolean.class),
    NUMBER("number", Number.class),
    INTEGER("integer", Integer.class),
    OBJECT("object", ObjectNode.class),
    ANY("any", JsonNode.class);

    public static final String NAMESPACE = "<primitive>";

    private static final Map<String, PrimitiveType> BY_NAME = new HashMap<>();

    static {
        for (var type : PrimitiveType.values()) {
            BY_NAME.put(type.name, type);
        }
    }

    public static PrimitiveType getByName(String name) {
        return BY_NAME.get(name);
    }

    public static boolean isPrimitive(String name) {
        return BY_NAME.containsKey(name);
    }

    private final String name;
    private final Class<?> javaClass;
    private final RawType rawType;

    PrimitiveType(String name, Class<?> javaClass) {
        this.name = name;
        this.javaClass = javaClass;
        this.rawType = RawType.parse(name);
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RawType getRawType() {
        return rawType;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    @Override
    public boolean isRoot() { return false; }

    @Override
    public void setRoot(boolean root) {
        throw new UnsupportedOperationException("Primitive type cannot be root");
    }

    @Override
    public Type getParent() { return null; }

    @Override
    public void setParent(Type parent) {
        throw new UnsupportedOperationException("Primitive type cannot have a parent");
    }

    @Override
    public boolean isLeaf() { return false; }

    @Override
    public void setLeaf(boolean leaf) {
        throw new UnsupportedOperationException("Primitive type cannot be leaf");
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public PrimitiveType copy() {
        return this;
    }

    @Override
    public boolean isPrimitiveType() { return true; }
}
