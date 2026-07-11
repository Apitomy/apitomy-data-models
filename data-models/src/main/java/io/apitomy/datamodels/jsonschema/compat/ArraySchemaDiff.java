package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.models.jsonschema.draft.JSDraftDocument;
import io.apitomy.datamodels.models.jsonschema.draft.JSDraftJSchema;
import io.apitomy.datamodels.models.union.BooleanJSchemaJSchemaListUnion;
import io.apitomy.datamodels.models.union.BooleanJSchemaUnion;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.*;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.*;

public class ArraySchemaDiff {

    private final DiffContext ctx;
    private final SchemaAccessor original;
    private final SchemaAccessor updated;

    public ArraySchemaDiff(DiffContext ctx, SchemaAccessor original, SchemaAccessor updated) {
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
            if (origItems.isJSchema() && updItems.isJSchema()) {
                var subCtx = ctx.sub("items");
                if (!DiffUtil.isSchemaCompatible(subCtx, origItems.asJSchema(), updItems.asJSchema(), true)) {
                    subCtx.addDifference(ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, origItems, updItems);
                }
            } else if (origItems.isJSchemaList() && updItems.isJSchemaList()) {
                diffTupleItems(origItems.asJSchemaList(), updItems.asJSchemaList());
            } else if (origItems.isJSchemaList() && updItems.isJSchema()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, origItems, updItems);
            } else if (origItems.isJSchema() && updItems.isJSchemaList()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, origItems, updItems);
            } else if (origItems.isBoolean() || updItems.isBoolean()) {
                // boolean items handled via isUnionSchemaCompatible indirectly
            }
        } else {
            diffAddedRemoved(ctx, origItems, updItems,
                    ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, ARRAY_TYPE_ALL_ITEM_SCHEMA_REMOVED);
        }
    }

    private void diffTupleItems(java.util.List<? extends io.apitomy.datamodels.models.Node> origList,
                                java.util.List<? extends io.apitomy.datamodels.models.Node> updList) {
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
            } else if (origAI != null && origAI.isJSchema()) {
                var allCompatible = true;
                for (var i = minSize; i < updList.size(); i++) {
                    var subCtx = ctx.sub("items/" + i);
                    if (!DiffUtil.isSchemaCompatible(subCtx, origAI.asJSchema(), updList.get(i), true)) {
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
            } else if (updAI != null && updAI.isJSchema()) {
                var allCompatible = true;
                for (var i = minSize; i < origList.size(); i++) {
                    var subCtx = ctx.sub("items/" + i);
                    if (!DiffUtil.isSchemaCompatible(subCtx, origList.get(i), updAI.asJSchema(), true)) {
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
        var origIsSchema = origAI != null && origAI.isJSchema();
        var updIsSchema = updAI != null && updAI.isJSchema();

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

    private static BooleanJSchemaJSchemaListUnion getItems(SchemaAccessor schema) {
        var node = schema.node();
        if (node instanceof JSDraftDocument d) return d.getItems();
        if (node instanceof JSDraftJSchema s) return s.getItems();
        return null;
    }

    private static BooleanJSchemaUnion getAdditionalItems(SchemaAccessor schema) {
        var node = schema.node();
        if (node instanceof JSDraftDocument d) return d.getAdditionalItems();
        if (node instanceof JSDraftJSchema s) return s.getAdditionalItems();
        return null;
    }

    private static BooleanJSchemaUnion getContains(SchemaAccessor schema) {
        var node = schema.node();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft6.JSDraft6Document d) return d.getContains();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft6.JSDraft6JSchema s) return s.getContains();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document d) return d.getContains();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema s) return s.getContains();
        return null;
    }
}
