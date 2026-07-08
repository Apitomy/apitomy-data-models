package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.jsonschema.convert.CompoundSchemaConverter;
import io.apitomy.datamodels.models.union.StringStringListUnion;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class DiffUtil {

    private DiffUtil() {
    }

    /**
     * Checks whether original/updated are added or removed.
     *
     * @return true if both objects are present (further comparison needed)
     */
    public static boolean diffAddedRemoved(DiffContext ctx, Object original, Object updated,
                                           DiffType addedType, DiffType removedType) {
        if (original == null && updated != null) {
            ctx.addDifference(addedType, original, updated);
        } else if (original != null && updated == null) {
            ctx.addDifference(removedType, original, updated);
        } else {
            return original != null;
        }
        return false;
    }

    public static <T> void diffSetChanged(DiffContext ctx, Set<T> original, Set<T> updated,
                                           DiffType addedType, DiffType removedType, DiffType changedType,
                                           DiffType addedMemberType, DiffType removedMemberType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType)) {
            var changed = false;
            var copyUpdated = new HashSet<>(updated);
            for (var originalMember : original) {
                if (updated.contains(originalMember)) {
                    copyUpdated.remove(originalMember);
                } else {
                    ctx.addDifference(removedMemberType, originalMember, null);
                    changed = true;
                }
            }
            for (var updatedMemberRemaining : copyUpdated) {
                ctx.addDifference(addedMemberType, null, updatedMemberRemaining);
                changed = true;
            }
            if (changed) {
                ctx.addDifference(changedType, original, updated);
            }
        }
    }

    public static boolean diffSubschemaAddedRemoved(DiffContext ctx, Object original, Object updated,
                                                     DiffType addedType, DiffType removedType) {
        return diffAddedRemoved(ctx, original, updated, addedType, removedType);
    }

    public static boolean diffInteger(DiffContext ctx, Integer original, Integer updated,
                                       DiffType addedType, DiffType removedType,
                                       DiffType increasedType, DiffType decreasedType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType)) {
            if (original < updated) {
                ctx.addDifference(increasedType, original, updated);
            } else if (original > updated) {
                ctx.addDifference(decreasedType, original, updated);
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean diffNumber(DiffContext ctx, Number original, Number updated,
                                      DiffType addedType, DiffType removedType,
                                      DiffType increasedType, DiffType decreasedType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType)) {
            var o = new BigDecimal(original.toString());
            var u = new BigDecimal(updated.toString());
            if (o.compareTo(u) < 0) {
                ctx.addDifference(increasedType, original, updated);
            } else if (o.compareTo(u) > 0) {
                ctx.addDifference(decreasedType, original, updated);
            } else {
                return true;
            }
        }
        return false;
    }

    public static void diffNumberOriginalMultipleOfUpdated(DiffContext ctx, Number original, Number updated,
                                                            DiffType multipleOfType,
                                                            DiffType notMultipleOfType) {
        requireNonNull(original);
        requireNonNull(updated);
        var o = new BigDecimal(original.toString());
        var u = new BigDecimal(updated.toString());
        if (o.remainder(u).compareTo(BigDecimal.ZERO) == 0) {
            ctx.addDifference(multipleOfType, original, updated);
        } else {
            ctx.addDifference(notMultipleOfType, original, updated);
        }
    }

    public static boolean diffBooleanTransition(DiffContext ctx, Boolean original, Boolean updated,
                                                 Boolean defaultValue,
                                                 DiffType changeFalseToTrue, DiffType changeTrueToFalse,
                                                 DiffType unchanged) {
        if (original == null) original = defaultValue;
        if (updated == null) updated = defaultValue;
        if (original && !updated) {
            ctx.addDifference(changeTrueToFalse, original, updated);
        } else if (!original && updated) {
            ctx.addDifference(changeFalseToTrue, original, updated);
        } else {
            ctx.addDifference(unchanged, original, updated);
            return true;
        }
        return false;
    }

    public static void diffObject(DiffContext ctx, Object original, Object updated,
                                   DiffType addedType, DiffType removedType, DiffType changedType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType) && !original.equals(updated)) {
            ctx.addDifference(changedType, original, updated);
        }
    }

    public static void diffObjectDefault(DiffContext ctx, Object original, Object updated,
                                          Object defaultValue,
                                          DiffType addedType, DiffType removedType, DiffType changedType) {
        if (Objects.equals(defaultValue, original)) original = null;
        if (Objects.equals(defaultValue, updated)) updated = null;
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType)
                && !original.equals(updated)) {
            ctx.addDifference(changedType, original, updated);
        }
    }

    public static boolean diffObjectIdentity(DiffContext ctx, Object original, Object updated, Object target,
                                              DiffType addedType, DiffType removedType,
                                              DiffType extendedType, DiffType narrowedType,
                                              DiffType changedType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType) && original != updated) {
            if (updated == target) {
                ctx.addDifference(extendedType, original, updated);
            } else if (original == target) {
                ctx.addDifference(narrowedType, original, updated);
            } else {
                ctx.addDifference(changedType, original, updated);
            }
            return false;
        }
        return true;
    }

    /**
     * Compare two sub-schemas for compatibility. Uses a fresh DiffContext to isolate
     * the comparison, but shares the visited set to prevent infinite recursion.
     */
    public static boolean isSchemaCompatible(DiffContext ctx, JFullSchema original, JFullSchema updated,
                                              boolean backward) {
        ctx.pushIsolatedScope();
        if (original instanceof JCFullSchema || updated instanceof JCFullSchema) {
            var origCompound = ensureCompound(original);
            var updCompound = ensureCompound(updated);
            if (backward) {
                CompoundSchemaDiffVisitor.diffSchemas(ctx, origCompound, updCompound);
            } else {
                CompoundSchemaDiffVisitor.diffSchemas(ctx, updCompound, origCompound);
            }
        } else {
            if (backward) {
                SchemaDiffVisitor.diffSchemas(ctx, original, updated);
            } else {
                SchemaDiffVisitor.diffSchemas(ctx, updated, original);
            }
        }
        return ctx.popScopeIsCompatible();
    }

    static JFullSchema ensureCompound(JFullSchema schema) {
        if (schema instanceof JCFullSchema) return schema;
        // Use instanceof checks to determine the model type, ignoring root().modelType()
        // because sub-schemas may have a compound root after top-level conversion
        // while themselves remaining draft-specific.
        ModelType modelType = detectModelType(schema);
        if (modelType != null) {
            var converted = CompoundSchemaConverter.toCompound((JsonSchema) schema, modelType);
            if (converted instanceof JFullSchema fs) return fs;
        }
        return schema;
    }

    static ModelType detectModelType(JFullSchema schema) {
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.modern.v202012.JM202012FullSchema) return ModelType.JM202012;
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.modern.v201909.JM201909FullSchema) return ModelType.JM201909;
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JD7FullSchema) return ModelType.JD7;
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.draft.draft6.JD6FullSchema) return ModelType.JD6;
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.draft.draft4.JD4FullSchema) return ModelType.JD4;
        throw new IllegalArgumentException("Unhandled schema type: " + schema.getClass().getName()
                + ". Add support for this type in detectModelTypeByInstance().");
    }

    public static boolean isUnionSchemaCompatible(DiffContext ctx, JsonSchema original,
                                                   JsonSchema updated, boolean backward) {
        if (original == null || updated == null) return original == updated;
        if (original.isBoolean() && updated.isBoolean()) {
            if (original.asBoolean() == updated.asBoolean()) return true;
            if (backward) {
                // false → true: widening (backward compatible)
                // true → false: narrowing (not backward compatible)
                return !original.asBoolean() && updated.asBoolean();
            } else {
                return original.asBoolean() && !updated.asBoolean();
            }
        }
        if (original.isBoolean()) {
            if (backward) {
                // false → schema: widening from nothing to something (backward compatible)
                // true → schema: narrowing from everything to something (not backward compatible)
                return !original.asBoolean();
            } else {
                return original.asBoolean();
            }
        }
        if (updated.isBoolean()) {
            if (backward) {
                // schema → true: widening to everything (backward compatible)
                // schema → false: narrowing to nothing (not backward compatible)
                return updated.asBoolean();
            } else {
                return !updated.asBoolean();
            }
        }
        return isSchemaCompatible(ctx, original.asFullSchema(), updated.asFullSchema(), backward);
    }

    public static void compareSchema(DiffContext ctx, JsonSchema original,
                                      JsonSchema updated,
                                      DiffType addedType, DiffType removedType,
                                      DiffType bothType, DiffType backwardNotForwardType,
                                      DiffType forwardNotBackwardType, DiffType noneType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType)) {
            compareSchemaWhenExist(ctx, original, updated, bothType,
                    backwardNotForwardType, forwardNotBackwardType, noneType);
        }
    }

    public static void compareSchemaWhenExist(DiffContext ctx, JsonSchema original,
                                               JsonSchema updated,
                                               DiffType bothType, DiffType backwardType,
                                               DiffType forwardType, DiffType noneType) {
        var backward = isUnionSchemaCompatible(ctx, original, updated, true);
        var forward = isUnionSchemaCompatible(ctx, original, updated, false);

        if (backward && forward) {
            ctx.addDifference(bothType, original, updated);
        } else if (backward) {
            ctx.addDifference(backwardType, original, updated);
        } else if (forward) {
            ctx.addDifference(forwardType, original, updated);
        } else {
            ctx.addDifference(noneType, original, updated);
        }
    }

    public static <T> T getExceptionally(DiffContext ctx, Supplier<T> getter) {
        try {
            return getter.get();
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getTypeString(JFullSchema schema) {
        StringStringListUnion type = schema.getType();
        if (type != null && type.isString()) {
            return type.asString();
        }
        return null;
    }

    public static List<String> getTypeList(JFullSchema schema) {
        StringStringListUnion type = schema.getType();
        if (type == null) return null;
        if (type.isString()) return List.of(type.asString());
        if (type.isStringList()) return type.asStringList();
        return null;
    }

    public static String get$ref(Node node) {
        if (node instanceof Referenceable ref) {
            return ref.get$ref();
        }
        return null;
    }
}
