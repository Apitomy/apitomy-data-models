package io.apitomy.umg.pipe.java;

import java.util.Set;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.VisitorModel;

/**
 * Creates combined visitor interfaces.  We create a combined visitor interface for the following:
 * <ul>
 *   <li>One for each specification</li>
 *   <li>One for the root</li>
 * </ul>
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateCombinedVisitorInterfacesStage extends AbstractVisitorStage {

    @Override
    protected void doProcess() {
        // Create a combined visitor for each specification
        getState().getSpecIndex().getAllSpecifications().forEach(spec -> {
            VisitorModel specVisitor = getState().getConceptIndex().lookupVisitor(spec.getNamespace());
            if (specVisitor == null) {
                warn("Visitor not found for specification: " + spec.getName());
            } else {
                createCombinedVisitor(specVisitor);
            }
        });

        // Create a combined visitor for the root
        VisitorModel rootVisitor = getState().getConceptIndex().lookupVisitor(getState().getConfig().getRootNamespace());
        if (rootVisitor == null) {
            warn("Root visitor not found!");
        } else {
            createCombinedVisitor(rootVisitor);
        }
    }

    private void createCombinedVisitor(VisitorModel visitor) {
        String visitorInterfaceName = getVisitorInterfaceName(visitor);
        String combinedVisitorInterfaceName = "Combined" + visitorInterfaceName;
        String combinedVisitorPackageName = getVisitorInterfacePackageName(visitor);

        // Create the combined visitor interface
        JavaInterfaceSource combinedVisitorInterfaceSource = Roaster.create(JavaInterfaceSource.class)
                .setPackage(combinedVisitorPackageName)
                .setName(combinedVisitorInterfaceName)
                .setPublic();

        // Extend all of the descendant visitor interfaces
        Set<VisitorModel> descendantVisitors = findDescendantVisitors(visitor);
        for (VisitorModel descendantVisitor : descendantVisitors) {
            String descendantVisitorFQCN = getVisitorInterfaceFullName(descendantVisitor);
            JavaInterfaceSource descendantVisitorInterfaceModel = getState().getJavaIndex().lookupInterface(descendantVisitorFQCN);
            if (descendantVisitorInterfaceModel == null) {
                warn("Could not find visitor java interface for: " + descendantVisitor);
            } else {
                combinedVisitorInterfaceSource.addImport(descendantVisitorInterfaceModel);
                combinedVisitorInterfaceSource.addInterface(descendantVisitorInterfaceModel);
            }
        }

        // Add the new combined visitor to the index.
        getState().getJavaIndex().index(combinedVisitorInterfaceSource);
    }
}
