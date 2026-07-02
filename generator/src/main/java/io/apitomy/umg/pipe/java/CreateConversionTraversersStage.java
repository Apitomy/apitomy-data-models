package io.apitomy.umg.pipe.java;

import java.util.Collection;

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
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.GetterMethod;

/**
 * Creates a ConversionTraverser for each specification version that has a
 * {@code conversionTarget} configured. A ConversionTraverser walks a source tree,
 * creating a corresponding target tree by dispatching field-level conversion to
 * the generated per-spec ConversionVisitor's typed methods.
 */
public class CreateConversionTraversersStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            if (specVer.getConversionTarget() != null) {
                createConversionTraverser(specVer);
            }
        });
    }

    private void createConversionTraverser(SpecificationVersion specVer) {
        String targetVersionName = specVer.getConversionTarget();
        SpecificationVersion targetSpecVer = resolveTargetSpecVersion(targetVersionName);
        if (targetSpecVer == null) {
            warn("Conversion target '%s' not found for spec '%s'", targetVersionName, specVer.getName());
            return;
        }

        String packageName = getTraverserPackageName(specVer);
        String className = specVer.getPrefix() + "To" + targetSpecVer.getPrefix() + "ConversionTraverser";

        debug("Creating conversion traverser: " + className);

        JavaClassSource classSource = Roaster.create(JavaClassSource.class)
                .setPackage(packageName)
                .setName(className)
                .setPublic();

        // Resolve the generated per-spec visitor
        String visitorClassName = specVer.getPrefix() + "To" + targetSpecVer.getPrefix() + "ConversionVisitor";
        String visitorFQN = packageName + "." + visitorClassName;
        JavaClassSource visitorSource = getState().getJavaIndex().lookupClass(visitorFQN);
        classSource.addImport(visitorSource);

        // Extend AbstractConversionTraverser<SpecConversionVisitor>
        String baseFQN = getState().getConfig().getRootNamespace()
                + ".visitors.convert.AbstractConversionTraverser";
        JavaClassSource baseSource = getState().getJavaIndex().lookupClass(baseFQN);
        classSource.addImport(baseSource);
        classSource.setSuperType("AbstractConversionTraverser<" + visitorClassName + ">");

        // Constructor
        MethodSource<JavaClassSource> constructor = classSource.addMethod()
                .setConstructor(true).setPublic();
        constructor.addParameter(visitorClassName, "visitor");
        constructor.setBody("super(visitor);");

        java.util.Set<String> createdMethods = new java.util.HashSet<>();

        createConvertDispatch(specVer, targetSpecVer, classSource, createdMethods);

        specVer.getEntities().forEach(entity -> {
            EntityModel sourceEntityModel = getState().getConceptIndex()
                    .lookupEntity(specVer.getNamespace() + "." + entity.getName());
            if (sourceEntityModel != null) {
                String methodName = "convert" + sourceEntityModel.getName();
                if (createdMethods.add(methodName)) {
                    createEntityConvertMethod(specVer, targetSpecVer, classSource, sourceEntityModel);
                }
            }
        });

        // Create convert methods for union types that contain entities
        String namespace = specVer.getNamespace();
        NamespaceModel nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        JavaTypeFactory jtf = getJavaTypeFactory();
        getState().getConceptIndex().findTypes(namespace).stream()
                .filter(t -> t instanceof io.apitomy.umg.models.concept.type.UnionType)
                .map(t -> (io.apitomy.umg.models.concept.type.UnionType) t)
                .forEach(unionType -> {
                    JavaType jt = jtf.createJavaType(unionType, nsModel);
                    String unionMethodName = "convert" + jt.getSimpleName();
                    if (createdMethods.add(unionMethodName)) {
                        createUnionConvertMethod(classSource, unionType, jt,
                                namespace, targetSpecVer);
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

    private void createConvertDispatch(SpecificationVersion specVer,
            SpecificationVersion targetSpecVer, JavaClassSource classSource,
            java.util.Set<String> createdMethods) {
        createdMethods.add("convert");

        String anyFQN = getState().getConfig().getRootNamespace() + ".Any";
        classSource.addImport(anyFQN);

        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName("convert")
                .setReturnType("Any")
                .setPublic();
        method.addParameter("Any", "source");
        method.addAnnotation(Override.class);

        BodyBuilder body = new BodyBuilder();
        body.append("if (source == null) return null;");

        boolean first = true;
        String namespace = specVer.getNamespace();
        NamespaceModel nsModel = getState().getConceptIndex().lookupNamespace(namespace);
        JavaTypeFactory jtf = getJavaTypeFactory();

        // Union types first
        for (var type : getState().getConceptIndex().findTypes(namespace)) {
            if (!(type instanceof io.apitomy.umg.models.concept.type.UnionType)) continue;
            io.apitomy.umg.models.concept.type.UnionType unionType =
                    (io.apitomy.umg.models.concept.type.UnionType) type;
            JavaType jt = jtf.createJavaType(unionType, nsModel);
            jt.addImportsTo(classSource);
            String unionTypeName = jt.getSimpleName();

            body.addContext("unionType", unionTypeName);
            String convertMethodName = "convert" + unionTypeName;
            body.addContext("methodName", convertMethodName);

            if (first) {
                body.append("if (source instanceof ${unionType}) {");
                first = false;
            } else {
                body.append("} else if (source instanceof ${unionType}) {");
            }
            body.append("    return this.${methodName}((${unionType}) source);");
        }

        // Collect entity names covered by unions
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
            String convertMethodName = "convert" + entityModel.getName();
            body.addContext("methodName", convertMethodName);

            if (first) {
                body.append("if (source instanceof ${entityType}) {");
                first = false;
            } else {
                body.append("} else if (source instanceof ${entityType}) {");
            }
            body.append("    return this.${methodName}((${entityType}) source);");
        }

        if (!first) {
            body.append("}");
        }
        body.append("return null;");

        method.setBody(body.toString());
    }

    private void createEntityConvertMethod(SpecificationVersion specVer,
            SpecificationVersion targetSpecVer,
            JavaClassSource classSource, EntityModel sourceEntityModel) {
        JavaInterfaceSource sourceJavaEntity = lookupJavaEntity(sourceEntityModel);
        classSource.addImport(sourceJavaEntity);

        String sourceEntityType = sourceJavaEntity.getName();
        String entityName = sourceEntityModel.getName();
        String methodName = "convert" + entityName;

        EntityModel targetEntityModel = getState().getConceptIndex()
                .lookupEntity(targetSpecVer.getNamespace() + "." + entityName);

        // Determine return type
        String returnType;
        if (targetEntityModel != null) {
            JavaInterfaceSource targetJavaEntity = lookupJavaEntity(targetEntityModel);
            classSource.addImport(targetJavaEntity);
            returnType = targetJavaEntity.getName();
        } else {
            returnType = "Object";
        }

        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnType(returnType)
                .setPublic();
        method.addParameter(sourceEntityType, "source");

        Collection<PropertyModelWithOrigin> sourceProperties =
                getState().getConceptIndex().getAllEntityProperties(sourceEntityModel);

        // Build target property lookup
        java.util.Map<String, PropertyModelWithOrigin> targetPropertiesByName =
                new java.util.LinkedHashMap<>();
        if (targetEntityModel != null) {
            Collection<PropertyModelWithOrigin> targetProperties =
                    getState().getConceptIndex().getAllEntityProperties(targetEntityModel);
            for (PropertyModelWithOrigin pwo : targetProperties) {
                targetPropertiesByName.put(pwo.getProperty().getName(), pwo);
            }
        }

        JavaTypeFactory jtf = getJavaTypeFactory();
        BodyBuilder body = new BodyBuilder();

        body.append("if (source == null) return null;");
        body.addContext("visitMethod", "visit" + entityName);
        body.addContext("returnType", returnType);
        body.append(returnType + " target = visitor.${visitMethod}(source);");
        body.append("if (target == null) return null;");

        for (PropertyModelWithOrigin propWithOrigin : sourceProperties) {
            PropertyModel property = propWithOrigin.getProperty();
            if (isStarProperty(property) || isRegexProperty(property)) {
                continue;
            }

            String getter = new GetterMethod(property).getName();
            String fieldSuffix = fieldMethodSuffix(property);
            String convertMethodCall = "convert" + entityName + fieldSuffix;

            body.addContext("getter", getter);
            body.addContext("convertMethod", convertMethodCall);
            body.addContext("propertyNameLiteral", property.getName());

            NamespaceModel sourceNs = propWithOrigin.getOrigin().getNamespace();
            PropertyModelWithOrigin targetPropWithOrigin = targetPropertiesByName.get(property.getName());

            body.append("{");
            body.append("    pushProperty(\"${propertyNameLiteral}\");");

            if (isEntity(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), sourceNs);
                jt.addImportsTo(classSource);

                // Find target entity type
                String sourceEntityRefName = property.getResolvedType().getName();
                EntityModel targetRefEntity = getState().getConceptIndex()
                        .lookupEntity(targetSpecVer.getNamespace() + "." + sourceEntityRefName);
                if (targetRefEntity != null) {
                    JavaInterfaceSource targetRefJavaEntity = lookupJavaEntity(targetRefEntity);
                    classSource.addImport(targetRefJavaEntity);
                    body.addContext("targetRefType", targetRefJavaEntity.getName());
                    body.addContext("convertEntity", "convert" + sourceEntityRefName);
                    body.append("    ${targetRefType} converted = ${convertEntity}(source.${getter}());");
                    body.append("    visitor.${convertMethod}(converted, target);");
                } else {
                    body.append("    visitor.${convertMethod}(source.${getter}(), target);");
                }
            } else if (isUnion(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), sourceNs);
                jt.addImportsTo(classSource);
                String unionSimpleName = jt.getSimpleName();

                // Find target union type
                NamespaceModel targetNsModel = getState().getConceptIndex()
                        .lookupNamespace(targetSpecVer.getNamespace());
                JavaType targetUnionJt = findTargetUnionType(jt.getSimpleName(),
                        targetSpecVer.getNamespace(), targetNsModel, jtf);

                if (targetUnionJt != null) {
                    targetUnionJt.addImportsTo(classSource);
                    body.addContext("targetUnionType", targetUnionJt.toJavaTypeString());
                    body.addContext("convertUnion", "convert" + unionSimpleName);
                    body.append("    ${targetUnionType} converted = ${convertUnion}(source.${getter}());");
                    body.append("    visitor.${convertMethod}(converted, target);");
                } else {
                    body.append("    visitor.${convertMethod}(source.${getter}(), target);");
                }
            } else if (isEntityList(property)) {
                ListType listType = (ListType) property.getResolvedType();
                JavaType valueJt = jtf.createJavaType(listType.getValueType(), sourceNs);
                valueJt.addImportsTo(classSource);
                classSource.addImport(java.util.List.class);
                classSource.addImport(java.util.ArrayList.class);

                String sourceEntityRefName = listType.getValueType().getName();
                EntityModel targetRefEntity = getState().getConceptIndex()
                        .lookupEntity(targetSpecVer.getNamespace() + "." + sourceEntityRefName);

                if (targetRefEntity != null) {
                    JavaInterfaceSource targetRefJavaEntity = lookupJavaEntity(targetRefEntity);
                    classSource.addImport(targetRefJavaEntity);
                    body.addContext("valueType", valueJt.toJavaTypeString());
                    body.addContext("targetValueType", targetRefJavaEntity.getName());
                    body.addContext("convertEntity", "convert" + sourceEntityRefName);
                    body.append("    if (source.${getter}() != null) {");
                    body.append("        List<" + targetRefJavaEntity.getName() + "> convertedList = new ArrayList<>();");
                    body.append("        for (" + valueJt.toJavaTypeString() + " item : source.${getter}()) {");
                    body.append("            convertedList.add(${convertEntity}(item));");
                    body.append("        }");
                    body.append("        visitor.${convertMethod}(convertedList, target);");
                    body.append("    }");
                } else {
                    body.append("    visitor.${convertMethod}(source.${getter}(), target);");
                }
            } else if (isUnionList(property)) {
                ListType listType = (ListType) property.getResolvedType();
                JavaType valueJt = jtf.createJavaType(listType.getValueType(), sourceNs);
                valueJt.addImportsTo(classSource);
                classSource.addImport(java.util.List.class);
                classSource.addImport(java.util.ArrayList.class);

                String unionSimpleName = valueJt.getSimpleName();
                NamespaceModel targetNsModel = getState().getConceptIndex()
                        .lookupNamespace(targetSpecVer.getNamespace());
                JavaType targetUnionJt = findTargetUnionType(unionSimpleName,
                        targetSpecVer.getNamespace(), targetNsModel, jtf);

                if (targetUnionJt != null) {
                    targetUnionJt.addImportsTo(classSource);
                    body.addContext("convertUnion", "convert" + unionSimpleName);
                    body.addContext("sourceValueType", valueJt.toJavaTypeString());
                    body.addContext("targetValueType", targetUnionJt.toJavaTypeString());
                    body.append("    if (source.${getter}() != null) {");
                    body.append("        List<${targetValueType}> convertedList = new ArrayList<>();");
                    body.append("        for (${sourceValueType} item : source.${getter}()) {");
                    body.append("            convertedList.add(${convertUnion}(item));");
                    body.append("        }");
                    body.append("        visitor.${convertMethod}(convertedList, target);");
                    body.append("    }");
                } else {
                    body.append("    visitor.${convertMethod}(source.${getter}(), target);");
                }
            } else if (isEntityMap(property)) {
                MapType mapType = (MapType) property.getResolvedType();
                JavaType valueJt = jtf.createJavaType(mapType.getValueType(), sourceNs);
                valueJt.addImportsTo(classSource);
                classSource.addImport(java.util.Map.class);
                classSource.addImport(java.util.LinkedHashMap.class);

                String sourceEntityRefName = mapType.getValueType().getName();
                EntityModel targetRefEntity = getState().getConceptIndex()
                        .lookupEntity(targetSpecVer.getNamespace() + "." + sourceEntityRefName);

                if (targetRefEntity != null) {
                    JavaInterfaceSource targetRefJavaEntity = lookupJavaEntity(targetRefEntity);
                    classSource.addImport(targetRefJavaEntity);
                    body.addContext("valueType", valueJt.toJavaTypeString());
                    body.addContext("targetValueType", targetRefJavaEntity.getName());
                    body.addContext("convertEntity", "convert" + sourceEntityRefName);
                    body.append("    if (source.${getter}() != null) {");
                    body.append("        Map<String, " + targetRefJavaEntity.getName() + "> convertedMap = new LinkedHashMap<>();");
                    body.append("        for (Map.Entry<String, " + valueJt.toJavaTypeString() + "> entry : source.${getter}().entrySet()) {");
                    body.append("            convertedMap.put(entry.getKey(), ${convertEntity}(entry.getValue()));");
                    body.append("        }");
                    body.append("        visitor.${convertMethod}(convertedMap, target);");
                    body.append("    }");
                } else {
                    body.append("    visitor.${convertMethod}(source.${getter}(), target);");
                }
            } else if (isUnionMap(property)) {
                MapType mapType = (MapType) property.getResolvedType();
                JavaType valueJt = jtf.createJavaType(mapType.getValueType(), sourceNs);
                valueJt.addImportsTo(classSource);
                classSource.addImport(java.util.Map.class);
                classSource.addImport(java.util.LinkedHashMap.class);

                String unionSimpleName = valueJt.getSimpleName();
                NamespaceModel targetNsModel = getState().getConceptIndex()
                        .lookupNamespace(targetSpecVer.getNamespace());
                JavaType targetUnionJt = findTargetUnionType(unionSimpleName,
                        targetSpecVer.getNamespace(), targetNsModel, jtf);

                if (targetUnionJt != null) {
                    targetUnionJt.addImportsTo(classSource);
                    body.addContext("convertUnion", "convert" + unionSimpleName);
                    body.addContext("sourceValueType", valueJt.toJavaTypeString());
                    body.addContext("targetValueType", targetUnionJt.toJavaTypeString());
                    body.append("    if (source.${getter}() != null) {");
                    body.append("        Map<String, ${targetValueType}> convertedMap = new LinkedHashMap<>();");
                    body.append("        for (Map.Entry<String, ${sourceValueType}> entry : source.${getter}().entrySet()) {");
                    body.append("            convertedMap.put(entry.getKey(), ${convertUnion}(entry.getValue()));");
                    body.append("        }");
                    body.append("        visitor.${convertMethod}(convertedMap, target);");
                    body.append("    }");
                } else {
                    body.append("    visitor.${convertMethod}(source.${getter}(), target);");
                }
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                JavaType jt = jtf.createJavaType(property.getResolvedType(), sourceNs);
                jt.addImportsTo(classSource);
                body.append("    visitor.${convertMethod}(source.${getter}(), target);");
            }

            body.append("    pop();");
            body.append("}");
        }

        body.append("return target;");
        method.setBody(body.toString());
    }

    private void createUnionConvertMethod(JavaClassSource classSource,
            io.apitomy.umg.models.concept.type.UnionType unionType,
            JavaType unionJt, String sourceNamespace,
            SpecificationVersion targetSpecVer) {
        String unionTypeName = unionJt.getSimpleName();
        String methodName = "convert" + unionTypeName;

        unionJt.addImportsTo(classSource);

        // Find corresponding target union type
        NamespaceModel targetNsModel = getState().getConceptIndex()
                .lookupNamespace(targetSpecVer.getNamespace());
        JavaTypeFactory jtf = getJavaTypeFactory();
        JavaType targetUnionJt = findTargetUnionType(unionTypeName,
                targetSpecVer.getNamespace(), targetNsModel, jtf);

        String returnType;
        if (targetUnionJt != null) {
            targetUnionJt.addImportsTo(classSource);
            returnType = targetUnionJt.toJavaTypeString();
        } else {
            returnType = unionJt.toJavaTypeString();
        }

        MethodSource<JavaClassSource> method = classSource.addMethod()
                .setName(methodName)
                .setReturnType(returnType)
                .setPublic();
        method.addParameter(unionJt.toJavaTypeString(), "source");

        BodyBuilder body = new BodyBuilder();
        body.append("if (source == null) return null;");

        // Let visitor transform the union value before recursing (e.g., true → {})
        String visitorConvertMethod = "convert" + unionTypeName;
        body.addContext("visitorConvertMethod", visitorConvertMethod);
        body.addContext("unionType", unionJt.toJavaTypeString());
        body.append("${unionType} converted = visitor.${visitorConvertMethod}(source);");
        body.append("if (converted == null) return null;");

        // For entity variants: recurse into the converted value
        for (var variantType : unionType.getTypes()) {
            if (variantType.isEntityType()) {
                var entity = ((io.apitomy.umg.models.concept.type.EntityType) variantType).getEntity();
                if (entity == null) {
                    entity = getState().getConceptIndex()
                            .lookupEntity(sourceNamespace, variantType.getName());
                }
                if (entity != null) {
                    JavaInterfaceSource entityInterface = lookupJavaEntity(entity);
                    classSource.addImport(entityInterface);
                    body.addContext("entityType", entityInterface.getName());
                    body.addContext("convertEntity", "convert" + entity.getName());
                    body.append("if (converted instanceof ${entityType}) {");
                    body.append("    return ${convertEntity}((${entityType}) converted);");
                    body.append("}");
                }
            }
        }

        // For primitive variants, return the visitor-converted value
        body.addContext("returnType", returnType);
        if (returnType.equals(unionJt.toJavaTypeString())) {
            body.append("return converted;");
        } else {
            body.append("return (" + returnType + ") converted;");
        }

        method.setBody(body.toString());
    }

    /**
     * Finds a union type in the target namespace that matches the given source union name.
     */
    private JavaType findTargetUnionType(String sourceUnionSimpleName,
            String targetNamespace, NamespaceModel targetNsModel, JavaTypeFactory jtf) {
        if (targetNsModel == null) return null;
        for (var type : getState().getConceptIndex().findTypes(targetNamespace)) {
            if (type instanceof io.apitomy.umg.models.concept.type.UnionType) {
                JavaType jt = jtf.createJavaType(type, targetNsModel);
                if (jt.getSimpleName().equals(sourceUnionSimpleName)) {
                    return jt;
                }
            }
        }
        return null;
    }

    private String fieldMethodSuffix(PropertyModel property) {
        String name = property.getName();
        if (name.startsWith("$")) {
            return name;
        }
        return StringUtils.capitalize(name);
    }

    private static String singularize(String name) {
        return CodeGenContext.singularize(name);
    }
}
