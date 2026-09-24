package io.apitomy.datamodels.jsonschema.compat.containment;

/**
 * A single piece of evidence contributing to a {@link ContainmentResult}: the
 * schema positions being compared and the rule that reached a conclusion
 * about them.
 * <p>
 * Evidence is explanatory provenance, not itself a verdict -- several pieces
 * of evidence (for example, one per composition branch) can together justify
 * a single {@link ContainmentResult}.
 */
public final class SchemaEvidence {

    private final String sourcePointer;
    private final String targetPointer;
    private final String rule;
    private final String message;

    public SchemaEvidence(String sourcePointer, String targetPointer, String rule, String message) {
        if (rule == null || rule.length() == 0) {
            throw new IllegalArgumentException("rule must not be null or empty");
        }
        if (message == null || message.length() == 0) {
            throw new IllegalArgumentException("message must not be null or empty");
        }
        this.sourcePointer = sourcePointer;
        this.targetPointer = targetPointer;
        this.rule = rule;
        this.message = message;
    }

    /** The JSON Pointer, within the source schema's own resource, this evidence concerns, or {@code null}. */
    public String getSourcePointer() {
        return sourcePointer;
    }

    /** The JSON Pointer, within the target schema's own resource, this evidence concerns, or {@code null}. */
    public String getTargetPointer() {
        return targetPointer;
    }

    /** A stable identifier for the rule that produced this evidence (for example, {@code "type-mismatch"}). */
    public String getRule() {
        return rule;
    }

    /** A human-readable explanation. */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SchemaEvidence{rule=" + rule + ", sourcePointer=" + sourcePointer + ", targetPointer=" + targetPointer
                + ", message='" + message + "'}";
    }
}
