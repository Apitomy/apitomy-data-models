package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.models.util.JsonUtil;
import io.apitomy.datamodels.util.ResourceUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Iterator;

/**
 * Lazily-loaded, cached store of long-form, {@code --explain}-style help text for {@link DiffType}
 * constants.
 *
 * <p>Help text is optional and curated: only a subset of diff types carry an entry. The manifest
 * lives at {@code difftype-help.json} beside this class and uses the envelope
 * {@code {"version":1,"help":{"<DiffType name>":[<markdown lines>]}}}, where each value is an array
 * of Markdown lines joined with {@code \n}.
 *
 * <p>This class is deliberately kept separate from the {@link DiffType} enum so the enum stays free
 * of Jackson and resource-loading concerns. The map is built once, on first access, and cached.
 * Access is guarded by {@code synchronized} — help lookups are rare (only when a caller asks for it),
 * so the coarse lock costs nothing in practice.
 */
final class DiffTypeHelp {

    // Kept for error messages. The loader below must repeat the path as a literal,
    // because the transpiler inlines the resource at the call site and cannot
    // resolve a constant reference.
    private static final String RESOURCE = "/io/apitomy/datamodels/jsonschema/compat/difftype-help.json";

    private static Map<String, String> help;

    private DiffTypeHelp() {
    }

    /**
     * Returns the curated help text for the given diff-type name, if one exists.
     *
     * @param diffTypeName the {@link DiffType#name()} of the constant
     * @return the joined Markdown help, or {@link Optional#empty()} if none is curated
     */
    static synchronized Optional<String> get(String diffTypeName) {
        if (help == null) {
            help = load();
        }
        return Optional.ofNullable(help.get(diffTypeName));
    }

    private static Map<String, String> load() {
        JsonNode root = JsonUtil.parseJSON(ResourceUtil.readResourceAsString(
                "/io/apitomy/datamodels/jsonschema/compat/difftype-help.json"));
        JsonNode helpNode = root.get("help");
        if (helpNode == null || !helpNode.isObject()) {
            throw new IllegalStateException(
                    "Help manifest is missing a 'help' object: " + RESOURCE);
        }
        Map<String, String> result = new LinkedHashMap<>();
        // Iterated by name rather than by entry: Map.Entry has no transpiled equivalent.
        Iterator<String> fieldNames = helpNode.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            // Fail fast on typos: every key must name a real DiffType constant.
            DiffType.valueOf(name);
            result.put(name, joinLines(name, helpNode.get(name)));
        }
        return result;
    }

    private static String joinLines(String name, JsonNode value) {
        if (value.isTextual()) {
            return value.asText();
        }
        if (!value.isArray()) {
            throw new IllegalStateException(
                    "Help entry '" + name + "' must be a string or array of strings, was: "
                            + value.getNodeType());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(value.get(i).asText());
        }
        return sb.toString();
    }
}
