package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;

/**
 * Abstract base for reusable code fragments that can be appended to a {@link BodyBuilder}.
 */
public abstract class CodeBlock implements CanAddImports {

    /**
     * Appends this code block's statements to the given body builder.
     */
    abstract void appendTo(BodyBuilder body);

    @Override
    public void addImportsTo(JavaSource<?> source) {
        // Default: no imports needed
    }

}
