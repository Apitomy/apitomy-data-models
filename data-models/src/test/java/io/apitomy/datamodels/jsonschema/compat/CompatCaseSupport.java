package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apitomy.datamodels.jsonschema.ref.AnchorFragmentResolver;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefDereferencer;
import io.apitomy.datamodels.jsonschema.ref.JsonSchemaRefResolverChain;
import io.apitomy.datamodels.jsonschema.ref.MapResourceResolver;
import io.apitomy.datamodels.jsonschema.ref.PointerFragmentResolver;
import io.apitomy.datamodels.jsonschema.ref.UnresolvableRefStrategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared helpers for reading the compatibility example catalog and building the
 * per-case checker from a case's {@code config}/{@code externalRefs}. Used by both
 * {@link JsonSchemaCompatibilityTest} and the {@link ExampleCatalogSeeder} maintenance tool.
 */
final class CompatCaseSupport {

    static final String CATALOG_RESOURCE = "compatibility-test-data.json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, JsonSchemaCompatibilityChecker> checkerCache = new HashMap<>();

    /** Reads and parses the example catalog from the classpath. */
    static JsonNode readCatalog() {
        try (var is = CompatCaseSupport.class.getResourceAsStream(CATALOG_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException("Catalog resource not found: " + CATALOG_RESOURCE);
            }
            return MAPPER.readTree(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Renders a schema node as a JSON string, or {@code null} for a boolean schema
     * (not supported as a top-level document yet, so such cases are skipped).
     */
    static String schemaString(JsonNode node) {
        if (node == null || node.isBoolean()) {
            return null;
        }
        return node.toString();
    }

    /** Builds (and caches) the checker configured for a case's {@code config}/{@code externalRefs}. */
    JsonSchemaCompatibilityChecker checkerFor(JsonNode configNode, JsonNode externalRefsNode) {
        String cacheKey = (configNode != null ? configNode.toString() : "default")
                + (externalRefsNode != null ? externalRefsNode.toString() : "");

        return checkerCache.computeIfAbsent(cacheKey, k -> {
            var checkerBuilder = JsonSchemaCompatibilityChecker.builder();

            if (configNode != null && configNode.has("allowCrossVersionChecking")) {
                checkerBuilder.allowCrossVersionChecking(
                        configNode.get("allowCrossVersionChecking").asBoolean());
            } else {
                checkerBuilder.allowCrossVersionChecking(true); // default for tests
            }

            var derefBuilder = JsonSchemaRefDereferencer.builder();

            if (configNode != null && configNode.has("onUnresolvableRef")) {
                derefBuilder.onUnresolvableRef(
                        UnresolvableRefStrategy.valueOf(configNode.get("onUnresolvableRef").asText()));
            }

            if (externalRefsNode != null && externalRefsNode.isObject()) {
                var mapBuilder = MapResourceResolver.builder();
                externalRefsNode.fields().forEachRemaining(entry ->
                        mapBuilder.addSchema(entry.getKey(), entry.getValue().toString()));

                derefBuilder.refResolver(JsonSchemaRefResolverChain.builder()
                        .addFragmentResolver(new PointerFragmentResolver())
                        .addFragmentResolver(new AnchorFragmentResolver())
                        .addResourceResolver(mapBuilder.build())
                        .build());
            }

            checkerBuilder.dereferencer(derefBuilder.build());
            return checkerBuilder.build();
        });
    }
}
