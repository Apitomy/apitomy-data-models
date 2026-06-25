package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaSource;

/**
 * Interface for objects that can contribute import statements to a Java source file.
 */
public interface CanAddImports {

    void addImportsTo(JavaSource<?> source);

}
