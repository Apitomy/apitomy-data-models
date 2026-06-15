package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaFullSchemaListUnion;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.union.StringStringListUnion;

import java.util.List;
import java.util.Map;

/**
 * Provides property access for JSON Schema entities via the common {@link JFullSchema}
 * trait interface.
 */
public final class SchemaAccessor {

    private final Node node;
    private final JFullSchema schema;

    private SchemaAccessor(Node node) {
        this.node = node;
        if (node instanceof JFullSchema s) {
            this.schema = s;
        } else {
            throw new IllegalArgumentException("Expected JFullSchema, got: "
                    + (node != null ? node.getClass().getName() : "null"));
        }
    }

    public static SchemaAccessor wrap(Node node) {
        return new SchemaAccessor(node);
    }

    public Node node() {
        return node;
    }

    public StringStringListUnion getType() { return schema.getType(); }
    public String getTitle() { return schema.getTitle(); }
    public String getDescription() { return schema.getDescription(); }
    public JsonNode getDefault() { return schema.getDefault(); }
    public List<JsonNode> getEnum() { return schema.getEnum(); }
    public String getFormat() { return schema.getFormat(); }
    public String getPattern() { return schema.getPattern(); }
    public Number getMultipleOf() { return schema.getMultipleOf(); }
    public Number getMinimum() { return schema.getMinimum(); }
    public Number getMaximum() { return schema.getMaximum(); }
    public Integer getMinLength() { return schema.getMinLength(); }
    public Integer getMaxLength() { return schema.getMaxLength(); }
    public Integer getMinItems() { return schema.getMinItems(); }
    public Integer getMaxItems() { return schema.getMaxItems(); }
    public Boolean isUniqueItems() { return schema.isUniqueItems(); }
    public Integer getMinProperties() { return schema.getMinProperties(); }
    public Integer getMaxProperties() { return schema.getMaxProperties(); }
    public List<String> getRequired() { return schema.getRequired(); }
    public JsonSchema getAdditionalProperties() { return schema.getAdditionalProperties(); }
    public Map<String, JsonSchema> getProperties() { return schema.getProperties(); }
    public Map<String, JsonSchema> getPatternProperties() { return schema.getPatternProperties(); }
    public List<JsonSchema> getAllOf() { return schema.getAllOf(); }
    public List<JsonSchema> getAnyOf() { return schema.getAnyOf(); }
    public List<JsonSchema> getOneOf() { return schema.getOneOf(); }
    public JsonSchema getNot() { return schema.getNot(); }
    public String get$schema() { return schema.get$schema(); }

    public String get$ref() {
        if (schema instanceof io.apitomy.datamodels.models.Referenceable ref) {
            return ref.get$ref();
        }
        return null;
    }

    public String getTypeString() {
        var type = getType();
        if (type != null && type.isString()) {
            return type.asString();
        }
        return null;
    }

    public List<String> getTypeList() {
        var type = getType();
        if (type == null) return null;
        if (type.isString()) return List.of(type.asString());
        if (type.isStringList()) return type.asStringList();
        return null;
    }

    public boolean isInstanceOf(Class<?> clazz) {
        return clazz.isInstance(node);
    }

    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> clazz) {
        return (T) node;
    }

    @Override
    public String toString() {
        return "SchemaAccessor{node=%s}".formatted(node.getClass().getSimpleName());
    }
}
