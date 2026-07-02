package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.java.type.JavaType;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import io.apitomy.umg.pipe.java.method.CodeGenContext;

/**
 * Creates a ConversionVisitor abstract class per spec version that has a
 * {@code conversionTarget} configured. Each generated visitor contains typed,
 * field-specific methods for converting source entities/fields to target entities.
 */
public class CreateConversionVisitorsStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            if (specVer.getConversionTarget() != null) {
                createConversionVisitor(specVer);
            }
        });
    }

    private void createConversionVisitor(SpecificationVersion specVer) {
        String targetVersionName = specVer.getConversionTarget();
        SpecificationVersion targetSpecVer = resolveTargetSpecVersion(targetVersionName);
        if (targetSpecVer == null) {
            warn("Conversion target '%s' not found for spec '%s'", targetVersionName, specVer.getName());
            return;
        }

        String packageName = getTraverserPackageName(specVer);
        String className = specVer.getPrefix() + "To" + targetSpecVer.getPrefix() + "ConversionVisitor";

        debug("Creating conversion visitor: " + className);

        JavaClassSource classSource = Roaster.create(JavaClassSource.class)
                .setPackage(packageName)
                .setName(className)
                .setPublic()
                .setAbstract(true);

        JavaTypeFactory jtf = getJavaTypeFactory();

        specVer.getEntities().forEach(entity -> {
            EntityModel sourceEntityModel = getState().getConceptIndex()
                    .lookupEntity(specVer.getNamespace() + "." + entity.getName());
            if (sourceEntityModel == null) {
                return;
            }

            EntityModel targetEntityModel = getState().getConceptIndex()
                    .lookupEntity(targetSpecVer.getNamespace() + "." + entity.getName());

            createEntityVisitMethod(classSource, sourceEntityModel, targetEntityModel, targetSpecVer);
            createPropertyMethods(classSource, sourceEntityModel, targetEntityModel, targetSpecVer, jtf);
        });

        // Union convert methods
        String namespace = specVer.getNamespace();
        var nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        getState().getConceptIndex().findTypes(namespace).stream()
                .filter(t -> t instanceof io.apitomy.umg.models.concept.type.UnionType)
                .map(t -> (io.apitomy.umg.models.concept.type.UnionType) t)
                .forEach(unionType -> {
                    var jt = jtf.createJavaType(unionType, nsModel);
                    jt.addImportsTo(classSource);
                    String unionTypeName = jt.getSimpleName();
                    String methodName = "convert" + unionTypeName;
                    if (!hasMethod(classSource, methodName)) {
                        MethodSource<JavaClassSource> method = classSource.addMethod()
                                .setName(methodName)
                                .setReturnType(jt.toJavaTypeString())
                                .setPublic();
                        method.addParameter(jt.toJavaTypeString(), "source");
                        method.setBody("return source;");
                    }
                });

        getState().getJavaIndex().index(classSource);
    }

    private SpecificationVersion resolveTargetSpecVersion(String targetVersionName) {
        for (SpecificationVersion sv : getState().getSpecIndex().getAllSpecificationVersions()) {
            if (targetVersionName.equals(sv.getVersion())) {
                return sv;
            }
        }
        return null;
    }

    private void createEntityVisitMethod(JavaClassSource classSource,
            EntityModel sourceEntityModel, EntityModel targetEntityModel,
            SpecificationVersion targetSpecVer) {
        JavaInterfaceSource sourceJavaEntity = lookupJavaEntity(sourceEntityModel);
        classSource.addImport(sourceJavaEntity);

        String methodName = "visit" + sourceEntityModel.getName();

        if (targetEntityModel != null) {
            JavaInterfaceSource targetJavaEntity = lookupJavaEntity(targetEntityModel);
            classSource.addImport(targetJavaEntity);

            String targetImplFQN = getJavaEntityClassFQN(targetEntityModel);
            JavaClassSource targetImplSource = getState().getJavaIndex().lookupClass(targetImplFQN);
            classSource.addImport(targetImplSource);

            MethodSource<JavaClassSource> method = classSource.addMethod()
                    .setName(methodName)
                    .setReturnType(targetJavaEntity.getName())
                    .setPublic();
            method.addParameter(sourceJavaEntity.getName(), "source");
            method.setBody("return new " + targetImplSource.getName() + "();");
        } else {
            // No matching target entity; return null
            MethodSource<JavaClassSource> method = classSource.addMethod()
                    .setName(methodName)
                    .setReturnType("Object")
                    .setPublic();
            method.addParameter(sourceJavaEntity.getName(), "source");
            method.setBody("return null;");
        }
    }

    private void createPropertyMethods(JavaClassSource classSource,
            EntityModel sourceEntityModel, EntityModel targetEntityModel,
            SpecificationVersion targetSpecVer, JavaTypeFactory jtf) {
        Collection<PropertyModelWithOrigin> sourceProperties =
                getState().getConceptIndex().getAllEntityProperties(sourceEntityModel);

        // Build a map of target properties by name for quick lookup
        Map<String, PropertyModelWithOrigin> targetPropertiesByName = new java.util.LinkedHashMap<>();
        if (targetEntityModel != null) {
            Collection<PropertyModelWithOrigin> targetProperties =
                    getState().getConceptIndex().getAllEntityProperties(targetEntityModel);
            for (PropertyModelWithOrigin pwo : targetProperties) {
                targetPropertiesByName.put(pwo.getProperty().getName(), pwo);
            }
        }

        for (PropertyModelWithOrigin propWithOrigin : sourceProperties) {
            PropertyModel property = propWithOrigin.getProperty();
            if (isStarProperty(property) || isRegexProperty(property)) {
                continue;
            }

            String entityName = sourceEntityModel.getName();
            String fieldSuffix = fieldMethodSuffix(property);
            NamespaceModel sourceNs = propWithOrigin.getOrigin().getNamespace();

            PropertyModelWithOrigin targetPropWithOrigin = targetPropertiesByName.get(property.getName());

            if (isEntity(property)) {
                createSingleFieldMethod(classSource, entityName, fieldSuffix, property, sourceNs,
                        targetEntityModel, targetPropWithOrigin, targetSpecVer, jtf);
            } else if (isEntityList(property) || isUnionList(property)) {
                createListFieldMethod(classSource, entityName, fieldSuffix, property, sourceNs,
                        targetEntityModel, targetPropWithOrigin, targetSpecVer, jtf);
            } else if (isEntityMap(property) || isUnionMap(property)) {
                createMapFieldMethod(classSource, entityName, fieldSuffix, property, sourceNs,
                        targetEntityModel, targetPropWithOrigin, targetSpecVer, jtf);
            } else if (isUnion(property)) {
                createSingleFieldMethod(classSource, entityName, fieldSuffix, property, sourceNs,
                        targetEntityModel, targetPropWithOrigin, targetSpecVer, jtf);
            } else if (isPrimitive(property)) {
                createSingleFieldMethod(classSource, entityName, fieldSuffix, property, sourceNs,
                        targetEntityModel, targetPropWithOrigin, targetSpecVer, jtf);
            } else if (isPrimitiveList(property)) {
                createListFieldMethod(classSource, entityName, fieldSuffix, property, sourceNs,
                        targetEntityModel, targetPropWithOrigin, targetSpecVer, jtf);
            } else if (isPrimitiveMap(property)) {
                createMapFieldMethod(classSource, entityName, fieldSuffix, property, sourceNs,
                        targetEntityModel, targetPropWithOrigin, targetSpecVer, jtf);
            }
        }
    }

    private void createSingleFieldMethod(JavaClassSource classSource, String entityName,
            String fieldSuffix, PropertyModel sourceProperty, NamespaceModel sourceNs,
            EntityModel targetEntityModel, PropertyModelWithOrigin targetPropWithOrigin,
            SpecificationVersion targetSpecVer, JavaTypeFactory jtf) {
        JavaType sourceJt = jtf.createJavaType(sourceProperty.getResolvedType(), sourceNs);
        sourceJt.addImportsTo(classSource);

        String methodName = "convert" + entityName + fieldSuffix;
        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter(sourceJt.toJavaTypeString(), "value");

        if (targetEntityModel != null) {
            JavaInterfaceSource targetJavaEntity = lookupJavaEntity(targetEntityModel);
            classSource.addImport(targetJavaEntity);
            method.addParameter(targetJavaEntity.getName(), "target");
        } else {
            method.addParameter("Object", "target");
        }

        // Generate default body
        if (targetPropWithOrigin != null && targetEntityModel != null) {
            PropertyModel targetProperty = targetPropWithOrigin.getProperty();
            NamespaceModel targetNs = targetPropWithOrigin.getOrigin().getNamespace();
            JavaType targetJt = jtf.createJavaType(targetProperty.getResolvedType(), targetNs);
            targetJt.addImportsTo(classSource);

            // Check type compatibility
            if (sourceJt.toJavaTypeString().equals(targetJt.toJavaTypeString())) {
                // Compatible types: generate setter call
                String setterName = "set" + StringUtils.capitalize(targetProperty.getName());
                method.setBody("target." + setterName + "(value);");
            } else {
                // Different types: empty body (override needed)
                method.setBody("// Type mismatch — custom conversion required");
            }
        } else {
            // No matching target field: empty body (field dropped)
            method.setBody("// Field not present in target — dropped during conversion");
        }
    }

    private void createListFieldMethod(JavaClassSource classSource, String entityName,
            String fieldSuffix, PropertyModel sourceProperty, NamespaceModel sourceNs,
            EntityModel targetEntityModel, PropertyModelWithOrigin targetPropWithOrigin,
            SpecificationVersion targetSpecVer, JavaTypeFactory jtf) {
        JavaType sourceJt = jtf.createJavaType(sourceProperty.getResolvedType(), sourceNs);
        sourceJt.addImportsTo(classSource);
        classSource.addImport(java.util.List.class);

        ListType sourceListType = (ListType) sourceProperty.getResolvedType();
        JavaType sourceValueJt = jtf.createJavaType(sourceListType.getValueType(), sourceNs);
        sourceValueJt.addImportsTo(classSource);

        String methodName = "convert" + entityName + fieldSuffix;
        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter("List<" + sourceValueJt.toJavaTypeString() + ">", "value");

        if (targetEntityModel != null) {
            JavaInterfaceSource targetJavaEntity = lookupJavaEntity(targetEntityModel);
            classSource.addImport(targetJavaEntity);
            method.addParameter(targetJavaEntity.getName(), "target");
        } else {
            method.addParameter("Object", "target");
        }

        // Generate default body
        if (targetPropWithOrigin != null && targetEntityModel != null) {
            PropertyModel targetProperty = targetPropWithOrigin.getProperty();
            NamespaceModel targetNs = targetPropWithOrigin.getOrigin().getNamespace();

            if (isEntityList(targetProperty) || isUnionList(targetProperty) || isPrimitiveList(targetProperty)) {
                ListType targetListType = (ListType) targetProperty.getResolvedType();
                JavaType targetValueJt = jtf.createJavaType(targetListType.getValueType(), targetNs);
                targetValueJt.addImportsTo(classSource);

                if (sourceValueJt.toJavaTypeString().equals(targetValueJt.toJavaTypeString())) {
                    if (isPrimitiveList(targetProperty)) {
                        // Primitive lists use setter
                        String setterName = "set" + StringUtils.capitalize(targetProperty.getName());
                        method.setBody("target." + setterName + "(value);");
                    } else {
                        // Entity/union lists use adder
                        String adderName = "add" + StringUtils.capitalize(
                                CodeGenContext.singularize(targetProperty.getName()));
                        method.setBody("if (value != null) { for (" + sourceValueJt.toJavaTypeString()
                                + " v : value) { target." + adderName + "(v); } }");
                    }
                } else {
                    method.setBody("// Type mismatch — custom conversion required");
                }
            } else {
                method.setBody("// Type mismatch — custom conversion required");
            }
        } else {
            method.setBody("// Field not present in target — dropped during conversion");
        }
    }

    private void createMapFieldMethod(JavaClassSource classSource, String entityName,
            String fieldSuffix, PropertyModel sourceProperty, NamespaceModel sourceNs,
            EntityModel targetEntityModel, PropertyModelWithOrigin targetPropWithOrigin,
            SpecificationVersion targetSpecVer, JavaTypeFactory jtf) {
        JavaType sourceJt = jtf.createJavaType(sourceProperty.getResolvedType(), sourceNs);
        sourceJt.addImportsTo(classSource);
        classSource.addImport(java.util.Map.class);

        MapType sourceMapType = (MapType) sourceProperty.getResolvedType();
        JavaType sourceValueJt = jtf.createJavaType(sourceMapType.getValueType(), sourceNs);
        sourceValueJt.addImportsTo(classSource);

        String methodName = "convert" + entityName + fieldSuffix;
        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnTypeVoid()
                .setPublic();
        method.addParameter("Map<String, " + sourceValueJt.toJavaTypeString() + ">", "value");

        if (targetEntityModel != null) {
            JavaInterfaceSource targetJavaEntity = lookupJavaEntity(targetEntityModel);
            classSource.addImport(targetJavaEntity);
            method.addParameter(targetJavaEntity.getName(), "target");
        } else {
            method.addParameter("Object", "target");
        }

        // Generate default body
        if (targetPropWithOrigin != null && targetEntityModel != null) {
            PropertyModel targetProperty = targetPropWithOrigin.getProperty();
            NamespaceModel targetNs = targetPropWithOrigin.getOrigin().getNamespace();

            if (isEntityMap(targetProperty) || isUnionMap(targetProperty) || isPrimitiveMap(targetProperty)) {
                MapType targetMapType = (MapType) targetProperty.getResolvedType();
                JavaType targetValueJt = jtf.createJavaType(targetMapType.getValueType(), targetNs);
                targetValueJt.addImportsTo(classSource);

                if (sourceValueJt.toJavaTypeString().equals(targetValueJt.toJavaTypeString())) {
                    if (isPrimitiveMap(targetProperty)) {
                        // Primitive maps use setter
                        String setterName = "set" + StringUtils.capitalize(targetProperty.getName());
                        method.setBody("target." + setterName + "(value);");
                    } else {
                        // Entity/union maps use adder
                        String adderName = "add" + StringUtils.capitalize(
                                CodeGenContext.singularize(targetProperty.getName()));
                        method.setBody("if (value != null) { for (Map.Entry<String, "
                                + sourceValueJt.toJavaTypeString() + "> e : value.entrySet()) { target."
                                + adderName + "(e.getKey(), e.getValue()); } }");
                    }
                } else {
                    method.setBody("// Type mismatch — custom conversion required");
                }
            } else {
                method.setBody("// Type mismatch — custom conversion required");
            }
        } else {
            method.setBody("// Field not present in target — dropped during conversion");
        }
    }

    private String fieldMethodSuffix(PropertyModel property) {
        String name = property.getName();
        if (name.startsWith("$")) {
            return name;
        }
        return StringUtils.capitalize(name);
    }

    private boolean hasMethod(JavaClassSource classSource, String methodName) {
        return classSource.getMethods().stream().anyMatch(m -> m.getName().equals(methodName));
    }
}
