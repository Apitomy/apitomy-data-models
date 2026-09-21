package io.apitomy.datamodels.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads a bundled resource as text.
 * <p>
 * This exists as a single indirection because the two targets have nothing in
 * common here: on the JVM a resource is read from the classpath, while the
 * TypeScript build has no classpath at all. The transpiler substitutes calls to
 * {@link #readResourceAsString(String)} with the resource content inlined
 * directly into the generated TypeScript (see {@code JacksonAdapter}), so the
 * Java body below never runs in the TS target and no file access happens there.
 * <p>
 * The method is marked {@code @Erased} so the body below — which is pure JVM I/O —
 * is not emitted at all in the TypeScript target. The class itself still transpiles,
 * so importing it stays valid there; it simply has no members.
 * <p>
 * Consequently the argument must be a <b>string literal</b> at the call site —
 * the substitution reads the file at transpile time and cannot evaluate a
 * computed path. Paths are absolute, rooted at the resources directory.
 */
public final class ResourceUtil {

    private ResourceUtil() {
    }

    /**
     * The contents of a bundled resource, decoded as UTF-8.
     *
     * @param resourcePath absolute resource path, e.g.
     *        {@code "/io/apitomy/datamodels/jsonschema/compat/difftype-help.json"};
     *        must be a string literal
     * @return the resource content
     * @throws IllegalStateException if the resource is missing or unreadable
     */
    @jsweet.lang.Erased
    public static String readResourceAsString(String resourcePath) {
        try (InputStream stream = ResourceUtil.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource: " + resourcePath);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = stream.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + resourcePath, e);
        }
    }
}
