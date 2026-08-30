package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.union.StringStringListUnion;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Emits the directional diff for a boolean-keyword transition. By convention the "unchanged"
     * case emits nothing (see the {@code *_UNCHANGED} note on {@link DiffType}); it only reports
     * {@code true} so callers can tell no difference was recorded.
     *
     * @return {@code true} if the value was unchanged (no difference emitted), {@code false} otherwise
     */
    public static boolean diffBooleanTransition(DiffContext ctx, Boolean original, Boolean updated,
                                                 Boolean defaultValue,
                                                 DiffType changeFalseToTrue, DiffType changeTrueToFalse) {
        if (original == null) original = defaultValue;
        if (updated == null) updated = defaultValue;
        if (original && !updated) {
            ctx.addDifference(changeTrueToFalse, original, updated);
        } else if (!original && updated) {
            ctx.addDifference(changeFalseToTrue, original, updated);
        } else {
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

    static ModelType detectModelType(JFullSchema schema) {
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.modern.v202012.JM202012FullSchema) return ModelType.JM202012;
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.modern.v201909.JM201909FullSchema) return ModelType.JM201909;
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.draft.draft7.JD7FullSchema) return ModelType.JD7;
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.draft.draft6.JD6FullSchema) return ModelType.JD6;
        if (schema instanceof io.apitomy.datamodels.models.jsonschema.draft.draft4.JD4FullSchema) return ModelType.JD4;
        throw new IllegalArgumentException("Unhandled schema type: " + schema.getClass().getName()
                + ". Add support for this type in detectModelType().");
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
