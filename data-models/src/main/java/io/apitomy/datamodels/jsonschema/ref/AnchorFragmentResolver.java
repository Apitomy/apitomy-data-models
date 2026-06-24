package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.JDFullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.JD4FullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.JD6FullSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.JD7FullSchema;
import io.apitomy.datamodels.models.jsonschema.modern.JMFullSchema;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves anchor-based fragments ({@code #anchorName}).
 * Scans the document tree for {@code $anchor} (2019-09+) or {@code $id}/{@code id} with
 * a fragment value (draft-4 through draft-7).
 * <p>
 * Manually walks the schema tree since the generated traverser
 * does not visit sub-schemas inside map/list properties like definitions.
 */
public class AnchorFragmentResolver implements FragmentResolver {

    @Override
    public Optional<Node> resolveFragment(JsonRef ref, Node targetDocument, RefResolutionContext context) {
        if (!ref.isAnchor()) {
            return Optional.empty();
        }
        return Optional.ofNullable(findAnchor(targetDocument, ref.anchor(), new HashSet<>()));
    }

    private static Node findAnchor(Node node, String anchorName, Set<Integer> visited) {
        if (node == null || !visited.add(System.identityHashCode(node))) {
            return null;
        }

        if (anchorName.equals(getAnchor(node))) {
            return node;
        }

        if (!(node instanceof JFullSchema schema)) {
            return null;
        }

        var result = searchMapProperty(getDefinitions(schema), anchorName, visited);
        if (result != null) return result;

        result = searchMapProperty(schema.getProperties(), anchorName, visited);
        if (result != null) return result;

        result = searchMapProperty(schema.getPatternProperties(), anchorName, visited);
        if (result != null) return result;

        result = searchUnionProperty(schema.getAdditionalProperties(), anchorName, visited);
        if (result != null) return result;

        result = searchUnionProperty(schema.getNot(), anchorName, visited);
        if (result != null) return result;

        result = searchListProperty(schema.getAllOf(), anchorName, visited);
        if (result != null) return result;

        result = searchListProperty(schema.getAnyOf(), anchorName, visited);
        if (result != null) return result;

        result = searchListProperty(schema.getOneOf(), anchorName, visited);
        if (result != null) return result;

        return null;
    }

    private static Node searchMapProperty(Map<String, JsonSchema> map, String anchorName,
                                           Set<Integer> visited) {
        if (map == null) return null;
        for (var entry : map.values()) {
            if (entry != null && entry.isFullSchema()) {
                var result = findAnchor(entry.asFullSchema(), anchorName, visited);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static Node searchUnionProperty(JsonSchema union, String anchorName,
                                             Set<Integer> visited) {
        if (union != null && union.isFullSchema()) {
            return findAnchor(union.asFullSchema(), anchorName, visited);
        }
        return null;
    }

    private static Node searchListProperty(List<JsonSchema> list, String anchorName,
                                            Set<Integer> visited) {
        if (list == null) return null;
        for (var item : list) {
            if (item != null && item.isFullSchema()) {
                var result = findAnchor(item.asFullSchema(), anchorName, visited);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static Map<String, JsonSchema> getDefinitions(JFullSchema schema) {
        if (schema instanceof JDFullSchema d) return d.getDefinitions() != null ? convertDefinitions(d.getDefinitions()) : null;
        // TODO: modern versions use $defs
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, JsonSchema> convertDefinitions(Map<String, ?> defs) {
        return (Map<String, JsonSchema>) (Map<String, ?>) defs;
    }

    private static String getAnchor(Node node) {
        if (node instanceof JMFullSchema d && d.get$anchor() != null) return d.get$anchor();

        var dollarId = getDollarId(node);
        if (dollarId != null && dollarId.startsWith("#") && dollarId.length() > 1) {
            return dollarId.substring(1);
        }

        var id = getLegacyId(node);
        if (id != null && id.startsWith("#") && id.length() > 1) {
            return id.substring(1);
        }

        return null;
    }

    private static String getDollarId(Node node) {
        if (node instanceof JD6FullSchema d) return d.get$id();
        if (node instanceof JD7FullSchema d) return d.get$id();
        if (node instanceof JMFullSchema d) return d.get$id();
        return null;
    }

    private static String getLegacyId(Node node) {
        if (node instanceof JD4FullSchema d) return d.getId();
        return null;
    }
}
