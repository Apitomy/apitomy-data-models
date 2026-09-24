package io.apitomy.datamodels.jsonschema.compat.containment;

/**
 * The JSON Schema dialect (or OAS Schema Object variant) a {@link SchemaView}
 * is interpreted under. Distinct from {@link io.apitomy.datamodels.models.ModelType}:
 * a dialect describes keyword/vocabulary semantics for containment purposes,
 * not which generated Java entity a node happens to be.
 */
public enum SchemaDialect {

    /** OpenAPI 2.0 (Swagger) Schema Object: JSON Schema Draft 4 subset, legacy exclusive-bound style. */
    OAS20,

    /** OpenAPI 3.0 Schema Object: JSON Schema Draft 4/Wright-00 subset, plus {@code nullable}. */
    OAS30,

    /** OpenAPI 3.1 Schema Object: JSON Schema 2020-12, plus {@code discriminator}/{@code xml}/etc. */
    OAS31,

    /** OpenAPI 3.2 Schema Object: JSON Schema 2020-12, plus 3.2-only additions. */
    OAS32,

    /** Bare JSON Schema Draft 4. */
    DRAFT4,

    /** Bare JSON Schema Draft 6. */
    DRAFT6,

    /** Bare JSON Schema Draft 7. */
    DRAFT7,

    /** Bare JSON Schema 2019-09. */
    DRAFT2019_09,

    /** Bare JSON Schema 2020-12. */
    DRAFT2020_12,

    /**
     * A resource whose {@code $schema}/{@code jsonSchemaDialect} names a dialect
     * this checker does not recognize, or which requires a vocabulary this
     * checker does not implement. Containment against a schema under this
     * dialect is {@link ContainmentVerdict#UNKNOWN}, never a supported dialect's
     * proof rules applied by coincidence.
     */
    UNSUPPORTED
}
