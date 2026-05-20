package io.apitomy.umg.pipe.java;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;

import io.apitomy.umg.models.concept.VisitorModel;

/**
 * Creates "all node" visitors for every spec.  This
 * @author eric.wittmann@gmail.com
 */
public class CreateAllNodeVisitorStage extends AbstractVisitorStage {

    @Override
    protected void doProcess() {
        // Create a combined visitor for the root
        VisitorModel rootVisitor = getState().getConceptIndex().lookupVisitor(getState().getConfig().getRootNamespace());
        if (rootVisitor == null) {
            warn("Root visitor not found!");
        } else {
            createAllNodeVisitor(rootVisitor);
        }
    }

    private void createAllNodeVisitor(VisitorModel visitor) {
        String allNodeVisitorPackageName = getVisitorInterfacePackageName(visitor);
        String allNodeVisitorName = "AllNodeVisitor";

        debug("Creating all node visitor.");

        // Create the all node visitor class
        JavaClassSource allNodeVisitorSource = Roaster.create(JavaClassSource.class)
                .setPackage(allNodeVisitorPackageName)
                .setName(allNodeVisitorName)
                .setPublic()
                .setAbstract(true);

        // Add "Node" as an import
        String nodeEntityInterfaceFQN = getNodeEntityInterfaceFQN();
        JavaInterfaceSource nodeEntitySource = lookupJavaEntity(nodeEntityInterfaceFQN);
        allNodeVisitorSource.addImport(nodeEntitySource);

        // Implement the "CombinedVisitor" interface
        String combinedVisitorFQN = allNodeVisitorPackageName + ".CombinedVisitor";
        JavaInterfaceSource combinedVisitorSource = getState().getJavaIndex().lookupInterface(combinedVisitorFQN);
        allNodeVisitorSource.addImport(combinedVisitorSource);
        allNodeVisitorSource.addInterface(combinedVisitorSource);

        // Get the list of visitors that make up the combined visitor.  We need to implement
        // them all.
        Set<VisitorModel> visitorsToImplement = findDescendantVisitors(visitor);

        // For each visitor, lookup its Java interface, add it to the list of interfaces implemented,
        // and then collect all methods in the interface (avoiding potential duplicates).
        List<MethodSource<?>> methodsToImplement = new LinkedList<MethodSource<?>>();
        Set<String> methodNames = new HashSet<>();
        for (VisitorModel visitorToImplement : visitorsToImplement) {
            JavaInterfaceSource vtiInterface = lookupJavaVisitor(visitorToImplement);
            if (vtiInterface == null) {
                warn("Visitor interface not found: " + visitorToImplement);
            }

            // Add all methods to the list (but avoid duplicates).
            List<MethodSource<?>> allMethods = getAllMethodsForVisitorInterface(visitorToImplement);
            allMethods.forEach(method -> {
                if (!methodNames.contains(method.getName())) {
                    methodsToImplement.add(method);
                    methodNames.add(method.getName());
                }
            });
        }

        // Create the "visitNode" method.
        MethodSource<JavaClassSource> visitNodeMethod = allNodeVisitorSource.addMethod().setName("visitNode")
                .setReturnTypeVoid().setProtected().setAbstract(true);
        visitNodeMethod.addParameter("Node", "node");

        // Now create an implementation for each visit method.
        methodsToImplement.forEach(method -> {
            MethodSource<JavaClassSource> methodSource = allNodeVisitorSource.addMethod()
                    .setName(method.getName())
                    .setReturnTypeVoid()
                    .setPublic();
            // We know each visit method will have a single parameter.
            ParameterSource<?> param = method.getParameters().get(0);
            allNodeVisitorSource.addImport(param.getType());
            methodSource.addParameter(param.getType().getSimpleName(), param.getName());
            methodSource.addAnnotation(Override.class);
            methodSource.setBody("this.visitNode(node);");
        });

        // Index the new class
        getState().getJavaIndex().index(allNodeVisitorSource);
    }

}
