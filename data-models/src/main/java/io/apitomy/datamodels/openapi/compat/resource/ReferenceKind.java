package io.apitomy.datamodels.openapi.compat.resource;

/**
 * Distinguishes the different mechanisms by which one part of a document can
 * refer to another, so that resolution and later analysis can apply the
 * correct family-specific rules.
 * <p>
 * Only {@link #REFERENCE_OBJECT}, {@link #PATH_ITEM_REFERENCE}, and
 * {@link #SCHEMA_REFERENCE} are populated by {@link ResourceIndex} as of this
 * task; {@link #SECURITY_SCHEME_REFERENCE} and {@link #DISCRIMINATOR_MAPPING}
 * are recognized here so later tasks (security and discriminator analysis) can
 * reuse this enum, but are not yet walked into the reference graph because
 * they are not expressed as a {@code $ref}.
 */
public enum ReferenceKind {

    /** An OAS Reference Object ({@code $ref}) at a position other than a schema or Path Item. */
    REFERENCE_OBJECT,

    /** A Path Item Object referenced by {@code $ref} from the Paths map or a Callback. */
    PATH_ITEM_REFERENCE,

    /** A JSON Schema {@code $ref} inside a Schema Object. */
    SCHEMA_REFERENCE,

    /** A security scheme name looked up in {@code components.securitySchemes} by a Security Requirement. */
    SECURITY_SCHEME_REFERENCE,

    /** A discriminator mapping value identifying a schema by name or URI. */
    DISCRIMINATOR_MAPPING
}
