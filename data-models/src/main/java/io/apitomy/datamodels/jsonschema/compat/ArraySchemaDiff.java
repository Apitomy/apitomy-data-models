package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.JDFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.JD6FullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.JD7FullSchema;
import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaFullSchemaListUnion;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;

import java.util.List;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.*;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.*;

public class ArraySchemaDiff {

    private final DiffContext ctx;
    private final JFullSchema original;
    private final JFullSchema updated;

    public ArraySchemaDiff(DiffContext ctx, JFullSchema original, JFullSchema updated) {
        this.ctx = ctx;
        this.original = original;
        this.updated = updated;
    }

    public void visit() {
        diffMinMaxItems();
        diffUniqueItems();
        diffItems();
        diffAdditionalItems();
        diffContains();
    }

    private void diffContains() {
        var origContains = getContains(original);
        var updContains = getContains(updated);
        if (origContains == null && updContains == null) return;
        if (origContains == null) {
            ctx.addDifference(ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_ADDED, null, updContains);
            return;
        }
        if (updContains == null) {
            ctx.addDifference(ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_REMOVED, origContains, null);
            return;
        }
        var subCtx = ctx.sub("contains");
        if (!isUnionSchemaCompatible(subCtx, origContains, updContains, true)) {
            subCtx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, origContains, updContains);
        }
    }

    private void diffMinMaxItems() {
        diffInteger(ctx, original.getMinItems(), updated.getMinItems(),
                ARRAY_TYPE_MIN_ITEMS_ADDED, ARRAY_TYPE_MIN_ITEMS_REMOVED,
                ARRAY_TYPE_MIN_ITEMS_INCREASED, ARRAY_TYPE_MIN_ITEMS_DECREASED);

        diffInteger(ctx, original.getMaxItems(), updated.getMaxItems(),
                ARRAY_TYPE_MAX_ITEMS_ADDED, ARRAY_TYPE_MAX_ITEMS_REMOVED,
                ARRAY_TYPE_MAX_ITEMS_INCREASED, ARRAY_TYPE_MAX_ITEMS_DECREASED);
    }

    private void diffUniqueItems() {
        diffBooleanTransition(ctx, original.isUniqueItems(), updated.isUniqueItems(), false,
                ARRAY_TYPE_UNIQUE_ITEMS_FALSE_TO_TRUE,
                ARRAY_TYPE_UNIQUE_ITEMS_TRUE_TO_FALSE,
                ARRAY_TYPE_UNIQUE_ITEMS_BOOLEAN_UNCHANGED);
    }

    private void diffItems() {
        var origItems = getItems(original);
        var updItems = getItems(updated);

        if (origItems == null && updItems == null) return;

        if (origItems != null && updItems != null) {
            if (origItems.isFullSchema() && updItems.isFullSchema()) {
                var subCtx = ctx.sub("items");
                if (!DiffUtil.isSchemaCompatible(subCtx, origItems.asFullSchema(), updItems.asFullSchema(), true)) {
                    subCtx.addDifference(ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, origItems, updItems);
                }
            } else if (origItems.isFullSchemaList() && updItems.isFullSchemaList()) {
                diffTupleItems(origItems.asFullSchemaList(), updItems.asFullSchemaList());
            } else if (origItems.isFullSchemaList() && updItems.isFullSchema()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, origItems, updItems);
            } else if (origItems.isFullSchema() && updItems.isFullSchemaList()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, origItems, updItems);
            } else if (origItems.isBoolean() || updItems.isBoolean()) {
                // boolean items handled via isUnionSchemaCompatible indirectly
            }
        } else {
            diffAddedRemoved(ctx, origItems, updItems,
                    ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, ARRAY_TYPE_ALL_ITEM_SCHEMA_REMOVED);
        }
    }

    @SuppressWarnings("unchecked")
    private void diffTupleItems(List<? extends Node> origListRaw,
                                List<? extends Node> updListRaw) {
        var origList = (List<JFullSchema>) origListRaw;
        var updList = (List<JFullSchema>) updListRaw;
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
            var origAI = getAdditionalItems(original);
            if (origAI != null && origAI.isBoolean() && !origAI.asBoolean()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED, origList.size(), updList.size());
            } else if (origAI != null && origAI.isFullSchema()) {
                var allCompatible = true;
                for (var i = minSize; i < updList.size(); i++) {
                    var subCtx = ctx.sub("items/" + i);
                    if (!DiffUtil.isSchemaCompatible(subCtx, origAI.asFullSchema(), updList.get(i), true)) {
                        allCompatible = false;
                        break;
                    }
                }
                if (allCompatible) {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES, origList.size(), updList.size());
                } else {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED, origList.size(), updList.size());
                }
            } else {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED, origList.size(), updList.size());
            }
        } else if (updList.size() < origList.size()) {
            var updAI = getAdditionalItems(updated);
            var updPermitsAdditional = updAI == null || (updAI.isBoolean() ? updAI.asBoolean() : true);
            if (!updPermitsAdditional) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED, origList.size(), updList.size());
            } else if (updAI != null && updAI.isFullSchema()) {
                var allCompatible = true;
                for (var i = minSize; i < origList.size(); i++) {
                    var subCtx = ctx.sub("items/" + i);
                    if (!DiffUtil.isSchemaCompatible(subCtx, origList.get(i), updAI.asFullSchema(), true)) {
                        allCompatible = false;
                        break;
                    }
                }
                if (allCompatible) {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED, origList.size(), updList.size());
                } else {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED, origList.size(), updList.size());
                }
            } else {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED, origList.size(), updList.size());
            }
        }
    }

    private void diffAdditionalItems() {
        var origAI = getAdditionalItems(original);
        var updAI = getAdditionalItems(updated);

        if (origAI == null && updAI == null) return;

        var origPermits = origAI == null || (origAI.isBoolean() ? origAI.asBoolean() : true);
        var updPermits = updAI == null || (updAI.isBoolean() ? updAI.asBoolean() : true);
        var origIsBoolean = origAI != null && origAI.isBoolean();
        var updIsBoolean = updAI != null && updAI.isBoolean();
        var origIsSchema = origAI != null && origAI.isFullSchema();
        var updIsSchema = updAI != null && updAI.isFullSchema();

        if ((origIsBoolean || origAI == null) && (updIsBoolean || updAI == null)) {
            diffBooleanTransition(ctx, origPermits, updPermits, true,
                    ARRAY_TYPE_ADDITIONAL_ITEMS_FALSE_TO_TRUE,
                    ARRAY_TYPE_ADDITIONAL_ITEMS_TRUE_TO_FALSE,
                    ARRAY_TYPE_ADDITIONAL_ITEMS_BOOLEAN_UNCHANGED);
        } else if (origIsSchema && updIsSchema) {
            if (isUnionSchemaCompatible(ctx, origAI, updAI, true)) {
                ctx.addDifference(ARRAY_TYPE_SCHEMA_OF_ADDITIONAL_ITEMS_UNCHANGED, origAI, updAI);
            } else {
                ctx.addDifference(ARRAY_TYPE_SCHEMA_OF_ADDITIONAL_ITEMS_CHANGED, origAI, updAI);
            }
        } else if (!origPermits && updIsSchema) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_EXTENDED, origAI, updAI);
        } else if (origPermits && !updPermits) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_NARROWED, origAI, updAI);
        } else if (origIsSchema && (updAI == null || (updIsBoolean && updPermits))) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_EXTENDED, origAI, updAI);
        } else if (origIsSchema && updIsBoolean && !updPermits) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_NARROWED, origAI, updAI);
        } else if ((origAI == null || (origIsBoolean && origPermits)) && updIsSchema) {
            ctx.addDifference(ARRAY_TYPE_ADDITIONAL_ITEMS_NARROWED, origAI, updAI);
        }
    }

    // --- Version-specific accessors ---

    private static BooleanFullSchemaFullSchemaListUnion getItems(JFullSchema schema) {
        if (schema instanceof JDFullSchema d) return d.getItems();
        return null;
    }

    private static JsonSchema getAdditionalItems(JFullSchema schema) {
        if (schema instanceof JDFullSchema d) return d.getAdditionalItems();
        return null;
    }

    private static JsonSchema getContains(JFullSchema schema) {
        if (schema instanceof JD6FullSchema d) return d.getContains();
        if (schema instanceof JD7FullSchema d) return d.getContains();
        return null;
    }
}
