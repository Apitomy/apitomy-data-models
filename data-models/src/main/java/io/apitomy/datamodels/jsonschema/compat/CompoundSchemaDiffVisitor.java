package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.jsonschema.convert.CompoundSchemaConverter;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.BooleanFullSchemaFullSchemaListUnion;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
import io.apitomy.datamodels.models.jsonschema.compound.visitors.JCDiffTraverser;
import io.apitomy.datamodels.models.jsonschema.compound.visitors.JCDiffVisitor;

import io.apitomy.datamodels.models.jsonschema.compound.JCRangeValue;
import io.apitomy.datamodels.models.visitors.diff.CollectionDiff;
import io.apitomy.datamodels.models.visitors.diff.DefaultPairingKey;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ADDITIONAL_ITEMS_EXTENDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ADDITIONAL_ITEMS_FALSE_TO_TRUE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ADDITIONAL_ITEMS_NARROWED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ADDITIONAL_ITEMS_TRUE_TO_FALSE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ALL_ITEM_SCHEMA_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ITEM_SCHEMAS_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ITEM_SCHEMAS_NARROWED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_ITEM_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_MAX_ITEMS_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_MAX_ITEMS_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_MAX_ITEMS_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_MAX_ITEMS_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_MIN_ITEMS_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_MIN_ITEMS_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_MIN_ITEMS_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_MIN_ITEMS_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_SCHEMA_OF_ADDITIONAL_ITEMS_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_UNIQUE_ITEMS_FALSE_TO_TRUE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ARRAY_TYPE_UNIQUE_ITEMS_TRUE_TO_FALSE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_ALL_OF_SIZE_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_ALL_OF_SIZE_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_ANY_OF_SIZE_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_ANY_OF_SIZE_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_CRITERION_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_CRITERION_EXTENDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_CRITERION_NARROWED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_ONE_OF_SIZE_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_ONE_OF_SIZE_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.COMBINED_TYPE_SUBSCHEMA_NOT_COMPATIBLE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_ELSE_SCHEMA_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BOTH;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_NONE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_ELSE_SCHEMA_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_IF_SCHEMA_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BOTH;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_NONE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_IF_SCHEMA_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_THEN_SCHEMA_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BOTH;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_NONE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONDITIONAL_TYPE_THEN_SCHEMA_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONST_TYPE_VALUE_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONST_TYPE_VALUE_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.CONST_TYPE_VALUE_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ENUM_TYPE_VALUES_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ENUM_TYPE_VALUES_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ENUM_TYPE_VALUES_MEMBER_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.ENUM_TYPE_VALUES_MEMBER_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NOT_TYPE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NOT_TYPE_SCHEMA_COMPATIBLE_BOTH;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NOT_TYPE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NOT_TYPE_SCHEMA_COMPATIBLE_NONE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_INTEGER_REQUIRED_FALSE_TO_TRUE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_INTEGER_REQUIRED_TRUE_TO_FALSE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MAXIMUM_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MAXIMUM_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MAXIMUM_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MAXIMUM_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MINIMUM_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MINIMUM_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MINIMUM_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MINIMUM_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MULTIPLE_OF_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MULTIPLE_OF_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MULTIPLE_OF_UPDATED_IS_DIVISIBLE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.NUMBER_TYPE_MULTIPLE_OF_UPDATED_IS_NOT_DIVISIBLE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_ADDITIONAL_PROPERTIES_EXTENDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_ADDITIONAL_PROPERTIES_FALSE_TO_TRUE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_ADDITIONAL_PROPERTIES_TRUE_TO_FALSE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_MAX_PROPERTIES_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_MAX_PROPERTIES_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_MAX_PROPERTIES_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_MAX_PROPERTIES_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_MIN_PROPERTIES_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_MIN_PROPERTIES_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_MIN_PROPERTIES_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_MIN_PROPERTIES_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PATTERN_PROPERTY_KEYS_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PATTERN_PROPERTY_KEYS_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PATTERN_PROPERTY_KEYS_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BOTH;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_NONE;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_SCHEMAS_EXTENDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_REQUIRED_PROPERTIES_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.OBJECT_TYPE_SCHEMA_DEPENDENCIES_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.REFERENCE_TYPE_TARGET_SCHEMA_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_CONTENT_ENCODING_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_CONTENT_ENCODING_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_CONTENT_ENCODING_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_CONTENT_MEDIA_TYPE_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_CONTENT_MEDIA_TYPE_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_CONTENT_MEDIA_TYPE_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_FORMAT_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_FORMAT_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_FORMAT_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MAX_LENGTH_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MAX_LENGTH_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MAX_LENGTH_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MAX_LENGTH_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MIN_LENGTH_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MIN_LENGTH_DECREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MIN_LENGTH_INCREASED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_MIN_LENGTH_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_PATTERN_ADDED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_PATTERN_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.STRING_TYPE_PATTERN_REMOVED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.SUBSCHEMA_TYPE_CHANGED;
import static io.apitomy.datamodels.jsonschema.compat.DiffType.SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.diffAddedRemoved;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.diffBooleanTransition;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.diffInteger;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.diffNumberOriginalMultipleOfUpdated;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.diffObject;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.diffSetChanged;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.getTypeList;
import static io.apitomy.datamodels.jsonschema.compat.DiffUtil.getTypeString;
import io.apitomy.datamodels.util.NumberUtil;

/**
 * Diff visitor driven by the generated {@link JCDiffTraverser}.
 * Extends the generated {@link JCDiffVisitor} and overrides per-field methods
 * to produce compatibility {@link Difference}s collected via {@link DiffContext}.
 * <p>
 * The traverser iterates all compound-schema fields and calls the appropriate
 * visitor method for each field. The visitor delegates to {@link DiffUtil} for
 * generic diff primitives; schema-level compatibility recursion lives in this class.
 */
// TODO: Modern schema support — $dynamicRef, $recursiveRef
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
    // Entry point + on-demand conversion
    // -----------------------------------------------------------------------

    /**
     * Entry point: compare original and updated schemas. Either operand is converted to
     * compound on demand (see {@link #toCompoundIfNeeded}); once the deep conversion is
     * genuinely deep (tuple {@code items} elements included) this becomes a passthrough
     * and the on-demand step can be dropped. Any {@code $ref} nodes should be resolved by
     * the dereferencer before calling this method.
     */
    static void diffSchemas(DiffContext ctx, JFullSchema original, JFullSchema updated) {
        JFullSchema compoundOriginal = toCompoundIfNeeded(original);
        JFullSchema compoundUpdated = toCompoundIfNeeded(updated);

        String pairKey = ctx.identityId(original) + ":" + ctx.identityId(updated);
        if (ctx.visited.contains(pairKey)) {
            return;
        }
        ctx.visited.add(pairKey);
        try {
            if (!(compoundOriginal instanceof JCFullSchema)
                    || !(compoundUpdated instanceof JCFullSchema)) {
                throw new IllegalStateException(
                        "diffSchemas requires both operands to be compound (JCFullSchema) after conversion, "
                        + "but one could not be converted. original=" + compoundOriginal.getClass().getName()
                        + ", updated=" + compoundUpdated.getClass().getName());
            }
            JCFullSchema origCompound = (JCFullSchema) compoundOriginal;
            JCFullSchema updCompound = (JCFullSchema) compoundUpdated;

            CompoundSchemaDiffVisitor visitor = new CompoundSchemaDiffVisitor(ctx);
            JCDiffTraverser<DefaultPairingKey> traverser = new JCDiffTraverser<>(visitor);
            traverser.traverseFullSchema(origCompound, updCompound);
        } finally {
            ctx.visited.remove(pairKey);
        }
    }

    private static JFullSchema toCompoundIfNeeded(JFullSchema schema) {
        if (schema instanceof JCFullSchema) {
            return schema;
        }
        ModelType modelType = DiffUtil.detectModelType(schema);
        if (modelType != null) {
            JsonSchema converted = CompoundSchemaConverter.toCompound((JsonSchema) schema, modelType);
            if (converted instanceof JFullSchema) {
                JFullSchema fs = (JFullSchema) converted;
                return fs;
            }
        }
        return schema;
    }

    // -----------------------------------------------------------------------
    // Entity visit — type dispatch, empty-schema detection
    // -----------------------------------------------------------------------

    @Override
    public void visitFullSchema(JCFullSchema original, JCFullSchema updated) {
        // Store for cross-field logic
        this.currentOriginal = original;
        this.currentUpdated = updated;

        if (original == null || updated == null) {
            return;
        }

        // If either side has a remaining $ref (cyclic back-edge or unresolved),
        // skip field-by-field comparison — the $ref stub has no meaningful fields.
        // Only compare the $ref strings themselves.
        String origRef = DiffUtil.get$ref(original);
        String updRef = DiffUtil.get$ref(updated);
        if (origRef != null || updRef != null) {
            if (origRef != null && updRef != null && !origRef.equals(updRef)) {
                ctx.addDifference(REFERENCE_TYPE_TARGET_SCHEMA_CHANGED, origRef, updRef);
            }
            traversalContext.skip(); return;
        }

        List<String> origTypeList = DiffUtil.getTypeList(original);
        List<String> updTypeList = DiffUtil.getTypeList(updated);
        String originalType = DiffUtil.getTypeString(original);
        String updatedType = DiffUtil.getTypeString(updated);

        if (origTypeList != null && updTypeList != null) {
            HashSet<String> origSet = new HashSet<>(origTypeList);
            HashSet<String> updSet = new HashSet<>(updTypeList);
            // Normalize: integer is a subset of number
            if (origSet.contains("integer") && updSet.contains("number")) {
                origSet.remove("integer");
                origSet.add("number");
            }
            if (updSet.contains("integer") && origSet.contains("number")) {
                updSet.remove("integer");
                updSet.add("number");
            }
            if (!origSet.equals(updSet)) {
                HashSet<String> added = new HashSet<>(updSet);
                added.removeAll(origSet);
                HashSet<String> removed = new HashSet<>(origSet);
                removed.removeAll(updSet);
                if (!removed.isEmpty() && added.isEmpty()) {
                    ctx.addDifference(SUBSCHEMA_TYPE_CHANGED, origTypeList, updTypeList);
                } else if (removed.isEmpty() && !added.isEmpty()) {
                    ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, origTypeList, updTypeList);
                } else {
                    ctx.addDifference(SUBSCHEMA_TYPE_CHANGED, origTypeList, updTypeList);
                }
                traversalContext.skip(); return;
            }
        } else if (originalType != null && updatedType != null && !originalType.equals(updatedType)) {
            if ("integer".equals(originalType) && "number".equals(updatedType)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, updatedType);
            } else if (updatedType.isEmpty() || isEmptyOrTrueSchema(updated)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, updatedType);
            } else {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED, originalType, updatedType);
            }
            traversalContext.skip(); return;
        } else if (originalType != null && updatedType == null) {
            if (isEmptyOrTrueSchema(updated)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, "");
                traversalContext.skip(); return;
            }
            List<JsonSchema> updAnyOf = updated.getAnyOf();
            List<JsonSchema> updOneOf = updated.getOneOf();
            if (updAnyOf != null || updOneOf != null) {
                List<JsonSchema> compositionList = updAnyOf != null ? updAnyOf : updOneOf;
                boolean origMatchesAny = false;
                for (JsonSchema sub : compositionList) {
                    if (sub.isFullSchema()) {
                        DiffContext subCtx = ctx.sub("compositionCheck");
                        if (isSchemaCompatible(subCtx, original, sub.asFullSchema(), true)) {
                            origMatchesAny = true;
                            break;
                        }
                    }
                }
                if (origMatchesAny) {
                    ctx.addDifference(SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE, originalType, "anyOf/oneOf");
                    traversalContext.skip(); return;
                }
            }
        } else if (originalType == null && updatedType != null) {
            if (isEmptyOrTrueSchema(original)) {
                ctx.addDifference(SUBSCHEMA_TYPE_CHANGED, "", updatedType);
                traversalContext.skip(); return;
            }
        }

        // Return true to let the traverser call all field-level diff methods
        return;
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
        if (schema instanceof JCFullSchema) {
            JCFullSchema c = (JCFullSchema) schema;
            if (c.getMinimum() != null || c.getMaximum() != null
                    || c.getItems() != null || c.getAdditionalItems() != null
                    || c.getConst() != null) {
                return false;
            }
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
    public void visitFullSchemaProperty(JsonSchema original, JsonSchema updated) { traversalContext.skip(); }

    @Override
    public void visitFullSchemaPatternProperty(JsonSchema original, JsonSchema updated) { traversalContext.skip(); }

    @Override
    public void visitFullSchemaDependentSchema(JsonSchema original, JsonSchema updated) {
        if (original != null && updated != null
                && original.isFullSchema() && updated.isFullSchema()) {
            DiffContext subCtx = ctx.sub("dependentSchemas");
            if (!isSchemaCompatible(subCtx, original.asFullSchema(),
                    updated.asFullSchema(), true)) {
                subCtx.addDifference(OBJECT_TYPE_SCHEMA_DEPENDENCIES_CHANGED,
                        original, updated);
            }
        }
        traversalContext.skip(); return;
    }

    @Override
    public void visitFullSchemaAllOfItem(JsonSchema original, JsonSchema updated) { traversalContext.skip(); }

    @Override
    public void visitFullSchemaAnyOfItem(JsonSchema original, JsonSchema updated) { traversalContext.skip(); }

    @Override
    public void visitFullSchemaOneOfItem(JsonSchema original, JsonSchema updated) { traversalContext.skip(); }

    // Auto-recursion would reuse this visitor for the nested pair, overwriting currentOriginal and
    // currentUpdated for every cross-field decision made after it. Positions are compared in
    // diffFullSchemaPrefixItems, and definitions in diffDefinitions.

    @Override
    public void visitFullSchemaPrefixItemsItem(JsonSchema original, JsonSchema updated) { traversalContext.skip(); }

    @Override
    public void visitFullSchemaDefinition(JsonSchema original, JsonSchema updated) { traversalContext.skip(); }

    @Override
    public void visitFullSchema$def(JsonSchema original, JsonSchema updated) { traversalContext.skip(); }

    @Override
    public void diffFullSchemaDefinitions(Map<String, JsonSchema> original, Map<String, JsonSchema> updated,
                                          CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        diffDefinitions(ctx, "definitions", original, updated);
    }

    @Override
    public void diffFullSchema$defs(Map<String, JsonSchema> original, Map<String, JsonSchema> updated,
                                    CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        diffDefinitions(ctx, "$defs", original, updated);
    }

    /**
     * Compares definitions present on both sides. A definition affects validation only through a
     * {@code $ref}. With a dereferencer the reference is inlined, so the change is also seen where
     * it is used. Without one, references are compared as strings, and this is the only place a
     * changed target is noticed. Each pair gets its own visitor, so nothing it sets can leak into
     * the enclosing schema's comparison.
     */
    private static void diffDefinitions(DiffContext ctx, String keyword, Map<String, JsonSchema> original,
                                        Map<String, JsonSchema> updated) {
        if (original == null || updated == null) {
            return;
        }
        HashSet<String> keys = new HashSet<String>(original.keySet());
        for (String key : keys) {
            JsonSchema origSchema = original.get(key);
            JsonSchema updSchema = updated.get(key);
            if (updSchema != null && origSchema.isFullSchema() && updSchema.isFullSchema()) {
                diffSchemas(ctx.sub(keyword + "/" + key), origSchema.asFullSchema(), updSchema.asFullSchema());
            }
        }
    }

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
    public void diffFullSchemaMinimum(JCRangeValue original, JCRangeValue updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        if (original == null) {
            ctx.addDifference(NUMBER_TYPE_MINIMUM_ADDED, null, rangeToString(updated));
            traversalContext.skip(); return;
        }
        if (updated == null) {
            ctx.addDifference(NUMBER_TYPE_MINIMUM_REMOVED, rangeToString(original), null);
            traversalContext.skip(); return;
        }
        // Both present -- compare values
        Number origVal = original.getValue();
        Number updVal = updated.getValue();
        if (origVal != null && updVal != null) {
            int cmp = NumberUtil.compare(origVal, updVal);
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
        traversalContext.skip(); return; // don't auto-recurse into RangeValue fields
    }

    @Override
    public void diffFullSchemaMaximum(JCRangeValue original, JCRangeValue updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        if (original == null) {
            ctx.addDifference(NUMBER_TYPE_MAXIMUM_ADDED, null, rangeToString(updated));
            traversalContext.skip(); return;
        }
        if (updated == null) {
            ctx.addDifference(NUMBER_TYPE_MAXIMUM_REMOVED, rangeToString(original), null);
            traversalContext.skip(); return;
        }
        // Both present -- compare values
        Number origVal = original.getValue();
        Number updVal = updated.getValue();
        if (origVal != null && updVal != null) {
            int cmp = NumberUtil.compare(origVal, updVal);
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
        traversalContext.skip(); return; // don't auto-recurse into RangeValue fields
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
                ARRAY_TYPE_UNIQUE_ITEMS_TRUE_TO_FALSE);
    }

    @Override
    public void diffFullSchemaItems(BooleanFullSchemaFullSchemaListUnion original,
                                    BooleanFullSchemaFullSchemaListUnion updated) {
        // After normalization, items is always a single schema or boolean (tuples → prefixItems)
        if (original == null && updated == null) { traversalContext.skip(); return; }

        if (original != null && updated != null) {
            if (original.isFullSchema() && updated.isFullSchema()) {
                DiffContext subCtx = ctx.sub("items");
                if (!isSchemaCompatible(subCtx, original.asFullSchema(),
                        updated.asFullSchema(), true)) {
                    subCtx.addDifference(ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, original, updated);
                }
            }
        } else {
            diffAddedRemoved(ctx, original, updated,
                    ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, ARRAY_TYPE_ALL_ITEM_SCHEMA_REMOVED);
        }
        traversalContext.skip(); return;
    }

    @Override
    public void diffFullSchemaPrefixItems(List<JsonSchema> original, List<JsonSchema> updated,
                                           CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        if (original == null && updated == null) return;

        List<JsonSchema> origList = original != null ? original : List.<JsonSchema>of();
        List<JsonSchema> updList = updated != null ? updated : List.<JsonSchema>of();
        int minSize = Math.min(origList.size(), updList.size());

        for (int i = 0; i < minSize; i++) {
            JsonSchema origSchema = origList.get(i);
            JsonSchema updSchema = updList.get(i);
            if (origSchema.isFullSchema() && updSchema.isFullSchema()) {
                DiffContext subCtx = ctx.sub("prefixItems/" + i);
                if (!isSchemaCompatible(subCtx, origSchema.asFullSchema(),
                        updSchema.asFullSchema(), true)) {
                    subCtx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, origSchema, updSchema);
                }
            }
        }

        if (updList.size() > origList.size()) {
            JsonSchema origAI = currentOriginal instanceof JCFullSchema ? ((JCFullSchema) currentOriginal).getAdditionalItems() : null;
            if (origAI != null && origAI.isBoolean() && !origAI.asBoolean()) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED, origList.size(), updList.size());
            } else if (origAI != null && origAI.isFullSchema()) {
                boolean allCompatible = true;
                for (int i = minSize; i < updList.size(); i++) {
                    if (updList.get(i).isFullSchema()) {
                        DiffContext subCtx = ctx.sub("prefixItems/" + i);
                        if (!isSchemaCompatible(subCtx, origAI.asFullSchema(),
                                updList.get(i).asFullSchema(), true)) {
                            allCompatible = false;
                            break;
                        }
                    }
                }
                if (allCompatible) {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES,
                            origList.size(), updList.size());
                } else {
                    ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED, origList.size(), updList.size());
                }
            } else {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED, origList.size(), updList.size());
            }
        } else if (updList.size() < origList.size()) {
            JsonSchema updAI = currentUpdated instanceof JCFullSchema ? ((JCFullSchema) currentUpdated).getAdditionalItems() : null;
            boolean updPermitsAdditional = updAI == null || (updAI.isBoolean() ? updAI.asBoolean() : true);
            if (!updPermitsAdditional) {
                ctx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_NARROWED, origList.size(), updList.size());
            } else if (updAI != null && updAI.isFullSchema()) {
                boolean allCompatible = true;
                for (int i = minSize; i < origList.size(); i++) {
                    if (origList.get(i).isFullSchema()) {
                        DiffContext subCtx = ctx.sub("prefixItems/" + i);
                        if (!isSchemaCompatible(subCtx, origList.get(i).asFullSchema(),
                                updAI.asFullSchema(), true)) {
                            allCompatible = false;
                            break;
                        }
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

    @Override
    public void diffFullSchemaAdditionalItems(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }

        boolean origPermits = original == null || (original.isBoolean() ? original.asBoolean() : true);
        boolean updPermits = updated == null || (updated.isBoolean() ? updated.asBoolean() : true);
        boolean origIsBoolean = original != null && original.isBoolean();
        boolean updIsBoolean = updated != null && updated.isBoolean();
        boolean origIsSchema = original != null && original.isFullSchema();
        boolean updIsSchema = updated != null && updated.isFullSchema();

        if ((origIsBoolean || original == null) && (updIsBoolean || updated == null)) {
            diffBooleanTransition(ctx, origPermits, updPermits, true,
                    ARRAY_TYPE_ADDITIONAL_ITEMS_FALSE_TO_TRUE,
                    ARRAY_TYPE_ADDITIONAL_ITEMS_TRUE_TO_FALSE);
        } else if (origIsSchema && updIsSchema) {
            if (!isUnionSchemaCompatible(ctx, original, updated, true)) {
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
        traversalContext.skip(); return;
    }

    @Override
    public void diffFullSchemaContains(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        if (original == null) {
            ctx.addDifference(ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_ADDED, null, updated);
            traversalContext.skip(); return;
        }
        if (updated == null) {
            ctx.addDifference(ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_REMOVED, original, null);
            traversalContext.skip(); return;
        }
        DiffContext subCtx = ctx.sub("contains");
        if (!isUnionSchemaCompatible(subCtx, original, updated, true)) {
            subCtx.addDifference(ARRAY_TYPE_ITEM_SCHEMAS_CHANGED, original, updated);
        }
        traversalContext.skip(); return;
    }

    @Override
    public void diffFullSchemaUnevaluatedItems(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        if (original != null && updated != null
                && original.isFullSchema() && updated.isFullSchema()) {
            if (!isUnionSchemaCompatible(ctx, original, updated, true)) {
                ctx.addDifference(ARRAY_TYPE_SCHEMA_OF_ADDITIONAL_ITEMS_CHANGED, original, updated);
            }
        } else {
            diffAddedRemoved(ctx, original, updated,
                    ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED, ARRAY_TYPE_ALL_ITEM_SCHEMA_REMOVED);
        }
        traversalContext.skip(); return;
    }

    @Override
    public void diffFullSchemaUnevaluatedProperties(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        if (original != null && updated != null
                && original.isFullSchema() && updated.isFullSchema()) {
            if (!isUnionSchemaCompatible(ctx, original, updated, true)) {
                ctx.addDifference(OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_CHANGED, original, updated);
            }
        } else {
            diffAddedRemoved(ctx, original, updated,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_ADDED,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_REMOVED);
        }
        traversalContext.skip(); return;
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

        HashSet<String> origSet = original != null ? new HashSet<>(original) : new HashSet<String>();
        HashSet<String> updSet = updated != null ? new HashSet<>(updated) : new HashSet<String>();

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

        HashSet<String> origKeys = original != null ? new HashSet<>(original.keySet()) : new HashSet<String>();
        HashSet<String> updKeys = updated != null ? new HashSet<>(updated.keySet()) : new HashSet<String>();

        // Properties present in both
        HashSet<String> commonKeys = new HashSet<>(origKeys);
        commonKeys.retainAll(updKeys);
        for (String key : commonKeys) {
            DiffContext subCtx = ctx.sub(key);
            JsonSchema origSchema = original.get(key);
            JsonSchema updSchema = updated.get(key);
            if (!isUnionSchemaCompatible(subCtx, origSchema, updSchema, true)) {
                subCtx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED, origSchema, updSchema);
            }
        }

        JsonSchema origAdditional = currentOriginal != null
                ? currentOriginal.getAdditionalProperties() : null;
        JsonSchema updAdditional = currentUpdated != null
                ? currentUpdated.getAdditionalProperties() : null;
        boolean origPermitsAdditional = permitsAdditional(origAdditional);
        boolean updPermitsAdditional = permitsAdditional(updAdditional);

        // Properties added in updated
        HashSet<String> addedKeys = new HashSet<>(updKeys);
        addedKeys.removeAll(origKeys);
        if (!addedKeys.isEmpty()) {
            if (!origPermitsAdditional) {
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_EXTENDED, null, addedKeys);
            } else if (origAdditional != null && origAdditional.isFullSchema()
                    && updated != null) {
                boolean allCompatible = true;
                for (String key : addedKeys) {
                    JsonSchema addedSchema = updated.get(key);
                    DiffContext subCtx = ctx.sub(key);
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
        HashSet<String> removedKeys = new HashSet<>(origKeys);
        removedKeys.removeAll(updKeys);
        if (!removedKeys.isEmpty()) {
            if (!updPermitsAdditional) {
                ctx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED, removedKeys, null);
            } else if (updAdditional != null && updAdditional.isFullSchema()
                    && original != null) {
                boolean allCompatible = true;
                for (String key : removedKeys) {
                    JsonSchema removedSchema = original.get(key);
                    DiffContext subCtx = ctx.sub(key);
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
    public void diffFullSchemaAdditionalProperties(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }

        boolean origPermits = permitsAdditional(original);
        boolean updPermits = permitsAdditional(updated);

        boolean origIsBoolean = original != null && original.isBoolean();
        boolean updIsBoolean = updated != null && updated.isBoolean();
        boolean origIsSchema = original != null && original.isFullSchema();
        boolean updIsSchema = updated != null && updated.isFullSchema();

        if ((origIsBoolean || original == null) && (updIsBoolean || updated == null)) {
            diffBooleanTransition(ctx, origPermits, updPermits, true,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_FALSE_TO_TRUE,
                    OBJECT_TYPE_ADDITIONAL_PROPERTIES_TRUE_TO_FALSE);
        } else if (origIsSchema && updIsSchema) {
            if (!isUnionSchemaCompatible(ctx, original, updated, true)) {
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
        traversalContext.skip(); return;
    }

    @Override
    public void diffFullSchemaPatternProperties(Map<String, JsonSchema> original,
                                                Map<String, JsonSchema> updated,
                                                CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        // Suppress auto-recursion for matched pattern property schemas
        if (original == null && updated == null) return;

        HashSet<String> origKeys = original != null ? new HashSet<>(original.keySet()) : new HashSet<String>();
        HashSet<String> updKeys = updated != null ? new HashSet<>(updated.keySet()) : new HashSet<String>();

        diffSetChanged(ctx, origKeys, updKeys,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_ADDED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_REMOVED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_CHANGED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_ADDED,
                OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_REMOVED);

        if (original != null && updated != null) {
            HashSet<String> commonKeys = new HashSet<>(origKeys);
            commonKeys.retainAll(updKeys);
            for (String key : commonKeys) {
                DiffContext subCtx = ctx.sub("patternProperties/" + key);
                JsonSchema origSchema = original.get(key);
                JsonSchema updSchema = updated.get(key);
                if (!isUnionSchemaCompatible(subCtx, origSchema, updSchema, true)) {
                    subCtx.addDifference(OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED,
                            origSchema, updSchema);
                }
            }
        }
    }

    @Override
    public void diffFullSchemaPropertyNames(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        compareSchema(ctx, original, updated,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_ADDED,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_REMOVED,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BOTH,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_NONE);
        traversalContext.skip(); return;
    }

    // dependencies is always empty after conversion — d4-d7 entries are split
    // into dependentSchemas/dependentRequired by the converters.

    @Override
    public void diffFullSchemaDependentSchemas(Map<String, JsonSchema> original,
                                                Map<String, JsonSchema> updated,
                                                CollectionDiff<DefaultPairingKey, JsonSchema> diff) {
        if (original == null && updated == null) return;

        HashSet<String> origKeys = original != null
                ? new HashSet<>(original.keySet()) : new HashSet<String>();
        HashSet<String> updKeys = updated != null
                ? new HashSet<>(updated.keySet()) : new HashSet<String>();

        diffSetChanged(ctx, origKeys, updKeys,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_ADDED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_REMOVED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_CHANGED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_ADDED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_REMOVED);
    }

    @Override
    public void diffFullSchemaDependentRequired(Map<String, JsonNode> original,
                                                 Map<String, JsonNode> updated) {
        if (original == null && updated == null) return;

        HashSet<String> origKeys = original != null
                ? new HashSet<>(original.keySet()) : new HashSet<String>();
        HashSet<String> updKeys = updated != null
                ? new HashSet<>(updated.keySet()) : new HashSet<String>();

        diffSetChanged(ctx, origKeys, updKeys,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_ADDED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_REMOVED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_CHANGED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_ADDED,
                OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_REMOVED);

        if (original != null && updated != null) {
            HashSet<String> commonKeys = new HashSet<>(origKeys);
            commonKeys.retainAll(updKeys);
            for (String key : commonKeys) {
                JsonNode origArray = original.get(key);
                JsonNode updArray = updated.get(key);
                Set<String> origSet = jsonArrayToStringSet(origArray);
                Set<String> updSet = jsonArrayToStringSet(updArray);
                for (String v : origSet) {
                    if (!updSet.contains(v)) {
                        ctx.addDifference(
                                OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_REMOVED,
                                v, null);
                    }
                }
                for (String v : updSet) {
                    if (!origSet.contains(v)) {
                        ctx.addDifference(
                                OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_ADDED,
                                null, v);
                    }
                }
                if (!origSet.equals(updSet)) {
                    ctx.addDifference(
                            OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_CHANGED,
                            origArray, updArray);
                }
            }
        }
    }

    private static Set<String> jsonArrayToStringSet(JsonNode arrayNode) {
        HashSet<String> set = new HashSet<String>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode element : arrayNode) {
                if (element.isTextual()) {
                    set.add(element.asText());
                }
            }
        }
        return set;
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
            List<JsonSchema> updAnyOf = currentUpdated.getAnyOf();

            // allOf -> anyOf transition
            if (original != null && updAnyOf != null && updated == null) {
                diffCompositionList(ctx, original, updAnyOf,
                        COMBINED_TYPE_CRITERION_EXTENDED, COMBINED_TYPE_CRITERION_EXTENDED);
                ctx.addDifference(COMBINED_TYPE_CRITERION_EXTENDED, "allOf", "anyOf");
                return;
            }
            // anyOf -> allOf transition
            List<JsonSchema> origAnyOf = currentOriginal.getAnyOf();
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
            List<JsonSchema> origAllOf = currentOriginal.getAllOf();
            List<JsonSchema> origOneOf = currentOriginal.getOneOf();
            List<JsonSchema> updAllOf = currentUpdated.getAllOf();
            List<JsonSchema> updOneOf = currentUpdated.getOneOf();

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
            List<JsonSchema> origAnyOf = currentOriginal.getAnyOf();
            List<JsonSchema> updAnyOf = currentUpdated.getAnyOf();

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

        int unmatchedCount = 0;
        for (JsonSchema updSub : updatedList) {
            boolean matched = false;
            for (JsonSchema origSub : originalList) {
                DiffContext subCtx = ctx.sub("composition");
                if (isUnionSchemaCompatible(subCtx, origSub, updSub, true)
                        && isUnionSchemaCompatible(subCtx, origSub, updSub, false)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                for (JsonSchema origSub : originalList) {
                    DiffContext subCtx = ctx.sub("composition");
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
        int newSubschemas = Math.max(0, updatedList.size() - originalList.size());
        int changedSubschemas = unmatchedCount - newSubschemas;
        if (changedSubschemas > 0) {
            ctx.addDifference(COMBINED_TYPE_SUBSCHEMA_NOT_COMPATIBLE,
                    originalList, updatedList);
        }
    }

    // -----------------------------------------------------------------------
    // Not schema
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaNot(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        compareSchema(ctx, original, updated,
                SUBSCHEMA_TYPE_CHANGED, SUBSCHEMA_TYPE_CHANGED,
                NOT_TYPE_SCHEMA_COMPATIBLE_BOTH,
                NOT_TYPE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                NOT_TYPE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                NOT_TYPE_SCHEMA_COMPATIBLE_NONE);
        traversalContext.skip(); return;
    }

    // -----------------------------------------------------------------------
    // Conditional keywords (if/then/else)
    // TODO: Add post-recursion callback to DiffTraverser (generator feature) --
    //       would allow checking nested comparison results without isolated contexts
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaIf(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        compareSchema(ctx, original, updated,
                CONDITIONAL_TYPE_IF_SCHEMA_ADDED, CONDITIONAL_TYPE_IF_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_NONE);
        traversalContext.skip(); return;
    }

    @Override
    public void diffFullSchemaThen(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        compareSchema(ctx, original, updated,
                CONDITIONAL_TYPE_THEN_SCHEMA_ADDED, CONDITIONAL_TYPE_THEN_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_NONE);
        traversalContext.skip(); return;
    }

    @Override
    public void diffFullSchemaElse(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        compareSchema(ctx, original, updated,
                CONDITIONAL_TYPE_ELSE_SCHEMA_ADDED, CONDITIONAL_TYPE_ELSE_SCHEMA_REMOVED,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BOTH,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD,
                CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_NONE);
        traversalContext.skip(); return;
    }

    // -----------------------------------------------------------------------
    // Enum and const
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaEnum(List<JsonNode> original, List<JsonNode> updated) {
        JsonNode origConst = currentOriginal instanceof JCFullSchema ? ((JCFullSchema) currentOriginal).getConst() : null;
        JsonNode updConst = currentUpdated instanceof JCFullSchema ? ((JCFullSchema) currentUpdated).getConst() : null;

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
                HashSet<String> origSet = new HashSet<String>();
                for (JsonNode v : original) {
                    origSet.add(v.toString());
                }
                HashSet<String> updSet = new HashSet<String>();
                for (JsonNode v : updated) {
                    updSet.add(v.toString());
                }
                if (!origSet.equals(updSet)) {
                    ctx.addDifference(ENUM_TYPE_VALUES_CHANGED, original, updated);
                    for (String v : updSet) {
                        if (!origSet.contains(v)) {
                            ctx.addDifference(ENUM_TYPE_VALUES_MEMBER_ADDED, null, v);
                        }
                    }
                    for (String v : origSet) {
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
            List<JsonNode> origEnum = currentOriginal != null ? currentOriginal.getEnum() : null;
            if (origEnum != null && origEnum.size() == 1
                    && origEnum.get(0).toString().equals(updated.toString())) {
                return;
            }
            ctx.addDifference(CONST_TYPE_VALUE_ADDED, null, updated);
        } else {
            List<JsonNode> updEnum = currentUpdated != null ? currentUpdated.getEnum() : null;
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
    public void diffFullSchemaType(
            io.apitomy.datamodels.models.union.StringStringListUnion original,
            io.apitomy.datamodels.models.union.StringStringListUnion updated) {
        // Type change detection is handled in visitFullSchema.
        // Here we handle integer->number transition for the integer-required logic.
        if (currentOriginal != null && currentUpdated != null) {
            String origType = DiffUtil.getTypeString(currentOriginal);
            String updType = DiffUtil.getTypeString(currentUpdated);
            String effectiveType = origType != null ? origType : updType;
            if (effectiveType != null
                    && ("integer".equals(effectiveType) || "number".equals(effectiveType))) {
                boolean origIsInteger = "integer".equals(origType);
                boolean updIsInteger = "integer".equals(updType);
                diffBooleanTransition(ctx, origIsInteger, updIsInteger, false,
                        NUMBER_TYPE_INTEGER_REQUIRED_FALSE_TO_TRUE,
                        NUMBER_TYPE_INTEGER_REQUIRED_TRUE_TO_FALSE);
            }
        }
        return;
    }

    // -----------------------------------------------------------------------
    // $ref — reference comparison
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchema$ref(String original, String updated) {
        // After upfront dereferencing, $ref is only present for cyclic back-edges
        // or unresolved refs. Only report a difference when both sides have
        // different $ref values — a one-sided $ref (cyclic or unresolved) is not
        // a compatibility issue by itself.
        if (original != null && updated != null && !original.equals(updated)) {
            ctx.addDifference(REFERENCE_TYPE_TARGET_SCHEMA_CHANGED, original, updated);
        }
    }

    // -----------------------------------------------------------------------
    // Content schema
    // -----------------------------------------------------------------------

    @Override
    public void diffFullSchemaContentSchema(JsonSchema original, JsonSchema updated) {
        if (original == null && updated == null) { traversalContext.skip(); return; }
        if (original != null && updated != null) {
            DiffContext subCtx = ctx.sub("contentSchema");
            if (!isUnionSchemaCompatible(subCtx, original, updated, true)) {
                subCtx.addDifference(SUBSCHEMA_TYPE_CHANGED, original, updated);
            }
        }
        traversalContext.skip(); return;
    }

    // -----------------------------------------------------------------------
    // No-op fields (metadata, no compatibility implications)
    // diffFullSchemaTitle, diffFullSchemaDescription, diffFullSchemaDefault,
    // diffFullSchemaExamples, diffFullSchema$schema, diffFullSchema$comment,
    // diffFullSchemaDeprecated, diffFullSchemaReadOnly, diffFullSchemaWriteOnly
    // are all inherited as no-ops from JCDiffVisitor
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Local helper methods
    // -----------------------------------------------------------------------

    /**
     * Compare two sub-schemas for compatibility. Pushes an isolated scope so the nested
     * comparison doesn't leak into the surrounding diff, while sharing the visited set to
     * prevent infinite recursion on cyclic schemas.
     *
     * <p>Delegates to {@link #diffSchemas}, which converts either operand to compound
     * ({@link JCFullSchema}) on demand — so raw draft sub-schemas that the top-level
     * conversion didn't reach (e.g. tuple {@code items} elements) are handled transparently.
     */
    private static boolean isSchemaCompatible(DiffContext ctx, JFullSchema original, JFullSchema updated,
                                              boolean backward) {
        ctx.pushIsolatedScope();
        if (backward) {
            diffSchemas(ctx, original, updated);
        } else {
            diffSchemas(ctx, updated, original);
        }
        return ctx.popScopeIsCompatible();
    }

    private static boolean isUnionSchemaCompatible(DiffContext ctx, JsonSchema original,
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

    private static void compareSchema(DiffContext ctx, JsonSchema original,
                                      JsonSchema updated,
                                      DiffType addedType, DiffType removedType,
                                      DiffType bothType, DiffType backwardNotForwardType,
                                      DiffType forwardNotBackwardType, DiffType noneType) {
        if (diffAddedRemoved(ctx, original, updated, addedType, removedType)) {
            compareSchemaWhenExist(ctx, original, updated, bothType,
                    backwardNotForwardType, forwardNotBackwardType, noneType);
        }
    }

    private static void compareSchemaWhenExist(DiffContext ctx, JsonSchema original,
                                               JsonSchema updated,
                                               DiffType bothType, DiffType backwardType,
                                               DiffType forwardType, DiffType noneType) {
        boolean backward = isUnionSchemaCompatible(ctx, original, updated, true);
        boolean forward = isUnionSchemaCompatible(ctx, original, updated, false);

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

    private static String rangeToString(JCRangeValue range) {
        if (range == null) return null;
        String prefix = Boolean.TRUE.equals(range.isExclusive()) ? "exclusive " : "";
        return prefix + range.getValue();
    }


    private static boolean permitsAdditional(JsonSchema additionalProperties) {
        if (additionalProperties == null) return true;
        if (additionalProperties.isBoolean()) return additionalProperties.asBoolean();
        return true;
    }
}
