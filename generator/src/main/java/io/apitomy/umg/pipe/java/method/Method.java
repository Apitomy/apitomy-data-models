package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;

/**
 * Represents a generated Java method. All method classes implement this interface
 * to expose their method name via {@link #getName()} and can write themselves
 * (signature + body + imports) to a target Java class or interface via
 * {@link #writeTo(JavaSource)}.
 */
public interface Method extends CanAddImports {

    /**
     * Returns the name of the generated method (e.g. "getTitle", "readDocument").
     */
    String getName();

    /**
     * Creates the full method on the target class or interface — signature,
     * parameters, body (for impl classes), and imports.
     */
    void writeTo(JavaSource<?> target);

}
