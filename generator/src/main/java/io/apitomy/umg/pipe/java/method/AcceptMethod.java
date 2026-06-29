package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;

/**
 * Generates the {@code accept(Visitor)} method on entity impl classes.
 * The method casts the visitor to the namespace-specific visitor interface
 * and calls the appropriate visit method for the entity.
 */
public class AcceptMethod implements Method {

    private final String entityName;
    private final JavaInterfaceSource javaVisitor;
    private final JavaInterfaceSource javaRootVisitorInterface;

    public AcceptMethod(String entityName, JavaInterfaceSource javaVisitor,
                        JavaInterfaceSource javaRootVisitorInterface) {
        this.entityName = entityName;
        this.javaVisitor = javaVisitor;
        this.javaRootVisitorInterface = javaRootVisitorInterface;
    }

    @Override
    public String getName() {
        return "accept";
    }

    @Override
    public void writeTo(JavaSource<?> target) {
        target.addImport(javaVisitor);
        target.addImport(javaRootVisitorInterface);

        MethodSource<?> method = ((MethodHolderSource<?>) target).addMethod()
                .setPublic()
                .setName(getName())
                .setReturnTypeVoid();
        method.addParameter(javaRootVisitorInterface.getName(), "visitor");
        method.addAnnotation(Override.class);

        BodyBuilder body = new BodyBuilder();
        body.addContext("visitorClass", javaVisitor.getName());
        body.addContext("entityType", entityName);
        body.append("${visitorClass} viz = (${visitorClass}) visitor;");
        body.append("viz.visit${entityType}(this);");
        method.setBody(body.toString());
    }

    @Override
    public void addImportsTo(JavaSource<?> source) {
        source.addImport(javaVisitor);
        source.addImport(javaRootVisitorInterface);
    }

}
