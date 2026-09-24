package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.jsonschema.ref.JsonPointer;
import io.apitomy.datamodels.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A location in the compared schemas, with the differences found there and the locations below it
 * that have differences of their own.
 * <p>
 * The root node is the whole schema ({@link CompatibilityCheckResult#getRoot()}). Each child is a
 * nested schema the checker compared as part of its parent, such as a property's schema
 * ({@code /properties/name}) or the schema of the items of an array ({@code /items}). Only
 * locations with a difference at or below them appear, so the tree can be rendered directly, and
 * {@link #flatten()} lists every difference in it.
 * <p>
 * The difference that explains a change is recorded where the change is. For a string property
 * whose {@code maxLength} was decreased, the node {@code /properties/name} holds
 * {@code STRING_TYPE_MAX_LENGTH_DECREASED} at {@code /properties/name/maxLength}. A comparison that
 * only matches alternatives, such as {@code anyOf} branches, is reported as one difference on the
 * node that owns the keyword.
 */
public final class DifferenceNode {

    private final JsonPointer pathOriginal;
    private final JsonPointer pathUpdated;
    private final List<Difference> differences;
    private final List<DifferenceNode> children;

    DifferenceNode(JsonPointer pathOriginal, JsonPointer pathUpdated, List<Difference> differences,
                   List<DifferenceNode> children) {
        this.pathOriginal = pathOriginal;
        this.pathUpdated = pathUpdated;
        this.differences = differences;
        this.children = CollectionUtil.copyOfList(children);
    }

    /** Where this location is in the original schema, in that schema's keywords. */
    public JsonPointer getPathOriginal() {
        return pathOriginal;
    }

    /** Where this location is in the updated schema, in that schema's keywords. */
    public JsonPointer getPathUpdated() {
        return pathUpdated;
    }

    /** The differences recorded at this location, excluding those of the locations below it. */
    public List<Difference> getDifferences() {
        return differences;
    }

    /** The locations below this one that have differences, in the order they were compared. */
    public List<DifferenceNode> getChildren() {
        return children;
    }

    /** Whether nothing at this location or below it breaks backward compatibility. */
    public boolean isCompatible() {
        for (Difference difference : differences) {
            if (!difference.getDiffType().isBackwardsCompatible()) {
                return false;
            }
        }
        for (DifferenceNode child : children) {
            if (!child.isCompatible()) {
                return false;
            }
        }
        return true;
    }

    /** Every difference at this location and below it, depth-first. */
    public List<Difference> flatten() {
        List<Difference> all = new ArrayList<Difference>();
        collect(all);
        return all;
    }

    private void collect(List<Difference> all) {
        all.addAll(differences);
        for (DifferenceNode child : children) {
            child.collect(all);
        }
    }

    /** Whether this location and every location below it have no differences. */
    boolean isEmpty() {
        return differences.isEmpty() && children.isEmpty();
    }

    @Override
    public String toString() {
        return "DifferenceNode{path='" + pathUpdated + "', differences=" + differences.size()
                + ", children=" + children.size() + "}";
    }
}
