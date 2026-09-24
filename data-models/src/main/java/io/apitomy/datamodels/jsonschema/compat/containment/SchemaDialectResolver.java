package io.apitomy.datamodels.jsonschema.compat.containment;

/**
 * Resolves a {@link SchemaDialect} from a {@code $schema} (or OAS 3.1+
 * {@code jsonSchemaDialect}) URI.
 * <p>
 * A plain utility class, not a static method on {@link SchemaDialect} itself:
 * JSweet transpiles an enum's own static methods onto a separate internal
 * wrapper class that is not reachable as {@code SchemaDialect.methodName(...)}
 * from TypeScript, so dialect resolution logic lives here instead.
 */
public final class SchemaDialectResolver {

    private SchemaDialectResolver() {
    }

    /**
     * The dialect named by a {@code $schema}/{@code jsonSchemaDialect} URI, or
     * {@link SchemaDialect#UNSUPPORTED} if the URI names a dialect this checker
     * does not recognize -- including any custom vocabulary requirement layered
     * onto a recognized base URI, since this checker cannot evaluate an unknown
     * required vocabulary's semantics regardless of the base draft it extends.
     *
     * @param uri the {@code $schema}/{@code jsonSchemaDialect} value, or
     *            {@code null} if the resource declares none (a caller supplies
     *            its own family default in that case; this method does not
     *            guess one)
     */
    public static SchemaDialect fromSchemaUri(String uri) {
        if (uri == null) {
            return null;
        }
        if (uri.indexOf("draft-04") >= 0) {
            return SchemaDialect.DRAFT4;
        }
        if (uri.indexOf("draft-06") >= 0) {
            return SchemaDialect.DRAFT6;
        }
        if (uri.indexOf("draft-07") >= 0) {
            return SchemaDialect.DRAFT7;
        }
        if (uri.indexOf("2019-09") >= 0) {
            return SchemaDialect.DRAFT2019_09;
        }
        if (uri.indexOf("2020-12") >= 0 && uri.indexOf("oas/3.1") < 0 && uri.indexOf("oas/3.2") < 0) {
            return SchemaDialect.DRAFT2020_12;
        }
        if (uri.indexOf("spec.openapi.org/oas/3.1") >= 0) {
            return SchemaDialect.OAS31;
        }
        if (uri.indexOf("spec.openapi.org/oas/3.2") >= 0) {
            return SchemaDialect.OAS32;
        }
        return SchemaDialect.UNSUPPORTED;
    }
}
