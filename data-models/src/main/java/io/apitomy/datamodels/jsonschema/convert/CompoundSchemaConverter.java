package io.apitomy.datamodels.jsonschema.convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apitomy.datamodels.models.Any;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaFullSchemaListUnion;
import io.apitomy.datamodels.models.jsonschema.Dependency;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.visitors.JD4ToJCConversionTraverser;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.visitors.JD6ToJCConversionTraverser;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.visitors.JD7ToJCConversionTraverser;
import io.apitomy.datamodels.models.jsonschema.modern.v201909.visitors.JM201909ToJCConversionTraverser;
import io.apitomy.datamodels.models.jsonschema.modern.v202012.visitors.JM202012ToJCConversionTraverser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts any JSON Schema version to the compound schema type.
 */
public class CompoundSchemaConverter {

    /**
     * Converts a JSON Schema of any draft version to the compound schema type.
     */
    public static JsonSchema toCompound(JsonSchema source, ModelType modelType) {
        if (source == null) return null;
        Any result;
        switch (modelType) {
            case JD4:
                result = new JD4ToJCConversionTraverser(new JD4ToCompoundConverter()).convert(source);
                break;
            case JD6:
                result = new JD6ToJCConversionTraverser(new JD6ToCompoundConverter()).convert(source);
                break;
            case JD7:
                result = new JD7ToJCConversionTraverser(new JD7ToCompoundConverter()).convert(source);
                break;
            case JM201909:
                result = new JM201909ToJCConversionTraverser(new JM201909ToCompoundConverter()).convert(source);
                break;
            case JM202012:
                result = new JM202012ToJCConversionTraverser(new JM202012ToCompoundConverter()).convert(source);
                break;
            default:
                return source;
        }
        return (JsonSchema) result;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Splits a d4-d7 {@code dependencies} map into {@code dependentSchemas}
     * and {@code dependentRequired} on the compound target.
     */
    static void splitDependencies(Map<String, Dependency> value, JCFullSchema target) {
        if (value == null) return;
        Map<String, JsonNode> requiredMap = null;
        for (Map.Entry<String, Dependency> entry : value.entrySet()) {
            var dep = entry.getValue();
            if (dep.isFullSchema()) {
                target.addDependentSchema(entry.getKey(), (JsonSchema) dep.asFullSchema());
            } else if (dep.isStringList()) {
                if (requiredMap == null) {
                    requiredMap = new LinkedHashMap<>();
                }
                requiredMap.put(entry.getKey(), MAPPER.valueToTree(dep.asStringList()));
            }
        }
        if (requiredMap != null) {
            target.setDependentRequired(requiredMap);
        }
    }

    /**
     * Normalizes d4-d7 {@code items} into compound fields:
     * tuple (list) → {@code prefixItems}, single schema → {@code items}.
     */
    static void normalizeItems(BooleanFullSchemaFullSchemaListUnion value, JCFullSchema target) {
        if (value == null) return;
        if (value.isFullSchemaList()) {
            for (var schema : value.asFullSchemaList()) {
                target.addPrefixItem((JsonSchema) schema);
            }
        } else {
            target.setItems(value);
        }
    }
}
