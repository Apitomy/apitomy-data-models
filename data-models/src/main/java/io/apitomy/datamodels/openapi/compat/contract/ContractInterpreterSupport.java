package io.apitomy.datamodels.openapi.compat.contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.MappedNode;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.SecurityRequirement;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialect;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaView;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathSegment;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.util.CollectionUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * Family-independent helpers shared by {@link OpenApi20Interpreter} and
 * {@link OpenApi3Interpreter}: parameter identity/merging, security
 * conversion, declaration-pointer computation, and schema-view construction
 * from a model {@code Schema} node. Kept separate from the two family
 * interpreters so neither has to duplicate logic that does not actually
 * differ between them.
 */
final class ContractInterpreterSupport {

    private ContractInterpreterSupport() {
    }

    /** The JSON Pointer-shaped declaration location of {@code node}, for provenance on effective values. */
    static String declarationPointer(Node node) {
        if (node == null) {
            return null;
        }
        return NodePathUtil.createNodePath(node).toString();
    }

    /**
     * The identity a parameter (or response header, passed with {@code in = "header"})
     * is merged/deduplicated by: {@code in} plus its name, where a header name is
     * case-folded (HTTP header names are case-insensitive) but every other
     * location's name is compared exactly as declared.
     */
    static String parameterIdentity(String in, String name) {
        String normalizedName = "header".equals(in) ? name.toLowerCase() : name;
        return in + ":" + normalizedName;
    }

    /**
     * Merges Path Item-level parameters with Operation-level parameters by
     * {@link #parameterIdentity}: an operation entry replaces a path entry of
     * the same identity in place (preserving the path entry's position), and
     * any operation entry with a new identity is appended after. Each raw
     * parameter is dereferenced ({@code $ref}) before its identity is read; an
     * unresolvable reference is recorded in {@code problems} and skipped
     * (its constraints cannot be silently assumed empty).
     * <p>
     * A duplicate identity within the same list (two path-level parameters
     * both named {@code id} in {@code query}, for example) is an invalid,
     * conflicting declaration: it is recorded in {@code problems}, and the
     * first declaration encountered wins for interpretation.
     */
    static List<ResolvedParameter> mergeParameters(List<OpenApiParameter> pathParameters,
            List<OpenApiParameter> operationParameters, String contextLabel, List<String> problems) {
        Map<String, ResolvedParameter> byIdentity = new LinkedHashMap<String, ResolvedParameter>();
        List<String> order = new ArrayList<String>();

        addParametersToMerge(pathParameters, false, contextLabel, problems, byIdentity, order);
        addParametersToMerge(operationParameters, true, contextLabel, problems, byIdentity, order);

        List<ResolvedParameter> result = new ArrayList<ResolvedParameter>();
        for (int i = 0; i < order.size(); i++) {
            result.add(byIdentity.get(order.get(i)));
        }
        return result;
    }

    private static void addParametersToMerge(List<OpenApiParameter> parameters, boolean operationLevel,
            String contextLabel, List<String> problems, Map<String, ResolvedParameter> byIdentity, List<String> order) {
        if (parameters == null) {
            return;
        }
        for (int i = 0; i < parameters.size(); i++) {
            OpenApiParameter raw = parameters.get(i);
            Node resolved = ReferenceUtil.resolveNodeRef((Node) raw);
            if (resolved == null || NodeUtil.getProperty(resolved, "$ref") != null) {
                problems.add(contextLabel + ": a parameter reference could not be resolved");
                continue;
            }
            OpenApiParameter parameter = (OpenApiParameter) resolved;
            String in = parameter.getIn();
            String name = parameter.getName();
            if (in == null || name == null) {
                problems.add(contextLabel + ": a parameter is missing its 'in' or 'name'");
                continue;
            }
            String identity = parameterIdentity(in, name);
            boolean alreadyFromThisLevel = operationLevel
                    ? order.contains(identity) && byIdentity.get(identity).isDeclaredAtOperationLevel()
                    : order.contains(identity);
            if (alreadyFromThisLevel) {
                problems.add(contextLabel + ": duplicate parameter declaration for " + identity);
                continue;
            }
            if (!order.contains(identity)) {
                order.add(identity);
            }
            byIdentity.put(identity, new ResolvedParameter(parameter, operationLevel));
        }
    }

    /** A dereferenced parameter paired with whether it came from the operation's own declaration. */
    static final class ResolvedParameter {
        private final OpenApiParameter parameter;
        private final boolean declaredAtOperationLevel;

        ResolvedParameter(OpenApiParameter parameter, boolean declaredAtOperationLevel) {
            this.parameter = parameter;
            this.declaredAtOperationLevel = declaredAtOperationLevel;
        }

        OpenApiParameter getParameter() {
            return parameter;
        }

        boolean isDeclaredAtOperationLevel() {
            return declaredAtOperationLevel;
        }
    }

    /**
     * The effective security alternatives: the operation's own declaration if
     * present (even if an empty list, meaning explicitly no security), else
     * the root document's declaration, else no security at all.
     * <p>
     * The generated model cannot by itself distinguish an absent {@code security}
     * key from an explicit {@code "security": []}: both leave the typed
     * {@code getSecurity()} list {@code null}, because the generated list
     * setter is only ever invoked once per array entry (never once-with-zero
     * entries) -- an empty JSON array adds nothing, so the backing list is
     * never lazily created. {@code rawDocumentRoot} (the original, unmutated
     * document JSON, read before the model reader stripped recognized
     * properties off its own working copy) recovers that distinction: if the
     * key was present in the source at all, its emptiness in the model means
     * an explicit override to no security, not "unspecified".
     */
    static List<Map<String, List<String>>> effectiveSecurity(Node operation, Node document, JsonNode rawDocumentRoot) {
        List<SecurityRequirement> operationSecurity = securityOf(operation);
        if (operationSecurity != null) {
            return convertSecurity(operationSecurity);
        }
        if (isKeyExplicitlyDeclared(rawDocumentRoot, operation, "security")) {
            return convertSecurity(null);
        }
        return convertSecurity(securityOf(document));
    }

    @SuppressWarnings("unchecked")
    private static List<SecurityRequirement> securityOf(Node node) {
        return (List<SecurityRequirement>) NodeUtil.getNodeProperty(node, "security");
    }

    /**
     * True if {@code targetNode}'s own JSON object (located in
     * {@code rawDocumentRoot} by walking the same path the model reader
     * assigned to {@code targetNode}) has a property literally named
     * {@code key}, regardless of that property's value -- including an empty
     * array, which the parsed model alone cannot distinguish from "absent"
     * for a list-typed property (see {@link #effectiveSecurity}).
     */
    static boolean isKeyExplicitlyDeclared(JsonNode rawDocumentRoot, Node targetNode, String key) {
        JsonNode rawNode = navigateRawJson(rawDocumentRoot, targetNode);
        if (rawNode == null || !io.apitomy.datamodels.models.util.JsonUtil.isObject(rawNode)) {
            return false;
        }
        return io.apitomy.datamodels.models.util.JsonUtil.getProperty(
                io.apitomy.datamodels.models.util.JsonUtil.toObject(rawNode), key) != null;
    }

    /**
     * Walks {@code rawDocumentRoot} along the same structural path
     * {@link NodePathUtil#createNodePath} assigns to {@code targetNode} in the
     * parsed model, to find {@code targetNode}'s own raw JSON -- since
     * property names in this generated model are the same strings as the JSON
     * keys they were read from, the path segments apply unchanged to either
     * tree.
     */
    private static JsonNode navigateRawJson(JsonNode rawDocumentRoot, Node targetNode) {
        NodePath path = NodePathUtil.createNodePath(targetNode);
        List<NodePathSegment> segments = path.getSegments();
        JsonNode current = rawDocumentRoot;
        for (int i = 0; i < segments.size(); i++) {
            if (current == null) {
                return null;
            }
            NodePathSegment segment = segments.get(i);
            if (!io.apitomy.datamodels.models.util.JsonUtil.isObject(current)
                    && !io.apitomy.datamodels.models.util.JsonUtil.isArray(current)) {
                return null;
            }
            if (io.apitomy.datamodels.models.util.JsonUtil.isArray(current)) {
                int index = Integer.parseInt(segment.getValue());
                List<JsonNode> items = io.apitomy.datamodels.models.util.JsonUtil.toList(current);
                current = index >= 0 && index < items.size() ? items.get(index) : null;
            } else {
                current = io.apitomy.datamodels.models.util.JsonUtil.getProperty(
                        io.apitomy.datamodels.models.util.JsonUtil.toObject(current), segment.getValue());
            }
        }
        return current;
    }

    private static List<Map<String, List<String>>> convertSecurity(List<SecurityRequirement> requirements) {
        List<Map<String, List<String>>> result = new ArrayList<Map<String, List<String>>>();
        if (requirements == null) {
            return result;
        }
        for (int i = 0; i < requirements.size(); i++) {
            SecurityRequirement requirement = requirements.get(i);
            Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
            List<String> names = requirement.getItemNames();
            for (int j = 0; j < names.size(); j++) {
                String name = names.get(j);
                map.put(name, CollectionUtil.copyOfList(requirement.getItem(name)));
            }
            result.add(map);
        }
        return result;
    }

    /** A {@link SchemaView} over a model {@code Schema} node's own serialized JSON, under {@code dialect}. */
    static SchemaView schemaViewOf(Schema schema, SchemaDialect dialect, String resourceUri, Node source) {
        if (schema == null) {
            return null;
        }
        ObjectNode json = Library.writeNode((Node) schema);
        return new SchemaView(json, dialect, resourceUri, declarationPointer(source));
    }

    /** Every name/value pair present in a {@link MappedNode}, as a plain map, for callers that need to iterate without holding a live view. */
    static <T> List<String> itemNamesOf(MappedNode<T> mappedNode) {
        if (mappedNode == null) {
            return new ArrayList<String>();
        }
        return CollectionUtil.copyOfList(mappedNode.getItemNames());
    }

    /**
     * Synthesizes a JSON Schema-shaped node from a 2.0 non-body parameter (or a
     * nested {@code items} object), whose type/format/enum/range/length
     * keywords sit directly on the parameter rather than under a nested
     * {@code schema} -- there is no other unification point for these in the
     * 2.0 object model. Recurses into {@code items} for an array-typed
     * parameter. Every keyword is copied through {@link NodeUtil#getNodeProperty}
     * reflectively so the same code handles both an {@code OpenApi2xParameter}
     * and a nested {@code OpenApiItems} without a second, near-duplicate method.
     */
    static ObjectNode synthesizeLegacySchema(Node typedNode) {
        ObjectNode schema = io.apitomy.datamodels.models.util.JsonUtil.objectNode();
        copyIfPresent(typedNode, schema, "type");
        copyIfPresent(typedNode, schema, "format");
        copyIfPresent(typedNode, schema, "default");
        copyIfPresent(typedNode, schema, "enum");
        copyIfPresent(typedNode, schema, "maximum");
        copyIfPresent(typedNode, schema, "exclusiveMaximum");
        copyIfPresent(typedNode, schema, "minimum");
        copyIfPresent(typedNode, schema, "exclusiveMinimum");
        copyIfPresent(typedNode, schema, "maxLength");
        copyIfPresent(typedNode, schema, "minLength");
        copyIfPresent(typedNode, schema, "pattern");
        copyIfPresent(typedNode, schema, "maxItems");
        copyIfPresent(typedNode, schema, "minItems");
        copyIfPresent(typedNode, schema, "uniqueItems");
        copyIfPresent(typedNode, schema, "multipleOf");
        Object items = NodeUtil.getNodeProperty(typedNode, "items");
        if (items instanceof Node) {
            io.apitomy.datamodels.models.util.JsonUtil.setProperty(schema, "items", synthesizeLegacySchema((Node) items));
        }
        return schema;
    }

    /**
     * Copies keyword {@code keyword} from {@code typedNode} (via reflective
     * {@link NodeUtil#getNodeProperty}) into {@code schema}, if present.
     * <p>
     * A legacy numeric field ({@code maximum}/{@code minimum}/{@code multipleOf})
     * is already read into a plain Java {@code Number} by the generated model
     * reader itself (losing exactness for any non-integer, non-long value at
     * that point, via {@code asDouble()}); this method does not introduce
     * further loss for the common (int/long-fitting) case, but a legacy
     * parameter bound whose magnitude exceeds the safe-double range is not
     * round-tripped exactly here -- an existing gap in the shared
     * {@code JsonUtil.toJsonNode(Object)} helper (no {@code long}-preserving
     * overload), not one introduced by this synthesis.
     */
    private static void copyIfPresent(Node typedNode, ObjectNode schema, String keyword) {
        Object value = NodeUtil.getNodeProperty(typedNode, keyword);
        if (value == null) {
            return;
        }
        JsonNode jsonValue;
        if (value instanceof List) {
            jsonValue = io.apitomy.datamodels.models.util.JsonUtil.toArrayNode((List<?>) value);
        } else {
            jsonValue = io.apitomy.datamodels.models.util.JsonUtil.toJsonNode(value);
        }
        if (jsonValue == null) {
            return;
        }
        io.apitomy.datamodels.models.util.JsonUtil.setProperty(schema, keyword, jsonValue);
    }
}
