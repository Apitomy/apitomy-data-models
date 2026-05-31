package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.models.union.BooleanJSchemaUnion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.*;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.*;

public class ObjectSchemaDiff {

    private final DiffContext ctx;
    private final SchemaAccessor original;
    private final SchemaAccessor updated;

    public ObjectSchemaDiff(DiffContext ctx, SchemaAccessor original, SchemaAccessor updated) {
        this.ctx = ctx;
        this.original = original;
        this.updated = updated;
    }

    public void visit() {
        diffRequired();
        diffProperties();
        diffAdditionalProperties();
        diffMinMaxProperties();
        diffPatternProperties();
        diffPropertyNames();
        diffDependencies();
    }

    private void diffRequired() {
        var origRequired = original.getRequired();
        var updRequired = updated.getRequired();
        if (origRequired == null && updRequired == null) return;

        var origSet = origRequired != null ? new HashSet<>(origRequired) : new HashSet<String>();
        var updSet = updRequired != null ? new HashSet<>(updRequired) : new HashSet<String>();

        diffSetChanged(ctx, origSet, updSet,
                OBJECT_TYPE_REQUIRED_PROPERTIES_ADDED, OBJECT_TYPE_REQUIRED_PROPERTIES_REMOVED,
                OBJECT_TYPE_REQUIRED_PROPERTIES_CHANGED,
                OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_ADDED, OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_REMOVED);
    }

    private void diffProperties() {
        var origProps = original.getProperties();
        var updProps = updated.getProperties();
        if (origProps == null && updProps == null) return;

        var origKeys = origProps != null ? new HashSet<>(origProps.keySet()) : new HashSet<String>();
        var updKeys = updProps != null ? new HashSet<>(updProps.keySet()) : new HashSet<String>();

        // Properties present in both
        var commonKeys = new HashSet<>(origKeys);
        commonKeys.retainAll(updKeys);
        for (var key : commonKeys) {
            var subCtx = ctx.sub(key);
            var origSchema = origProps.get(key);
            var updSchema = updProps.get(key);
            if (!isUnionSchemaCompatible(subCtx, origSchema, updSchema, true)) {
                subCtx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED, origSchema, updSchema);
            }
        }

        var origAdditional = original.getAdditionalProperties();
        var updAdditional = updated.getAdditionalProperties();
        var origPermitsAdditional = permitsAdditional(origAdditional);
        var updPermitsAdditional = permitsAdditional(updAdditional);

        // Properties added in updated
        var addedKeys = new HashSet<>(updKeys);
        addedKeys.removeAll(origKeys);
        if (!addedKeys.isEmpty()) {
            if (!origPermitsAdditional) {
                // Original forbids additional, so adding properties extends the schema
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_EXTENDED, null, addedKeys);
            } else if (origAdditional != null && origAdditional.isJSchema() && updProps != null) {
                // Original has an additionalProperties schema: adding a property replaces the
                // additionalProperties schema with a specific one, which is compatible
                // only if the new property schema is compatible with the old additionalProperties schema.
                var allCompatible = true;
                for (var key : addedKeys) {
                    var addedSchema = updProps.get(key);
                    var subCtx = ctx.sub(key);
                    if (!isUnionSchemaCompatible(subCtx, origAdditional, addedSchema, true)) {
                        allCompatible = false;
                        break;
                    }
                }
                if (allCompatible) {
                    ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES, null, addedKeys);
                } else {
                    ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED, null, addedKeys);
                }
            } else {
                // Original has additionalProperties: true (or default), adding explicit property
                // schemas narrows (any value was accepted, now constrained)
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED, null, addedKeys);
            }
        }

        // Properties removed in updated
        var removedKeys = new HashSet<>(origKeys);
        removedKeys.removeAll(updKeys);
        if (!removedKeys.isEmpty()) {
            if (!updPermitsAdditional) {
                // Updated forbids additional, so the removed property can no longer be set
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED, removedKeys, null);
            } else if (updAdditional != null && updAdditional.isJSchema() && origProps != null) {
                // Updated has an additionalProperties schema: removed property values
                // will now be validated against it. Compatible only if the AP schema
                // accepts a superset of what the original property schema accepted.
                var allCompatible = true;
                for (var key : removedKeys) {
                    var removedSchema = origProps.get(key);
                    var subCtx = ctx.sub(key);
                    if (!isUnionSchemaCompatible(subCtx, removedSchema, updAdditional, true)) {
                        allCompatible = false;
                        break;
                    }
                }
                if (allCompatible) {
                    ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_EXTENDED, removedKeys, null);
                } else {
                    ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED, removedKeys, null);
                }
            } else {
                // Updated has additionalProperties: true (or default), so removed property
                // is still accepted (any value OK). This is backward compatible.
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_EXTENDED, removedKeys, null);
            }
        }
    }

    private void diffAdditionalProperties() {
        var origAP = original.getAdditionalProperties();
        var updAP = updated.getAdditionalProperties();
        if (origAP == null && updAP == null) return;

        var origPermits = permitsAdditional(origAP);
        var updPermits = permitsAdditional(updAP);

        var origIsBoolean = origAP != null && origAP.isBoolean();
        var updIsBoolean = updAP != null && updAP.isBoolean();
        var origIsSchema = origAP != null && origAP.isJSchema();
        var updIsSchema = updAP != null && updAP.isJSchema();

        if ((origIsBoolean || origAP == null) && (updIsBoolean || updAP == null)) {
            diffBooleanTransition(ctx, origPermits, updPermits, true,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_FALSE_TO_TRUE,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_TRUE_TO_FALSE,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_BOOLEAN_UNCHANGED);
        } else if (origIsSchema && updIsSchema) {
            if (isUnionSchemaCompatible(ctx, origAP, updAP, true)) {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_UNCHANGED, origAP, updAP);
            } else {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_CHANGED, origAP, updAP);
            }
        } else if (!origPermits && updIsSchema) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_EXTENDED, origAP, updAP);
        } else if (origPermits && !updPermits) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED, origAP, updAP);
        } else if (origIsSchema && (updAP == null || (updIsBoolean && updPermits))) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_EXTENDED, origAP, updAP);
        } else if (origIsSchema && updIsBoolean && !updPermits) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED, origAP, updAP);
        } else if ((origAP == null || (origIsBoolean && origPermits)) && updIsSchema) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED, origAP, updAP);
        } else {
            if (origPermits && !updPermits) {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED, origAP, updAP);
            } else if (!origPermits && updPermits) {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_EXTENDED, origAP, updAP);
            }
        }
    }

    private void diffMinMaxProperties() {
        diffInteger(ctx, original.getMinProperties(), updated.getMinProperties(),
                OBJECT_TYPE_MIN_PROPERTIES_ADDED, OBJECT_TYPE_MIN_PROPERTIES_REMOVED,
                OBJECT_TYPE_MIN_PROPERTIES_INCREASED, OBJECT_TYPE_MIN_PROPERTIES_DECREASED);

        diffInteger(ctx, original.getMaxProperties(), updated.getMaxProperties(),
                OBJECT_TYPE_MAX_PROPERTIES_ADDED, OBJECT_TYPE_MAX_PROPERTIES_REMOVED,
                OBJECT_TYPE_MAX_PROPERTIES_INCREASED, OBJECT_TYPE_MAX_PROPERTIES_DECREASED);
    }

    private void diffPatternProperties() {
        var origPP = original.getPatternProperties();
        var updPP = updated.getPatternProperties();
        if (origPP == null && updPP == null) return;

        var origKeys = origPP != null ? new HashSet<>(origPP.keySet()) : new HashSet<String>();
        var updKeys = updPP != null ? new HashSet<>(updPP.keySet()) : new HashSet<String>();

        diffSetChanged(ctx, origKeys, updKeys,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_ADDED, OBJECT_TYPE_PATTERN_PROPERTY_KEYS_REMOVED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_CHANGED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_ADDED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_REMOVED);

        if (origPP != null && updPP != null) {
            var commonKeys = new HashSet<>(origKeys);
            commonKeys.retainAll(updKeys);
            for (var key : commonKeys) {
                var subCtx = ctx.sub("patternProperties/" + key);
                var origSchema = origPP.get(key);
                var updSchema = updPP.get(key);
                if (!isUnionSchemaCompatible(subCtx, origSchema, updSchema, true)) {
                    subCtx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED, origSchema, updSchema);
                }
            }
        }
    }

    private void diffPropertyNames() {
        var origPN = getPropertyNames(original);
        var updPN = getPropertyNames(updated);
        if (origPN == null && updPN == null) return;
        compareSchema(ctx, origPN, updPN,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_ADDED,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_REMOVED,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BOTH,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_NONE);
    }

    private static BooleanJSchemaUnion getPropertyNames(SchemaAccessor schema) {
        var node = schema.node();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document d) return d.getPropertyNames();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema s) return s.getPropertyNames();
        return null;
    }

    private void diffDependencies() {
        var origDeps = getDependencies(original);
        var updDeps = getDependencies(updated);
        if (origDeps == null && updDeps == null) return;

        var origKeys = origDeps != null ? new HashSet<>(origDeps.keySet()) : new HashSet<String>();
        var updKeys = updDeps != null ? new HashSet<>(updDeps.keySet()) : new HashSet<String>();

        // Dependency keys added (new dependency = narrowing)
        // Dependency keys removed (removed dependency = widening)
        diffSetChanged(ctx, origKeys, updKeys,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_ADDED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_REMOVED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_CHANGED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_ADDED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_REMOVED);

        // Compare values for common keys
        if (origDeps != null && updDeps != null) {
            var commonKeys = new HashSet<>(origKeys);
            commonKeys.retainAll(updKeys);
            for (var key : commonKeys) {
                var origValue = origDeps.get(key);
                var updValue = updDeps.get(key);
                if (origValue.isArray() && updValue.isArray()) {
                    var origSet = new HashSet<String>();
                    origValue.forEach(n -> origSet.add(n.asText()));
                    var updSet = new HashSet<String>();
                    updValue.forEach(n -> updSet.add(n.asText()));
                    for (var v : origSet) {
                        if (!updSet.contains(v)) {
                            ctx.addDifference(OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_REMOVED, v, null);
                        }
                    }
                    for (var v : updSet) {
                        if (!origSet.contains(v)) {
                            ctx.addDifference(OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_ADDED, null, v);
                        }
                    }
                    if (!origSet.equals(updSet)) {
                        ctx.addDifference(OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_CHANGED, origValue, updValue);
                    }
                } else if (origValue.isObject() && updValue.isObject()) {
                    // Schema dependencies — compare as schemas
                    // This is a simplified comparison; full comparison would require
                    // parsing the JsonNode as a schema document
                    if (!origValue.equals(updValue)) {
                        ctx.addDifference(OBJECT_TYPE_SCHEMA_DEPENDENCIES_CHANGED, origValue, updValue);
                    }
                }
            }
        }
    }

    private static Map<String, com.fasterxml.jackson.databind.JsonNode> getDependencies(SchemaAccessor schema) {
        var node = schema.node();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.JSDraftDocument d) return d.getDependencies();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.JSDraftJSchema s) return s.getDependencies();
        return null;
    }

    private static boolean permitsAdditional(BooleanJSchemaUnion additionalProperties) {
        if (additionalProperties == null) return true; // default is true
        if (additionalProperties.isBoolean()) return additionalProperties.asBoolean();
        return true; // schema means additional properties are allowed (with constraints)
    }
}
