package io.apitomy.umg.pipe.java;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;

/**
 * Creates a writer dispatcher writer generated.  A writer dispatcher is just a visitor that knows
 * how to dispatch to an appropriate writer implementation.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateWriterDispatchersStage extends AbstractVisitorStage {

    @Override
    protected void doProcess() {
        // Create a visitor adapter for each spec version visitor
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            createWriterDispatcher(specVer);
        });
    }

    private void createWriterDispatcher(SpecificationVersion specVer) {
        VisitorModel visitor = getState().getConceptIndex().lookupVisitor(specVer.getNamespace());

        String writerPackageName = getWriterPackageName(specVer);
        String writerClassName = getWriterClassName(specVer);
        String writerFQN = writerPackageName + "." + writerClassName;

        String dispatcherPackageName = writerPackageName;
        String dispatcherClassName = writerClassName + "Dispatcher";

        debug("Creating writer dispatcher: " + dispatcherClassName);

        // Create the writer dispatcher class
        JavaClassSource writerDispatcherSource = Roaster.create(JavaClassSource.class)
                .setPackage(dispatcherPackageName)
                .setName(dispatcherClassName)
                .setPublic();

        // Determine which visitors this adapter is implementing
        Set<VisitorModel> visitorsToImplement = Collections.singleton(visitor);

        // For each visitor, lookup its Java interface, add it to the list of interfaces implemented,
        // and then collect all methods in the interface (avoiding potential duplicates).
        List<MethodSource<?>> methodsToImplement = new LinkedList<MethodSource<?>>();
        Set<String> methodNames = new HashSet<>();
        for (VisitorModel visitorToImplement : visitorsToImplement) {
            JavaInterfaceSource vtiInterface = lookupJavaVisitor(visitorToImplement);
            if (vtiInterface == null) {
                warn("Visitor interface not found: " + visitorToImplement);
            }

            writerDispatcherSource.addImport(vtiInterface);
            writerDispatcherSource.addInterface(vtiInterface.getName());

            // Add all methods to the list (but avoid duplicates).
            List<MethodSource<?>> allMethods = getAllMethodsForVisitorInterface(visitorToImplement);
            allMethods.forEach(method -> {
                if (!methodNames.contains(method.getName())) {
                    methodsToImplement.add(method);
                    methodNames.add(method.getName());
                }
            });
        }

        // Add imports for the JSON and the Writer
        writerDispatcherSource.addImport(ObjectNode.class);
        writerDispatcherSource.addImport(writerFQN);

        // Create fields for the JSON to write from and the Writer to dispatch to
        writerDispatcherSource.addField().setName("json").setType("ObjectNode").setPrivate().setFinal(true);
        writerDispatcherSource.addField().setName("writer").setType(writerClassName).setPrivate().setFinal(true);

        // Create a constructor for the dispatcher.
        MethodSource<JavaClassSource> ctor = writerDispatcherSource.addMethod().setPublic().setConstructor(true);
        ctor.addParameter("ObjectNode", "json");
        ctor.addParameter(writerClassName, "writer");
        BodyBuilder ctorBody = new BodyBuilder();
        ctorBody.append("this.json = json;");
        ctorBody.append("this.writer = writer;");
        ctor.setBody(ctorBody.toString());

        // Now create an implementation for each visit method.
        methodsToImplement.forEach(methodToImplement -> {
            MethodSource<JavaClassSource> methodSource = writerDispatcherSource.addMethod()
                    .setName(methodToImplement.getName())
                    .setReturnTypeVoid()
                    .setPublic();
            // We know each visit method will have a single parameter.
            ParameterSource<?> param = methodToImplement.getParameters().get(0);
            writerDispatcherSource.addImport(param.getType());
            methodSource.addParameter(param.getType().getSimpleName(), param.getName());
            methodSource.addAnnotation(Override.class);

            // Figure out the entity name from the "visit" method name (absent another way).
            String entityName = methodToImplement.getName().replace("visit", "");
            JavaInterfaceSource javaEntityType = resolveJavaEntity(specVer.getNamespace(), entityName);
            writerDispatcherSource.addImport(javaEntityType);

            // Create the method body.
            BodyBuilder body = new BodyBuilder();
            body.addContext("writeMethodName", methodToImplement.getName().replace("visit", "write"));
            body.addContext("javaEntityType", javaEntityType.getName());
            body.append("this.writer.${writeMethodName}((${javaEntityType}) node, this.json);");
            methodSource.setBody(body.toString());
        });

        // Index the new class
        getState().getJavaIndex().index(writerDispatcherSource);
    }

}
