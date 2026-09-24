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
            ctx.addDifference(addedType);
        } else if (original != null && updated == null) {
            ctx.addDifference(removedType);
        } else {
            return original != null;
        }
        return false;
    }

    /**
     * Compares the members of a keyword's value, ignoring order: the names in {@code required}, or
     * the keys of a map-valued keyword such as {@code patternProperties}. Each removed or added
     * member is reported at its element in the schema that has it, by key when {@code byKey},
     * otherwise by index, followed by one difference for the change as a whole.
     */
    static void diffMembers(DiffContext ctx, List<String> original, List<String> updated, boolean byKey,
                            DiffType addedType, DiffType removedType, DiffType changedType,
                            DiffType addedMemberType, DiffType removedMemberType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType)
                && diffMemberLists(ctx, null, original, updated, byKey, addedMemberType, removedMemberType)) {
            ctx.addDifference(changedType);
        }
    }

    /**
     * Reports each member removed from {@code original} or added to {@code updated} at its element,
     * below {@code key} when not {@code null}. Returns whether any member changed.
     */
    static boolean diffMemberLists(DiffContext ctx, String key, List<String> original, List<String> updated,
                                   boolean byKey, DiffType addedMemberType, DiffType removedMemberType) {
        boolean changed = false;
        HashSet<String> updatedMembers = new HashSet<String>(updated);
        HashSet<String> originalMembers = new HashSet<String>(original);
        for (int i = 0; i < original.size(); i++) {
            if (!updatedMembers.contains(original.get(i))) {
                ctx.addMemberDifference(removedMemberType, key, byKey ? original.get(i) : String.valueOf(i), null);
                changed = true;
            }
        }
        for (int i = 0; i < updated.size(); i++) {
            if (!originalMembers.contains(updated.get(i))) {
                ctx.addMemberDifference(addedMemberType, key, null, byKey ? updated.get(i) : String.valueOf(i));
                changed = true;
            }
        }
        return changed;
    }

    public static boolean diffInteger(DiffContext ctx, Integer original, Integer updated,
                                       DiffType addedType, DiffType removedType,
                                       DiffType increasedType, DiffType decreasedType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType)) {
            if (original < updated) {
                ctx.addDifference(increasedType);
            } else if (original > updated) {
                ctx.addDifference(decreasedType);
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
            ctx.addDifference(multipleOfType);
        } else {
            ctx.addDifference(notMultipleOfType);
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
            ctx.addDifference(changeTrueToFalse);
        } else if (!original && updated) {
            ctx.addDifference(changeFalseToTrue);
        } else {
            return true;
        }
        return false;
    }

    public static void diffObject(DiffContext ctx, Object original, Object updated,
                                   DiffType addedType, DiffType removedType, DiffType changedType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType) && !original.equals(updated)) {
            ctx.addDifference(changedType);
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
