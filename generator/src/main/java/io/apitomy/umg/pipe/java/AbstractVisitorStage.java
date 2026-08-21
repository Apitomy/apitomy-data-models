package io.apitomy.umg.pipe.java;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.models.concept.SpecificationVersionId;
import io.apitomy.umg.models.concept.VisitorModel;

public abstract class AbstractVisitorStage extends AbstractJavaStage {

    /**
     * Finds all of the descendant visitors for the given visitor.  This walks down the
     * visitor hierarchy and finds all of the "leaf" visitor nodes in that tree.
     */
    protected Set<VisitorModel> findDescendantVisitors(VisitorModel visitor) {
        return getState().getConceptIndex().findVisitors(visitor.getNamespace().fullName()).stream().filter(v -> isVisitorForSpecVersion(v)).collect(Collectors.toSet());
    }

    /**
     * Returns ture if the given visitor is associated with a specification version.
     */
    private boolean isVisitorForSpecVersion(VisitorModel visitor) {
        SpecificationVersionId id = SpecificationVersionId.create(visitor.getNamespace().fullName());
        return getState().getSpecIndex().getSpecIndex().containsKey(id);
    }

    /**
     * Returns all of the methods defined for the visitor interface generated for the
     * given visitor model.  This walks up the visitor hierarchy, collecting all methods
     * defined on visitor interfaces.  It returns the full collection of methods (for
     * this visitor and all super-interfaces).
     */
    protected List<MethodSource<?>> getAllMethodsForVisitorInterface(VisitorModel visitor) {
        List<MethodSource<?>> methods = new LinkedList<>();
        while (visitor != null) {
            JavaInterfaceSource visitorInterface = lookupJavaVisitor(visitor);
            methods.addAll(visitorInterface.getMethods());
            visitor = visitor.getParent();
        }
        return methods;
    }

    protected List<MethodSource<?>> getVisitMethodsOnly(VisitorModel visitor) {
        return getAllMethodsForVisitorInterface(visitor).stream()
                .filter(m -> m.getName().startsWith("visit"))
                .collect(Collectors.toList());
    }

    protected List<MethodSource<?>> getAfterVisitMethodsOnly(VisitorModel visitor) {
        return getAllMethodsForVisitorInterface(visitor).stream()
                .filter(m -> m.getName().startsWith("afterVisit"))
                .collect(Collectors.toList());
    }

    protected void addAfterVisitImplementations(org.jboss.forge.roaster.model.source.JavaClassSource classSource,
                                                 VisitorModel visitor) {
        Set<String> methodNames = new HashSet<>();
        for (MethodSource<?> method : getAfterVisitMethodsOnly(visitor)) {
            if (methodNames.contains(method.getName())) {
                continue;
            }
            methodNames.add(method.getName());

            org.jboss.forge.roaster.model.source.ParameterSource<?> param = method.getParameters().get(0);
            classSource.addImport(param.getType());

            MethodSource<org.jboss.forge.roaster.model.source.JavaClassSource> methodSource = classSource.addMethod()
                    .setName(method.getName())
                    .setReturnTypeVoid()
                    .setPublic();
            methodSource.addParameter(param.getType().getSimpleName(), param.getName());
            methodSource.addAnnotation(Override.class);
            methodSource.setBody("");
        }
    }

}
