package io.apitomy.datamodels.jsonschema.compat;

import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefTraversal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DiffContext {

    private final Set<Difference> diffs = new HashSet<>();
    private final List<String> unsupportedFeatures = new ArrayList<>();
    private final DiffContext parentContext;
    private final DiffContext rootContext;
    private final String pathUpdated;
    private final JsonSchemaRefTraversal refTraversal;
    final Set<String> visited = new HashSet<>();

    private DiffContext(DiffContext rootContext, DiffContext parentContext, String pathUpdated,
                        Set<String> visited, JsonSchemaRefTraversal refTraversal) {
        this.rootContext = rootContext;
        this.parentContext = parentContext;
        this.pathUpdated = pathUpdated;
        this.visited.addAll(visited);
        this.refTraversal = refTraversal;
    }

    public static DiffContext createRootContext() {
        return createRootContext("", null, null);
    }

    public static DiffContext createRootContext(String basePath, Set<String> visited,
                                                JsonSchemaRefTraversal refTraversal) {
        if (visited == null) {
            visited = new HashSet<>();
        }
        var root = new DiffContext(null, null, basePath, visited, refTraversal);
        root.initRootContext();
        return root;
    }

    private void initRootContext() {
        if (rootContext != null || parentContext != null) {
            throw new IllegalStateException("Root context already initialized");
        }
    }

    public DiffContext sub(String pathFragment) {
        return new DiffContext(
                rootContext != null ? rootContext : this,
                this,
                pathUpdated + "/" + pathFragment,
                visited,
                refTraversal
        );
    }

    public JsonSchemaRefTraversal getRefTraversal() {
        return refTraversal;
    }

    public String getPathUpdated() {
        return pathUpdated;
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
