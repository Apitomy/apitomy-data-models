package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.GetterMethod;

/**
 * Creates a DiffTraverser for each specification version. A DiffTraverser walks two
 * trees of the same spec version in parallel, dispatching field-level diffs to a
 * DiffVisitor.
 */
public class CreateDiffTraversersStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecificationVersions().forEach(this::createDiffTraverser);
    }

    private void createDiffTraverser(SpecificationVersion specVer) {
        String packageName = getTraverserPackageName(specVer);
        String className = specVer.getPrefix() + "DiffTraverser";

        debug("Creating diff traverser: " + className);

        JavaClassSource classSource = Roaster.create(JavaClassSource.class)
                .setPackage(packageName)
                .setName(className)
                .setPublic();

        String baseFQN = getState().getConfig().getRootNamespace() + ".visitors.diff.AbstractDiffTraverser";
        JavaClassSource baseSource = getState().getJavaIndex().lookupClass(baseFQN);
        classSource.addImport(baseSource);
        classSource.extendSuperType(baseSource);

        String diffVisitorFQN = getState().getConfig().getRootNamespace() + ".visitors.diff.DiffVisitor";
        JavaClassSource diffVisitorSource = getState().getJavaIndex().lookupClass(diffVisitorFQN);
        classSource.addImport(diffVisitorSource);

        MethodSource<JavaClassSource> constructor = classSource.addMethod()
                .setConstructor(true).setPublic();
        constructor.addParameter("DiffVisitor", "visitor");
        constructor.setBody("super(visitor);");

        String nodeFQN = getNodeEntityInterfaceFQN();
        JavaInterfaceSource nodeSource = getState().getJavaIndex().lookupInterface(nodeFQN);
        classSource.addImport(nodeSource);

        java.util.Set<String> createdMethods = new java.util.HashSet<>();

        createTraverseNodeDispatch(specVer, classSource, createdMethods);

        specVer.getEntities().forEach(entity -> {
            EntityModel entityModel = getState().getConceptIndex()
                    .lookupEntity(specVer.getNamespace() + "." + entity.getName());
            if (entityModel != null) {
                String methodName = "traverse" + entityModel.getName();
                if (createdMethods.add(methodName)) {
                    createEntityTraverseMethod(specVer, classSource, entityModel);
                }
            }
        });

        getState().getJavaIndex().index(classSource);
    }

    private void createTraverseNodeDispatch(SpecificationVersion specVer, JavaClassSource classSource,
            java.util.Set<String> createdMethods) {
        createdMethods.add("traverseNode");
        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName("traverseNode")
                .setReturnTypeVoid()
                .setProtected();
        method.addParameter("Node", "original");
        method.addParameter("Node", "updated");
        method.addAnnotation(Override.class);

        BodyBuilder body = new BodyBuilder();
        body.append("Node target = original != null ? original : updated;");

        boolean first = true;
        for (var entity : specVer.getEntities()) {
            EntityModel entityModel = getState().getConceptIndex()
                    .lookupEntity(specVer.getNamespace() + "." + entity.getName());
            if (entityModel == null) continue;

            JavaInterfaceSource javaEntity = lookupJavaEntity(entityModel);
            classSource.addImport(javaEntity);
            String entityType = javaEntity.getName();

            body.addContext("entityType", entityType);
            String methodName = "traverse" + entityModel.getName();
            body.addContext("methodName", methodName);

            if (first) {
                body.append("if (target instanceof ${entityType}) {");
                first = false;
            } else {
                body.append("} else if (target instanceof ${entityType}) {");
            }
            body.append("    this.${methodName}((${entityType}) original, (${entityType}) updated);");
        }
        if (!first) {
            body.append("}");
        }

        method.setBody(body.toString());
    }

    private void createEntityTraverseMethod(SpecificationVersion specVer,
            JavaClassSource classSource, EntityModel entityModel) {
        JavaInterfaceSource javaEntity = lookupJavaEntity(entityModel);
        classSource.addImport(javaEntity);

        String entityType = javaEntity.getName();
        String methodName = "traverse" + entityModel.getName();

        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter(entityType, "original");
        method.addParameter(entityType, "updated");

        Collection<PropertyModelWithOrigin> allProperties =
                getState().getConceptIndex().getAllEntityProperties(entityModel);

        BodyBuilder body = new BodyBuilder();

        body.append("if (original == null && updated == null) return;");
        body.append("if (!visitor.visitEntityPair(original, updated)) return;");
        body.append("if (original == null || updated == null) return;");

        for (PropertyModelWithOrigin propWithOrigin : allProperties) {
            PropertyModel property = propWithOrigin.getProperty();
            if (isStarProperty(property) || isRegexProperty(property)) {
                continue;
            }

            String getter = new GetterMethod(property).getName();
            body.addContext("propertyName", property.getName());
            body.addContext("getter", getter);

            if (isEntity(property)) {
                body.append("this.diffEntityField(\"${propertyName}\", original.${getter}(), updated.${getter}());");
            } else if (isEntityList(property) || isUnionList(property)) {
                body.append("this.diffList(\"${propertyName}\", original.${getter}(), updated.${getter}());");
            } else if (isEntityMap(property) || isUnionMap(property)) {
                body.append("this.diffMap(\"${propertyName}\", original.${getter}(), updated.${getter}());");
            } else if (isUnion(property)) {
                body.append("this.diffUnionField(\"${propertyName}\", original.${getter}(), updated.${getter}());");
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                body.append("visitor.diffPrimitive(\"${propertyName}\", original.${getter}(), updated.${getter}());");
            }
        }

        method.setBody(body.toString());
    }
}
