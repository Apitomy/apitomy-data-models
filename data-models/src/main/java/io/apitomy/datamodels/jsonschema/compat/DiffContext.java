package io.apitomy.datamodels.jsonschema.compat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

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

    private DiffContext(DiffContext rootContext, DiffContext parentContext, String pathUpdated,
                        Set<String> visited) {
        this.rootContext = rootContext;
        this.parentContext = parentContext;
        this.pathUpdated = pathUpdated;
        this.visited = visited;
    }

    public static DiffContext createRootContext() {
        return createRootContext("", null);
    }

    public static DiffContext createRootContext(String basePath, Set<String> visited) {
        if (visited == null) {
            visited = new HashSet<>();
        }
        return new DiffContext(null, null, basePath, visited);
    }

    public DiffContext sub(String pathFragment) {
        return new DiffContext(
                rootContext != null ? rootContext : this,
                this,
                pathUpdated + "/" + pathFragment,
                visited
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
        return scope.diffs.stream().allMatch(d -> d.getDiffType().isBackwardsCompatible());
    }

    public void addDifference(DiffType type, Object original, Object updated) {
        var difference = new Difference(
                type, "",  pathUpdated,
                Objects.toString(original),
                Objects.toString(updated)
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
        return diffs.stream().anyMatch(d -> !d.getDiffType().isBackwardsCompatible());
    }

    public Set<Difference> getIncompatibleDifferences() {
        return diffs.stream()
                .filter(d -> !d.getDiffType().isBackwardsCompatible())
                .collect(Collectors.toUnmodifiableSet());
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
        return List.copyOf(unsupportedFeatures);
    }

    @Override
    public String toString() {
        return "DiffContext{compatible=%s, diffs=%d, unsupported=%d, path='%s'}"
                .formatted(foundAllDifferencesAreCompatible(), diffs.size(),
                        unsupportedFeatures.size(), pathUpdated);
    }
}
