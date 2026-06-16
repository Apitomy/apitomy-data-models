package io.apitomy.umg.base;

/**
 * Common supertype for all tree values — both {@link Node} (entities) and
 * {@link io.apitomy.umg.base.union.Union} (union values including primitives).
 * <p>
 * <b>Parent-child relationships:</b>
 * <ul>
 *   <li>Every value in the tree has at most one parent (a {@link Node}).</li>
 *   <li>Parent and property metadata are set automatically by generated setters,
 *       collection add methods, and factory methods ({@code createXxx()}).</li>
 *   <li>Parent and property metadata are cleared automatically by generated
 *       collection remove/clear methods, and by {@link #detach()}.</li>
 *   <li>There is no need to manage parent relationships manually in normal usage.</li>
 * </ul>
 * <p>
 * <b>Attachment state:</b>
 * <ul>
 *   <li>A value is <em>attached</em> if it has a non-null parent ({@link #isAttached()}).
 *       Note: this only checks for an immediate parent, not whether the value is
 *       reachable from a root.</li>
 *   <li>A value is <em>fully attached</em> (reachable from a root) if {@link #root()}
 *       returns non-null.</li>
 *   <li>Calling {@link #detach()} clears the parent and all property metadata,
 *       making the value unattached.</li>
 * </ul>
 * <p>
 * <b>Root nodes:</b> A {@link RootCapable} node is the root of the tree when
 * {@link RootCapable#isRoot()} returns {@code true} (i.e., it was created with a
 * {@code ModelType}). The root's {@link #parent()} is {@code null}, and
 * {@link #root()} returns itself.
 */
public interface Any {

    boolean isNode();

    Node parent();

    String parentPropertyName();

    ParentPropertyType parentPropertyType();

    String mapPropertyName();

    RootCapable root();

    boolean isAttached();

    /**
     * Detaches this value from its parent by clearing the parent reference
     * and all property metadata (parentPropertyName, parentPropertyType,
     * mapPropertyName). After calling this method, {@link #isAttached()}
     * returns {@code false} and {@link #root()} returns {@code null}.
     * <p>
     * Note: this does NOT remove the value from the parent's collection
     * or property. The caller is responsible for that, or should use the
     * generated remove/clear methods which call detach automatically.
     */
    void detach();

}
