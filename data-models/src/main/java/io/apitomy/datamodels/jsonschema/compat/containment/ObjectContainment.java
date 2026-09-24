package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.util.RegexUtil;

/**
 * Object containment: does every object satisfying {@code source} also
 * satisfy {@code target}? Tracks each property name's <em>effective</em>
 * schema -- the named schema if one applies, else the first matching
 * {@code patternProperties} entry, else {@code additionalProperties} (true by
 * default) -- rather than conflating a named definition with actual instance
 * presence: a property named in {@code properties} is not required merely by
 * being named, and a property not named in either schema is still governed by
 * whichever schema's {@code additionalProperties} applies to it.
 */
public final class ObjectContainment {

    private ObjectContainment() {
    }

    /** True if {@code schema}'s own size/requiredness constraints admit no object at all. */
    public static boolean isProvenUnsatisfiable(SchemaView schema) {
        JsonNode minPropertiesNode = schema.getKeyword("minProperties");
        JsonNode maxPropertiesNode = schema.getKeyword("maxProperties");
        if (minPropertiesNode != null && maxPropertiesNode != null
                && JsonUtil.isNumber(minPropertiesNode) && JsonUtil.isNumber(maxPropertiesNode)
                && JsonUtil.toNumber(minPropertiesNode).intValue() > JsonUtil.toNumber(maxPropertiesNode).intValue()) {
            return true;
        }
        if (maxPropertiesNode != null && JsonUtil.isNumber(maxPropertiesNode)
                && requiredNames(schema).size() > JsonUtil.toNumber(maxPropertiesNode).intValue()) {
            return true;
        }
        return false;
    }

    public static ContainmentResult compare(SchemaView source, SchemaView target, ContainmentContext context) {
        if (isProvenUnsatisfiable(source)) {
            return ContainmentResult.yes(evidence(source, target, "empty-source-object",
                    "The source schema's own size/requiredness constraints admit no object"));
        }

        List<String> sourceRequired = requiredNames(source);
        List<String> targetRequired = requiredNames(target);
        for (int i = 0; i < targetRequired.size(); i++) {
            String name = targetRequired.get(i);
            if (sourceRequired.contains(name)) {
                continue;
            }
            JsonNode witness = buildWitnessObject(source, name, null, null, context);
            ContainmentResult failure = confirmOrUnknown(source, target, witness, context, "required-not-guaranteed",
                    "The target schema requires '" + name + "', which the source schema does not guarantee is present");
            if (failure != null) {
                return failure;
            }
        }

        Set<String> names = new LinkedHashSet<String>();
        names.addAll(propertyKeys(source));
        names.addAll(propertyKeys(target));

        boolean unresolved = false;
        for (String name : names) {
            SchemaView sourceEffective = effectivePropertySchema(source, name);
            SchemaView targetEffective = effectivePropertySchema(target, name);
            ContainmentResult propertyResult = SchemaContainment.compare(sourceEffective, targetEffective, context);
            if (propertyResult.getVerdict() == ContainmentVerdict.NO) {
                JsonNode witness = buildWitnessObject(source, null, name, propertyResult.getWitness(), context);
                ContainmentResult failure = confirmOrUnknown(source, target, witness, context, "property-not-contained",
                        "Property '" + name + "'s effective schema on the source side is not contained by its "
                                + "effective schema on the target side");
                if (failure != null) {
                    return failure;
                }
                unresolved = true;
                continue;
            }
            if (propertyResult.getVerdict() == ContainmentVerdict.UNKNOWN) {
                unresolved = true;
            }
        }

        if (unresolved) {
            return ContainmentResult.unknown(evidence(source, target, "object-property-unresolved",
                    "At least one property's effective schema could not be fully proven contained"));
        }
        return ContainmentResult.yes(evidence(source, target, "object-properties-contained",
                "Every property name's effective schema on the source side is contained by its effective schema "
                        + "on the target side, and every target-required property is guaranteed present by the source"));
    }

    /**
     * The schema that actually governs a property named {@code name} on
     * {@code schema}: the named schema in {@code properties} if present, else
     * the first {@code patternProperties} entry whose pattern matches
     * {@code name}, else {@code additionalProperties} (the {@code true} schema
     * if {@code additionalProperties} is itself absent).
     */
    static SchemaView effectivePropertySchema(SchemaView schema, String name) {
        JsonNode propertiesNode = schema.getKeyword("properties");
        if (propertiesNode != null && JsonUtil.isObject(propertiesNode)) {
            ObjectNode properties = JsonUtil.toObject(propertiesNode);
            if (JsonUtil.keys(properties).contains(name)) {
                return childOf(schema, JsonUtil.getProperty(properties, name), "properties/" + name);
            }
        }
        JsonNode patternPropertiesNode = schema.getKeyword("patternProperties");
        if (patternPropertiesNode != null && JsonUtil.isObject(patternPropertiesNode)) {
            ObjectNode patternProperties = JsonUtil.toObject(patternPropertiesNode);
            List<String> patterns = JsonUtil.keys(patternProperties);
            for (int i = 0; i < patterns.size(); i++) {
                String pattern = patterns.get(i);
                if (!RegexUtil.findMatches(name, pattern).isEmpty()) {
                    return childOf(schema, JsonUtil.getProperty(patternProperties, pattern), "patternProperties/" + pattern);
                }
            }
        }
        return catchAllPropertySchema(schema);
    }

    /** The schema that governs a property name matching neither {@code properties} nor {@code patternProperties}. */
    static SchemaView catchAllPropertySchema(SchemaView schema) {
        JsonNode additionalPropertiesNode = schema.getKeyword("additionalProperties");
        if (additionalPropertiesNode != null) {
            return childOf(schema, additionalPropertiesNode, "additionalProperties");
        }
        return trueView(schema);
    }

    static List<String> requiredNames(SchemaView schema) {
        List<String> result = new ArrayList<String>();
        JsonNode requiredNode = schema.getKeyword("required");
        if (requiredNode != null && JsonUtil.isArray(requiredNode)) {
            List<JsonNode> items = JsonUtil.toList(requiredNode);
            for (int i = 0; i < items.size(); i++) {
                result.add(JsonUtil.toString(items.get(i)));
            }
        }
        return result;
    }

    private static List<String> propertyKeys(SchemaView schema) {
        List<String> result = new ArrayList<String>();
        JsonNode propertiesNode = schema.getKeyword("properties");
        if (propertiesNode != null && JsonUtil.isObject(propertiesNode)) {
            result.addAll(JsonUtil.keys(JsonUtil.toObject(propertiesNode)));
        }
        return result;
    }

    /** A minimal object satisfying {@code schema}'s own required properties, or {@code null} if one could not be confidently constructed. */
    public static JsonNode buildSatisfyingWitness(SchemaView schema, ContainmentContext context) {
        return buildWitnessObject(schema, null, null, null, context);
    }

    /**
     * A minimal object satisfying {@code source}'s own required properties
     * (each filled with a representative value for its effective schema),
     * optionally omitting {@code omitName} and/or overriding {@code overrideName}
     * with {@code overrideValue} -- or {@code null} if a representative value
     * could not be confidently constructed for some other required property.
     */
    private static JsonNode buildWitnessObject(SchemaView source, String omitName, String overrideName,
            JsonNode overrideValue, ContainmentContext context) {
        ObjectNode result = JsonUtil.objectNode();
        List<String> required = requiredNames(source);
        for (int i = 0; i < required.size(); i++) {
            String name = required.get(i);
            if (name.equals(omitName)) {
                continue;
            }
            JsonNode value;
            if (name.equals(overrideName)) {
                value = overrideValue;
            } else {
                value = representativeValue(effectivePropertySchema(source, name), context);
                if (value == null) {
                    return null;
                }
            }
            JsonUtil.setProperty(result, name, value);
        }
        if (overrideName != null && !required.contains(overrideName) && !overrideName.equals(omitName)) {
            JsonUtil.setProperty(result, overrideName, overrideValue);
        }
        return result;
    }

    /** Some concrete value satisfying {@code schema}, or {@code null} if one could not be confidently constructed. */
    private static JsonNode representativeValue(SchemaView schema, ContainmentContext context) {
        if (schema == null) {
            return null;
        }
        JsonNode constNode = schema.getKeyword("const");
        if (constNode != null) {
            return constNode;
        }
        JsonNode enumNode = schema.getKeyword("enum");
        if (enumNode != null) {
            List<JsonNode> values = JsonUtil.toList(enumNode);
            for (int i = 0; i < values.size(); i++) {
                if (WitnessValidator.validate(values.get(i), schema, context) == ContainmentVerdict.YES) {
                    return values.get(i);
                }
            }
        }
        List<String> types = SchemaNormalizer.normalizeEffectiveTypes(schema);
        String type = types != null && !types.isEmpty() ? types.get(0) : ScalarTypes.STRING;
        JsonNode candidate = ScalarWitness.exampleOfType(type);
        if (candidate != null && WitnessValidator.validate(candidate, schema, context) == ContainmentVerdict.YES) {
            return candidate;
        }
        return null;
    }

    private static ContainmentResult confirmOrUnknown(SchemaView source, SchemaView target, JsonNode witness,
            ContainmentContext context, String rule, String message) {
        if (witness != null && WitnessValidator.validate(witness, source, context) == ContainmentVerdict.YES
                && WitnessValidator.validate(witness, target, context) == ContainmentVerdict.NO) {
            return ContainmentResult.no(evidence(source, target, rule, message), witness);
        }
        return null;
    }

    private static SchemaView childOf(SchemaView parent, JsonNode node, String pointerSuffix) {
        String pointer = parent.getPointer() != null ? parent.getPointer() + "/" + pointerSuffix : null;
        return new SchemaView(node, parent.getDialect(), parent.getResourceUri(), pointer);
    }

    private static SchemaView trueView(SchemaView parent) {
        return new SchemaView(JsonUtil.toJsonNode(Boolean.TRUE), parent.getDialect(), parent.getResourceUri(), parent.getPointer());
    }

    private static SchemaEvidence evidence(SchemaView source, SchemaView target, String rule, String message) {
        return new SchemaEvidence(source.getPointer(), target.getPointer(), rule, message);
    }
}
