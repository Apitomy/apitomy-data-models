package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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

    private static final String RESOURCE = "difftype-help.json";

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
        var mapper = new ObjectMapper();
        try (InputStream in = DiffTypeHelp.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing help manifest resource: " + RESOURCE);
            }
            JsonNode root = mapper.readTree(in);
            JsonNode helpNode = root.get("help");
            if (helpNode == null || !helpNode.isObject()) {
                throw new IllegalStateException(
                        "Help manifest is missing a 'help' object: " + RESOURCE);
            }
            Map<String, String> result = new LinkedHashMap<>();
            var fields = helpNode.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                String name = entry.getKey();
                // Fail fast on typos: every key must name a real DiffType constant.
                DiffType.valueOf(name);
                result.put(name, joinLines(name, entry.getValue()));
            }
            return Collections.unmodifiableMap(result);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read help manifest: " + RESOURCE, e);
        }
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
        var sb = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(value.get(i).asText());
        }
        return sb.toString();
    }
}
