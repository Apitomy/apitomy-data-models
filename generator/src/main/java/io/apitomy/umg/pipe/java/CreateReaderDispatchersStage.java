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
 * Creates a reader dispatcher reader generated.  A reader dispatcher is just a visitor that knows
 * how to dispatch to an appropriate reader implementation.
 * @author eric.wittmann@gmail.com
 */
public class CreateReaderDispatchersStage extends AbstractVisitorStage {

    @Override
    protected void doProcess() {
        // Create a visitor adapter for each spec version visitor
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            createReaderDispatcher(specVer);
        });
    }

    private void createReaderDispatcher(SpecificationVersion specVer) {
        VisitorModel visitor = getState().getConceptIndex().lookupVisitor(specVer.getNamespace());

        String readerPackageName = getReaderPackageName(specVer);
        String readerClassName = getReaderClassName(specVer);
        String readerFQN = readerPackageName + "." + readerClassName;

        String dispatcherPackageName = readerPackageName;
        String dispatcherClassName = readerClassName + "Dispatcher";

        debug("Creating reader dispatcher: " + dispatcherClassName);

        // Create the reader dispatcher class
        JavaClassSource readerDispatcherSource = Roaster.create(JavaClassSource.class)
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

            readerDispatcherSource.addImport(vtiInterface);
            readerDispatcherSource.addInterface(vtiInterface.getName());

            // Add all methods to the list (but avoid duplicates).
            List<MethodSource<?>> allMethods = getAllMethodsForVisitorInterface(visitorToImplement);
            allMethods.forEach(method -> {
                if (!methodNames.contains(method.getName())) {
                    methodsToImplement.add(method);
                    methodNames.add(method.getName());
                }
            });
        }

        // Add imports for the JSON and the Reader
        readerDispatcherSource.addImport(ObjectNode.class);
        readerDispatcherSource.addImport(readerFQN);

        // Create fields for the JSON to read from and the Reader to dispatch to
        readerDispatcherSource.addField().setName("json").setType("ObjectNode").setPrivate().setFinal(true);
        readerDispatcherSource.addField().setName("reader").setType(readerClassName).setPrivate().setFinal(true);

        // Create a constructor for the dispatcher.
        MethodSource<JavaClassSource> ctor = readerDispatcherSource.addMethod().setPublic().setConstructor(true);
        ctor.addParameter("ObjectNode", "json");
        ctor.addParameter(readerClassName, "reader");
        BodyBuilder ctorBody = new BodyBuilder();
        ctorBody.append("this.json = json;");
        ctorBody.append("this.reader = reader;");
        ctor.setBody(ctorBody.toString());

        // Now create an implementation for each visit method.
        methodsToImplement.forEach(methodToImplement -> {
            MethodSource<JavaClassSource> methodSource = readerDispatcherSource.addMethod()
                    .setName(methodToImplement.getName())
                    .setReturnTypeVoid()
                    .setPublic();
            // We know each visit method will have a single parameter.
            ParameterSource<?> param = methodToImplement.getParameters().get(0);
            readerDispatcherSource.addImport(param.getType());
            methodSource.addParameter(param.getType().getSimpleName(), param.getName());
            methodSource.addAnnotation(Override.class);

            // Figure out the entity name from the "visit" method name (absent another way).
            String entityName = methodToImplement.getName().replace("visit", "");
            JavaInterfaceSource javaEntityType = resolveJavaEntity(specVer.getNamespace(), entityName);
            readerDispatcherSource.addImport(javaEntityType);

            // Create the method body.
            BodyBuilder body = new BodyBuilder();
            body.addContext("readMethodName", methodToImplement.getName().replace("visit", "read"));
            body.addContext("javaEntityType", javaEntityType.getName());
            body.append("this.reader.${readMethodName}(this.json, (${javaEntityType}) node);");
            methodSource.setBody(body.toString());
        });

        // Index the new class
        getState().getJavaIndex().index(readerDispatcherSource);
    }

}
