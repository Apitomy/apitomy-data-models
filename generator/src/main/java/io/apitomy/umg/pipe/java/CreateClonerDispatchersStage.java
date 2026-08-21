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

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.ClonerMethod;

/**
 * Creates a cloner dispatcher for each specification version. A cloner dispatcher is a
 * visitor that, when a node is visited, creates an empty clone and delegates to the
 * cloner's entity-specific clone method. This allows {@code cloneNode(Node)} to dispatch
 * via the visitor pattern without instanceof checks.
 */
public class CreateClonerDispatchersStage extends AbstractVisitorStage {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            createClonerDispatcher(specVer);
        });
    }

    private void createClonerDispatcher(SpecificationVersion specVer) {
        VisitorModel visitor = getState().getConceptIndex().lookupVisitor(specVer.getNamespace());

        String clonerPackageName = getClonerPackageName(specVer);
        String clonerClassName = getClonerClassName(specVer);
        String clonerFQN = clonerPackageName + "." + clonerClassName;

        String dispatcherPackageName = clonerPackageName;
        String dispatcherClassName = clonerClassName + "Dispatcher";

        debug("Creating cloner dispatcher: " + dispatcherClassName);

        JavaClassSource dispatcherSource = Roaster.create(JavaClassSource.class)
                .setPackage(dispatcherPackageName)
                .setName(dispatcherClassName)
                .setPublic();

        // Determine which visitors this dispatcher implements
        Set<VisitorModel> visitorsToImplement = Collections.singleton(visitor);

        List<MethodSource<?>> methodsToImplement = new LinkedList<>();
        Set<String> methodNames = new HashSet<>();
        for (VisitorModel visitorToImplement : visitorsToImplement) {
            JavaInterfaceSource vtiInterface = lookupJavaVisitor(visitorToImplement);
            if (vtiInterface == null) {
                warn("Visitor interface not found: " + visitorToImplement);
            }

            dispatcherSource.addImport(vtiInterface);
            dispatcherSource.addInterface(vtiInterface.getName());

            List<MethodSource<?>> allMethods = getVisitMethodsOnly(visitorToImplement);
            allMethods.forEach(method -> {
                if (!methodNames.contains(method.getName())) {
                    methodsToImplement.add(method);
                    methodNames.add(method.getName());
                }
            });
        }

        // Import the Node interface and the cloner class
        String nodeInterfaceFQN = getNodeEntityInterfaceFQN();
        JavaInterfaceSource nodeInterface = getState().getJavaIndex().lookupInterface(nodeInterfaceFQN);
        dispatcherSource.addImport(nodeInterface);
        dispatcherSource.addImport(clonerFQN);

        // Implement the ModelCloner interface
        JavaInterfaceSource modelClonerSource = getState().getJavaIndex().lookupInterface(getModelClonerInterfaceFQN());
        dispatcherSource.addImport(modelClonerSource);
        dispatcherSource.addInterface(modelClonerSource.getName());

        // Fields
        dispatcherSource.addField().setName("cloner").setType(clonerClassName).setPrivate().setFinal(true);
        dispatcherSource.addField().setName("clonedNode").setType(nodeInterface.getName()).setPrivate();

        // Constructor
        MethodSource<JavaClassSource> ctor = dispatcherSource.addMethod().setPublic().setConstructor(true);
        ctor.addParameter(clonerClassName, "cloner");
        BodyBuilder ctorBody = new BodyBuilder();
        ctorBody.append("this.cloner = cloner;");
        ctor.setBody(ctorBody.toString());

        // cloneNode(Node) method — the public entry point
        MethodSource<JavaClassSource> cloneNodeMethod = dispatcherSource.addMethod()
                .setName("cloneNode")
                .setReturnType(nodeInterface.getName())
                .setPublic();
        cloneNodeMethod.addParameter(nodeInterface.getName(), "source");
        cloneNodeMethod.addAnnotation(Override.class);

        BodyBuilder cloneNodeBody = new BodyBuilder();
        cloneNodeBody.append("this.clonedNode = source.emptyClone();");
        cloneNodeBody.append("source.accept(this);");
        cloneNodeBody.append("return this.clonedNode;");
        cloneNodeMethod.setBody(cloneNodeBody.toString());

        // Implement each visit method
        methodsToImplement.forEach(methodToImplement -> {
            MethodSource<JavaClassSource> methodSource = dispatcherSource.addMethod()
                    .setName(methodToImplement.getName())
                    .setReturnTypeVoid()
                    .setPublic();
            ParameterSource<?> param = methodToImplement.getParameters().get(0);
            dispatcherSource.addImport(param.getType());
            methodSource.addParameter(param.getType().getSimpleName(), param.getName());
            methodSource.addAnnotation(Override.class);

            String entityName = methodToImplement.getName().replace("visit", "");
            JavaInterfaceSource javaEntityType = resolveJavaEntity(specVer.getNamespace(), entityName);
            dispatcherSource.addImport(javaEntityType);

            BodyBuilder body = new BodyBuilder();
            body.addContext("cloneMethodName", new ClonerMethod(entityName).getName());
            body.addContext("javaEntityType", javaEntityType.getName());
            body.append("this.cloner.${cloneMethodName}((${javaEntityType}) node, (${javaEntityType}) this.clonedNode);");
            methodSource.setBody(body.toString());
        });

        addAfterVisitImplementations(dispatcherSource, visitor);
        getState().getJavaIndex().index(dispatcherSource);
    }

}
