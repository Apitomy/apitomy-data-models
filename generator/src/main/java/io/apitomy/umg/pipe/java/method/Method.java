package io.apitomy.umg.pipe.java.method;

/**
 * Represents a generated Java method. All method classes implement this interface
 * to expose their method name via {@link #getName()}.
 */
public interface Method extends CanAddImports {

    /**
     * Returns the name of the generated method (e.g. "getTitle", "readDocument").
     */
    String getName();

}
