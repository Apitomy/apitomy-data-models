package io.apitomy.umg.pipe.concept;

import java.util.Arrays;
import java.util.List;

import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.pipe.AbstractStage;

public class CreateNamespaceModelsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        info("-- Creating Namespace Models --");
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            String ns = specVersion.getNamespace();
            this.makeNamespaces(ns);
        });
    }

    /**
     * Creates the namespace (if it does not yet exist) and all parent namespaces
     * (if necessary).
     */
    private NamespaceModel makeNamespaces(String namespaceName) {
        List<String> nsComponents = Arrays.asList(namespaceName.split("\\."));
        String currentNs = null;
        NamespaceModel lastModel = null;
        for (String nsComponent : nsComponents) {
            currentNs = (currentNs == null) ? nsComponent : currentNs + "." + nsComponent;
            final NamespaceModel parentModel = lastModel;
            NamespaceModel nsModel = getState().getConceptIndex().lookupNamespace(currentNs, (ns) -> {
                NamespaceModel rval = NamespaceModel.builder().name(nsComponent).parent(parentModel).build();
                if (parentModel != null) {
                    parentModel.getChildren().put(nsComponent, rval);
                }
                info("Created namespace model: %s", rval.fullName());
                return rval;
            });
            lastModel = nsModel;
        }

        return lastModel;
    }

}
