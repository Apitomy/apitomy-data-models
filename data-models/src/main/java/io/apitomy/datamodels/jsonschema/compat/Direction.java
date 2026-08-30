package io.apitomy.datamodels.jsonschema.compat;

/**
 * The direction of a compatibility check — which schema plays the role of the reader.
 *
 * <ul>
 *   <li>{@link #BACKWARD} — a reader using the <em>new</em> schema must accept data written against
 *       the <em>old</em> schema (the common "backward compatible" case);</li>
 *   <li>{@link #FORWARD} — a reader using the <em>old</em> schema must accept data written against
 *       the <em>new</em> schema.</li>
 * </ul>
 */
public enum Direction {
    BACKWARD,
    FORWARD
}
