package io.apitomy.umg.base.visitors;

import java.util.List;

/**
 * Context available during model traversal, providing path information
 * and traversal control.
 * <p>
 * The traverser maintains this context and makes it available to the visitor
 * if the visitor implements {@link TraversingVisitor}. Visitors can use it to:
 * <ul>
 *   <li>Inspect the current traversal path ({@link #getAllSteps()},
 *       {@link #getMostRecentStep()})</li>
 *   <li>Control traversal behavior ({@link #skip()} to prevent auto-recursion
 *       into the current node's children)</li>
 * </ul>
 * <p>
 * The traverser resets the action to {@link TraversalAction#CONTINUE} before
 * each visit method call, so visitors only need to call {@link #skip()} when
 * they want non-default behavior.
 */
public interface TraversalContext {

    /**
     * Returns the most recent traversal step (the current position).
     */
    public TraversalStep getMostRecentStep();

    /**
     * Returns all traversal steps from root to the current position.
     */
    public List<TraversalStep> getAllSteps();

    /**
     * Returns {@code true} if the traversal path contains a step of the
     * given type and value.
     */
    public boolean containsStep(TraversalStepType type, Object value);

    /**
     * Returns the name of the most recent property step in the traversal path,
     * or {@code null} if no property step exists.
     */
    public String getMostRecentPropertyStep();

    /**
     * Request that the traverser skip auto-recursion into the current node's
     * children. Call this from a visit method to handle recursion manually
     * or to stop traversal at this level.
     * <p>
     * The action resets to {@link TraversalAction#CONTINUE} before each
     * visit method call — only call this when you want to override the default.
     * <p>
     * The {@code afterVisit} callback is still called even when skipping.
     */
    public void skip();

}
