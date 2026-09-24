package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.jsonschema.convert.CompoundSchemaConverter;
import io.apitomy.datamodels.jsonschema.ref.JsonPointer;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.visitors.TraversalContext;
import io.apitomy.datamodels.util.CollectionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Records the differences found while comparing two schemas, as a tree of schema locations.
 * <p>
 * Each context is one location, identified by a JSON Pointer into each schema. A nested schema
 * that is compared as part of this one gets an attached child ({@link #sub}); its differences
 * become part of the result. A speculative comparison, whose outcome only decides what to report
 * here, runs in a detached context ({@link #probe}) that never enters the tree.
 * <p>
 * Internal to the checker. {@link CompatibilityCheckResult} takes an immutable snapshot of the
 * tree ({@link DifferenceNode}) when the check finishes.
 */
final class DiffContext {

    /** State shared by every context of one check. */
    private static final class Shared {
        final Set<String> visited = new HashSet<String>();
        // Stable per-object ids. Replaces System.identityHashCode, which has no transpiled
        // equivalent. Model nodes do not override equals/hashCode, so this map is keyed by identity.
        final Map<Object, Integer> identityIds = new HashMap<Object, Integer>();
        final List<String> unsupportedFeatures = new ArrayList<String>();
    }

    private final Shared shared;
    private final JsonPointer pathOriginal;
    private final JsonPointer pathUpdated;
    private final Set<Difference> differences = new LinkedHashSet<Difference>();
    private final List<DiffContext> children = new ArrayList<DiffContext>();

    /** The schemas compared at this location, which own the keywords named in paths. */
    private JFullSchema originalSchema;
    private JFullSchema updatedSchema;
    /** The traversal comparing this location's schemas; its current property is the keyword. */
    private TraversalContext traversal;

    private DiffContext(Shared shared, JsonPointer pathOriginal, JsonPointer pathUpdated) {
        this.shared = shared;
        this.pathOriginal = pathOriginal;
        this.pathUpdated = pathUpdated;
    }

    static DiffContext createRootContext() {
        return new DiffContext(new Shared(), JsonPointer.root(), JsonPointer.root());
    }

    // -----------------------------------------------------------------------
    // Tree
    // -----------------------------------------------------------------------

    /**
     * The attached child for the nested schema under {@code keyword}, e.g. {@code items}. The
     * keyword is given in compound-schema terms and is written into each path in the terms of that
     * side's schema.
     */
    DiffContext sub(String keyword) {
        return attach(new DiffContext(shared,
                pathOriginal.append(sourceKeyword(originalSchema, keyword)),
                pathUpdated.append(sourceKeyword(updatedSchema, keyword))));
    }

    /** The attached child for the nested schema {@code keyword/key}, e.g. {@code properties/a}. */
    DiffContext sub(String keyword, String key) {
        return attach(new DiffContext(shared,
                pathOriginal.append(sourceKeyword(originalSchema, keyword)).append(key),
                pathUpdated.append(sourceKeyword(updatedSchema, keyword)).append(key)));
    }

    /**
     * A detached context for a speculative comparison, e.g. matching an {@code anyOf} branch. Only
     * its verdict ({@link #isCompatible()}) is used; nothing recorded in it enters the result.
     */
    DiffContext probe() {
        return new DiffContext(shared, pathOriginal, pathUpdated);
    }

    private DiffContext attach(DiffContext child) {
        children.add(child);
        return child;
    }

    /** Called when the schemas at this location are compared. */
    void startComparison(JFullSchema original, JFullSchema updated, TraversalContext traversalContext) {
        this.originalSchema = original;
        this.updatedSchema = updated;
        this.traversal = traversalContext;
    }

    // -----------------------------------------------------------------------
    // Differences
    // -----------------------------------------------------------------------

    /**
     * Records a difference in the keyword that is currently being compared at this location, or at
     * the location itself when no keyword is being compared.
     */
    void addDifference(DiffType type) {
        addDifference(type, currentKeyword());
    }

    /**
     * Records a difference in the given keyword (compound-schema terms) of this location, or at the
     * location itself when {@code keyword} is {@code null}.
     */
    void addDifference(DiffType type, String keyword) {
        record(type, keyword, null, null, null);
    }

    /**
     * Records a difference in one member of the value of the keyword currently being compared, e.g.
     * a name added to {@code required}. The member's segment, an index or a map key, is appended on
     * the side where the member exists; the other side's path ends at the keyword. {@code key}, when
     * not {@code null}, is a map key between the keyword and the member, as in
     * {@code /dependentRequired/a/0}.
     */
    void addMemberDifference(DiffType type, String key, String originalMember, String updatedMember) {
        record(type, currentKeyword(), key, originalMember, updatedMember);
    }

    private String currentKeyword() {
        return traversal != null ? traversal.getMostRecentPropertyStep() : null;
    }

    private void record(DiffType type, String keyword, String key, String originalMember, String updatedMember) {
        differences.add(new Difference(type,
                path(pathOriginal, originalSchema, keyword, key, originalMember),
                path(pathUpdated, updatedSchema, keyword, key, updatedMember)));
    }

    private static JsonPointer path(JsonPointer base, JFullSchema owner, String keyword, String key, String member) {
        if (keyword == null) {
            return base;
        }
        JsonPointer path = base.append(sourceKeyword(owner, keyword));
        if (key != null) {
            path = path.append(key);
        }
        return member != null ? path.append(member) : path;
    }

    private static String sourceKeyword(JFullSchema owner, String keyword) {
        return CompoundSchemaConverter.getSourceKeyword(owner, keyword);
    }

    /** Whether nothing recorded at this location or below it breaks backward compatibility. */
    boolean isCompatible() {
        for (Difference d : differences) {
            if (!d.getDiffType().isBackwardsCompatible()) {
                return false;
            }
        }
        for (DiffContext child : children) {
            if (!child.isCompatible()) {
                return false;
            }
        }
        return true;
    }

    /** An immutable snapshot of this location and the attached locations below it. */
    DifferenceNode toNode() {
        List<DifferenceNode> childNodes = new ArrayList<DifferenceNode>();
        for (DiffContext child : children) {
            DifferenceNode node = child.toNode();
            if (!node.isEmpty()) {
                childNodes.add(node);
            }
        }
        return new DifferenceNode(pathOriginal, pathUpdated,
                CollectionUtil.copyOfList(new ArrayList<Difference>(differences)), childNodes);
    }

    // -----------------------------------------------------------------------
    // Per-check state
    // -----------------------------------------------------------------------

    Set<String> visited() {
        return shared.visited;
    }

    /**
     * A stable id for the given object, assigned on first use. Two calls with the same instance
     * return the same id; equal-but-distinct instances get different ids, which is the identity
     * semantics the diff traversal needs.
     */
    int identityId(Object o) {
        Integer id = shared.identityIds.get(o);
        if (id == null) {
            id = shared.identityIds.size() + 1;
            shared.identityIds.put(o, id);
        }
        return id;
    }

    void addUnsupported(String feature) {
        shared.unsupportedFeatures.add(feature);
    }

    List<String> getUnsupportedFeatures() {
        return CollectionUtil.copyOfList(shared.unsupportedFeatures);
    }

    @Override
    public String toString() {
        return "DiffContext{path='" + pathUpdated + "', differences=" + differences.size()
                + ", children=" + children.size() + "}";
    }
}
