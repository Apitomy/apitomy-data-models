package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

/**
 * Generates the {@code emptyClone()} method on entity impl classes.
 * Returns a new empty instance of the same implementation class, typed as
 * the {@code Node} interface.
 */
public class EmptyCloneMethod implements Method {

    private final String implClassName;
    private final JavaInterfaceSource nodeInterfaceSource;

    public EmptyCloneMethod(String implClassName, JavaInterfaceSource nodeInterfaceSource) {
        this.implClassName = implClassName;
        this.nodeInterfaceSource = nodeInterfaceSource;
    }

    @Override
    public String getName() {
        return "emptyClone";
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        target.addImport(nodeInterfaceSource);

        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setPublic()
                .setName(getName())
                .setReturnType(nodeInterfaceSource);
        method.addAnnotation(Override.class);

        BodyBuilder body = new BodyBuilder();
        body.addContext("implClassName", implClassName);
        body.append("return new ${implClassName}();");
        method.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        source.addImport(nodeInterfaceSource);
    }

}
