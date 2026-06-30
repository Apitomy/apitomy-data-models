package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.java.type.JavaType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;

/**
 * Creates a DiffVisitor abstract class per spec version. Each generated visitor
 * contains typed, field-specific methods for every entity and property in the spec.
 */
public class CreateDiffVisitorsStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecificationVersions().forEach(this::createDiffVisitor);
    }

    private void createDiffVisitor(SpecificationVersion specVer) {
        String packageName = getTraverserPackageName(specVer);
        String className = specVer.getPrefix() + "DiffVisitor";

        debug("Creating diff visitor: " + className);

        JavaClassSource classSource = Roaster.create(JavaClassSource.class)
                .setPackage(packageName)
                .setName(className)
                .setPublic()
                .setAbstract(true);

        classSource.addTypeVariable("P");

        JavaTypeFactory jtf = getJavaTypeFactory();

        specVer.getEntities().forEach(entity -> {
            EntityModel entityModel = getState().getConceptIndex()
                    .lookupEntity(specVer.getNamespace() + "." + entity.getName());
            if (entityModel == null) {
                return;
            }

            createEntityVisitMethod(classSource, entityModel);
            createPropertyMethods(classSource, entityModel, jtf);
        });

        getState().getJavaIndex().index(classSource);
    }

    private void createEntityVisitMethod(JavaClassSource classSource, EntityModel entityModel) {
        var javaEntity = lookupJavaEntity(entityModel);
        classSource.addImport(javaEntity);

        String methodName = "visit" + entityModel.getName();
        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnType(boolean.class)
                .setPublic();
        method.addParameter(javaEntity.getName(), "original");
        method.addParameter(javaEntity.getName(), "updated");
        method.setBody("return true;");
    }

    private void createPropertyMethods(JavaClassSource classSource, EntityModel entityModel,
            JavaTypeFactory jtf) {
        Collection<PropertyModelWithOrigin> allProperties =
                getState().getConceptIndex().getAllEntityProperties(entityModel);

        for (PropertyModelWithOrigin propWithOrigin : allProperties) {
            PropertyModel property = propWithOrigin.getProperty();
            if (isStarProperty(property) || isRegexProperty(property)) {
                continue;
            }

            String entityName = entityModel.getName();
            String fieldSuffix = fieldMethodSuffix(property);
            NamespaceModel ns = propWithOrigin.getOrigin().getNamespace();

            if (isEntity(property)) {
                createEntityFieldMethod(classSource, entityName, fieldSuffix, property, ns, jtf);
            } else if (isEntityList(property)) {
                createListFieldMethods(classSource, entityName, fieldSuffix, property, ns, jtf);
            } else if (isEntityMap(property)) {
                createMapFieldMethods(classSource, entityName, fieldSuffix, property, ns, jtf);
            } else if (isUnion(property)) {
                createUnionFieldMethod(classSource, entityName, fieldSuffix, property, ns, jtf);
            } else if (isUnionList(property)) {
                createListFieldMethods(classSource, entityName, fieldSuffix, property, ns, jtf);
            } else if (isUnionMap(property)) {
                createMapFieldMethods(classSource, entityName, fieldSuffix, property, ns, jtf);
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                createPrimitiveFieldMethod(classSource, entityName, fieldSuffix, property, ns, jtf);
            }
        }
    }

    private void createPrimitiveFieldMethod(JavaClassSource classSource, String entityName,
            String fieldSuffix, PropertyModel property, NamespaceModel ns, JavaTypeFactory jtf) {
        JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
        jt.addImportsTo(classSource);

        String methodName = "diff" + entityName + fieldSuffix;
        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter(jt.toJavaTypeString(), "original");
        method.addParameter(jt.toJavaTypeString(), "updated");
        method.setBody("");
    }

    private void createEntityFieldMethod(JavaClassSource classSource, String entityName,
            String fieldSuffix, PropertyModel property, NamespaceModel ns, JavaTypeFactory jtf) {
        JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
        jt.addImportsTo(classSource);

        String methodName = "diff" + entityName + fieldSuffix;
        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter(jt.toJavaTypeString(), "original");
        method.addParameter(jt.toJavaTypeString(), "updated");
        method.setBody("");
    }

    private void createUnionFieldMethod(JavaClassSource classSource, String entityName,
            String fieldSuffix, PropertyModel property, NamespaceModel ns, JavaTypeFactory jtf) {
        JavaType jt = jtf.createJavaType(property.getResolvedType(), ns);
        jt.addImportsTo(classSource);

        String methodName = "diff" + entityName + fieldSuffix;
        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter(jt.toJavaTypeString(), "original");
        method.addParameter(jt.toJavaTypeString(), "updated");
        method.setBody("");
    }

    private void createListFieldMethods(JavaClassSource classSource, String entityName,
            String fieldSuffix, PropertyModel property, NamespaceModel ns, JavaTypeFactory jtf) {
        ListType listType = (ListType) property.getResolvedType();
        JavaType valueJt = jtf.createJavaType(listType.getValueType(), ns);
        valueJt.addImportsTo(classSource);

        String collectionDiffFQN = getState().getConfig().getRootNamespace()
                + ".visitors.diff.CollectionDiff";
        classSource.addImport(collectionDiffFQN);

        classSource.addImport(java.util.List.class);

        String diffMethodName = "diff" + entityName + fieldSuffix;
        MethodSource<JavaClassSource> diffMethod = classSource.addMethod()
                .setName(diffMethodName)
                .setReturnTypeVoid()
                .setPublic();
        diffMethod.addParameter("List<" + valueJt.toJavaTypeString() + ">", "original");
        diffMethod.addParameter("List<" + valueJt.toJavaTypeString() + ">", "updated");
        diffMethod.addParameter("CollectionDiff<P," + valueJt.toJavaTypeString() + ">", "diff");
        diffMethod.setBody("");

        String visitMethodName = "visit" + entityName + fieldSuffix + "Item";
        MethodSource<JavaClassSource> visitMethod = classSource.addMethod()
                .setName(visitMethodName)
                .setReturnTypeVoid()
                .setPublic();
        visitMethod.addParameter(valueJt.toJavaTypeString(), "original");
        visitMethod.addParameter(valueJt.toJavaTypeString(), "updated");
        visitMethod.setBody("");
    }

    private void createMapFieldMethods(JavaClassSource classSource, String entityName,
            String fieldSuffix, PropertyModel property, NamespaceModel ns, JavaTypeFactory jtf) {
        MapType mapType = (MapType) property.getResolvedType();
        JavaType valueJt = jtf.createJavaType(mapType.getValueType(), ns);
        valueJt.addImportsTo(classSource);

        String collectionDiffFQN = getState().getConfig().getRootNamespace()
                + ".visitors.diff.CollectionDiff";
        classSource.addImport(collectionDiffFQN);
        classSource.addImport(java.util.Map.class);

        String diffMethodName = "diff" + entityName + fieldSuffix;
        MethodSource<JavaClassSource> diffMethod = classSource.addMethod()
                .setName(diffMethodName)
                .setReturnTypeVoid()
                .setPublic();
        diffMethod.addParameter("Map<String," + valueJt.toJavaTypeString() + ">", "original");
        diffMethod.addParameter("Map<String," + valueJt.toJavaTypeString() + ">", "updated");
        diffMethod.addParameter("CollectionDiff<P," + valueJt.toJavaTypeString() + ">", "diff");
        diffMethod.setBody("");

        String singularSuffix = singularize(fieldSuffix);
        String visitMethodName = "visit" + entityName + singularSuffix;
        MethodSource<JavaClassSource> visitMethod = classSource.addMethod()
                .setName(visitMethodName)
                .setReturnTypeVoid()
                .setPublic();
        visitMethod.addParameter(valueJt.toJavaTypeString(), "original");
        visitMethod.addParameter(valueJt.toJavaTypeString(), "updated");
        visitMethod.setBody("");
    }

    private static String singularize(String name) {
        return CodeGenContext.singularize(name);
    }

    /**
     * Turns a property name into a method name suffix.
     * "title" → "Title", "$ref" → "$ref", "allOf" → "AllOf"
     */
    private String fieldMethodSuffix(PropertyModel property) {
        String name = property.getName();
        if (name.startsWith("$")) {
            return name;
        }
        return StringUtils.capitalize(name);
    }
}
