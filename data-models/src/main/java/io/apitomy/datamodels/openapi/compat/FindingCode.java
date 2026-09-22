package io.apitomy.datamodels.openapi.compat;

/**
 * Stable identifier for the reason behind a {@link CompatibilityFinding}.
 * <p>
 * A code identifies <em>what changed</em>; it does not by itself determine
 * compatibility. The same code can be attached to findings with different
 * {@link FindingImpact} values depending on context (for example, a matched
 * schema usage that was proven compatible still records evidence under a
 * schema-related code with {@link FindingImpact#COMPATIBLE}).
 * <p>
 * New codes are added only alongside the rule that emits them.
 */
public enum FindingCode {

    // --- Analysis coverage and limits ---

    /** A relevant external resource could not be resolved. */
    RESOURCE_UNRESOLVED,

    /** A relevant external resource was acquired but was not a valid document. */
    RESOURCE_INVALID,

    /** A schema resource declares a dialect or vocabulary this checker does not support. */
    DIALECT_UNSUPPORTED,

    /** A relevant construct is recognized but not yet semantically analyzed. */
    FEATURE_UNANALYZED,

    /** A numeric value's original precision could not be established. */
    NUMERIC_PRECISION_UNCERTAIN,

    /** Analysis stopped after exhausting a configured resource, depth, or proof budget. */
    ANALYSIS_LIMIT_REACHED,

    /** Two interactions or usages could not be paired unambiguously. */
    MATCH_AMBIGUOUS,

    // --- Routing and input capabilities ---

    /** A documented operation is no longer present. */
    OPERATION_REMOVED,

    /** A new operation was added. */
    OPERATION_ADDED,

    /** An effective server address is no longer present. */
    ADDRESS_REMOVED,

    /** A new route may shadow requests previously handled by another operation. */
    ROUTE_SHADOWING,

    /** A documented parameter is no longer present. */
    PARAMETER_REMOVED,

    /** A parameter became required without an equivalent old guarantee. */
    PARAMETER_REQUIRED_ADDED,

    /** A request body became required without an equivalent old guarantee. */
    REQUEST_BODY_REQUIRED_ADDED,

    /** A documented request body is no longer present. */
    REQUEST_BODY_REMOVED,

    // --- Representation, serialization, and response availability ---

    /** A change to serialization (style, explode, encoding, and similar) was detected. */
    SERIALIZATION_CHANGED,

    /** A request media type is no longer accepted. */
    REQUEST_MEDIA_TYPE_REMOVED,

    /** A response media type is no longer available. */
    RESPONSE_MEDIA_TYPE_REMOVED,

    /** A response media type was added. */
    RESPONSE_MEDIA_TYPE_ADDED,

    /** A response outcome is newly documented with no covering old definition. */
    RESPONSE_STATUS_ADDED,

    /** A documented response capability was withdrawn. */
    RESPONSE_CAPABILITY_REMOVED,

    /** A documented response header guarantee was removed. */
    RESPONSE_HEADER_GUARANTEE_REMOVED,

    // --- Schema containment ---

    /** An input schema no longer accepts a value it previously accepted. */
    SCHEMA_INPUT_NARROWED,

    /** An output schema may now produce a value old consumers cannot handle. */
    SCHEMA_OUTPUT_WIDENED,

    /** A schema usage was checked and found compatible. */
    SCHEMA_COMPATIBLE,

    /** A schema usage could not be resolved (for example, due to a missing reference). */
    SCHEMA_UNRESOLVED,

    /** Read/write annotation enforcement could affect the result but is not established. */
    READ_WRITE_ENFORCEMENT_UNCERTAIN,

    /** A changed {@code format} value's relationship to the original could not be established. */
    FORMAT_RELATION_UNKNOWN,

    // --- Security, reverse interactions, and relationships ---

    /** An effective security requirement became stricter. */
    SECURITY_REQUIREMENT_STRENGTHENED,

    /** A security scheme's mechanism changed in a way that could affect existing credentials. */
    SECURITY_MECHANISM_CHANGED,

    /** A documented credential-acquisition or refresh capability was removed. */
    CREDENTIAL_FLOW_REMOVED,

    /** Whether two security mechanisms accept the same credentials could not be established. */
    SECURITY_EQUIVALENCE_UNCERTAIN,

    /** A new callback or webhook interaction was added. */
    REVERSE_INTERACTION_ADDED,

    /** A documented callback or webhook interaction was removed. */
    REVERSE_INTERACTION_REMOVED,

    /** A callback's runtime destination expression changed. */
    CALLBACK_DESTINATION_CHANGED,

    /** A discriminator mapping changed in a way that could affect dispatch. */
    DISCRIMINATOR_MAPPING_CHANGED,

    /** A Link's target operation could not be resolved. */
    LINK_TARGET_UNRESOLVED,

    /** A behavioral default changed in a way this checker cannot evaluate. */
    BEHAVIORAL_DEFAULT_CHANGED,

    /** An unrecognized specification extension changed in a reachable location. */
    EXTENSION_SEMANTICS_UNKNOWN,

    /** Documentation-only metadata changed (informational only). */
    METADATA_CHANGED
}
