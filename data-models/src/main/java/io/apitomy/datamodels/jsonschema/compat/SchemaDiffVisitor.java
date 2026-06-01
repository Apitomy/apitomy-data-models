package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.models.union.BooleanJSchemaUnion;

import java.util.List;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.*;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.*;

/**
 * Top-level diff visitor that dispatches to type-specific visitors based on the schema's type property.
 * <p>
 * In the Registry's everit-based checker, dispatch was via instanceof on typed Schema subclasses.
 * Here we inspect the type property at runtime since the model is flat.
 */
public class SchemaDiffVisitor {

    private final DiffContext ctx;
    private final SchemaAccessor original;

    public SchemaDiffVisitor(DiffContext ctx, SchemaAccessor original) {
        this.ctx = ctx;
        this.original = original;
    }

    /**
     * Entry point: compare original and updated schemas.
     */
    public static void diffSchemas(DiffContext ctx, SchemaAccessor original, SchemaAccessor updated) {
        var resolvedOriginal = resolveIfRef(ctx, original);
        var resolvedUpdated = resolveIfRef(ctx, updated);

        // Cycle detection: if we've already compared this pair, stop
        var pairKey = System.identityHashCode(resolvedOriginal.node())
                + ":" + System.identityHashCode(resolvedUpdated.node());
        if (ctx.visited.contains(pairKey)) {
            return;
        }
        ctx.visited.add(pairKey);

        new SchemaDiffVisitor(ctx, resolvedOriginal).visit(resolvedUpdated);
    }

    private static SchemaAccessor resolveIfRef(DiffContext ctx, SchemaAccessor schema) {
        var ref = schema.get$ref();
        if (ref == null) return schema;

        var traversal = ctx.getRefTraversal();
        if (traversal == null) {
            ctx.addUnsupported("$ref resolution not available: " + ref);
            return schema;
        }

        var resolved = traversal.resolveRef(ref, schema.node());
        if (resolved.isPresent()) {
            return SchemaAccessor.wrap(resolved.get().node());
        }

        ctx.addUnsupported("Unresolvable $ref: " + ref);
        return schema;
    }

    public void visit(SchemaAccessor updated) {
        var originalType = original.getTypeString();
        var updatedType = updated.getTypeString();

        if (originalType != null && updatedType != null && !originalType.equals(updatedType)) {
            if ("integer".equals(originalType) && "number".equals(updatedType)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, updatedType);
            } else if (updatedType.isEmpty() || isEmptyOrTrueSchema(updated)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, updatedType);
            } else {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED, originalType, updatedType);
            }
            return;
        }

        if (originalType != null && updatedType == null) {
            if (isEmptyOrTrueSchema(updated)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, "");
                return;
            }
            // Check if updated uses composition (anyOf/oneOf) that includes the original type
            var updAnyOf = updated.getAnyOf();
            var updOneOf = updated.getOneOf();
            if (updAnyOf != null || updOneOf != null) {
                var compositionList = updAnyOf != null ? updAnyOf : updOneOf;
                // Check if any subschema in the composition is backward-compatible
                // with the original schema (i.e., the original type is widened into a union)
                var origMatchesAny = false;
                for (var sub : compositionList) {
                    if (sub.isJSchema()) {
                        var subCtx = ctx.sub("compositionCheck");
                        if (isSchemaCompatible(subCtx, original.node(), sub.asJSchema(), true)) {
                            origMatchesAny = true;
                            break;
                        }
                    }
                }
                if (origMatchesAny) {
                    ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, "anyOf/oneOf");
                    return;
                }
            }
        }

        if (originalType == null && updatedType != null) {
            if (isEmptyOrTrueSchema(original)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED, "", updatedType);
                return;
            }
        }

        var effectiveType = originalType != null ? originalType : updatedType;
        if (effectiveType == null) {
            var origTypeList = original.getTypeList();
            var updTypeList = updated.getTypeList();
            if ((origTypeList != null && origTypeList.size() > 1)
                    || (updTypeList != null && updTypeList.size() > 1)) {
                ctx.addUnsupported("Multi-valued type field (e.g. type: [\"string\", \"number\"])");
            }
            diffAllPropertyGroups(original, updated);
            return;
        }

        switch (effectiveType) {
            case "object" -> new ObjectSchemaDiff(ctx, original, updated).visit();
            case "array" -> new ArraySchemaDiff(ctx, original, updated).visit();
            case "string" -> new StringSchemaDiff(ctx, original, updated).visit();
            case "number", "integer" -> new NumberSchemaDiff(ctx, original, updated, effectiveType).visit();
            case "boolean" -> { /* no type-specific constraints */ }
            case "null" -> { /* no type-specific constraints */ }
            default -> { /* unknown type, nothing to compare */ }
        }

        diffCompositionKeywords(original, updated);
        diffConditionalKeywords(original, updated);
        diffEnumConst(original, updated);
        diffReference(original, updated);
    }

    private boolean isEmptyOrTrueSchema(SchemaAccessor schema) {
        if (schema.getTypeString() != null
                || schema.getAllOf() != null
                || schema.getAnyOf() != null
                || schema.getOneOf() != null
                || schema.getNot() != null
                || schema.getEnum() != null
                || schema.getProperties() != null
                || schema.getRequired() != null
                || schema.getMinLength() != null
                || schema.getMaxLength() != null
                || schema.getMinimum() != null
                || schema.getMaximum() != null
                || schema.getMinItems() != null
                || schema.getMaxItems() != null
                || schema.getMinProperties() != null
                || schema.getMaxProperties() != null
                || schema.getPattern() != null
                || schema.getFormat() != null
                || schema.getMultipleOf() != null
                || schema.getAdditionalProperties() != null
                || schema.getPatternProperties() != null
                || getConst(schema) != null) {
            return false;
        }
        var node = schema.node();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.JSDraftDocument d
                && (d.getItems() != null || d.getAdditionalItems() != null)) {
            return false;
        }
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.JSDraftJSchema s
                && (s.getItems() != null || s.getAdditionalItems() != null)) {
            return false;
        }
        return true;
    }

    private void diffAllPropertyGroups(SchemaAccessor original, SchemaAccessor updated) {
        new ObjectSchemaDiff(ctx, original, updated).visit();
        new ArraySchemaDiff(ctx, original, updated).visit();
        new StringSchemaDiff(ctx, original, updated).visit();
        new NumberSchemaDiff(ctx, original, updated, null).visit();
        diffCompositionKeywords(original, updated);
        diffConditionalKeywords(original, updated);
        diffEnumConst(original, updated);
        diffReference(original, updated);
    }

    private void diffCompositionKeywords(SchemaAccessor original, SchemaAccessor updated) {
        var origAllOf = original.getAllOf();
        var origAnyOf = original.getAnyOf();
        var origOneOf = original.getOneOf();
        var updAllOf = updated.getAllOf();
        var updAnyOf = updated.getAnyOf();
        var updOneOf = updated.getOneOf();

        // allOf → anyOf is backward compatible (widening: from "must satisfy ALL" to "must satisfy ANY")
        // oneOf → anyOf is backward compatible (widening: from "must satisfy EXACTLY ONE" to "must satisfy ANY")
        if (origAllOf != null && updAnyOf != null && updAllOf == null) {
            diffCompositionList(ctx, origAllOf, updAnyOf,
                    COMBINED_TYPE_CRITERION_EXTENDED, COMBINED_TYPE_CRITERION_EXTENDED);
            ctx.addDifference(COMBINED_TYPE_CRITERION_EXTENDED, "allOf", "anyOf");
        } else if (origOneOf != null && updAnyOf != null && updOneOf == null) {
            diffCompositionList(ctx, origOneOf, updAnyOf,
                    COMBINED_TYPE_CRITERION_EXTENDED, COMBINED_TYPE_CRITERION_EXTENDED);
            ctx.addDifference(COMBINED_TYPE_CRITERION_EXTENDED, "oneOf", "anyOf");
        } else if (origAnyOf != null && updAllOf != null && updAnyOf == null) {
            ctx.addDifference(COMBINED_TYPE_CRITERION_NARROWED, "anyOf", "allOf");
        } else if (origAnyOf != null && updOneOf != null && updAnyOf == null) {
            ctx.addDifference(COMBINED_TYPE_CRITERION_NARROWED, "anyOf", "oneOf");
        } else {
            diffCompositionList(ctx, origAllOf, updAllOf,
                    COMBINED_TYPE_ALL_OF_SIZE_INCREASED, COMBINED_TYPE_ALL_OF_SIZE_DECREASED);
            diffCompositionList(ctx, origAnyOf, updAnyOf,
                    COMBINED_TYPE_ANY_OF_SIZE_INCREASED, COMBINED_TYPE_ANY_OF_SIZE_DECREASED);
            diffCompositionList(ctx, origOneOf, updOneOf,
                    COMBINED_TYPE_ONE_OF_SIZE_INCREASED, COMBINED_TYPE_ONE_OF_SIZE_DECREASED);
        }

        diffNotSchema(original, updated);
    }

    private void diffCompositionList(DiffContext ctx,
                                     List<BooleanJSchemaUnion> originalList,
                                     List<BooleanJSchemaUnion> updatedList,
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
            ctx.addDifference(COMBINED_TYPE_SUBSCHEMA_NOT_COMPATIBLE, originalList, updatedList);
        }
    }

    private void diffNotSchema(SchemaAccessor original, SchemaAccessor updated) {
        var origNot = original.getNot();
        var updNot = updated.getNot();
        if (origNot == null && updNot == null) return;

        compareSchema(ctx, origNot, updNot,
                // not schema: backwards compatibility is inverted
                SUBSCHEMA_TYPE_CHANGED, SUBSCHEMA_TYPE_CHANGED,
                NOT_TYPE_SCHEMA_COMPATIBLE_BOTH,
                NOT_TYPE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                NOT_TYPE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                NOT_TYPE_SCHEMA_COMPATIBLE_NONE);
    }

    private void diffConditionalKeywords(SchemaAccessor original, SchemaAccessor updated) {
        var origIf = getConditional(original, "if");
        var updIf = getConditional(updated, "if");
        var origThen = getConditional(original, "then");
        var updThen = getConditional(updated, "then");
        var origElse = getConditional(original, "else");
        var updElse = getConditional(updated, "else");

        if (origIf == null && updIf == null && origThen == null && updThen == null
                && origElse == null && updElse == null) {
            return;
        }

        compareSchema(ctx, origIf, updIf,
                CONDITIONAL_TYPE_IF_SCHEMA_ADDED, CONDITIONAL_TYPE_IF_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_NONE);

        compareSchema(ctx, origThen, updThen,
                CONDITIONAL_TYPE_THEN_SCHEMA_ADDED, CONDITIONAL_TYPE_THEN_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_NONE);

        compareSchema(ctx, origElse, updElse,
                CONDITIONAL_TYPE_ELSE_SCHEMA_ADDED, CONDITIONAL_TYPE_ELSE_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_NONE);
    }

    private static BooleanJSchemaUnion getConditional(SchemaAccessor schema, String keyword) {
        var node = schema.node();
        return switch (keyword) {
            case "if" -> {
                if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document d) yield d.getIf();
                if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema s) yield s.getIf();
                yield null;
            }
            case "then" -> {
                if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document d) yield d.getThen();
                if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema s) yield s.getThen();
                yield null;
            }
            case "else" -> {
                if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document d) yield d.getElse();
                if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema s) yield s.getElse();
                yield null;
            }
            default -> null;
        };
    }

    private void diffEnumConst(SchemaAccessor original, SchemaAccessor updated) {
        var origEnum = original.getEnum();
        var updEnum = updated.getEnum();
        var origConst = getConst(original);
        var updConst = getConst(updated);

        if (origEnum != null || updEnum != null) {
            if (origEnum == null) {
                if (origConst != null && updEnum.size() == 1
                        && updEnum.get(0).toString().equals(origConst.toString())) {
                    // enum added is equivalent to existing const — no diff
                } else {
                    ctx.addDifference(ENUM_TYPE_VALUES_ADDED, null, updEnum);
                }
            } else if (updEnum == null) {
                if (updConst != null && origEnum.size() == 1
                        && origEnum.get(0).toString().equals(updConst.toString())) {
                    // enum removed is equivalent to new const — no diff
                } else {
                    ctx.addDifference(ENUM_TYPE_VALUES_CHANGED, origEnum, null);
                }
            } else {
                var origSet = new java.util.HashSet<>(origEnum.stream().map(Object::toString).toList());
                var updSet = new java.util.HashSet<>(updEnum.stream().map(Object::toString).toList());
                if (!origSet.equals(updSet)) {
                    ctx.addDifference(ENUM_TYPE_VALUES_CHANGED, origEnum, updEnum);
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

        diffConst(original, updated);
    }

    private void diffConst(SchemaAccessor original, SchemaAccessor updated) {
        var origConst = getConst(original);
        var updConst = getConst(updated);
        if (origConst == null && updConst == null) return;
        if (origConst != null && updConst != null) {
            if (!origConst.equals(updConst)) {
                ctx.addDifference(CONST_TYPE_VALUE_CHANGED, origConst, updConst);
            }
        } else if (origConst == null) {
            var origEnum = original.getEnum();
            if (origEnum != null && origEnum.size() == 1
                    && origEnum.get(0).toString().equals(updConst.toString())) {
                return;
            }
            ctx.addDifference(CONST_TYPE_VALUE_ADDED, null, updConst);
        } else {
            var updEnum = updated.getEnum();
            if (updEnum != null && updEnum.size() == 1
                    && updEnum.get(0).toString().equals(origConst.toString())) {
                return;
            }
            ctx.addDifference(CONST_TYPE_VALUE_REMOVED, origConst, null);
        }
    }

    private static com.fasterxml.jackson.databind.JsonNode getConst(SchemaAccessor schema) {
        var node = schema.node();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft6.JSDraft6Document d) return d.getConst();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft6.JSDraft6JSchema s) return s.getConst();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document d) return d.getConst();
        if (node instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema s) return s.getConst();
        return null;
    }

    private void diffReference(SchemaAccessor original, SchemaAccessor updated) {
        var origRef = original.get$ref();
        var updRef = updated.get$ref();

        if (origRef == null && updRef == null) return;

        var traversal = ctx.getRefTraversal();

        // Resolve both $ref values to their target schemas
        var origResolved = origRef != null && traversal != null
                ? traversal.resolveRef(origRef, original.node()).map(r -> SchemaAccessor.wrap(r.node())).orElse(null)
                : null;
        var updResolved = updRef != null && traversal != null
                ? traversal.resolveRef(updRef, updated.node()).map(r -> SchemaAccessor.wrap(r.node())).orElse(null)
                : null;

        if (origRef != null && origResolved == null) {
            ctx.addUnsupported("Unresolvable $ref: " + origRef);
        }
        if (updRef != null && updResolved == null) {
            ctx.addUnsupported("Unresolvable $ref: " + updRef);
        }

        // Compare resolved targets
        if (origResolved != null && updResolved != null) {
            var subCtx = ctx.sub("[ref]");
            diffSchemas(subCtx, origResolved, updResolved);
        } else if (origRef != null && updRef == null) {
            // Original had $ref, updated inlined — compare resolved original vs updated
            if (origResolved != null) {
                var subCtx = ctx.sub("[ref]");
                diffSchemas(subCtx, origResolved, updated);
            } else {
                ctx.addDifference(REFERENCE_TYPE_TARGET_SCHEMA_REMOVED, origRef, null);
            }
        } else if (origRef == null && updRef != null) {
            // Original was inlined, updated uses $ref — compare original vs resolved updated
            if (updResolved != null) {
                var subCtx = ctx.sub("[ref]");
                diffSchemas(subCtx, original, updResolved);
            } else {
                ctx.addDifference(REFERENCE_TYPE_TARGET_SCHEMA_ADDED, null, updRef);
            }
        }
    }
}
