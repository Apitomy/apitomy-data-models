package io.apitomy.datamodels.jsonschema.convert;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.models.Any;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaJsonSchemaListUnion;
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

    private static final String SOURCE_KEYWORD_ATTRIBUTE = "sourceKeyword:";

    /**
     * Records that the compound schema holds {@code compoundKeyword} where the source schema wrote
     * {@code sourceKeyword}, e.g. {@code prefixItems} for a draft 7 tuple {@code items}. Recorded on
     * the schema that owns the keyword, since the value itself may be a boolean schema, which
     * cannot carry attributes.
     */
    static void recordSourceKeyword(JCFullSchema target, String compoundKeyword, String sourceKeyword) {
        target.setNodeAttribute(SOURCE_KEYWORD_ATTRIBUTE + compoundKeyword, sourceKeyword);
    }

    /**
     * The keyword the source schema wrote for a keyword of a converted compound schema, so that a
     * path can be reported in the terms of the schema the user wrote. Conversion renames a few
     * keywords: a draft 4 to 2019-09 tuple {@code items} becomes {@code prefixItems}, draft 4 to 7
     * {@code dependencies} becomes {@code dependentSchemas} or {@code dependentRequired}, 2020-12
     * {@code items} next to {@code prefixItems} becomes {@code additionalItems}, and an exclusive
     * bound becomes {@code minimum} or {@code maximum}. Any other keyword is returned unchanged.
     *
     * @param schema          a schema converted by {@link #toCompound}, or {@code null}
     * @param compoundKeyword a keyword of the compound schema
     */
    public static String getSourceKeyword(JFullSchema schema, String compoundKeyword) {
        if (schema == null) {
            return compoundKeyword;
        }
        Object source = schema.getNodeAttribute(SOURCE_KEYWORD_ATTRIBUTE + compoundKeyword);
        return source instanceof String ? (String) source : compoundKeyword;
    }

    /**
     * Splits a d4-d7 {@code dependencies} map into {@code dependentSchemas}
     * and {@code dependentRequired} on the compound target.
     */
    static void splitDependencies(Map<String, Dependency> value, JCFullSchema target) {
        if (value == null) return;
        Map<String, JsonNode> requiredMap = null;
        for (Map.Entry<String, Dependency> entry : value.entrySet()) {
            Dependency dep = entry.getValue();
            if (dep.isFullSchema()) {
                target.addDependentSchema(entry.getKey(), (JsonSchema) dep.asFullSchema());
                recordSourceKeyword(target, "dependentSchemas", "dependencies");
            } else if (dep.isBoolean()) {
                // A boolean is a schema too: false forbids the key property, true allows anything.
                target.addDependentSchema(entry.getKey(), (JsonSchema) dep);
                recordSourceKeyword(target, "dependentSchemas", "dependencies");
            } else if (dep.isStringList()) {
                if (requiredMap == null) {
                    requiredMap = new LinkedHashMap<>();
                }
                requiredMap.put(entry.getKey(), JsonUtil.toArrayNode(dep.asStringList()));
            }
        }
        if (requiredMap != null) {
            target.setDependentRequired(requiredMap);
            recordSourceKeyword(target, "dependentRequired", "dependencies");
        }
    }

    /**
     * Normalizes d4-2019-09 {@code items} into compound fields:
     * tuple (list) → {@code prefixItems}, single schema → {@code items}.
     * The generated traverser converts a single-schema {@code items} but not the elements of a
     * tuple, so they are converted here. Boolean elements pass through unchanged.
     */
    static void normalizeItems(BooleanFullSchemaJsonSchemaListUnion value, JCFullSchema target,
                               ModelType modelType) {
        if (value == null) return;
        if (value.isJsonSchemaList()) {
            for (JsonSchema schema : value.asJsonSchemaList()) {
                target.addPrefixItem(toCompound(schema, modelType));
            }
            recordSourceKeyword(target, "prefixItems", "items");
        } else {
            target.setItems(value);
        }
    }
}
