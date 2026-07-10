package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.jsonschema.convert.CompoundSchemaConverter;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaFullSchemaListUnion;
import io.apitomy.datamodels.models.jsonschema.Dependency;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.compound.visitors.JCDiffTraverser;
import io.apitomy.datamodels.models.jsonschema.compound.visitors.JCDiffVisitor;

import io.apitomy.datamodels.jsonschema.ref.UnresolvableRefStrategy;
import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValue;
import io.apitomy.datamodels.models.visitors.diff.CollectionDiff;
import io.apitomy.datamodels.models.visitors.diff.DefaultPairingKey;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.*;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.*;

/**
 * Diff visitor driven by the generated {@link JCDiffTraverser}.
 * Extends the generated {@link JCDiffVisitor} and overrides per-field methods
 * to produce compatibility {@link Difference}s collected via {@link DiffContext}.
 * <p>
 * The traverser iterates all compound-schema fields and calls the appropriate
 * visitor method for each field. The visitor delegates to {@link DiffUtil} and
 * the existing diff helper classes for the actual comparison logic.
 */
// TODO: Modern schema support — unevaluatedItems, unevaluatedProperties, $dynamicRef, etc.
public class CompoundSchemaDiffVisitor extends JCDiffVisitor<DefaultPairingKey> {

    private final DiffContext ctx;

    /**
     * The original and updated schemas being compared at this level.
     * Set in {@link #visitFullSchema} after $ref resolution.
     * Needed by cross-field logic (type dispatch, composition keyword transitions).
     */
    private JFullSchema currentOriginal;
    private JFullSchema currentUpdated;

    public CompoundSchemaDiffVisitor(DiffContext ctx) {
        this.ctx = ctx;
    }

    // -----------------------------------------------------------------------
    // Static entry points (preserved for backward compatibility with DiffUtil)
    // -----------------------------------------------------------------------

    /**
     * Entry point: compare original and updated schemas.
     */
    public static void diffSchemas(DiffContext ctx, JFullSchema original, JFullSchema updated) {
        var resolvedOriginal = resolveIfRef(ctx, original);
        var resolvedUpdated = resolveIfRef(ctx, updated);

        // If either side could not be resolved (COLLECT), skip comparison
        if (resolvedOriginal == null || resolvedUpdated == null) {
            return;
        }

        // Convert to compound if needed (e.g., $ref resolved to draft-specific type)
        var compoundOriginal = toCompoundIfNeeded(resolvedOriginal);
        var compoundUpdated = toCompoundIfNeeded(resolvedUpdated);

        // Cycle detection: prevent infinite recursion on the same node pair.
        // Use the resolved (pre-conversion) identity for cycle detection so that
        // converting the same node twice doesn't break the check.
        var pairKey = System.identityHashCode(resolvedOriginal)
                + ":" + System.identityHashCode(resolvedUpdated);
        if (ctx.visited.contains(pairKey)) {
            return;
        }
        ctx.visited.add(pairKey);
        try {
            if (!(compoundOriginal instanceof JCFullSchema origCompound)
                    || !(compoundUpdated instanceof JCFullSchema updCompound)) {
                // Should not happen after conversion, but guard just in case
                return;
            }

            var visitor = new CompoundSchemaDiffVisitor(ctx);
            var traverser = new JCDiffTraverser<>(visitor);
            traverser.traverseFullSchema(origCompound, updCompound);
        } finally {
            ctx.visited.remove(pairKey);
        }
    }

    /**
     * Converts a schema to compound type if it isn't already.
     * This is needed when $ref resolution returns draft-specific schemas.
     */
    private static JFullSchema toCompoundIfNeeded(JFullSchema schema) {
        if (schema instanceof JCFullSchema) {
            return schema;
        }
        var modelType = detectModelType(schema);
        if (modelType != null) {
            var converted = CompoundSchemaConverter.toCompound((JsonSchema) schema, modelType);
            if (converted instanceof JFullSchema fs) {
                return fs;
            }
        }
        return schema;
    }

    private static ModelType detectModelType(JFullSchema schema) {
        if (schema.root() != null && schema.root().modelType() != null) {
            return schema.root().modelType();
        }
        return DiffUtil.detectModelType(schema);
    }

    private static JFullSchema resolveIfRef(DiffContext ctx, JFullSchema schema) {
        var ref = DiffUtil.get$ref(schema);
        if (ref == null) return schema;

        var traversal = ctx.getRefTraversal();
        if (traversal != null) {
            var resolved = traversal.resolveRef(ref, schema);
            if (resolved.isPresent()) {
                return (JFullSchema) resolved.get();
            }
        }

        // Ref is unresolvable — apply strategy
        handleUnresolvableRef(ctx, ref);
        // For COLLECT, return null to signal caller to skip comparison.
        // For FAIL, handleUnresolvableRef already threw.
        return null;
    }

    private static void handleUnresolvableRef(DiffContext ctx, String ref) {
        switch (ctx.getUnresolvableRefStrategy()) {
            case FAIL:
                throw new JsonSchemaCompatibilityException("Unresolvable $ref: " + ref);
            case COLLECT:
                ctx.addUnsupported("Unresolvable $ref: " + ref);
                break;
        }
    }

    // -----------------------------------------------------------------------
    // Entity visit — type dispatch, empty-schema detection
    // -----------------------------------------------------------------------

    @Override
    public boolean visitFullSchema(JCFullSchema original, JCFullSchema updated) {
        // Store for cross-field logic
        this.currentOriginal = original;
        this.currentUpdated = updated;

        // If either is null the traverser already handles the null guard before calling field methods
        if (original == null || updated == null) {
            return true;
        }

        var originalType = DiffUtil.getTypeString(original);
        var updatedType = DiffUtil.getTypeString(updated);

        if (originalType != null && updatedType != null && !originalType.equals(updatedType)) {
            if ("integer".equals(originalType) && "number".equals(updatedType)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, updatedType);
            } else if (updatedType.isEmpty() || isEmptyOrTrueSchema(updated)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, updatedType);
            } else {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED, originalType, updatedType);
            }
            return false; // type changed — no point comparing fields
        }

        if (originalType != null && updatedType == null) {
            if (isEmptyOrTrueSchema(updated)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, "");
                return false;
            }
            // Check if updated uses composition (anyOf/oneOf) that includes the original type
            var updAnyOf = updated.getAnyOf();
            var updOneOf = updated.getOneOf();
            if (updAnyOf != null || updOneOf != null) {
                var compositionList = updAnyOf != null ? updAnyOf : updOneOf;
                var origMatchesAny = false;
                for (var sub : compositionList) {
                    if (sub.isFullSchema()) {
                        var subCtx = ctx.sub("compositionCheck");
                        if (isSchemaCompatible(subCtx, original, sub.asFullSchema(), true)) {
                            origMatchesAny = true;
                            break;
                        }
                    }
                }
                if (origMatchesAny) {
                    ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, "anyOf/oneOf");
                    return false;
                }
            }
        }

        if (originalType == null && updatedType != null) {
            if (isEmptyOrTrueSchema(original)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED, "", updatedType);
                return false;
            }
        }

        // Multi-valued type check
        var effectiveType = originalType != null ? originalType : updatedType;
        if (effectiveType == null) {
            var origTypeList = DiffUtil.getTypeList(original);
            var updTypeList = DiffUtil.getTypeList(updated);
            if ((origTypeList != null && origTypeList.size() > 1)
                    || (updTypeList != null && updTypeList.size() > 1)) {
                ctx.addUnsupported("Multi-valued type field (e.g. type: [\"string\", \"number\"])");
            }
        }

        // Return true to let the traverser call all field-level diff methods
        return true;
    }

    private boolean isEmptyOrTrueSchema(JFullSchema schema) {
        if (DiffUtil.getTypeString(schema) != null
                || schema.getAllOf() != null
                || schema.getAnyOf() != null
                || schema.getOneOf() != null
                || schema.getNot() != null
                || schema.getEnum() != null
                || schema.getProperties() != null
                || schema.getRequired() != null
                || schema.getMinLength() != null
                || schema.getMaxLength() != null
                || schema.getMinItems() != null
                || schema.getMaxItems() != null
                || schema.getMinProperties() != null
                || schema.getMaxProperties() != null
                || schema.getPattern() != null
                || schema.getFormat() != null
                || schema.getMultipleOf() != null
                || schema.getAdditionalProperties() != null
                || schema.getPatternProperties() != null) {
            return false;
        }
        if (schema instanceof JCFullSchema c
                && (c.getMinimum() != null || c.getMaximum() != null
                    || c.getItems() != null || c.getAdditionalItems() != null
                    || c.getConst() != null)) {
            return false;
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Union visit methods
    // -----------------------------------------------------------------------

    // Collection item visitors: skip auto-recursion because the collection-level
    // diff methods (diffFullSchemaProperties, diffFullSchemaAllOf, etc.) handle
    // comparison with custom matching logic (e.g., key-based property matching,
    // compatibility-based composition matching).

    @Override
    public boolean visitFullSchemaProperty(JsonSchema original, JsonSchema updated) { return false; }

    @Override
    public boolean visitFullSchemaPatternProperty(JsonSchema original, JsonSchema updated) { return false; }

    @Override
    public boolean visitFullSchemaDependency(Dependency original, Dependency updated) { return false; }

    @Override
    public boolean visitFullSchemaAllOfItem(JsonSchema original, JsonSchema updated) { return false; }

    @Override
    public boolean visitFullSchemaAnyOfItem(JsonSchema original, JsonSchema updated) { return false; }

    @Override
    public boolean visitFullSchemaOneOfItem(JsonSchema original, JsonSchema updated) { return false; }

    // -----------------------------------------------------------------------
    // String type fields
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaMinLength(Integer original, Integer updated) {
        diffInteger(ctx, original, updated,
                STRING_TYPE_MIN_LENGTH_ADDED, STRING_TYPE_MIN_LENGTH_REMOVED,
                STRING_TYPE_MIN_LENGTH_INCREASED, STRING_TYPE_MIN_LENGTH_DECREASED);
    }

    @Override
    public void diffFullSchemaMaxLength(Integer original, Integer updated) {
        diffInteger(ctx, original, updated,
                STRING_TYPE_MAX_LENGTH_ADDED, STRING_TYPE_MAX_LENGTH_REMOVED,
                STRING_TYPE_MAX_LENGTH_INCREASED, STRING_TYPE_MAX_LENGTH_DECREASED);
    }

    @Override
    public void diffFullSchemaPattern(String original, String updated) {
        diffObject(ctx, original, updated,
                STRING_TYPE_PATTERN_ADDED, STRING_TYPE_PATTERN_REMOVED,
                STRING_TYPE_PATTERN_CHANGED);
    }

    @Override
    public void diffFullSchemaFormat(String original, String updated) {
        diffObject(ctx, original, updated,
                STRING_TYPE_FORMAT_ADDED, STRING_TYPE_FORMAT_REMOVED,
                STRING_TYPE_FORMAT_CHANGED);
    }

    @Override
    public void diffFullSchemaContentMediaType(String original, String updated) {
        diffObject(ctx, original, updated,
                STRING_TYPE_CONTENT_MEDIA_TYPE_ADDED, STRING_TYPE_CONTENT_MEDIA_TYPE_REMOVED,
                STRING_TYPE_CONTENT_MEDIA_TYPE_CHANGED);
    }

    @Override
    public void diffFullSchemaContentEncoding(String original, String updated) {
        diffObject(ctx, original, updated,
                STRING_TYPE_CONTENT_ENCODING_ADDED, STRING_TYPE_CONTENT_ENCODING_REMOVED,
                STRING_TYPE_CONTENT_ENCODING_CHANGED);
    }

    // -----------------------------------------------------------------------
    // Number type fields
    // -----------------------------------------------------------------------

    @Override
    public boolean diffFullSchemaMinimum(JCRangeValue original, JCRangeValue updated) {
        if (original == null && updated == null) return false;
        if (original == null) {
            ctx.addDifference(NUMBER_TYPE_MINIMUM_ADDED, null, rangeToString(updated));
            return false;
        }
        if (updated == null) {
            ctx.addDifference(NUMBER_TYPE_MINIMUM_REMOVED, rangeToString(original), null);
            return false;
        }
        // Both present -- compare values
        Number origVal = original.getValue();
        Number updVal = updated.getValue();
        if (origVal != null && updVal != null) {
            int cmp = toBigDecimal(origVal).compareTo(toBigDecimal(updVal));
            boolean origExcl = Boolean.TRUE.equals(original.isExclusive());
            boolean updExcl = Boolean.TRUE.equals(updated.isExclusive());
            if (cmp < 0 || (cmp == 0 && !origExcl && updExcl)) {
                // minimum increased (tightened)
                ctx.addDifference(NUMBER_TYPE_MINIMUM_INCREASED,
                        rangeToString(original), rangeToString(updated));
            } else if (cmp > 0 || (cmp == 0 && origExcl && !updExcl)) {
                // minimum decreased (relaxed)
                ctx.addDifference(NUMBER_TYPE_MINIMUM_DECREASED,
                        rangeToString(original), rangeToString(updated));
            }
            // else: same value and exclusivity -- no diff
        }
        return false; // don't auto-recurse into RangeValue fields
    }

    @Override
    public boolean diffFullSchemaMaximum(JCRangeValue original, JCRangeValue updated) {
        if (original == null && updated == null) return false;
        if (original == null) {
            ctx.addDifference(NUMBER_TYPE_MAXIMUM_ADDED, null, rangeToString(updated));
            return false;
        }
        if (updated == null) {
            ctx.addDifference(NUMBER_TYPE_MAXIMUM_REMOVED, rangeToString(original), null);
            return false;
        }
        // Both present -- compare values
        Number origVal = original.getValue();
        Number updVal = updated.getValue();
        if (origVal != null && updVal != null) {
            int cmp = toBigDecimal(origVal).compareTo(toBigDecimal(updVal));
            boolean origExcl = Boolean.TRUE.equals(original.isExclusive());
            boolean updExcl = Boolean.TRUE.equals(updated.isExclusive());
            if (cmp > 0 || (cmp == 0 && !origExcl && updExcl)) {
                // maximum decreased (tightened)
                ctx.addDifference(NUMBER_TYPE_MAXIMUM_DECREASED,
                        rangeToString(original), rangeToString(updated));
            } else if (cmp < 0 || (cmp == 0 && origExcl && !updExcl)) {
                // maximum increased (relaxed)
                ctx.addDifference(NUMBER_TYPE_MAXIMUM_INCREASED,
                        rangeToString(original), rangeToString(updated));
            }
            // else: same value and exclusivity -- no diff
        }
        return false; // don't auto-recurse into RangeValue fields
    }

    @Override
    public void diffFullSchemaMultipleOf(Number original, Number updated) {
        if (diffAddedRemoved(ctx, original, updated,
                NUMBER_TYPE_MULTIPLE_OF_ADDED, NUMBER_TYPE_MULTIPLE_OF_REMOVED)) {
            diffNumberOriginalMultipleOfUpdated(ctx, original, updated,
                    NUMBER_TYPE_MULTIPLE_OF_UPDATED_IS_DIVISIBLE,
                    NUMBER_TYPE_MULTIPLE_OF_UPDATED_IS_NOT_DIVISIBLE);
        }
    }

    // -----------------------------------------------------------------------
    // Array type fields
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaMinItems(Integer original, Integer updated) {
        diffInteger(ctx, original, updated,
                ARRAY_TYPE_MIN_ITEMS_ADDED, ARRAY_TYPE_MIN_ITEMS_REMOVED,
                ARRAY_TYPE_MIN_ITEMS_INCREASED, ARRAY_TYPE_MIN_ITEMS_DECREASED);
    }

    @Override
    public void diffFullSchemaMaxItems(Integer original, Integer updated) {
        diffInteger(ctx, original, updated,
                ARRAY_TYPE_MAX_ITEMS_ADDED, ARRAY_TYPE_MAX_ITEMS_REMOVED,
                ARRAY_TYPE_MAX_ITEMS_INCREASED, ARRAY_TYPE_MAX_ITEMS_DECREASED);
    }

    @Override
    public void diffFullSchemaUniqueItems(Boolean original, Boolean updated) {
        diffBooleanTransition(ctx, original, updated, false,
                ARRAY_TYPE_UNIQUE_ITEMS_FALSE_TO_TRUE,
                ARRAY_TYPE_UNIQUE_ITEMS_TRUE_TO_FALSE,
                ARRAY_TYPE_UNIQUE_ITEMS_BOOLEAN_UNCHANGED);
    }

    @Override
    public boolean diffFullSchemaItems(BooleanFullSchemaFullSchemaListUnion original,
                                    BooleanFullSchemaFullSchemaListUnion updated) {
        if (original == null && updated == null) return false;

        if (original != null && updated != null) {
            if (original.isFullSchema() && updated.isFullSchema()) {
                var subCtx = ctx.sub("items");
                if (!DiffUtil.isSchemaCompatible(subCtx, original.asFullSchema(),
                        updated.asFullSchema(), true)) {
                    subCtx.addDifference(ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, original, updated);
                }
            } else if (original.isFullSchemaList() && updated.isFullSchemaList()) {
                diffTupleItems(original.asFullSchemaList(), updated.asFullSchemaList());
            } else if (original.isFullSchemaList() && updated.isFullSchema()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, original, updated);
            } else if (original.isFullSchema() && updated.isFullSchemaList()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, original, updated);
            } else if (original.isBoolean() || updated.isBoolean()) {
                // boolean items handled via isUnionSchemaCompatible indirectly
            }
        } else {
            diffAddedRemoved(ctx, original, updated,
                    ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, ARRAY_TYPE_ALL_ITEM_SCHEMA_REMOVED);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void diffTupleItems(List<JFullSchema> origList, List<JFullSchema> updList) {
        var minSize = Math.min(origList.size(), updList.size());
        for (var i = 0; i < minSize; i++) {
            var subCtx = ctx.sub("items/" + i);
            var origSchema = origList.get(i);
            var updSchema = updList.get(i);
            if (!DiffUtil.isSchemaCompatible(subCtx, origSchema, updSchema, true)) {
                subCtx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, origSchema, updSchema);
            }
        }

        if (updList.size() > origList.size()) {
            var origAI = currentOriginal instanceof JCFullSchema c ? c.getAdditionalItems() : null;
            if (origAI != null && origAI.isBoolean() && !origAI.asBoolean()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED, origList.size(), updList.size());
            } else if (origAI != null && origAI.isFullSchema()) {
                var allCompatible = true;
                for (var i = minSize; i < updList.size(); i++) {
                    var subCtx = ctx.sub("items/" + i);
                    if (!DiffUtil.isSchemaCompatible(subCtx, origAI.asFullSchema(),
                            updList.get(i), true)) {
                        allCompatible = false;
                        break;
                    }
                }
                if (allCompatible) {
                    ctx.addDifference(
                            ARRAY_TYPE_ITEM_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES,
                            origList.size(), updList.size());
                } else {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED,
                            origList.size(), updList.size());
                }
            } else {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED,
                        origList.size(), updList.size());
            }
        } else if (updList.size() < origList.size()) {
            var updAI = currentUpdated instanceof JCFullSchema c ? c.getAdditionalItems() : null;
            var updPermitsAdditional = updAI == null
                    || (updAI.isBoolean() ? updAI.asBoolean() : true);
            if (!updPermitsAdditional) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED,
                        origList.size(), updList.size());
            } else if (updAI != null && updAI.isFullSchema()) {
                var allCompatible = true;
                for (var i = minSize; i < origList.size(); i++) {
                    var subCtx = ctx.sub("items/" + i);
                    if (!DiffUtil.isSchemaCompatible(subCtx, origList.get(i),
                            updAI.asFullSchema(), true)) {
                        allCompatible = false;
                        break;
                    }
                }
                if (allCompatible) {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED,
                            origList.size(), updList.size());
                } else {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED,
                            origList.size(), updList.size());
                }
            } else {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED,
                        origList.size(), updList.size());
            }
        }
    }

    @Override
    public boolean diffFullSchemaAdditionalItems(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;

        var origPermits = original == null || (original.isBoolean() ? original.asBoolean() : true);
        var updPermits = updated == null || (updated.isBoolean() ? updated.asBoolean() : true);
        var origIsBoolean = original != null && original.isBoolean();
        var updIsBoolean = updated != null && updated.isBoolean();
        var origIsSchema = original != null && original.isFullSchema();
        var updIsSchema = updated != null && updated.isFullSchema();

        if ((origIsBoolean || original == null) && (updIsBoolean || updated == null)) {
            diffBooleanTransition(ctx, origPermits, updPermits, true,
                    ARRAY_TYPE_ADDITIONAL_ITEMS_FALSE_TO_TRUE,
                    ARRAY_TYPE_ADDITIONAL_ITEMS_TRUE_TO_FALSE,
                    ARRAY_TYPE_ADDITIONAL_ITEMS_BOOLEAN_UNCHANGED);
        } else if (origIsSchema && updIsSchema) {
            if (isUnionSchemaCompatible(ctx, original, updated, true)) {
                ctx.addDifference(ARRAY_TYPE_SCHEMA_OF_ADDITIONAL_ITEMS_UNCHANGED,
                        original, updated);
            } else {
                ctx.addDifference(ARRAY_TYPE_SCHEMA_OF_ADDITIONAL_ITEMS_CHANGED,
                        original, updated);
            }
        } else if (!origPermits && updIsSchema) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_EXTENDED, original, updated);
        } else if (origPermits && !updPermits) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_NARROWED, original, updated);
        } else if (origIsSchema && (updated == null || (updIsBoolean && updPermits))) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_EXTENDED, original, updated);
        } else if (origIsSchema && updIsBoolean && !updPermits) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_NARROWED, original, updated);
        } else if ((original == null || (origIsBoolean && origPermits)) && updIsSchema) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_NARROWED, original, updated);
        }
        return false;
    }

    @Override
    public boolean diffFullSchemaContains(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;
        if (original == null) {
            ctx.addDifference(ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_ADDED, null, updated);
            return false;
        }
        if (updated == null) {
            ctx.addDifference(ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_REMOVED, original, null);
            return false;
        }
        var subCtx = ctx.sub("contains");
        if (!isUnionSchemaCompatible(subCtx, original, updated, true)) {
            subCtx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, original, updated);
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Object type fields
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaMinProperties(Integer original, Integer updated) {
        diffInteger(ctx, original, updated,
                OBJECT_TYPE_MIN_PROPERTIES_ADDED, OBJECT_TYPE_MIN_PROPERTIES_REMOVED,
                OBJECT_TYPE_MIN_PROPERTIES_INCREASED, OBJECT_TYPE_MIN_PROPERTIES_DECREASED);
    }

    @Override
    public void diffFullSchemaMaxProperties(Integer original, Integer updated) {
        diffInteger(ctx, original, updated,
                OBJECT_TYPE_MAX_PROPERTIES_ADDED, OBJECT_TYPE_MAX_PROPERTIES_REMOVED,
                OBJECT_TYPE_MAX_PROPERTIES_INCREASED, OBJECT_TYPE_MAX_PROPERTIES_DECREASED);
    }

    @Override
    public void diffFullSchemaRequired(List<String> original, List<String> updated) {
        if (original == null && updated == null) return;

        var origSet = original != null ? new HashSet<>(original) : new HashSet<String>();
        var updSet = updated != null ? new HashSet<>(updated) : new HashSet<String>();

        diffSetChanged(ctx, origSet, updSet,
                OBJECT_TYPE_REQUIRED_PROPERTIES_ADDED, OBJECT_TYPE_REQUIRED_PROPERTIES_REMOVED,
                OBJECT_TYPE_REQUIRED_PROPERTIES_CHANGED,
                OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_ADDED,
                OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_REMOVED);
    }

    @Override
    public void diffFullSchemaProperties(Map<String, JsonSchema> original,
                                         Map<String, JsonSchema> updated,
                                         CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        // Suppress auto-recursion for matched property schemas — we handle it ourselves
        if (original == null && updated == null) return;

        var origKeys = original != null ? new HashSet<>(original.keySet()) : new HashSet<String>();
        var updKeys = updated != null ? new HashSet<>(updated.keySet()) : new HashSet<String>();

        // Properties present in both
        var commonKeys = new HashSet<>(origKeys);
        commonKeys.retainAll(updKeys);
        for (var key : commonKeys) {
            var subCtx = ctx.sub(key);
            var origSchema = original.get(key);
            var updSchema = updated.get(key);
            if (!isUnionSchemaCompatible(subCtx, origSchema, updSchema, true)) {
                subCtx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED, origSchema, updSchema);
            }
        }

        var origAdditional = currentOriginal != null
                ? currentOriginal.getAdditionalProperties() : null;
        var updAdditional = currentUpdated != null
                ? currentUpdated.getAdditionalProperties() : null;
        var origPermitsAdditional = permitsAdditional(origAdditional);
        var updPermitsAdditional = permitsAdditional(updAdditional);

        // Properties added in updated
        var addedKeys = new HashSet<>(updKeys);
        addedKeys.removeAll(origKeys);
        if (!addedKeys.isEmpty()) {
            if (!origPermitsAdditional) {
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_EXTENDED, null, addedKeys);
            } else if (origAdditional != null && origAdditional.isFullSchema()
                    && updated != null) {
                var allCompatible = true;
                for (var key : addedKeys) {
                    var addedSchema = updated.get(key);
                    var subCtx = ctx.sub(key);
                    if (!isUnionSchemaCompatible(subCtx, origAdditional, addedSchema, true)) {
                        allCompatible = false;
                        break;
                    }
                }
                if (allCompatible) {
                    ctx.addDifference(
                            OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES,
                            null, addedKeys);
                } else {
                    ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED, null, addedKeys);
                }
            } else {
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED, null, addedKeys);
            }
        }

        // Properties removed in updated
        var removedKeys = new HashSet<>(origKeys);
        removedKeys.removeAll(updKeys);
        if (!removedKeys.isEmpty()) {
            if (!updPermitsAdditional) {
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED, removedKeys, null);
            } else if (updAdditional != null && updAdditional.isFullSchema()
                    && original != null) {
                var allCompatible = true;
                for (var key : removedKeys) {
                    var removedSchema = original.get(key);
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
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_EXTENDED, removedKeys, null);
            }
        }
    }

    @Override
    public boolean diffFullSchemaAdditionalProperties(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;

        var origPermits = permitsAdditional(original);
        var updPermits = permitsAdditional(updated);

        var origIsBoolean = original != null && original.isBoolean();
        var updIsBoolean = updated != null && updated.isBoolean();
        var origIsSchema = original != null && original.isFullSchema();
        var updIsSchema = updated != null && updated.isFullSchema();

        if ((origIsBoolean || original == null) && (updIsBoolean || updated == null)) {
            diffBooleanTransition(ctx, origPermits, updPermits, true,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_FALSE_TO_TRUE,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_TRUE_TO_FALSE,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_BOOLEAN_UNCHANGED);
        } else if (origIsSchema && updIsSchema) {
            if (isUnionSchemaCompatible(ctx, original, updated, true)) {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_UNCHANGED,
                        original, updated);
            } else {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_CHANGED,
                        original, updated);
            }
        } else if (!origPermits && updIsSchema) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_EXTENDED, original, updated);
        } else if (origPermits && !updPermits) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED, original, updated);
        } else if (origIsSchema && (updated == null || (updIsBoolean && updPermits))) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_EXTENDED, original, updated);
        } else if (origIsSchema && updIsBoolean && !updPermits) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED, original, updated);
        } else if ((original == null || (origIsBoolean && origPermits)) && updIsSchema) {
            ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED, original, updated);
        } else {
            if (origPermits && !updPermits) {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED, original, updated);
            } else if (!origPermits && updPermits) {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_EXTENDED, original, updated);
            }
        }
        return false;
    }

    @Override
    public void diffFullSchemaPatternProperties(Map<String, JsonSchema> original,
                                                Map<String, JsonSchema> updated,
                                                CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        // Suppress auto-recursion for matched pattern property schemas
        if (original == null && updated == null) return;

        var origKeys = original != null ? new HashSet<>(original.keySet()) : new HashSet<String>();
        var updKeys = updated != null ? new HashSet<>(updated.keySet()) : new HashSet<String>();

        diffSetChanged(ctx, origKeys, updKeys,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_ADDED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_REMOVED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_CHANGED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_ADDED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_REMOVED);

        if (original != null && updated != null) {
            var commonKeys = new HashSet<>(origKeys);
            commonKeys.retainAll(updKeys);
            for (var key : commonKeys) {
                var subCtx = ctx.sub("patternProperties/" + key);
                var origSchema = original.get(key);
                var updSchema = updated.get(key);
                if (!isUnionSchemaCompatible(subCtx, origSchema, updSchema, true)) {
                    subCtx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED,
                            origSchema, updSchema);
                }
            }
        }
    }

    @Override
    public boolean diffFullSchemaPropertyNames(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;
        compareSchema(ctx, original, updated,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_ADDED,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_REMOVED,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BOTH,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_NONE);
        return false;
    }

    @Override
    public void diffFullSchemaDependencies(Map<String, Dependency> original,
                                           Map<String, Dependency> updated,
                                           CollectionDiff<DefaultPairingKey, Dependency> diff) {
        // Suppress auto-recursion for matched dependencies
        if (original == null && updated == null) return;

        var origKeys = original != null
                ? new HashSet<>(original.keySet()) : new HashSet<String>();
        var updKeys = updated != null
                ? new HashSet<>(updated.keySet()) : new HashSet<String>();

        diffSetChanged(ctx, origKeys, updKeys,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_ADDED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_REMOVED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_CHANGED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_ADDED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_REMOVED);

        if (original != null && updated != null) {
            var commonKeys = new HashSet<>(origKeys);
            commonKeys.retainAll(updKeys);
            for (var key : commonKeys) {
                var origValue = original.get(key);
                var updValue = updated.get(key);
                if (origValue.isStringList() && updValue.isStringList()) {
                    var origSet = new HashSet<>(origValue.asStringList());
                    var updSet = new HashSet<>(updValue.asStringList());
                    for (var v : origSet) {
                        if (!updSet.contains(v)) {
                            ctx.addDifference(
                                    OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_REMOVED,
                                    v, null);
                        }
                    }
                    for (var v : updSet) {
                        if (!origSet.contains(v)) {
                            ctx.addDifference(
                                    OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_ADDED,
                                    null, v);
                        }
                    }
                    if (!origSet.equals(updSet)) {
                        ctx.addDifference(
                                OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_CHANGED,
                                origValue, updValue);
                    }
                } else if (origValue.isFullSchema() && updValue.isFullSchema()) {
                    var subCtx = ctx.sub("dependencies/" + key);
                    if (!isSchemaCompatible(subCtx, origValue.asFullSchema(),
                            updValue.asFullSchema(), true)) {
                        subCtx.addDifference(OBJECT_TYPE_SCHEMA_DEPENDENCIES_CHANGED,
                                origValue, updValue);
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Composition keywords (allOf, anyOf, oneOf)
    // TODO: Composition keyword transitions (allOf->anyOf, etc.) -- needs cross-field logic in visitFullSchema
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaAllOf(List<JsonSchema> original, List<JsonSchema> updated,
                                    CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        // Suppress auto-recursion: the traverser will iterate diff.getMatched() and
        // call traverseJsonSchema for each pair. We handle matching in diffCompositionList.

        // Cross-field transition detection: check if composition keyword changed
        if (currentOriginal != null && currentUpdated != null) {
            var updAnyOf = currentUpdated.getAnyOf();

            // allOf -> anyOf transition
            if (original != null && updAnyOf != null && updated == null) {
                diffCompositionList(ctx, original, updAnyOf,
                        COMBINED_TYPE_CRITERION_EXTENDED, COMBINED_TYPE_CRITERION_EXTENDED);
                ctx.addDifference(COMBINED_TYPE_CRITERION_EXTENDED, "allOf", "anyOf");
                return;
            }
            // anyOf -> allOf transition
            var origAnyOf = currentOriginal.getAnyOf();
            if (origAnyOf != null && updated != null && original == null
                    && currentUpdated.getAnyOf() == null) {
                ctx.addDifference(COMBINED_TYPE_CRITERION_NARROWED, "anyOf", "allOf");
                return;
            }
        }

        diffCompositionList(ctx, original, updated,
                COMBINED_TYPE_ALL_OF_SIZE_INCREASED, COMBINED_TYPE_ALL_OF_SIZE_DECREASED);
    }

    @Override
    public void diffFullSchemaAnyOf(List<JsonSchema> original, List<JsonSchema> updated,
                                    CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        if (currentOriginal != null && currentUpdated != null) {
            var origAllOf = currentOriginal.getAllOf();
            var origOneOf = currentOriginal.getOneOf();
            var updAllOf = currentUpdated.getAllOf();
            var updOneOf = currentUpdated.getOneOf();

            // allOf -> anyOf: already handled in diffFullSchemaAllOf
            if (origAllOf != null && updated != null && original == null && updAllOf == null) {
                return;
            }
            // oneOf -> anyOf
            if (origOneOf != null && updated != null && original == null && updOneOf == null) {
                diffCompositionList(ctx, origOneOf, updated,
                        COMBINED_TYPE_CRITERION_EXTENDED, COMBINED_TYPE_CRITERION_EXTENDED);
                ctx.addDifference(COMBINED_TYPE_CRITERION_EXTENDED, "oneOf", "anyOf");
                return;
            }
            // anyOf -> allOf: already handled in diffFullSchemaAllOf
            if (original != null && updAllOf != null && updated == null) {
                return;
            }
            // anyOf -> oneOf
            if (original != null && updOneOf != null && updated == null) {
                ctx.addDifference(COMBINED_TYPE_CRITERION_NARROWED, "anyOf", "oneOf");
                return;
            }
        }

        diffCompositionList(ctx, original, updated,
                COMBINED_TYPE_ANY_OF_SIZE_INCREASED, COMBINED_TYPE_ANY_OF_SIZE_DECREASED);
    }

    @Override
    public void diffFullSchemaOneOf(List<JsonSchema> original, List<JsonSchema> updated,
                                    CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        if (currentOriginal != null && currentUpdated != null) {
            var origAnyOf = currentOriginal.getAnyOf();
            var updAnyOf = currentUpdated.getAnyOf();

            // oneOf -> anyOf: already handled in diffFullSchemaAnyOf
            if (original != null && updAnyOf != null && updated == null
                    && origAnyOf == null) {
                return;
            }
            // anyOf -> oneOf: already handled in diffFullSchemaAnyOf
            if (origAnyOf != null && updated != null && original == null) {
                return;
            }
        }

        diffCompositionList(ctx, original, updated,
                COMBINED_TYPE_ONE_OF_SIZE_INCREASED, COMBINED_TYPE_ONE_OF_SIZE_DECREASED);
    }

    private void diffCompositionList(DiffContext ctx,
                                     List<JsonSchema> originalList,
                                     List<JsonSchema> updatedList,
                                     DiffType increasedType, DiffType decreasedType) {
        if (originalList == null && updatedList == null) return;
        if (originalList == null || updatedList == null) {
            ctx.addDifference(COMBINED_TYPE_CRITERION_CHANGED, originalList, updatedList);
            return;
        }

        if (updatedList.size() > originalList.size()) {
            ctx.addDifference(increasedType, originalList.size(), updatedList.size());
        } else if (updatedList.size() < originalList.size()) {
            ctx.addDifference(decreasedType, originalList.size(), updatedList.size());
        }

        var unmatchedCount = 0;
        for (var updSub : updatedList) {
            var matched = false;
            for (var origSub : originalList) {
                var subCtx = ctx.sub("composition");
                if (isUnionSchemaCompatible(subCtx, origSub, updSub, true)
                        && isUnionSchemaCompatible(subCtx, origSub, updSub, false)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                for (var origSub : originalList) {
                    var subCtx = ctx.sub("composition");
                    if (isUnionSchemaCompatible(subCtx, origSub, updSub, true)) {
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                unmatchedCount++;
            }
        }
        var newSubschemas = Math.max(0, updatedList.size() - originalList.size());
        var changedSubschemas = unmatchedCount - newSubschemas;
        if (changedSubschemas > 0) {
            ctx.addDifference(COMBINED_TYPE_SUBSCHEMA_NOT_COMPATIBLE,
                    originalList, updatedList);
        }
    }

    // -----------------------------------------------------------------------
    // Not schema
    // -----------------------------------------------------------------------

    @Override
    public boolean diffFullSchemaNot(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;
        compareSchema(ctx, original, updated,
                SUBSCHEMA_TYPE_CHANGED, SUBSCHEMA_TYPE_CHANGED,
                NOT_TYPE_SCHEMA_COMPATIBLE_BOTH,
                NOT_TYPE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                NOT_TYPE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                NOT_TYPE_SCHEMA_COMPATIBLE_NONE);
        return false;
    }

    // -----------------------------------------------------------------------
    // Conditional keywords (if/then/else)
    // TODO: Add post-recursion callback to DiffTraverser (generator feature) --
    //       would allow checking nested comparison results without isolated contexts
    // -----------------------------------------------------------------------

    @Override
    public boolean diffFullSchemaIf(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;
        compareSchema(ctx, original, updated,
                CONDITIONAL_TYPE_IF_SCHEMA_ADDED, CONDITIONAL_TYPE_IF_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_NONE);
        return false;
    }

    @Override
    public boolean diffFullSchemaThen(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;
        compareSchema(ctx, original, updated,
                CONDITIONAL_TYPE_THEN_SCHEMA_ADDED, CONDITIONAL_TYPE_THEN_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_NONE);
        return false;
    }

    @Override
    public boolean diffFullSchemaElse(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;
        compareSchema(ctx, original, updated,
                CONDITIONAL_TYPE_ELSE_SCHEMA_ADDED, CONDITIONAL_TYPE_ELSE_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_NONE);
        return false;
    }

    // -----------------------------------------------------------------------
    // Enum and const
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaEnum(List<JsonNode> original, List<JsonNode> updated) {
        var origConst = currentOriginal instanceof JCFullSchema c ? c.getConst() : null;
        var updConst = currentUpdated instanceof JCFullSchema c ? c.getConst() : null;

        if (original != null || updated != null) {
            if (original == null) {
                if (origConst != null && updated.size() == 1
                        && updated.get(0).toString().equals(origConst.toString())) {
                    // enum added is equivalent to existing const
                } else {
                    ctx.addDifference(ENUM_TYPE_VALUES_ADDED, null, updated);
                }
            } else if (updated == null) {
                if (updConst != null && original.size() == 1
                        && original.get(0).toString().equals(updConst.toString())) {
                    // enum removed is equivalent to new const
                } else {
                    ctx.addDifference(ENUM_TYPE_VALUES_CHANGED, original, null);
                }
            } else {
                var origSet = new HashSet<>(original.stream()
                        .map(Object::toString).toList());
                var updSet = new HashSet<>(updated.stream()
                        .map(Object::toString).toList());
                if (!origSet.equals(updSet)) {
                    ctx.addDifference(ENUM_TYPE_VALUES_CHANGED, original, updated);
                    for (var v : updSet) {
                        if (!origSet.contains(v)) {
                            ctx.addDifference(ENUM_TYPE_VALUES_MEMBER_ADDED, null, v);
                        }
                    }
                    for (var v : origSet) {
                        if (!updSet.contains(v)) {
                            ctx.addDifference(ENUM_TYPE_VALUES_MEMBER_REMOVED, v, null);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void diffFullSchemaConst(JsonNode original, JsonNode updated) {
        if (original == null && updated == null) return;
        if (original != null && updated != null) {
            if (!original.equals(updated)) {
                ctx.addDifference(CONST_TYPE_VALUE_CHANGED, original, updated);
            }
        } else if (original == null) {
            var origEnum = currentOriginal != null ? currentOriginal.getEnum() : null;
            if (origEnum != null && origEnum.size() == 1
                    && origEnum.get(0).toString().equals(updated.toString())) {
                return;
            }
            ctx.addDifference(CONST_TYPE_VALUE_ADDED, null, updated);
        } else {
            var updEnum = currentUpdated != null ? currentUpdated.getEnum() : null;
            if (updEnum != null && updEnum.size() == 1
                    && updEnum.get(0).toString().equals(original.toString())) {
                return;
            }
            ctx.addDifference(CONST_TYPE_VALUE_REMOVED, original, null);
        }
    }

    // -----------------------------------------------------------------------
    // Type field — integer/number transition
    // -----------------------------------------------------------------------

    @Override
    public boolean diffFullSchemaType(
            io.apitomy.datamodels.models.union.StringStringListUnion original,
            io.apitomy.datamodels.models.union.StringStringListUnion updated) {
        // Type change detection is handled in visitFullSchema.
        // Here we handle integer->number transition for the integer-required logic.
        if (currentOriginal != null && currentUpdated != null) {
            var origType = DiffUtil.getTypeString(currentOriginal);
            var updType = DiffUtil.getTypeString(currentUpdated);
            var effectiveType = origType != null ? origType : updType;
            if (effectiveType != null
                    && ("integer".equals(effectiveType) || "number".equals(effectiveType))) {
                var origIsInteger = "integer".equals(origType);
                var updIsInteger = "integer".equals(updType);
                diffBooleanTransition(ctx, origIsInteger, updIsInteger, false,
                        NUMBER_TYPE_INTEGER_REQUIRED_FALSE_TO_TRUE,
                        NUMBER_TYPE_INTEGER_REQUIRED_TRUE_TO_FALSE,
                        NUMBER_TYPE_INTEGER_REQUIRED_UNCHANGED);
            }
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // $ref — reference comparison
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchema$ref(String original, String updated) {
        // $ref resolution is handled in diffSchemas() before traversal.
        // This method handles nested $ref that might appear on resolved schemas.
        if (original == null && updated == null) return;

        var traversal = ctx.getRefTraversal();

        var origResolved = original != null && traversal != null
                ? traversal.resolveRef(original, currentOriginal)
                        .map(n -> (JFullSchema) n).orElse(null)
                : null;
        var updResolved = updated != null && traversal != null
                ? traversal.resolveRef(updated, currentUpdated)
                        .map(n -> (JFullSchema) n).orElse(null)
                : null;

        if (original != null && origResolved == null) {
            handleUnresolvableRef(ctx, original);
            if (ctx.getUnresolvableRefStrategy() != UnresolvableRefStrategy.FAIL) {
                return;
            }
        }
        if (updated != null && updResolved == null) {
            handleUnresolvableRef(ctx, updated);
            if (ctx.getUnresolvableRefStrategy() != UnresolvableRefStrategy.FAIL) {
                return;
            }
        }

        if (origResolved != null && updResolved != null) {
            var subCtx = ctx.sub("[ref]");
            diffSchemas(subCtx, origResolved, updResolved);
        } else if (original != null && updated == null) {
            if (origResolved != null) {
                var subCtx = ctx.sub("[ref]");
                diffSchemas(subCtx, origResolved, currentUpdated);
            } else {
                ctx.addDifference(REFERENCE_TYPE_TARGET_SCHEMA_REMOVED, original, null);
            }
        } else if (original == null && updated != null) {
            if (updResolved != null) {
                var subCtx = ctx.sub("[ref]");
                diffSchemas(subCtx, currentOriginal, updResolved);
            } else {
                ctx.addDifference(REFERENCE_TYPE_TARGET_SCHEMA_ADDED, null, updated);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Content schema
    // -----------------------------------------------------------------------

    @Override
    public boolean diffFullSchemaContentSchema(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) return false;
        if (original != null && updated != null) {
            var subCtx = ctx.sub("contentSchema");
            if (!isUnionSchemaCompatible(subCtx, original, updated, true)) {
                subCtx.addDifference(SUBSCHEMA_TYPE_CHANGED, original, updated);
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // No-op fields (metadata, no compatibility implications)
    // diffFullSchemaTitle, diffFullSchemaDescription, diffFullSchemaDefault,
    // diffFullSchemaExamples, diffFullSchema$schema, diffFullSchema$comment,
    // diffFullSchemaDeprecated, diffFullSchemaReadOnly, diffFullSchemaWriteOnly
    // are all inherited as no-ops from JCDiffVisitor
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Helper methods from NumberSchemaDiff
    // -----------------------------------------------------------------------

    private static String rangeToString(JCRangeValue range) {
        if (range == null) return null;
        String prefix = Boolean.TRUE.equals(range.isExclusive()) ? "exclusive " : "";
        return prefix + range.getValue();
    }

    private static BigDecimal toBigDecimal(Number n) {
        return new BigDecimal(n.toString());
    }

    private static boolean permitsAdditional(JsonSchema additionalProperties) {
        if (additionalProperties == null) return true;
        if (additionalProperties.isBoolean()) return additionalProperties.asBoolean();
        return true;
    }
}
