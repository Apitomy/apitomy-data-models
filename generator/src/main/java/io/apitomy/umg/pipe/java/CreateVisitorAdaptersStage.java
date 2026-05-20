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

import io.apitomy.umg.models.concept.VisitorModel;

/**
 * Creates an adapter for each visitor interface.  An adapter is just a class that implements
 * the visitor interface with empty implementations for each visit method.
 * @author eric.wittmann@gmail.com
 */
public class CreateVisitorAdaptersStage extends AbstractVisitorStage {

    private static final String TYPE_NORMAL = "normal";
    private static final String TYPE_COMBINED = "combined";

    @Override
    protected void doProcess() {
        // Create a visitor adapter for each spec version visitor
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            VisitorModel visitor = getState().getConceptIndex().lookupVisitor(specVer.getNamespace());
            createVisitorAdapter(visitor, TYPE_NORMAL);
        });
        // Create a visitor adapter for each specification (combined adapter)
        getState().getSpecifications().forEach(spec -> {
            VisitorModel visitor = getState().getConceptIndex().lookupVisitor(spec.getNamespace());
            if (visitor != null) {
                createVisitorAdapter(visitor, TYPE_COMBINED);
            }
        });
        // Create a visitor adapter at the root (combined adapter)
        VisitorModel rootVisitor = getState().getConceptIndex().lookupVisitor(getState().getConfig().getRootNamespace());
        if (rootVisitor == null) {
            warn("Root visitor not found!");
        } else {
            createVisitorAdapter(rootVisitor, TYPE_COMBINED);
        }
    }

    private void createVisitorAdapter(VisitorModel visitor, String type) {
        String visitorPackageName = getVisitorInterfacePackageName(visitor);
        String visitorPrefix = getVisitorInterfacePrefix(visitor);
        if (type == TYPE_COMBINED) {
            visitorPrefix = "Combined" + visitorPrefix;
        }
        String visitorInterfaceName = visitorPrefix + "Visitor";
        String visitorAdapterName = visitorInterfaceName + "Adapter";
        debug("Creating visitor adapter: " + visitorAdapterName);

        // Create the visitor adapter class
        JavaClassSource visitorAdapterSource = Roaster.create(JavaClassSource.class)
                .setPackage(visitorPackageName)
                .setName(visitorAdapterName)
                .setPublic();

        // Determine which visitors this adapter is implementing
        Set<VisitorModel> visitorsToImplement;
        if (type == TYPE_NORMAL) {
            visitorsToImplement = Collections.singleton(visitor);
        } else {
            visitorsToImplement = findDescendantVisitors(visitor);
        }

        // For each visitor, lookup its Java interface, add it to the list of interfaces implemented,
        // and then collect all methods in the interface (avoiding potential duplicates).
        List<MethodSource<?>> methodsToImplement = new LinkedList<MethodSource<?>>();
        Set<String> methodNames = new HashSet<>();
        for (VisitorModel visitorToImplement : visitorsToImplement) {
            JavaInterfaceSource vtiInterface = lookupJavaVisitor(visitorToImplement);
            if (vtiInterface == null) {
                warn("Visitor interface not found: " + visitorToImplement);
            }

            visitorAdapterSource.addImport(vtiInterface);
            visitorAdapterSource.addInterface(vtiInterface);

            // Add all methods to the list (but avoid duplicates).
            List<MethodSource<?>> allMethods = getAllMethodsForVisitorInterface(visitorToImplement);
            allMethods.forEach(method -> {
                if (!methodNames.contains(method.getName())) {
                    methodsToImplement.add(method);
                    methodNames.add(method.getName());
                }
            });
        }

        // Now create an empty implementation for each visit method.
        methodsToImplement.forEach(method -> {
            MethodSource<JavaClassSource> methodSource = visitorAdapterSource.addMethod()
                    .setName(method.getName())
                    .setReturnTypeVoid()
                    .setPublic();
            // We know each visit method will have a single parameter.
            ParameterSource<?> param = method.getParameters().get(0);
            visitorAdapterSource.addImport(param.getType());
            methodSource.addParameter(param.getType().getSimpleName(), param.getName());
            methodSource.addAnnotation(Override.class);
            methodSource.setBody("");
        });

        // Index the new class
        getState().getJavaIndex().index(visitorAdapterSource);
    }

}
