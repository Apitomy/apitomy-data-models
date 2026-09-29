package io.apitomy.datamodels.jsonschema.compat.containment;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.openapi.compat.CompatibilityPolicy;
import io.apitomy.datamodels.openapi.compat.HttpRole;

/**
 * A schema's obligations projected for one {@link HttpRole}: whether a
 * {@code readOnly}/{@code writeOnly} property is actually obligated to be
 * present, without deleting the property or its schema. A request never
 * requires a {@code readOnly} property (the server supplies or ignores it); a
 * response never requires a {@code writeOnly} property (the server never
 * sends it back).
 * <p>
 * This is a projection, not a rewrite: {@link #effectiveView()} returns the
 * same schema with only its {@code required} list adjusted (and only when an
 * adjustment is actually needed); {@code properties}, {@code additionalProperties},
 * {@code dependentRequired}/{@code dependentSchemas}, and every other keyword
 * are left exactly as they were, so later containment proofs still see the
 * full, real structure -- including a property's own type, in case it is
 * sent anyway.
 * <p>
 * Dialect matters here: OAS 2.0 and OAS 3.0 both document {@code readOnly}/
 * {@code writeOnly} as a directional <em>prohibition</em> (the property must
 * not appear on the wrong side at all), which is exactly what dropping it
 * from the effective {@code required} list reflects for the side it is
 * obligated on. OAS 3.1/3.2 (bare JSON Schema semantics) only document these
 * as non-normative, conventional annotations with no validation effect at
 * all -- there is no dialect-independent way to be sure a {@code writeOnly}
 * property is actually absent from a real response body, so a proof that
 * would only hold given the strict interpretation is Unknown for these
 * dialects rather than assumed.
 */
public final class DirectionalSchemaView {

    private final SchemaView schema;
    private final HttpRole httpRole;
    private final CompatibilityPolicy policy;

    private DirectionalSchemaView(SchemaView schema, HttpRole httpRole, CompatibilityPolicy policy) {
        this.schema = schema;
        this.httpRole = httpRole;
        this.policy = policy;
    }

    public static DirectionalSchemaView of(SchemaView schema, HttpRole httpRole, CompatibilityPolicy policy) {
        if (schema == null) {
            throw new IllegalArgumentException("schema must not be null");
        }
        if (httpRole == null) {
            throw new IllegalArgumentException("httpRole must not be null");
        }
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        return new DirectionalSchemaView(schema, httpRole, policy);
    }

    /** True under the strict (prohibition, not mere convention) readOnly/writeOnly dialects: OAS 2.0 and OAS 3.0. */
    private boolean isStrictDialect() {
        return schema.getDialect() == SchemaDialect.OAS20 || schema.getDialect() == SchemaDialect.OAS30;
    }

    /** The annotation keyword that excuses a property from this role's obligations: `readOnly` for a request, `writeOnly` for a response. */
    private String excusingKeyword() {
        return httpRole == HttpRole.REQUEST ? "readOnly" : "writeOnly";
    }

    /**
     * True if enforcement of the excusing annotation for this role/dialect is
     * certain enough to drop an excused property from {@code required}. Always
     * true for a request ({@code readOnly}'s "response only" direction is
     * documented consistently everywhere, including bare JSON Schema's
     * conventional reading); for a response, only true under a strict dialect
     * -- see the class Javadoc on {@code writeOnly} response enforcement being
     * uncertain under OAS 3.1/3.2.
     */
    public boolean isEnforcementCertain() {
        if (httpRole == HttpRole.REQUEST) {
            return true;
        }
        return isStrictDialect();
    }

    /**
     * The schema with its {@code required} list projected for this role: a
     * name whose property is marked with the excusing annotation (directly,
     * or through any {@code allOf} branch) is dropped, when
     * {@link #isEnforcementCertain()}. Returns {@code schema} itself,
     * unchanged, if no adjustment applies or enforcement is uncertain.
     */
    public SchemaView effectiveView() {
        if (!schema.isObject() || !isEnforcementCertain()) {
            return schema;
        }
        List<String> required = ObjectContainment.requiredNames(schema);
        if (required.isEmpty()) {
            return schema;
        }
        String excusingKeyword = excusingKeyword();
        List<String> effectiveRequired = new ArrayList<String>();
        for (int i = 0; i < required.size(); i++) {
            String name = required.get(i);
            if (!isMarked(schema, name, excusingKeyword)) {
                effectiveRequired.add(name);
            }
        }
        if (effectiveRequired.size() == required.size()) {
            return schema;
        }
        ObjectNode result = JsonUtil.objectNode();
        ObjectNode source = JsonUtil.toObject(schema.getNode());
        List<String> keys = JsonUtil.keys(source);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if ("required".equals(key)) {
                continue;
            }
            JsonUtil.setProperty(result, key, JsonUtil.getProperty(source, key));
        }
        ArrayNode requiredArray = JsonUtil.arrayNode();
        for (int i = 0; i < effectiveRequired.size(); i++) {
            JsonUtil.addToArray(requiredArray, JsonUtil.toJsonNode(effectiveRequired.get(i)));
        }
        JsonUtil.setProperty(result, "required", requiredArray);
        return new SchemaView(result, schema.getDialect(), schema.getResourceUri(), schema.getPointer());
    }

    /**
     * True if property {@code name}'s own schema (found directly in
     * {@code properties}, or through any {@code allOf} branch) has
     * {@code annotationKeyword: true}.
     */
    private static boolean isMarked(SchemaView schema, String name, String annotationKeyword) {
        SchemaView properties = schema.childView("properties");
        if (properties != null) {
            SchemaView property = properties.childView(name);
            if (property != null && property.isObject() && isTrue(property, annotationKeyword)) {
                return true;
            }
        }
        JsonNode allOfNode = schema.getKeyword("allOf");
        if (allOfNode != null && JsonUtil.isArray(allOfNode)) {
            List<JsonNode> branches = JsonUtil.toList(allOfNode);
            for (int i = 0; i < branches.size(); i++) {
                JsonNode branchNode = branches.get(i);
                if (!JsonUtil.isObject(branchNode)) {
                    continue;
                }
                SchemaView branch = new SchemaView(branchNode, schema.getDialect(), schema.getResourceUri(), schema.getPointer());
                if (isMarked(branch, name, annotationKeyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTrue(SchemaView schema, String keyword) {
        JsonNode node = schema.getKeyword(keyword);
        return node != null && JsonUtil.isBoolean(node) && JsonUtil.toBoolean(node).booleanValue();
    }
}
