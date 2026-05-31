package io.apitomy.datamodels.jsonschema.ref;

import io.apitomy.datamodels.jsonschema.compat.SchemaAccessor;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.jsonschema.JsonSchemaDocument;
import io.apitomy.datamodels.models.jsonschema.JsonSchemaJSchema;
import io.apitomy.datamodels.models.jsonschema.draft.JSDraftDocument;
import io.apitomy.datamodels.models.jsonschema.draft.JSDraftJSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.JSDraft4Document;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.JSDraft4JSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.JSDraft6Document;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.JSDraft6JSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7Document;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.JSDraft7JSchema;
import io.apitomy.datamodels.models.jsonschema.modern.JSModernDocument;
import io.apitomy.datamodels.models.jsonschema.modern.JSModernJSchema;
import io.apitomy.datamodels.models.union.BooleanJSchemaUnion;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves anchor-based references ({@code #anchorName}).
 * Manually walks the schema tree since the generated traverser
 * does not visit sub-schemas inside map/list properties like definitions.
 */
public class AnchorRefResolver implements JsonSchemaRefResolver {

    @Override
    public Optional<ResolvedRef> resolve(JsonRef ref, RefResolutionContext context) {
        if (!ref.isInternal() || !ref.isAnchor()) {
            return Optional.empty();
        }
        var root = context.from().root();
        var result = findAnchor(root, ref.anchor(), new HashSet<>());
        return Optional.ofNullable(result).map(ResolvedRef::attached);
    }

    private static Node findAnchor(Node node, String anchorName, Set<Integer> visited) {
        if (node == null || !visited.add(System.identityHashCode(node))) {
            return null;
        }

        if (anchorName.equals(getAnchor(node))) {
            return node;
        }

        // Walk into sub-schemas via the accessor
        var accessor = SchemaAccessor.wrap(node);

        // Map properties: definitions, properties, patternProperties
        var result = searchMapProperty(getDefinitions(node), anchorName, visited);
        if (result != null) return result;

        result = searchMapProperty(accessor.getProperties(), anchorName, visited);
        if (result != null) return result;

        result = searchMapProperty(accessor.getPatternProperties(), anchorName, visited);
        if (result != null) return result;

        // Union properties
        result = searchUnionProperty(accessor.getAdditionalProperties(), anchorName, visited);
        if (result != null) return result;

        result = searchUnionProperty(accessor.getNot(), anchorName, visited);
        if (result != null) return result;

        // List properties: allOf, anyOf, oneOf
        result = searchListProperty(accessor.getAllOf(), anchorName, visited);
        if (result != null) return result;

        result = searchListProperty(accessor.getAnyOf(), anchorName, visited);
        if (result != null) return result;

        result = searchListProperty(accessor.getOneOf(), anchorName, visited);
        if (result != null) return result;

        return null;
    }

    private static Node searchMapProperty(Map<String, BooleanJSchemaUnion> map, String anchorName,
                                           Set<Integer> visited) {
        if (map == null) return null;
        for (var entry : map.values()) {
            if (entry != null && entry.isJSchema()) {
                var result = findAnchor(entry.asJSchema(), anchorName, visited);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static Node searchUnionProperty(BooleanJSchemaUnion union, String anchorName,
                                             Set<Integer> visited) {
        if (union != null && union.isJSchema()) {
            return findAnchor(union.asJSchema(), anchorName, visited);
        }
        return null;
    }

    private static Node searchListProperty(List<BooleanJSchemaUnion> list, String anchorName,
                                            Set<Integer> visited) {
        if (list == null) return null;
        for (var item : list) {
            if (item != null && item.isJSchema()) {
                var result = findAnchor(item.asJSchema(), anchorName, visited);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static Map<String, BooleanJSchemaUnion> getDefinitions(Node node) {
        if (node instanceof JSDraftDocument d) return d.getDefinitions() != null ? convertDefinitions(d.getDefinitions()) : null;
        if (node instanceof JSDraftJSchema s) return s.getDefinitions() != null ? convertDefinitions(s.getDefinitions()) : null;
        // TODO: modern versions use $defs
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, BooleanJSchemaUnion> convertDefinitions(Map<String, ?> defs) {
        return (Map<String, BooleanJSchemaUnion>) (Map<String, ?>) defs;
    }

    private static String getAnchor(Node node) {
        if (node instanceof JSModernDocument d && d.get$anchor() != null) return d.get$anchor();
        if (node instanceof JSModernJSchema s && s.get$anchor() != null) return s.get$anchor();

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
        if (node instanceof JSDraft6Document d) return d.get$id();
        if (node instanceof JSDraft6JSchema s) return s.get$id();
        if (node instanceof JSDraft7Document d) return d.get$id();
        if (node instanceof JSDraft7JSchema s) return s.get$id();
        if (node instanceof JSModernDocument d) return d.get$id();
        if (node instanceof JSModernJSchema s) return s.get$id();
        return null;
    }

    private static String getLegacyId(Node node) {
        if (node instanceof JSDraft4Document d) return d.getId();
        if (node instanceof JSDraft4JSchema s) return s.getId();
        return null;
    }
}
