package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.java.type.JavaType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.GetterMethod;

/**
 * Creates a DiffTraverser for each specification version. A DiffTraverser walks two
 * trees of the same spec version in parallel, dispatching field-level diffs to the
 * generated per-spec DiffVisitor's typed methods.
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

        // Resolve the generated per-spec visitor
        String visitorClassName = specVer.getPrefix() + "DiffVisitor";
        String visitorFQN = packageName + "." + visitorClassName;
        JavaClassSource visitorSource = getState().getJavaIndex().lookupClass(visitorFQN);
        classSource.addImport(visitorSource);

        // Extend AbstractDiffTraverser<SpecDiffVisitor>
        String baseFQN = getState().getConfig().getRootNamespace() + ".visitors.diff.AbstractDiffTraverser";
        JavaClassSource baseSource = getState().getJavaIndex().lookupClass(baseFQN);
        classSource.addImport(baseSource);
        classSource.addTypeVariable("P");
        classSource.setSuperType("AbstractDiffTraverser<P, " + visitorClassName + "<P>>");

        // Import CollectionDiff for collection field handling
        String collectionDiffFQN = getState().getConfig().getRootNamespace() + ".visitors.diff.CollectionDiff";
        classSource.addImport(collectionDiffFQN);

        String nodeFQN = getNodeEntityInterfaceFQN();
        JavaInterfaceSource nodeSource = getState().getJavaIndex().lookupInterface(nodeFQN);
        classSource.addImport(nodeSource);

        // Import PairingStrategyProvider
        String providerFQN = getState().getConfig().getRootNamespace() + ".visitors.diff.PairingStrategyProvider";
        classSource.addImport(providerFQN);

        // Constructors
        MethodSource<JavaClassSource> constructor = classSource.addMethod()
                .setConstructor(true).setPublic();
        constructor.addParameter(visitorClassName + "<P>", "visitor");
        constructor.setBody("super(visitor);");

        MethodSource<JavaClassSource> constructor2 = classSource.addMethod()
                .setConstructor(true).setPublic();
        constructor2.addParameter(visitorClassName, "visitor");
        constructor2.addParameter("PairingStrategyProvider<P>", "pairingProvider");
        constructor2.setBody("super(visitor, pairingProvider);");

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
        String entityName = entityModel.getName();
        String methodName = "traverse" + entityName;

        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter(entityType, "original");
        method.addParameter(entityType, "updated");

        Collection<PropertyModelWithOrigin> allProperties =
                getState().getConceptIndex().getAllEntityProperties(entityModel);

        JavaTypeFactory jtf = getJavaTypeFactory();
        BodyBuilder body = new BodyBuilder();

        body.append("if (original == null && updated == null) return;");
        body.addContext("visitMethod", "visit" + entityName);
        body.append("if (!visitor.${visitMethod}(original, updated)) return;");
        body.append("if (original == null || updated == null) return;");

        for (PropertyModelWithOrigin propWithOrigin : allProperties) {
            PropertyModel property = propWithOrigin.getProperty();
            if (isStarProperty(property) || isRegexProperty(property)) {
                continue;
            }

            String getter = new GetterMethod(property).getName();
            String fieldSuffix = fieldMethodSuffix(property);
            String diffMethod = "diff" + entityName + fieldSuffix;

            body.addContext("getter", getter);
            body.addContext("diffMethod", diffMethod);

            NamespaceModel ns = propWithOrigin.getOrigin().getNamespace();

            if (isEntity(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
                jt.addImportsTo(classSource);
                body.append("visitor.${diffMethod}(original.${getter}(), updated.${getter}());");
                body.append("if (original.${getter}() != null && updated.${getter}() != null) {");
                body.append("    traverseNode(original.${getter}(), updated.${getter}());");
                body.append("}");
            } else if (isEntityList(property) || isUnionList(property)) {
                ListType listType = (ListType) property.getResolvedType();
                JavaType valueJt = jtf.createJavaType(listType.getValueType(), ns);
                valueJt.addImportsTo(classSource);

                String valueTypeStr = valueJt.toJavaTypeString();
                String visitMethod = "visit" + entityName + fieldSuffix + "Item";
                body.addContext("valueType", valueTypeStr);
                body.addContext("visitMethod2", visitMethod);
                body.addContext("propertyName", property.getName());

                body.append("{");
                body.append("    CollectionDiff<P," + valueTypeStr + "> diff = this.pairList(\"${propertyName}\", original.${getter}(), updated.${getter}());");
                body.append("    visitor.${diffMethod}(diff);");
                body.append("    for (CollectionDiff.MatchedPair<P, " + valueTypeStr + "> pair : diff.getMatched()) {");
                body.append("        visitor.${visitMethod2}(pair.getOriginal(), pair.getUpdated());");
                if (isEntityList(property)) {
                    body.append("        if (pair.getOriginal() != null && pair.getUpdated() != null) {");
                    body.append("            traverseNode(pair.getOriginal(), pair.getUpdated());");
                    body.append("        }");
                }
                body.append("    }");
                body.append("}");
            } else if (isEntityMap(property) || isUnionMap(property)) {
                MapType mapType = (MapType) property.getResolvedType();
                JavaType valueJt = jtf.createJavaType(mapType.getValueType(), ns);
                valueJt.addImportsTo(classSource);

                String valueTypeStr = valueJt.toJavaTypeString();
                String visitMethod = "visit" + entityName + fieldSuffix;
                body.addContext("valueType", valueTypeStr);
                body.addContext("visitMethod2", visitMethod);
                body.addContext("propertyName", property.getName());

                body.append("{");
                body.append("    CollectionDiff<P," + valueTypeStr + "> diff = this.pairMap(\"${propertyName}\", original.${getter}(), updated.${getter}());");
                body.append("    visitor.${diffMethod}(diff);");
                body.append("    for (CollectionDiff.MatchedPair<P, " + valueTypeStr + "> pair : diff.getMatched()) {");
                body.append("        visitor.${visitMethod2}(pair.getOriginal(), pair.getUpdated());");
                if (isEntityMap(property)) {
                    body.append("        if (pair.getOriginal() != null && pair.getUpdated() != null) {");
                    body.append("            traverseNode(pair.getOriginal(), pair.getUpdated());");
                    body.append("        }");
                }
                body.append("    }");
                body.append("}");
            } else if (isUnion(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
                jt.addImportsTo(classSource);
                body.append("visitor.${diffMethod}(original.${getter}(), updated.${getter}());");
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
                jt.addImportsTo(classSource);
                body.append("visitor.${diffMethod}(original.${getter}(), updated.${getter}());");
            }
        }

        method.setBody(body.toString());
    }

    private String fieldMethodSuffix(PropertyModel property) {
        String name = property.getName();
        if (name.startsWith("$")) {
            return name;
        }
        return StringUtils.capitalize(name);
    }
}
