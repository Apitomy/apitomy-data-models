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
import io.apitomy.umg.pipe.java.method.CodeGenContext;
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
        String pairingKeyFQN = getState().getConfig().getRootNamespace() + ".visitors.diff.PairingKey";
        classSource.addImport(pairingKeyFQN);
        classSource.addTypeVariable("P").setBounds("PairingKey");
        classSource.setSuperType("AbstractDiffTraverser<P, " + visitorClassName + "<P>>");

        // Import CollectionDiff for collection field handling
        String collectionDiffFQN = getState().getConfig().getRootNamespace() + ".visitors.diff.CollectionDiff";
        classSource.addImport(collectionDiffFQN);

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

        createTraverseDispatch(specVer, classSource, createdMethods);

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

        // Create traverse methods for union types that contain entities
        String namespace = specVer.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        getState().getConceptIndex().findTypes(namespace).stream()
                .filter(t -> t instanceof io.apitomy.umg.models.concept.type.UnionType)
                .map(t -> (io.apitomy.umg.models.concept.type.UnionType) t)
                .forEach(unionType -> {
                    var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
                    String unionMethodName = "traverse" + jt.getSimpleName();
                    if (createdMethods.add(unionMethodName)) {
                        createUnionTraverseMethod(classSource, unionType, jt, namespace);
                    }
                });

        getState().getJavaIndex().index(classSource);
    }

    private void createTraverseDispatch(SpecificationVersion specVer, JavaClassSource classSource,
            java.util.Set<String> createdMethods) {
        createdMethods.add("traverse");

        String anyFQN = getState().getConfig().getRootNamespace() + ".Any";
        classSource.addImport(anyFQN);

        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName("traverse")
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter("Any", "original");
        method.addParameter("Any", "updated");
        method.addAnnotation(Override.class);

        BodyBuilder body = new BodyBuilder();
        body.append("Any target = original != null ? original : updated;");
        body.append("if (target == null) return;");

        boolean first = true;
        String namespace = specVer.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);

        // Union types first — they encompass entity types
        for (var type : getState().getConceptIndex().findTypes(namespace)) {
            if (!(type instanceof io.apitomy.umg.models.concept.type.UnionType)) continue;
            var unionType = (io.apitomy.umg.models.concept.type.UnionType) type;
            var jt = getJavaTypeFactory().createJavaType(unionType, nsModel);
            jt.addImportsTo(classSource);
            String unionTypeName = jt.getSimpleName();

            body.addContext("unionType", unionTypeName);
            String methodName = "traverse" + unionTypeName;
            body.addContext("methodName", methodName);

            if (first) {
                body.append("if (target instanceof ${unionType}) {");
                first = false;
            } else {
                body.append("} else if (target instanceof ${unionType}) {");
            }
            body.append("    this.${methodName}((${unionType}) original, (${unionType}) updated);");
        }

        // Collect entity names that are variants of unions (already dispatched above)
        java.util.Set<String> unionEntityNames = new java.util.HashSet<>();
        for (var type : getState().getConceptIndex().findTypes(namespace)) {
            if (type instanceof io.apitomy.umg.models.concept.type.UnionType) {
                for (var variant : ((io.apitomy.umg.models.concept.type.UnionType) type).getTypes()) {
                    if (variant.isEntityType()) {
                        unionEntityNames.add(variant.getName());
                    }
                }
            }
        }

        // Entity types not covered by unions
        for (var entity : specVer.getEntities()) {
            EntityModel entityModel = getState().getConceptIndex()
                    .lookupEntity(namespace + "." + entity.getName());
            if (entityModel == null) continue;
            if (unionEntityNames.contains(entityModel.getName())) continue;

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
            body.addContext("propertyNameLiteral", property.getName());

            NamespaceModel ns = propWithOrigin.getOrigin().getNamespace();

            body.append("{");
            body.append("    pushProperty(\"${propertyNameLiteral}\");");

            if (isEntity(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
                jt.addImportsTo(classSource);
                body.append("    visitor.${diffMethod}(original.${getter}(), updated.${getter}());");
                body.append("    if (original.${getter}() != null && updated.${getter}() != null) {");
                body.append("        traverse(original.${getter}(), updated.${getter}());");
                body.append("    }");
            } else if (isEntityList(property) || isUnionList(property)) {
                ListType listType = (ListType) property.getResolvedType();
                JavaType valueJt = jtf.createJavaType(listType.getValueType(), ns);
                valueJt.addImportsTo(classSource);

                String valueTypeStr = valueJt.toJavaTypeString();
                String visitMethod = "visit" + entityName + fieldSuffix + "Item";
                body.addContext("valueType", valueTypeStr);
                body.addContext("visitMethod2", visitMethod);
                body.addContext("propertyName", property.getName());

                body.append("    CollectionDiff<P," + valueTypeStr + "> diff = this.pairList(\"${propertyName}\", original.${getter}(), updated.${getter}());");
                body.append("    visitor.${diffMethod}(original.${getter}(), updated.${getter}(), diff);");
                body.append("    for (CollectionDiff.MatchedPair<P, " + valueTypeStr + "> pair : diff.getMatched()) {");
                body.append("        pushListIndex(pair.getKey());");
                body.append("        visitor.${visitMethod2}(pair.getOriginal(), pair.getUpdated());");
                if (isEntityList(property)) {
                    body.append("        if (pair.getOriginal() != null && pair.getUpdated() != null) {");
                    body.append("            traverse(pair.getOriginal(), pair.getUpdated());");
                    body.append("        }");
                } else if (isUnionList(property)) {
                    ListType lt = (ListType) property.getResolvedType();
                    JavaType unionJt = jtf.createJavaType(lt.getValueType(), ns);
                    String traverseUnion = "traverse" + unionJt.getSimpleName();
                    body.addContext("traverseUnion", traverseUnion);
                    body.append("        this.${traverseUnion}(pair.getOriginal(), pair.getUpdated());");
                }
                body.append("        pop();");
                body.append("    }");
            } else if (isEntityMap(property) || isUnionMap(property)) {
                MapType mapType = (MapType) property.getResolvedType();
                JavaType valueJt = jtf.createJavaType(mapType.getValueType(), ns);
                valueJt.addImportsTo(classSource);

                String valueTypeStr = valueJt.toJavaTypeString();
                String visitMethod = "visit" + entityName + singularize(fieldSuffix);
                body.addContext("valueType", valueTypeStr);
                body.addContext("visitMethod2", visitMethod);
                body.addContext("propertyName", property.getName());

                body.append("    CollectionDiff<P," + valueTypeStr + "> diff = this.pairMap(\"${propertyName}\", original.${getter}(), updated.${getter}());");
                body.append("    visitor.${diffMethod}(original.${getter}(), updated.${getter}(), diff);");
                body.append("    for (CollectionDiff.MatchedPair<P, " + valueTypeStr + "> pair : diff.getMatched()) {");
                body.append("        pushMapKey(pair.getKey());");
                body.append("        visitor.${visitMethod2}(pair.getOriginal(), pair.getUpdated());");
                if (isEntityMap(property)) {
                    body.append("        if (pair.getOriginal() != null && pair.getUpdated() != null) {");
                    body.append("            traverse(pair.getOriginal(), pair.getUpdated());");
                    body.append("        }");
                } else if (isUnionMap(property)) {
                    MapType mt = (MapType) property.getResolvedType();
                    JavaType unionJt = jtf.createJavaType(mt.getValueType(), ns);
                    String traverseUnion = "traverse" + unionJt.getSimpleName();
                    body.addContext("traverseUnion", traverseUnion);
                    body.append("        this.${traverseUnion}(pair.getOriginal(), pair.getUpdated());");
                }
                body.append("        pop();");
                body.append("    }");
            } else if (isUnion(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
                jt.addImportsTo(classSource);
                String traverseUnion = "traverse" + jt.getSimpleName();
                body.addContext("traverseUnion", traverseUnion);
                body.append("    visitor.${diffMethod}(original.${getter}(), updated.${getter}());");
                body.append("    this.${traverseUnion}(original.${getter}(), updated.${getter}());");
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
                jt.addImportsTo(classSource);
                body.append("    visitor.${diffMethod}(original.${getter}(), updated.${getter}());");
            }

            body.append("    pop();");
            body.append("}");
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

    private void createUnionTraverseMethod(JavaClassSource classSource,
            io.apitomy.umg.models.concept.type.UnionType unionType,
            JavaType unionJt, String namespace) {
        String unionTypeName = unionJt.getSimpleName();
        String methodName = "traverse" + unionTypeName;
        String diffMethodName = "diff" + unionTypeName;

        unionJt.addImportsTo(classSource);

        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter(unionJt.toJavaTypeString(), "original");
        method.addParameter(unionJt.toJavaTypeString(), "updated");

        BodyBuilder body = new BodyBuilder();
        body.append("if (original == null && updated == null) return;");
        body.addContext("diffMethod", diffMethodName);
        body.append("visitor.${diffMethod}(original, updated);");

        // Auto-recurse into entity variants when both sides are the same entity type
        for (var variantType : unionType.getTypes()) {
            if (variantType.isEntityType()) {
                var entity = ((io.apitomy.umg.models.concept.type.EntityType) variantType).getEntity();
                if (entity == null) {
                    entity = getState().getConceptIndex().lookupEntity(namespace, variantType.getName());
                }
                if (entity != null) {
                    var entityInterface = lookupJavaEntity(entity);
                    classSource.addImport(entityInterface);
                    body.addContext("entityType", entityInterface.getName());
                    body.addContext("traverseEntity", "traverse" + entity.getName());
                    body.append("if (original instanceof ${entityType} && updated instanceof ${entityType}) {");
                    body.append("    this.${traverseEntity}((${entityType}) original, (${entityType}) updated);");
                    body.append("}");
                }
            }
        }

        method.setBody(body.toString());
    }

    private static String singularize(String name) {
        return CodeGenContext.singularize(name);
    }
}
