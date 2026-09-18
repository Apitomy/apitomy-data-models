package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.jsonschema.JFullSchema;
import io.apitomy.datamodels.models.jsonschema.compound.JCFullSchema;
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
 * Walks the schema tree manually (rather than using the generated traverser)
 * because anchor search needs to return a found node, not fire visitor callbacks.
 */
public class AnchorFragmentResolver implements FragmentResolver {

    @Override
    public Optional<Node> resolveFragment(JsonRef ref, Node targetDocument, RefResolutionContext context) {
        if (!ref.isAnchor()) {
            return Optional.empty();
        }
        return Optional.ofNullable(findAnchor(targetDocument, ref.anchor(), new HashSet<>()));
    }

    private static Node findAnchor(Node node, String anchorName, Set<Node> visited) {
        // Model nodes do not override equals/hashCode, so this set already tracks
        // identity -- System.identityHashCode has no transpiled equivalent.
        if (node == null || !visited.add(node)) {
            return null;
        }

        if (anchorName.equals(getAnchor(node))) {
            return node;
        }

        if (!(node instanceof JFullSchema)) {
            return null;
        }
        JFullSchema schema = (JFullSchema) node;

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
                                           Set<Node> visited) {
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
                                             Set<Node> visited) {
        if (union != null && union.isFullSchema()) {
            return findAnchor(union.asFullSchema(), anchorName, visited);
        }
        return null;
    }

    private static Node searchListProperty(List<JsonSchema> list, String anchorName,
                                            Set<Node> visited) {
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
        if (schema instanceof JCFullSchema) {
            JCFullSchema c = (JCFullSchema) schema;
            if (c.getDefinitions() != null) return convertDefinitions(c.getDefinitions());
            if (c.get$defs() != null) return convertDefinitions(c.get$defs());
            return null;
        }
        if (schema instanceof JDFullSchema) return ((JDFullSchema) schema).getDefinitions() != null ? convertDefinitions(((JDFullSchema) schema).getDefinitions()) : null;
        // TODO: modern versions use $defs
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, JsonSchema> convertDefinitions(Map<String, ?> defs) {
        return (Map<String, JsonSchema>) (Map<String, ?>) defs;
    }

    private static String getAnchor(Node node) {
        if (node instanceof JCFullSchema) {
            var c = (JCFullSchema) node;
            if (c.get$anchor() != null) return c.get$anchor();
        }
        if (node instanceof JMFullSchema) {
            var d = (JMFullSchema) node;
            if (d.get$anchor() != null) return d.get$anchor();
        }

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
        if (node instanceof JCFullSchema) return ((JCFullSchema) node).get$id();
        if (node instanceof JD6FullSchema) return ((JD6FullSchema) node).get$id();
        if (node instanceof JD7FullSchema) return ((JD7FullSchema) node).get$id();
        if (node instanceof JMFullSchema) return ((JMFullSchema) node).get$id();
        return null;
    }

    private static String getLegacyId(Node node) {
        if (node instanceof JCFullSchema) return ((JCFullSchema) node).getId();
        if (node instanceof JD4FullSchema) return ((JD4FullSchema) node).getId();
        return null;
    }
}
