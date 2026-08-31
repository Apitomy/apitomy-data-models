package io.apitomy.datamodels.jsonschema.compat;

import java.util.List;
import java.util.Optional;

/**
 * Classifies a single difference found between two schemas.
 * <p>
 * Each constant carries:
 * <ul>
 *   <li>{@link #isBackwardsCompatible()} — whether the change is acceptable in the direction being
 *       compared (the diff runs original→updated for a backward check, and updated→original for a
 *       forward check, so the flag reads as "is this delta compatible in the checked direction");</li>
 *   <li>{@link #getShortDescription()} — a curated, human-readable one-line description of the change,
 *       shown per difference.</li>
 * </ul>
 * Longer "why is this (in)compatible" help text and worked examples are resolved separately by ID
 * (the enum constant name is the stable identifier).
 *
 * <h2>Short-description wording conventions</h2>
 * <ul>
 *   <li><b>Describe the change, not the verdict.</b> Each line states <i>what</i> changed; the
 *       compatibility reasoning (<i>why</i> it is or isn't compatible) is left to the help text.
 *       Where a phrase like "relaxing"/"narrowed" is used it merely names the direction of the
 *       change and always agrees with {@link #isBackwardsCompatible()}.</li>
 *   <li><b>Version-general vocabulary.</b> The checker converts every supported draft/version into a
 *       single normalized ("compound") schema model before diffing, so a difference is always
 *       described in terms of that canonical model — never a specific draft. Concepts whose keyword
 *       spelling varies across versions are described neutrally (e.g. "positional (tuple) item
 *       schemas" rather than {@code items}/{@code prefixItems}; "property dependency" rather than
 *       {@code dependencies}/{@code dependentRequired}). Keywords that are spelled identically in
 *       every version (e.g. {@code required}, {@code format}, {@code maxItems}, {@code $ref}) may be
 *       quoted directly, since they are already version-general.</li>
 *   <li><b>Comparison framing.</b> The wording reads in the direction the comparison runs
 *       (source→target); for the common backward check that is old→new. Disambiguating the framing
 *       for forward/full checks is deferred (see epic item C45).</li>
 * </ul>
 *
 * <h2>"Unchanged" outcomes are not emitted</h2>
 * <p>
 * When a comparison finds a keyword or subschema unchanged, it records no {@link Difference}. An
 * unchanged value is compatible in both directions and carries no information for a consumer, so
 * surfacing it would add nothing but noise to a check result. The diff logic therefore skips the
 * "no change" case rather than reporting it: there is no {@code DiffType} for an unchanged outcome.
 */
public enum DiffType {

    ARRAY_TYPE_ADDITIONAL_ITEMS_EXTENDED(true, "The schema for additional array items was widened to accept more items."),
    ARRAY_TYPE_ADDITIONAL_ITEMS_FALSE_TO_TRUE(true, "Additional array items changed from disallowed to allowed."),
    ARRAY_TYPE_ADDITIONAL_ITEMS_NARROWED(false, "The schema for additional array items was narrowed to accept fewer items."),
    ARRAY_TYPE_ADDITIONAL_ITEMS_TRUE_TO_FALSE(false, "Additional array items changed from allowed to disallowed."),
    ARRAY_TYPE_ALL_ITEM_SCHEMA_ADDED(false, "A schema constraining all array items was added."),
    ARRAY_TYPE_ALL_ITEM_SCHEMA_REMOVED(true, "The schema constraining all array items was removed."),
    ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_ADDED(false, "A constraint requiring at least one matching array item was added."),
    ARRAY_TYPE_CONTAINED_ITEM_SCHEMA_REMOVED(true, "The constraint requiring at least one matching array item was removed."),
    ARRAY_TYPE_ITEM_SCHEMAS_CHANGED(false, "A positional (tuple) item schema was changed."),
    ARRAY_TYPE_ITEM_SCHEMAS_EXTENDED(true, "The positional (tuple) item schemas were extended, accepting more arrays."),
    ARRAY_TYPE_ITEM_SCHEMAS_NARROWED(false, "The positional (tuple) item schemas were narrowed, accepting fewer arrays."),
    ARRAY_TYPE_ITEM_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES(true, "The positional (tuple) item schemas were narrowed but remain compatible via the additional-items schema."),
    ARRAY_TYPE_MAX_ITEMS_ADDED(false, "A 'maxItems' upper bound on array length was added."),
    ARRAY_TYPE_MAX_ITEMS_DECREASED(false, "The 'maxItems' array-length limit was decreased."),
    ARRAY_TYPE_MAX_ITEMS_INCREASED(true, "The 'maxItems' array-length limit was increased."),
    ARRAY_TYPE_MAX_ITEMS_REMOVED(true, "The 'maxItems' array-length limit was removed."),
    ARRAY_TYPE_MIN_ITEMS_ADDED(false, "A 'minItems' lower bound on array length was added."),
    ARRAY_TYPE_MIN_ITEMS_DECREASED(true, "The 'minItems' array-length minimum was decreased."),
    ARRAY_TYPE_MIN_ITEMS_INCREASED(false, "The 'minItems' array-length minimum was increased."),
    ARRAY_TYPE_MIN_ITEMS_REMOVED(true, "The 'minItems' array-length minimum was removed."),
    ARRAY_TYPE_SCHEMA_OF_ADDITIONAL_ITEMS_CHANGED(false, "The schema constraining additional array items was changed."),
    ARRAY_TYPE_UNIQUE_ITEMS_FALSE_TO_TRUE(false, "'uniqueItems' changed from false to true, now requiring distinct array elements."),
    ARRAY_TYPE_UNIQUE_ITEMS_TRUE_TO_FALSE(true, "'uniqueItems' changed from true to false, no longer requiring distinct elements."),

    COMBINED_TYPE_ALL_OF_SIZE_DECREASED(true, "The number of 'allOf' subschemas decreased, relaxing the constraints."),
    COMBINED_TYPE_ALL_OF_SIZE_INCREASED(false, "The number of 'allOf' subschemas increased, adding constraints."),
    COMBINED_TYPE_ANY_OF_SIZE_DECREASED(false, "The number of 'anyOf' subschemas decreased, allowing fewer alternatives."),
    COMBINED_TYPE_ANY_OF_SIZE_INCREASED(true, "The number of 'anyOf' subschemas increased, allowing more alternatives."),
    COMBINED_TYPE_CRITERION_CHANGED(false, "The composition keyword (allOf/anyOf/oneOf) was changed."),
    COMBINED_TYPE_CRITERION_EXTENDED(true, "The composition criterion was relaxed to a more permissive keyword."),
    COMBINED_TYPE_CRITERION_NARROWED(false, "The composition criterion was tightened to a more restrictive keyword."),
    COMBINED_TYPE_ONE_OF_SIZE_DECREASED(false, "The number of 'oneOf' subschemas decreased."),
    COMBINED_TYPE_ONE_OF_SIZE_INCREASED(true, "The number of 'oneOf' subschemas increased."),
    COMBINED_TYPE_SUBSCHEMA_NOT_COMPATIBLE(false, "A composition subschema (allOf/anyOf/oneOf) is not compatible."),

    CONDITIONAL_TYPE_ELSE_SCHEMA_ADDED(false, "An 'else' subschema was added."),
    CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD(true, "The 'else' subschema is compatible backward but not forward."),
    CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_BOTH(true, "The 'else' subschema is compatible in both directions."),
    CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD(false, "The 'else' subschema is compatible forward but not backward."),
    CONDITIONAL_TYPE_ELSE_SCHEMA_COMPATIBLE_NONE(false, "The 'else' subschema is compatible in neither direction."),
    CONDITIONAL_TYPE_ELSE_SCHEMA_REMOVED(true, "The 'else' subschema was removed."),
    CONDITIONAL_TYPE_IF_SCHEMA_ADDED(false, "An 'if' subschema was added."),
    CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD(false, "The 'if' subschema is compatible backward but not forward."),
    CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_BOTH(true, "The 'if' subschema is compatible in both directions."),
    CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD(false, "The 'if' subschema is compatible forward but not backward."),
    CONDITIONAL_TYPE_IF_SCHEMA_COMPATIBLE_NONE(false, "The 'if' subschema is compatible in neither direction."),
    CONDITIONAL_TYPE_IF_SCHEMA_REMOVED(false, "The 'if' subschema was removed."),
    CONDITIONAL_TYPE_THEN_SCHEMA_ADDED(false, "A 'then' subschema was added."),
    CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD(true, "The 'then' subschema is compatible backward but not forward."),
    CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_BOTH(true, "The 'then' subschema is compatible in both directions."),
    CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD(false, "The 'then' subschema is compatible forward but not backward."),
    CONDITIONAL_TYPE_THEN_SCHEMA_COMPATIBLE_NONE(false, "The 'then' subschema is compatible in neither direction."),
    CONDITIONAL_TYPE_THEN_SCHEMA_REMOVED(true, "The 'then' subschema was removed."),

    CONST_TYPE_VALUE_ADDED(false, "A 'const' fixed-value constraint was added."),
    CONST_TYPE_VALUE_CHANGED(false, "The 'const' fixed value was changed."),
    CONST_TYPE_VALUE_REMOVED(true, "The 'const' fixed-value constraint was removed."),

    ENUM_TYPE_VALUES_ADDED(false, "An 'enum' constraint restricting values to a fixed set was added."),
    ENUM_TYPE_VALUES_CHANGED(true, "The set of 'enum' allowed values was changed."),
    ENUM_TYPE_VALUES_MEMBER_ADDED(true, "A value was added to the 'enum' allowed set."),
    ENUM_TYPE_VALUES_MEMBER_REMOVED(false, "A value was removed from the 'enum' allowed set."),

    NOT_TYPE_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD(false, "The 'not' subschema is compatible backward but not forward."),
    NOT_TYPE_SCHEMA_COMPATIBLE_BOTH(true, "The 'not' subschema is compatible in both directions."),
    NOT_TYPE_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD(true, "The 'not' subschema is compatible forward but not backward."),
    NOT_TYPE_SCHEMA_COMPATIBLE_NONE(false, "The 'not' subschema is compatible in neither direction."),

    NUMBER_TYPE_INTEGER_REQUIRED_FALSE_TO_TRUE(false, "The numeric type was restricted from 'number' to 'integer'."),
    NUMBER_TYPE_INTEGER_REQUIRED_TRUE_TO_FALSE(true, "The numeric type was widened from 'integer' to 'number'."),
    NUMBER_TYPE_MAXIMUM_ADDED(false, "A 'maximum' upper bound was added."),
    NUMBER_TYPE_MAXIMUM_DECREASED(false, "The 'maximum' upper bound was decreased."),
    NUMBER_TYPE_MAXIMUM_INCREASED(true, "The 'maximum' upper bound was increased."),
    NUMBER_TYPE_MAXIMUM_REMOVED(true, "The 'maximum' upper bound was removed."),
    NUMBER_TYPE_MINIMUM_ADDED(false, "A 'minimum' lower bound was added."),
    NUMBER_TYPE_MINIMUM_DECREASED(true, "The 'minimum' lower bound was decreased."),
    NUMBER_TYPE_MINIMUM_INCREASED(false, "The 'minimum' lower bound was increased."),
    NUMBER_TYPE_MINIMUM_REMOVED(true, "The 'minimum' lower bound was removed."),
    NUMBER_TYPE_MULTIPLE_OF_ADDED(false, "A 'multipleOf' divisibility constraint was added."),
    NUMBER_TYPE_MULTIPLE_OF_REMOVED(true, "The 'multipleOf' divisibility constraint was removed."),
    NUMBER_TYPE_MULTIPLE_OF_UPDATED_IS_DIVISIBLE(true, "The new 'multipleOf' evenly divides the old value, relaxing the constraint."),
    NUMBER_TYPE_MULTIPLE_OF_UPDATED_IS_NOT_DIVISIBLE(false, "The new 'multipleOf' does not evenly divide the old value, tightening the constraint."),

    OBJECT_TYPE_ADDITIONAL_PROPERTIES_EXTENDED(true, "The 'additionalProperties' schema was widened to accept more properties."),
    OBJECT_TYPE_ADDITIONAL_PROPERTIES_FALSE_TO_TRUE(true, "'additionalProperties' changed from false to true, now allowing extra properties."),
    OBJECT_TYPE_ADDITIONAL_PROPERTIES_NARROWED(false, "The 'additionalProperties' schema was narrowed to accept fewer properties."),
    OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_ADDED(false, "A schema for 'additionalProperties' was added."),
    OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_CHANGED(false, "The schema for 'additionalProperties' was changed."),
    OBJECT_TYPE_ADDITIONAL_PROPERTIES_SCHEMA_REMOVED(true, "The schema for 'additionalProperties' was removed."),
    OBJECT_TYPE_ADDITIONAL_PROPERTIES_TRUE_TO_FALSE(false, "'additionalProperties' changed from true to false, no longer allowing extra properties."),
    OBJECT_TYPE_MAX_PROPERTIES_ADDED(false, "A 'maxProperties' upper bound on property count was added."),
    OBJECT_TYPE_MAX_PROPERTIES_DECREASED(false, "The 'maxProperties' property-count limit was decreased."),
    OBJECT_TYPE_MAX_PROPERTIES_INCREASED(true, "The 'maxProperties' property-count limit was increased."),
    OBJECT_TYPE_MAX_PROPERTIES_REMOVED(true, "The 'maxProperties' property-count limit was removed."),
    OBJECT_TYPE_MIN_PROPERTIES_ADDED(false, "A 'minProperties' lower bound on property count was added."),
    OBJECT_TYPE_MIN_PROPERTIES_DECREASED(true, "The 'minProperties' property-count minimum was decreased."),
    OBJECT_TYPE_MIN_PROPERTIES_INCREASED(false, "The 'minProperties' property-count minimum was increased."),
    OBJECT_TYPE_MIN_PROPERTIES_REMOVED(true, "The 'minProperties' property-count minimum was removed."),
    OBJECT_TYPE_PATTERN_PROPERTY_KEYS_ADDED(false, "A 'patternProperties' set of pattern keys was added."),
    OBJECT_TYPE_PATTERN_PROPERTY_KEYS_CHANGED(true, "The set of 'patternProperties' pattern keys was changed."),
    OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_ADDED(false, "A pattern key was added to 'patternProperties'."),
    OBJECT_TYPE_PATTERN_PROPERTY_KEYS_MEMBER_REMOVED(true, "A pattern key was removed from 'patternProperties'."),
    OBJECT_TYPE_PATTERN_PROPERTY_KEYS_REMOVED(true, "The 'patternProperties' pattern keys were removed."),
    OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_ADDED(false, "A property dependency (a property whose presence requires other properties) was added."),
    OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_CHANGED(true, "The set of triggering properties for property dependencies was changed."),
    OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_ADDED(false, "A triggering property for a property dependency was added."),
    OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_MEMBER_REMOVED(true, "A triggering property for a property dependency was removed."),
    OBJECT_TYPE_PROPERTY_DEPENDENCIES_KEYS_REMOVED(true, "All property dependencies (a property requiring other properties) were removed."),
    OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_ADDED(false, "A required property was added to a property dependency's list."),
    OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_CHANGED(true, "A property dependency's required-property list was changed."),
    OBJECT_TYPE_PROPERTY_DEPENDENCIES_VALUE_MEMBER_REMOVED(true, "A required property was removed from a property dependency's list."),
    OBJECT_TYPE_PROPERTY_SCHEMAS_CHANGED(false, "A property's schema in 'properties' was changed."),
    OBJECT_TYPE_PROPERTY_SCHEMAS_EXTENDED(true, "The 'properties' constraints were widened to accept more objects."),
    OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED(false, "The 'properties' constraints were narrowed to accept fewer objects."),
    OBJECT_TYPE_PROPERTY_SCHEMAS_NARROWED_COMPATIBLE_WITH_ADDITIONAL_PROPERTIES(true, "The 'properties' constraints were narrowed but remain compatible via 'additionalProperties'."),
    OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_ADDED(false, "A 'propertyNames' schema constraining property names was added."),
    OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_REMOVED(true, "The 'propertyNames' schema was removed."),
    OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BOTH(true, "The 'propertyNames' schema is compatible in both directions."),
    OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_BACKWARD_NOT_FORWARD(true, "The 'propertyNames' schema is compatible backward but not forward."),
    OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_FORWARD_NOT_BACKWARD(false, "The 'propertyNames' schema is compatible forward but not backward."),
    OBJECT_TYPE_PROPERTY_NAMES_SCHEMA_COMPATIBLE_NONE(false, "The 'propertyNames' schema is compatible in neither direction."),
    OBJECT_TYPE_REQUIRED_PROPERTIES_ADDED(false, "A 'required' properties constraint was added."),
    OBJECT_TYPE_REQUIRED_PROPERTIES_CHANGED(true, "The set of 'required' properties was changed."),
    OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_ADDED(false, "A property was made 'required'."),
    OBJECT_TYPE_REQUIRED_PROPERTIES_MEMBER_REMOVED(true, "A property is no longer 'required'."),
    OBJECT_TYPE_REQUIRED_PROPERTIES_REMOVED(true, "The 'required' properties constraint was removed."),
    OBJECT_TYPE_SCHEMA_DEPENDENCIES_CHANGED(true, "The set of schema dependencies was changed."),

    REFERENCE_TYPE_TARGET_SCHEMA_CHANGED(false, "A '$ref' now points to a different or changed target schema."),

    STRING_TYPE_CONTENT_ENCODING_ADDED(false, "A 'contentEncoding' constraint was added."),
    STRING_TYPE_CONTENT_ENCODING_CHANGED(false, "The 'contentEncoding' constraint was changed."),
    STRING_TYPE_CONTENT_ENCODING_REMOVED(true, "The 'contentEncoding' constraint was removed."),
    STRING_TYPE_CONTENT_MEDIA_TYPE_ADDED(false, "A 'contentMediaType' constraint was added."),
    STRING_TYPE_CONTENT_MEDIA_TYPE_CHANGED(false, "The 'contentMediaType' constraint was changed."),
    STRING_TYPE_CONTENT_MEDIA_TYPE_REMOVED(true, "The 'contentMediaType' constraint was removed."),
    STRING_TYPE_FORMAT_ADDED(false, "A 'format' constraint was added."),
    STRING_TYPE_FORMAT_CHANGED(false, "The 'format' constraint was changed."),
    STRING_TYPE_FORMAT_REMOVED(true, "The 'format' constraint was removed."),
    STRING_TYPE_MAX_LENGTH_ADDED(false, "A 'maxLength' upper bound on string length was added."),
    STRING_TYPE_MAX_LENGTH_DECREASED(false, "The 'maxLength' string-length limit was decreased."),
    STRING_TYPE_MAX_LENGTH_INCREASED(true, "The 'maxLength' string-length limit was increased."),
    STRING_TYPE_MAX_LENGTH_REMOVED(true, "The 'maxLength' string-length limit was removed."),
    STRING_TYPE_MIN_LENGTH_ADDED(false, "A 'minLength' lower bound on string length was added."),
    STRING_TYPE_MIN_LENGTH_DECREASED(true, "The 'minLength' string-length minimum was decreased."),
    STRING_TYPE_MIN_LENGTH_INCREASED(false, "The 'minLength' string-length minimum was increased."),
    STRING_TYPE_MIN_LENGTH_REMOVED(true, "The 'minLength' string-length minimum was removed."),
    STRING_TYPE_PATTERN_ADDED(false, "A 'pattern' regular-expression constraint was added."),
    STRING_TYPE_PATTERN_CHANGED(false, "The 'pattern' regular expression was changed."),
    STRING_TYPE_PATTERN_REMOVED(true, "The 'pattern' regular-expression constraint was removed."),

    SUBSCHEMA_TYPE_CHANGED(false, "The declared 'type' of the schema was changed."),
    SUBSCHEMA_TYPE_CHANGED_TO_EMPTY_OR_TRUE(true, "The schema's 'type' was broadened to allow any type (removed or empty).");

    private final boolean backwardsCompatible;
    private final String shortDescription;

    DiffType(boolean backwardsCompatible, String shortDescription) {
        this.backwardsCompatible = backwardsCompatible;
        this.shortDescription = shortDescription;
    }

    public boolean isBackwardsCompatible() {
        return backwardsCompatible;
    }

    /**
     * A curated, human-readable one-line description of this kind of difference.
     * Never {@code null} — every constant carries one.
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * A curated, long-form explanation of this kind of difference, formatted as Markdown.
     * <p>
     * Unlike {@link #getShortDescription()}, help text is optional: only a curated subset of diff
     * types carry one, so this returns {@link Optional#empty()} for the rest. The text is loaded
     * lazily from a bundled manifest on first access and cached for the lifetime of the JVM.
     *
     * @return the help text, or {@link Optional#empty()} if none is curated for this constant
     */
    public Optional<String> getHelp() {
        return DiffTypeHelp.get(name());
    }

    /**
     * Worked examples of this kind of difference, drawn from the bundled example catalog.
     * <p>
     * Each example is a concrete schema pair (one direction of a catalog case) whose check emits
     * this diff type. The list is loaded lazily on first access and cached; it is empty for diff
     * types the catalog does not yet exercise.
     *
     * @return an unmodifiable list of examples, possibly empty
     */
    public List<CompatibilityExample> getExamples() {
        return DiffTypeExamples.get(this);
    }
}
