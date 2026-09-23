package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.union.StringStringListUnion;

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
            boolean changed = false;
            Set<T> copyUpdated = new HashSet<T>(updated);
            for (T originalMember : original) {
                if (updated.contains(originalMember)) {
                    copyUpdated.remove(originalMember);
                } else {
                    ctx.addDifference(removedMemberType, originalMember, null);
                    changed = true;
                }
            }
            for (T updatedMemberRemaining : copyUpdated) {
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
        if (isMultipleOf(original, updated)) {
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
        if (node instanceof Referenceable) {
            Referenceable ref = (Referenceable) node;
            return ref.get$ref();
        }
        return null;
    }

    /**
     * Whether {@code original} is an exact multiple of {@code updated}.
     * <p>
     * Tested on the quotient rather than with a remainder, and with a tolerance,
     * because BigDecimal has no equivalent in the TypeScript target and a plain
     * floating-point remainder is unreliable for decimals — {@code 0.3 % 0.1} is
     * not zero in binary floating point, though 0.3 plainly is a multiple of 0.1.
     * Dividing first keeps that case correct: the quotient rounds to 3 with an
     * error far below the tolerance.
     * <p>
     * A zero divisor yields {@code false}. JSON Schema requires {@code multipleOf}
     * to be greater than zero, so this only arises for a schema that is already
     * invalid.
     */
    private static boolean isMultipleOf(Number original, Number updated) {
        double value = original.doubleValue();
        double divisor = updated.doubleValue();
        if (divisor == 0.0) {
            return false;
        }
        double quotient = value / divisor;
        return Math.abs(quotient - Math.round(quotient)) < 1e-9;
    }
}
