package io.apitomy.datamodels.jsonschema.compat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.LinkedHashSet;
import io.apitomy.datamodels.util.CollectionUtil;

public class DiffContext {

    private final Set<Difference> diffs = new HashSet<>();
    private final List<String> unsupportedFeatures = new ArrayList<>();
    private final Stack<Scope> scopeStack = new Stack<>();

    private static class Scope {
        final Set<Difference> diffs = new HashSet<>();
        final boolean isolated;
        Scope(boolean isolated) { this.isolated = isolated; }
    }
    private final DiffContext parentContext;
    private final DiffContext rootContext;
    private final String pathUpdated;
    // Shared by reference across all sub-contexts
    final Set<String> visited;
    // Stable per-object ids, shared with visited so sub-contexts agree on identity.
    // Replaces System.identityHashCode, which has no transpiled equivalent. Model
    // nodes do not override equals/hashCode, so this map is keyed by identity.
    private final Map<Object, Integer> identityIds;

    private DiffContext(DiffContext rootContext, DiffContext parentContext, String pathUpdated,
                        Set<String> visited, Map<Object, Integer> identityIds) {
        this.rootContext = rootContext;
        this.parentContext = parentContext;
        this.pathUpdated = pathUpdated;
        this.visited = visited;
        this.identityIds = identityIds;
    }

    /**
     * A stable id for the given object, assigned on first use. Two calls with the
     * same instance return the same id; equal-but-distinct instances get different
     * ids, which is the identity semantics the diff traversal needs.
     */
    int identityId(Object o) {
        var id = identityIds.get(o);
        if (id == null) {
            id = identityIds.size() + 1;
            identityIds.put(o, id);
        }
        return id;
    }

    public static DiffContext createRootContext() {
        return createRootContext("", null);
    }

    public static DiffContext createRootContext(String basePath, Set<String> visited) {
        if (visited == null) {
            visited = new HashSet<>();
        }
        return new DiffContext(null, null, basePath, visited, new HashMap<>());
    }

    public DiffContext sub(String pathFragment) {
        return new DiffContext(
                rootContext != null ? rootContext : this,
                this,
                pathUpdated + "/" + pathFragment,
                visited,
                identityIds
        );
    }

    public String getPathUpdated() {
        return pathUpdated;
    }

    /**
     * Start a scope that collects diffs AND propagates them to the parent.
     * Use with afterDiff callbacks to inspect results of auto-recursion.
     */
    public void pushScope() {
        scopeStack.push(new Scope(false));
    }

    /**
     * Start an isolated scope — diffs are collected but NOT propagated to the parent.
     */
    public void pushIsolatedScope() {
        scopeStack.push(new Scope(true));
    }

    /**
     * End the current scope and return whether all diffs collected in it
     * are backward-compatible.
     */
    public boolean popScopeIsCompatible() {
        if (scopeStack.isEmpty()) {
            throw new IllegalStateException("No scope to pop");
        }
        Scope scope = scopeStack.pop();
        for (Difference d : scope.diffs) {
            if (!d.getDiffType().isBackwardsCompatible()) {
                return false;
            }
        }
        return true;
    }

    public void addDifference(DiffType type, Object original, Object updated) {
        var difference = new Difference(
                type, "",  pathUpdated,
                original == null ? "null" : original.toString(),
                updated == null ? "null" : updated.toString()
        );
        addToDifferenceSets(difference);
    }

    private void addToDifferenceSets(Difference difference) {
        if (!scopeStack.isEmpty()) {
            Scope activeScope = scopeStack.peek();
            activeScope.diffs.add(difference);
            if (activeScope.isolated) {
                return;
            }
        }
        diffs.add(difference);
        if (parentContext != null) {
            parentContext.addToDifferenceSets(difference);
        }
    }

    public Set<Difference> getDiffs() {
        return new HashSet<>(diffs);
    }

    public boolean foundIncompatibleDifference() {
        for (Difference d : diffs) {
            if (!d.getDiffType().isBackwardsCompatible()) {
                return true;
            }
        }
        return false;
    }

    public Set<Difference> getIncompatibleDifferences() {
        Set<Difference> incompatible = new LinkedHashSet<Difference>();
        for (Difference d : diffs) {
            if (!d.getDiffType().isBackwardsCompatible()) {
                incompatible.add(d);
            }
        }
        return incompatible;
    }

    public boolean foundAllDifferencesAreCompatible() {
        return !foundIncompatibleDifference();
    }

    public void addUnsupported(String feature) {
        unsupportedFeatures.add(feature);
        if (parentContext != null) {
            parentContext.addUnsupported(feature);
        }
    }

    public boolean hasUnsupportedFeatures() {
        return !unsupportedFeatures.isEmpty();
    }

    public List<String> getUnsupportedFeatures() {
        return CollectionUtil.copyOfList(unsupportedFeatures);
    }

    @Override
    public String toString() {
        return "DiffContext{compatible=" + foundAllDifferencesAreCompatible()
                + ", diffs=" + diffs.size()
                + ", unsupported=" + unsupportedFeatures.size()
                + ", path='" + pathUpdated + "'}";
    }
}
