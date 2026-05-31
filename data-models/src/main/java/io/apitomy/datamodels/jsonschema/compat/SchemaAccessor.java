package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.jsonschema.JsonSchemaDocument;
import io.apitomy.datamodels.models.jsonschema.JsonSchemaJSchema;
import io.apitomy.datamodels.models.union.BooleanJSchemaJSchemaListUnion;
import io.apitomy.datamodels.models.union.BooleanJSchemaUnion;
import io.apitomy.datamodels.models.union.StringStringListUnion;

import java.util.List;
import java.util.Map;

/**
 * Provides uniform property access for both JsonSchemaDocument and JsonSchemaJSchema,
 * which have identical properties but no shared schema interface.
 * <p>
 * GENERATOR NOTE: This class exists because the generator creates separate Document
 * and JSchema entities with duplicated properties and no common parent interface.
 * If union-as-root (FEATURES.md F1) is implemented, this class becomes unnecessary.
 */
public final class SchemaAccessor {

    private final Node node;
    private final JsonSchemaDocument doc;
    private final JsonSchemaJSchema schema;

    private SchemaAccessor(Node node) {
        this.node = node;
        if (node instanceof JsonSchemaDocument d) {
            this.doc = d;
            this.schema = null;
        } else if (node instanceof JsonSchemaJSchema s) {
            this.doc = null;
            this.schema = s;
        } else {
            throw new IllegalArgumentException("Expected JsonSchemaDocument or JsonSchemaJSchema, got: "
                    + (node != null ? node.getClass().getName() : "null"));
        }
    }

    public static SchemaAccessor wrap(Node node) {
        return new SchemaAccessor(node);
    }

    public Node node() {
        return node;
    }

    public boolean isDocument() {
        return doc != null;
    }

    // --- Common properties (available on both Document and JSchema at all versions) ---

    public StringStringListUnion getType() {
        return doc != null ? doc.getType() : schema.getType();
    }

    public String getTitle() {
        return doc != null ? doc.getTitle() : schema.getTitle();
    }

    public String getDescription() {
        return doc != null ? doc.getDescription() : schema.getDescription();
    }

    public JsonNode getDefault() {
        return doc != null ? doc.getDefault() : schema.getDefault();
    }

    public List<JsonNode> getEnum() {
        return doc != null ? doc.getEnum() : schema.getEnum();
    }

    public String getFormat() {
        return doc != null ? doc.getFormat() : schema.getFormat();
    }

    public String getPattern() {
        return doc != null ? doc.getPattern() : schema.getPattern();
    }

    public Number getMultipleOf() {
        return doc != null ? doc.getMultipleOf() : schema.getMultipleOf();
    }

    public Number getMinimum() {
        return doc != null ? doc.getMinimum() : schema.getMinimum();
    }

    public Number getMaximum() {
        return doc != null ? doc.getMaximum() : schema.getMaximum();
    }

    public Integer getMinLength() {
        return doc != null ? doc.getMinLength() : schema.getMinLength();
    }

    public Integer getMaxLength() {
        return doc != null ? doc.getMaxLength() : schema.getMaxLength();
    }

    public Integer getMinItems() {
        return doc != null ? doc.getMinItems() : schema.getMinItems();
    }

    public Integer getMaxItems() {
        return doc != null ? doc.getMaxItems() : schema.getMaxItems();
    }

    public Boolean isUniqueItems() {
        return doc != null ? doc.isUniqueItems() : schema.isUniqueItems();
    }

    public Integer getMinProperties() {
        return doc != null ? doc.getMinProperties() : schema.getMinProperties();
    }

    public Integer getMaxProperties() {
        return doc != null ? doc.getMaxProperties() : schema.getMaxProperties();
    }

    public List<String> getRequired() {
        return doc != null ? doc.getRequired() : schema.getRequired();
    }

    public BooleanJSchemaUnion getAdditionalProperties() {
        return doc != null ? doc.getAdditionalProperties() : schema.getAdditionalProperties();
    }

    public Map<String, BooleanJSchemaUnion> getProperties() {
        return doc != null ? doc.getProperties() : schema.getProperties();
    }

    public Map<String, BooleanJSchemaUnion> getPatternProperties() {
        return doc != null ? doc.getPatternProperties() : schema.getPatternProperties();
    }

    public List<BooleanJSchemaUnion> getAllOf() {
        return doc != null ? doc.getAllOf() : schema.getAllOf();
    }

    public List<BooleanJSchemaUnion> getAnyOf() {
        return doc != null ? doc.getAnyOf() : schema.getAnyOf();
    }

    public List<BooleanJSchemaUnion> getOneOf() {
        return doc != null ? doc.getOneOf() : schema.getOneOf();
    }

    public BooleanJSchemaUnion getNot() {
        return doc != null ? doc.getNot() : schema.getNot();
    }

    public String get$schema() {
        return doc != null ? doc.get$schema() : schema.get$schema();
    }

    public String get$ref() {
        // Document does not have $ref (no Referenceable trait); only JSchema does
        if (schema != null && schema instanceof io.apitomy.datamodels.models.Referenceable ref) {
            return ref.get$ref();
        }
        return null;
    }

    /**
     * Get the string value of the type field, or null if absent or multi-valued.
     */
    public String getTypeString() {
        var type = getType();
        if (type != null && type.isString()) {
            return type.asString();
        }
        return null;
    }

    /**
     * Get the type field as a list of strings, handling both single and multi-valued cases.
     */
    public List<String> getTypeList() {
        var type = getType();
        if (type == null) {
            return null;
        }
        if (type.isString()) {
            return List.of(type.asString());
        }
        if (type.isStringList()) {
            return type.asStringList();
        }
        return null;
    }

    // --- Version-specific accessors (Draft group) ---
    // These require casting to version-specific interfaces.
    // The diff visitors handle version-specific properties via instanceof checks.

    /**
     * Check if the underlying node is an instance of a given type.
     */
    public boolean isInstanceOf(Class<?> clazz) {
        return clazz.isInstance(node);
    }

    /**
     * Cast the underlying node to a specific type.
     */
    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> clazz) {
        return (T) node;
    }

    @Override
    public String toString() {
        return "SchemaAccessor{type=%s, node=%s}".formatted(
                doc != null ? "Document" : "JSchema",
                node.getClass().getSimpleName()
        );
    }
}
