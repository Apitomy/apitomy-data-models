package io.apitomy.umg.pipe.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Creates the deep-copy cloner classes. There is a bespoke cloner for each specification
 * version. Each cloner can clone any node in the tree by walking the source node and
 * copying property values directly into a new target node — avoiding JSON serialization.
 */
public class CreateClonersStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            createCloner(specVersion);
        });
    }

    /**
     * Creates a cloner for the given spec version.
     * @param specVersion
     */
    private void createCloner(SpecificationVersion specVersion) {
        String clonerPackageName = getClonerPackageName(specVersion);
        String clonerClassName = getClonerClassName(specVersion);

        debug("Creating cloner: " + clonerPackageName + "." + clonerClassName);

        JavaClassSource clonerClassSource = Roaster.create(JavaClassSource.class)
                .setPackage(clonerPackageName)
                .setName(clonerClassName)
                .setPublic();
        clonerClassSource.addImport(getState().getConfig().getRootNamespace() + ".util." + "JsonUtil");

        specVersion.getEntities().forEach(entity -> {
            EntityModel entityModel = getState().getConceptIndex().lookupEntity(specVersion.getNamespace() + "." + entity.getName());
            if (entityModel == null) {
                warn("Entity model not found for entity: " + entity);
            } else {
                createCloneMethodFor(specVersion, clonerClassSource, entityModel);
            }
        });

        getState().getJavaIndex().index(clonerClassSource);
    }

    /**
     * Creates a single "cloneXyz" method for the given entity.
     * @param specVersion
     * @param clonerClassSource
     * @param entityModel
     */
    private void createCloneMethodFor(SpecificationVersion specVersion, JavaClassSource clonerClassSource, EntityModel entityModel) {
        String entityFQN = getJavaEntityInterfaceFQN(entityModel);
        String cloneMethodName = cloneMethodName(entityModel);

        debug("Creating clone method: " + cloneMethodName);

        JavaInterfaceSource javaEntity = getState().getJavaIndex().lookupInterface(entityFQN);
        if (javaEntity == null) {
            warn("Java interface for entity not found: " + entityFQN);
            return;
        }

        clonerClassSource.addImport(javaEntity);
        MethodSource<JavaClassSource> methodSource = clonerClassSource.addMethod()
                .setName(cloneMethodName)
                .setReturnTypeVoid()
                .setPublic();
        methodSource.addParameter(javaEntity.getName(), "source");
        methodSource.addParameter(javaEntity.getName(), "target");

        BodyBuilder body = new BodyBuilder();

        Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entityModel);
        allProperties.forEach(property -> {
            createClonePropertyCode(body, property, entityModel, javaEntity, clonerClassSource);
        });

        createCloneExtraPropertiesCode(body, clonerClassSource);

        methodSource.setBody(body.toString());
    }

    private void createClonePropertyCode(BodyBuilder body, PropertyModelWithOrigin property, EntityModel entityModel,
            JavaInterfaceSource javaEntity, JavaClassSource clonerClassSource) {
        CreateCloneProperty ccp = new CreateCloneProperty(property, entityModel, javaEntity, clonerClassSource);
        body.clearContext();
        ccp.writeTo(body);
    }

    private void createCloneExtraPropertiesCode(BodyBuilder body, JavaClassSource clonerClassSource) {
        clonerClassSource.addImport(JsonNode.class);
        clonerClassSource.addImport(List.class);
        body.append("{");
        body.append("    List<String> extraPropertyNames = source.getExtraPropertyNames();");
        body.append("    if (extraPropertyNames != null) {");
        body.append("        extraPropertyNames.forEach(name -> {");
        body.append("            JsonNode value = source.getExtraProperty(name);");
        body.append("            if (value != null) {");
        body.append("                target.addExtraProperty(name, JsonUtil.clone(value));");
        body.append("            }");
        body.append("        });");
        body.append("    }");
        body.append("}");
    }

    private static String cloneMethodName(EntityModel entityModel) {
        return "clone" + entityModel.getName();
    }

    @Data
    @AllArgsConstructor
    private class CreateCloneProperty {
        PropertyModelWithOrigin propertyWithOrigin;
        EntityModel entityModel;
        JavaInterfaceSource javaEntity;
        JavaClassSource clonerClassSource;

        /**
         * Generates code to clone a property from source to target.
         * @param body
         */
        public void writeTo(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isStarProperty(property)) {
                handleStarProperty(body);
            } else if (isRegexProperty(property)) {
                handleRegexProperty(body);
            } else if (handleViaResolvedType(body)) {
                // Handled by resolved type dispatch
            } else {
                warn("Entity property '" + property.getName() + "' not cloned (unsupported) for entity: " + entityModel.fullyQualifiedName());
            }
        }

        private boolean handleViaResolvedType(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            var resolved = property.getResolvedType();
            if (resolved == null) return false;

            if (resolved instanceof io.apitomy.umg.models.concept.type.UnionType) {
                handleUnionProperty(body);
                return true;
            }

            if (resolved instanceof io.apitomy.umg.models.concept.type.ListType lt
                    && lt.getValueType() instanceof io.apitomy.umg.models.concept.type.UnionType) {
                handleUnionListProperty(body);
                return true;
            }

            if (resolved instanceof io.apitomy.umg.models.concept.type.MapType mt
                    && mt.getValueType() instanceof io.apitomy.umg.models.concept.type.UnionType) {
                handleUnionMapProperty(body);
                return true;
            }

            if (resolved.isEntityType()) {
                handleEntityProperty(body);
                return true;
            }
            if (resolved.isPrimitiveType()) {
                handlePrimitiveProperty(body);
                return true;
            }
            if (resolved.isListType()) {
                handleListProperty(body);
                return true;
            }
            if (resolved.isMapType()) {
                handleMapProperty(body);
                return true;
            }

            return false;
        }

        private void handlePrimitiveProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            body.addContext("getterMethodName", getterMethodName(property));
            body.addContext("setterMethodName", setterMethodName(property));
            body.append("target.${setterMethodName}(source.${getterMethodName}());");
        }

        private void handleEntityProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            String propertyTypeEntityName = entityModel.getNamespace().fullName() + "." + property.getType().getSimpleType();
            EntityModel propertyTypeEntity = getState().getConceptIndex().lookupEntity(propertyTypeEntityName);
            if (propertyTypeEntity == null) {
                warn("Property entity type not found for property: '" + property.getName() + "' of entity: " + entityModel.fullyQualifiedName());
                return;
            }
            JavaInterfaceSource propertyTypeJavaEntity = resolveJavaEntityType(entityModel.getNamespace(), property);
            clonerClassSource.addImport(propertyTypeJavaEntity);

            body.addContext("getterMethodName", getterMethodName(property));
            body.addContext("setterMethodName", setterMethodName(property));
            body.addContext("createMethodName", createMethodName(propertyTypeEntity));
            body.addContext("cloneMethodName", cloneMethodName(propertyTypeEntity));
            body.addContext("entityType", propertyTypeJavaEntity.getName());

            body.append("{");
            body.append("    if (source.${getterMethodName}() != null) {");
            body.append("        target.${setterMethodName}(target.${createMethodName}());");
            body.append("        this.${cloneMethodName}((${entityType}) source.${getterMethodName}(), (${entityType}) target.${getterMethodName}());");
            body.append("    }");
            body.append("}");
        }

        private void handleStarProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isEntity(property)) {
                String entityTypeName = entityModel.getNamespace().fullName() + "." + property.getType().getSimpleType();
                EntityModel propertyTypeEntity = getState().getConceptIndex().lookupEntity(entityTypeName);
                if (propertyTypeEntity == null) {
                    warn("STAR Property entity type not found for property: '" + property.getName() + "' of entity: " + entityModel.fullyQualifiedName());
                    return;
                }
                JavaInterfaceSource propertyTypeJavaEntity = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(propertyTypeEntity));
                if (propertyTypeJavaEntity == null) {
                    warn("STAR Entity property '" + property.getName() + "' not cloned for entity: " + entityModel.fullyQualifiedName());
                    return;
                }
                clonerClassSource.addImport(propertyTypeJavaEntity);
                clonerClassSource.addImport(List.class);

                body.addContext("entityJavaType", propertyTypeJavaEntity.getName());
                body.addContext("createMethodName", createMethodName(propertyTypeEntity));
                body.addContext("cloneMethodName", cloneMethodName(propertyTypeEntity));

                body.append("{");
                body.append("    List<String> itemNames = source.getItemNames();");
                body.append("    if (itemNames != null) {");
                body.append("        itemNames.forEach(name -> {");
                body.append("            ${entityJavaType} srcItem = (${entityJavaType}) source.getItem(name);");
                body.append("            if (srcItem != null) {");
                body.append("                ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();");
                body.append("                this.${cloneMethodName}(srcItem, tgtItem);");
                body.append("                target.addItem(name, tgtItem);");
                body.append("            }");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                clonerClassSource.addImport(List.class);

                body.append("{");
                body.append("    List<String> itemNames = source.getItemNames();");
                body.append("    if (itemNames != null) {");
                body.append("        itemNames.forEach(name -> {");
                body.append("            target.addItem(name, source.getItem(name));");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else {
                warn("STAR Entity property '" + property.getName() + "' not cloned (unhandled) for entity: " + entityModel.fullyQualifiedName());
            }
        }

        private void handleRegexProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isEntity(property)) {
                String entityTypeName = entityModel.getNamespace().fullName() + "." + property.getType().getSimpleType();
                EntityModel propertyTypeEntity = getState().getConceptIndex().lookupEntity(entityTypeName);
                if (propertyTypeEntity == null) {
                    warn("REGEX Property entity type not found for property: '" + property.getName() + "' of entity: " + entityModel.fullyQualifiedName());
                    return;
                }
                JavaInterfaceSource propertyTypeJavaEntity = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(propertyTypeEntity));
                if (propertyTypeJavaEntity == null) {
                    warn("REGEX Entity property '" + property.getName() + "' not cloned for entity: " + entityModel.fullyQualifiedName());
                    return;
                }
                JavaInterfaceSource commonEntityTypeJavaModel = resolveCommonJavaEntity(propertyTypeEntity);

                clonerClassSource.addImport(propertyTypeJavaEntity);
                clonerClassSource.addImport(commonEntityTypeJavaModel);
                clonerClassSource.addImport(Map.class);

                body.addContext("entityJavaType", propertyTypeJavaEntity.getName());
                body.addContext("commonEntityType", commonEntityTypeJavaModel.getName());
                body.addContext("getterMethodName", getterMethodName(property));
                body.addContext("createMethodName", createMethodName(propertyTypeEntity));
                body.addContext("cloneMethodName", cloneMethodName(propertyTypeEntity));
                body.addContext("addMethodName", addMethodName(singularize(property.getCollection())));

                body.append("{");
                body.append("    Map<String, ? extends ${commonEntityType}> srcMap = source.${getterMethodName}();");
                body.append("    if (srcMap != null && !srcMap.isEmpty()) {");
                body.append("        srcMap.keySet().forEach(name -> {");
                body.append("            ${entityJavaType} srcItem = (${entityJavaType}) srcMap.get(name);");
                body.append("            ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();");
                body.append("            this.${cloneMethodName}(srcItem, tgtItem);");
                body.append("            target.${addMethodName}(name, tgtItem);");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else if (isPrimitive(property) || isPrimitiveList(property) || isPrimitiveMap(property)) {
                clonerClassSource.addImport(Map.class);

                body.addContext("getterMethodName", getterMethodName(property));
                body.addContext("addMethodName", addMethodName(singularize(property.getCollection())));
                body.addContext("valueType", determineValueType(property.getType()));

                clonerClassSource.addImport(List.class);
                body.append("{");
                body.append("    Map<String, ${valueType}> srcMap = source.${getterMethodName}();");
                body.append("    if (srcMap != null && !srcMap.isEmpty()) {");
                body.append("        List<String> keys = new java.util.ArrayList<>(srcMap.keySet());");
                body.append("        keys.forEach(name -> {");
                body.append("            target.${addMethodName}(name, srcMap.get(name));");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else {
                warn("REGEX Entity property '" + property.getName() + "' not cloned (unhandled) for entity: " + entityModel.fullyQualifiedName());
            }
        }

        private void handleListProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            PropertyType listValuePropertyType = property.getType().getNested().iterator().next();

            body.addContext("getterMethodName", getterMethodName(property));

            if (listValuePropertyType.isPrimitiveType()) {
                clonerClassSource.addImport(List.class);
                clonerClassSource.addImport(ArrayList.class);
                body.addContext("setterMethodName", setterMethodName(property));
                body.addContext("valueType", determineValueType(listValuePropertyType));

                body.append("{");
                body.append("    List<${valueType}> srcList = source.${getterMethodName}();");
                body.append("    if (srcList != null) {");
                body.append("        target.${setterMethodName}(new ArrayList<>(srcList));");
                body.append("    }");
                body.append("}");
            } else if (listValuePropertyType.isEntityType()) {
                String entityTypeName = listValuePropertyType.getSimpleType();
                String fqEntityName = entityModel.getNamespace().fullName() + "." + entityTypeName;
                EntityModel entityTypeModel = getState().getConceptIndex().lookupEntity(fqEntityName);
                if (entityTypeModel == null) {
                    warn("LIST Entity property '" + property.getName() + "' not cloned for entity: " + entityModel.fullyQualifiedName());
                    return;
                }
                JavaInterfaceSource entityTypeJavaModel = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(entityTypeModel));
                if (entityTypeJavaModel == null) {
                    warn("LIST Entity property '" + property.getName() + "' not cloned (java) for entity: " + entityModel.fullyQualifiedName());
                    return;
                }
                JavaInterfaceSource commonEntityTypeJavaModel = resolveCommonJavaEntity(entityTypeModel);
                clonerClassSource.addImport(entityTypeJavaModel);
                clonerClassSource.addImport(commonEntityTypeJavaModel);
                clonerClassSource.addImport(List.class);

                body.addContext("entityJavaType", entityTypeJavaModel.getName());
                body.addContext("commonEntityType", commonEntityTypeJavaModel.getName());
                body.addContext("createMethodName", createMethodName(entityTypeModel));
                body.addContext("cloneMethodName", cloneMethodName(entityTypeModel));
                body.addContext("addMethodName", addMethodName(singularize(property.getName())));

                body.append("{");
                body.append("    List<? extends ${commonEntityType}> srcList = source.${getterMethodName}();");
                body.append("    if (srcList != null && !srcList.isEmpty()) {");
                body.append("        srcList.forEach(srcItem -> {");
                body.append("            ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();");
                body.append("            this.${cloneMethodName}((${entityJavaType}) srcItem, tgtItem);");
                body.append("            target.${addMethodName}(tgtItem);");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else {
                warn("LIST Entity property '" + property.getName() + "' not cloned (unsupported) for entity: " + entityModel.fullyQualifiedName());
            }
        }

        private void handleMapProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            PropertyType mapValuePropertyType = property.getType().getNested().iterator().next();

            body.addContext("getterMethodName", getterMethodName(property));

            if (mapValuePropertyType.isPrimitiveType()) {
                clonerClassSource.addImport(Map.class);
                clonerClassSource.addImport(LinkedHashMap.class);
                body.addContext("setterMethodName", setterMethodName(property));
                body.addContext("valueType", determineValueType(mapValuePropertyType));

                body.append("{");
                body.append("    Map<String, ${valueType}> srcMap = source.${getterMethodName}();");
                body.append("    if (srcMap != null && !srcMap.isEmpty()) {");
                body.append("        target.${setterMethodName}(new LinkedHashMap<>(srcMap));");
                body.append("    }");
                body.append("}");
            } else if (mapValuePropertyType.isEntityType()) {
                String entityTypeName = mapValuePropertyType.getSimpleType();
                String fqEntityName = entityModel.getNamespace().fullName() + "." + entityTypeName;
                EntityModel entityTypeModel = getState().getConceptIndex().lookupEntity(fqEntityName);
                if (entityTypeModel == null) {
                    warn("MAP Entity property '" + property.getName() + "' not cloned for entity: " + entityModel.fullyQualifiedName());
                    return;
                }
                JavaInterfaceSource entityTypeJavaModel = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(entityTypeModel));
                if (entityTypeJavaModel == null) {
                    warn("MAP Entity property '" + property.getName() + "' not cloned (java) for entity: " + entityModel.fullyQualifiedName());
                    return;
                }
                JavaInterfaceSource commonEntityTypeJavaModel = resolveCommonJavaEntity(entityTypeModel);

                clonerClassSource.addImport(Map.class);
                clonerClassSource.addImport(entityTypeJavaModel);
                clonerClassSource.addImport(commonEntityTypeJavaModel);

                body.addContext("entityJavaType", entityTypeJavaModel.getName());
                body.addContext("commonEntityType", commonEntityTypeJavaModel.getName());
                body.addContext("createMethodName", "create" + entityTypeName);
                body.addContext("cloneMethodName", "clone" + entityTypeName);
                body.addContext("addMethodName", addMethodName(singularize(property.getName())));

                body.append("{");
                body.append("    Map<String, ? extends ${commonEntityType}> srcMap = source.${getterMethodName}();");
                body.append("    if (srcMap != null && !srcMap.isEmpty()) {");
                body.append("        srcMap.keySet().forEach(name -> {");
                body.append("            ${entityJavaType} tgtItem = (${entityJavaType}) target.${createMethodName}();");
                body.append("            this.${cloneMethodName}((${entityJavaType}) srcMap.get(name), tgtItem);");
                body.append("            target.${addMethodName}(name, tgtItem);");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else {
                warn("MAP Entity property '" + property.getName() + "' not cloned (unsupported) for entity: " + entityModel.fullyQualifiedName());
            }
        }

        private PropertyType getEffectiveUnionType(PropertyModel property) {
            if (property.getType().isUnion()) {
                return property.getType();
            }
            var resolved = property.getResolvedType();
            if (resolved != null && resolved.getRawType() != null) {
                return PropertyType.parse(resolved.getRawType().asRawType());
            }
            return property.getType();
        }

        private String getUnionJavaTypeName(PropertyModel property, UnionPropertyType ut) {
            var resolved = property.getResolvedType();
            if (resolved instanceof io.apitomy.umg.models.concept.type.UnionType unionType
                    && unionType.getAliasName() != null) {
                var nsModel = propertyWithOrigin.getOrigin().getNamespace();
                var jt = getJavaTypeFactory().createJavaType(resolved, nsModel);
                jt.addImportsTo(clonerClassSource);
                return jt.getSimpleName();
            }
            return ut.toJavaTypeString();
        }

        private void handleUnionProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            NamespaceModel nsContext = propertyWithOrigin.getOrigin().getNamespace();
            UnionPropertyType ut = new UnionPropertyType(getEffectiveUnionType(property));

            body.addContext("unionJavaType", getUnionJavaTypeName(property, ut));
            body.addContext("getterMethodName", getterMethodName(property));
            body.addContext("setterMethodName", setterMethodName(property));

            body.append("{");
            body.append("    ${unionJavaType} srcUnion = source.${getterMethodName}();");
            body.append("    if (srcUnion != null) {");

            ut.getNestedTypes().forEach(nestedType -> {
                String typeName = getTypeName(nestedType);
                String isMethodName = "is" + typeName;
                String asMethodName = "as" + typeName;
                JavaType jt = new JavaType(nestedType, nsContext);

                body.addContext("isMethodName", isMethodName);
                body.addContext("asMethodName", asMethodName);

                body.append("        if (srcUnion.${isMethodName}()) {");

                if (jt.isPrimitive()) {
                    String unionValueInterfaceFQN = getUnionTypeFQN(typeName + "UnionValue");
                    String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                    JavaInterfaceSource unionValueInterface = getState().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                    JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                    if (unionValueInterface != null && unionValueClass != null) {
                        clonerClassSource.addImport(unionValueInterface);
                        clonerClassSource.addImport(unionValueClass);
                        body.addContext("unionValueInterfaceName", unionValueInterface.getName());
                        body.addContext("unionValueClassName", unionValueClass.getName());
                        body.append("            target.${setterMethodName}(new ${unionValueClassName}(srcUnion.${asMethodName}()));");
                    }
                } else if (jt.isPrimitiveList()) {
                    String unionValueName = getTypeName(nestedType);
                    String unionValueInterfaceFQN = getUnionTypeFQN(unionValueName + "UnionValue");
                    String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                    JavaInterfaceSource unionValueInterface = getState().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                    JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                    if (unionValueInterface != null && unionValueClass != null) {
                        clonerClassSource.addImport(unionValueInterface);
                        clonerClassSource.addImport(unionValueClass);
                        clonerClassSource.addImport(ArrayList.class);
                        body.addContext("unionValueInterfaceName", unionValueInterface.getName());
                        body.addContext("unionValueClassName", unionValueClass.getName());
                        body.append("            target.${setterMethodName}(new ${unionValueClassName}(new ArrayList<>(srcUnion.${asMethodName}())));");
                    }
                } else if (jt.isEntity()) {
                    NamespaceModel nestedTypeEntityNS = entityModel.getNamespace();
                    String nestedTypeEntityName = nestedTypeEntityNS.fullName() + "." + nestedType.getSimpleType();
                    EntityModel nestedTypeEntity = getState().getConceptIndex().lookupEntity(nestedTypeEntityName);
                    if (nestedTypeEntity != null) {
                        JavaInterfaceSource entityJavaSource = resolveJavaEntityType(nestedTypeEntityNS, nestedType);
                        JavaClassSource entityImplSource = lookupJavaEntityImpl(getJavaEntityClassFQN(nestedTypeEntity));
                        if (entityJavaSource != null && entityImplSource != null) {
                            clonerClassSource.addImport(entityJavaSource);
                            clonerClassSource.addImport(entityImplSource);
                            body.addContext("entityType", entityJavaSource.getName());
                            body.addContext("entityImplType", entityImplSource.getName());
                            body.addContext("cloneMethodName", cloneMethodName(nestedTypeEntity));
                            body.append("            ${entityType} tgtEntity = new ${entityImplType}();");
                            body.append("            this.${cloneMethodName}((${entityType}) srcUnion.${asMethodName}(), tgtEntity);");
                            body.append("            target.${setterMethodName}(tgtEntity);");
                        }
                    }
                } else if (jt.isEntityList()) {
                    PropertyType listItemType = nestedType.getNested().iterator().next();
                    String listItemEntityName = entityModel.getNamespace().fullName() + "." + listItemType.getSimpleType();
                    EntityModel listItemEntity = getState().getConceptIndex().lookupEntity(listItemEntityName);
                    if (listItemEntity != null) {
                        JavaInterfaceSource listItemEntitySource = getState().getJavaIndex().lookupInterface(getJavaEntityInterfaceFQN(listItemEntity));
                        if (listItemEntitySource != null) {
                            String unionValueName = getTypeName(nestedType);
                            String unionValueInterfaceFQN = getUnionTypeFQN(unionValueName + "UnionValue");
                            String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                            JavaInterfaceSource unionValueInterface = getState().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                            JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                            if (unionValueInterface == null) {
                                unionValueInterface = getState().getJavaIndex().findInterfaceBySimpleName(unionValueName + "UnionValue");
                            }
                            if (unionValueClass == null) {
                                unionValueClass = getState().getJavaIndex().findClassBySimpleName(unionValueName + "UnionValueImpl");
                            }
                            if (unionValueInterface != null && unionValueClass != null) {
                                clonerClassSource.addImport(listItemEntitySource);
                                clonerClassSource.addImport(unionValueInterface);
                                clonerClassSource.addImport(unionValueClass);
                                clonerClassSource.addImport(List.class);
                                clonerClassSource.addImport(ArrayList.class);
                                body.addContext("listItemType", listItemEntitySource.getName());
                                body.addContext("createMethodName", createMethodName(listItemEntity));
                                body.addContext("cloneMethodName", cloneMethodName(listItemEntity));
                                body.addContext("unionValueClassName", unionValueClass.getName());
                                body.append("            List<${listItemType}> clonedList = new ArrayList<>();");
                                body.append("            srcUnion.${asMethodName}().forEach(srcItem -> {");
                                body.append("                ${listItemType} tgtItem = (${listItemType}) target.${createMethodName}();");
                                body.append("                this.${cloneMethodName}((${listItemType}) srcItem, tgtItem);");
                                body.append("                clonedList.add(tgtItem);");
                                body.append("            });");
                                body.append("            @SuppressWarnings({ \"unchecked\", \"rawtypes\" })");
                                body.append("            ${unionValueClassName} unionValue = new ${unionValueClassName}((List) clonedList);");
                                body.append("            target.${setterMethodName}(unionValue);");
                            }
                        }
                    }
                } else {
                    warn("UNION property '" + property.getName() + "' nested type not cloned (unsupported): " + nestedType);
                }

                body.append("        }");
            });

            body.append("    }");
            body.append("}");

            var resolved2 = property.getResolvedType();
            boolean isAlias = resolved2 instanceof io.apitomy.umg.models.concept.type.UnionType ut2
                    && ut2.getAliasName() != null;
            if (!isAlias) {
                ut.addImportsTo(clonerClassSource);
            }
        }

        private void handleUnionListProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            NamespaceModel nsContext = propertyWithOrigin.getOrigin().getNamespace();
            PropertyType unionType;
            var resolved = property.getResolvedType();
            if (resolved instanceof io.apitomy.umg.models.concept.type.ListType lt
                    && lt.getValueType().getRawType() != null) {
                unionType = PropertyType.parse(lt.getValueType().getRawType().asRawType());
            } else {
                unionType = property.getType().getNested().iterator().next();
            }
            UnionPropertyType ut = new UnionPropertyType(unionType);

            clonerClassSource.addImport(List.class);

            String unionJavaType;
            if (resolved instanceof io.apitomy.umg.models.concept.type.ListType lt2
                    && lt2.getValueType() instanceof io.apitomy.umg.models.concept.type.UnionType vut
                    && vut.getAliasName() != null) {
                var nsModel = propertyWithOrigin.getOrigin().getNamespace();
                var jt = getJavaTypeFactory().createJavaType(vut, nsModel);
                jt.addImportsTo(clonerClassSource);
                unionJavaType = jt.getSimpleName();
            } else {
                ut.addImportsTo(clonerClassSource);
                unionJavaType = ut.toJavaTypeString();
            }
            body.addContext("unionJavaType", unionJavaType);
            body.addContext("getterMethodName", getterMethodName(property));
            body.addContext("addMethodName", addMethodName(singularize(property.getName())));

            body.append("{");
            body.append("    List<${unionJavaType}> srcList = source.${getterMethodName}();");
            body.append("    if (srcList != null && !srcList.isEmpty()) {");
            body.append("        srcList.forEach(srcUnion -> {");

            ut.getNestedTypes().forEach(nestedType -> {
                String typeName = getTypeName(nestedType);
                String isMethodName = "is" + typeName;
                String asMethodName = "as" + typeName;
                JavaType jt = new JavaType(nestedType, nsContext);

                body.addContext("isMethodName", isMethodName);
                body.addContext("asMethodName", asMethodName);

                body.append("            if (srcUnion.${isMethodName}()) {");

                if (jt.isPrimitive()) {
                    String unionValueInterfaceFQN = getUnionTypeFQN(typeName + "UnionValue");
                    String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                    JavaInterfaceSource unionValueInterface = getState().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                    JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                    if (unionValueInterface != null && unionValueClass != null) {
                        clonerClassSource.addImport(unionValueInterface);
                        clonerClassSource.addImport(unionValueClass);
                        body.addContext("unionValueClassName", unionValueClass.getName());
                        body.append("                target.${addMethodName}(new ${unionValueClassName}(srcUnion.${asMethodName}()));");
                    }
                } else if (jt.isEntity()) {
                    NamespaceModel nestedTypeEntityNS = entityModel.getNamespace();
                    String nestedTypeEntityName = nestedTypeEntityNS.fullName() + "." + nestedType.getSimpleType();
                    EntityModel nestedTypeEntity = getState().getConceptIndex().lookupEntity(nestedTypeEntityName);
                    if (nestedTypeEntity != null) {
                        JavaInterfaceSource entityJavaSource = resolveJavaEntityType(nestedTypeEntityNS, nestedType);
                        JavaClassSource entityImplSource = lookupJavaEntityImpl(getJavaEntityClassFQN(nestedTypeEntity));
                        if (entityJavaSource != null && entityImplSource != null) {
                            clonerClassSource.addImport(entityJavaSource);
                            clonerClassSource.addImport(entityImplSource);
                            body.addContext("entityType", entityJavaSource.getName());
                            body.addContext("entityImplType", entityImplSource.getName());
                            body.addContext("cloneMethodName", cloneMethodName(nestedTypeEntity));
                            body.append("                ${entityType} tgtItem = new ${entityImplType}();");
                            body.append("                this.${cloneMethodName}((${entityType}) srcUnion.${asMethodName}(), tgtItem);");
                            body.append("                target.${addMethodName}(tgtItem);");
                        }
                    }
                } else {
                    warn("UNION LIST property '" + property.getName() + "' nested type not cloned (unsupported): " + nestedType);
                }

                body.append("            }");
            });

            body.append("        });");
            body.append("    }");
            body.append("}");
        }

        private void handleUnionMapProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            NamespaceModel nsContext = propertyWithOrigin.getOrigin().getNamespace();
            PropertyType unionType;
            var resolved = property.getResolvedType();
            if (resolved instanceof io.apitomy.umg.models.concept.type.MapType mt
                    && mt.getValueType().getRawType() != null) {
                unionType = PropertyType.parse(mt.getValueType().getRawType().asRawType());
            } else {
                unionType = property.getType().getNested().iterator().next();
            }
            UnionPropertyType ut = new UnionPropertyType(unionType);

            clonerClassSource.addImport(Map.class);

            String unionJavaType;
            if (resolved instanceof io.apitomy.umg.models.concept.type.MapType mt2
                    && mt2.getValueType() instanceof io.apitomy.umg.models.concept.type.UnionType vut
                    && vut.getAliasName() != null) {
                var nsModel = propertyWithOrigin.getOrigin().getNamespace();
                var jt = getJavaTypeFactory().createJavaType(vut, nsModel);
                jt.addImportsTo(clonerClassSource);
                unionJavaType = jt.getSimpleName();
            } else {
                ut.addImportsTo(clonerClassSource);
                unionJavaType = ut.toJavaTypeString();
            }
            body.addContext("unionJavaType", unionJavaType);
            body.addContext("getterMethodName", getterMethodName(property));
            body.addContext("addMethodName", addMethodName(singularize(property.getName())));

            body.append("{");
            body.append("    Map<String, ${unionJavaType}> srcMap = source.${getterMethodName}();");
            body.append("    if (srcMap != null && !srcMap.isEmpty()) {");
            body.append("        srcMap.keySet().forEach(key -> {");
            body.append("            ${unionJavaType} srcUnion = srcMap.get(key);");

            ut.getNestedTypes().forEach(nestedType -> {
                String typeName = getTypeName(nestedType);
                String isMethodName = "is" + typeName;
                String asMethodName = "as" + typeName;
                JavaType jt = new JavaType(nestedType, nsContext);

                body.addContext("isMethodName", isMethodName);
                body.addContext("asMethodName", asMethodName);

                body.append("            if (srcUnion.${isMethodName}()) {");

                if (jt.isPrimitive()) {
                    String unionValueInterfaceFQN = getUnionTypeFQN(typeName + "UnionValue");
                    String unionValueClassFQN = unionValueInterfaceFQN + "Impl";
                    JavaInterfaceSource unionValueInterface = getState().getJavaIndex().lookupInterface(unionValueInterfaceFQN);
                    JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                    if (unionValueInterface != null && unionValueClass != null) {
                        clonerClassSource.addImport(unionValueInterface);
                        clonerClassSource.addImport(unionValueClass);
                        body.addContext("unionValueClassName", unionValueClass.getName());
                        body.append("                target.${addMethodName}(key, new ${unionValueClassName}(srcUnion.${asMethodName}()));");
                    }
                } else if (jt.isEntity()) {
                    NamespaceModel nestedTypeEntityNS = entityModel.getNamespace();
                    String nestedTypeEntityName = nestedTypeEntityNS.fullName() + "." + nestedType.getSimpleType();
                    EntityModel nestedTypeEntity = getState().getConceptIndex().lookupEntity(nestedTypeEntityName);
                    if (nestedTypeEntity != null) {
                        JavaInterfaceSource entityJavaSource = resolveJavaEntityType(nestedTypeEntityNS, nestedType);
                        JavaClassSource entityImplSource = lookupJavaEntityImpl(getJavaEntityClassFQN(nestedTypeEntity));
                        if (entityJavaSource != null && entityImplSource != null) {
                            clonerClassSource.addImport(entityJavaSource);
                            clonerClassSource.addImport(entityImplSource);
                            body.addContext("entityType", entityJavaSource.getName());
                            body.addContext("entityImplType", entityImplSource.getName());
                            body.addContext("cloneMethodName", cloneMethodName(nestedTypeEntity));
                            body.append("                ${entityType} tgtItem = new ${entityImplType}();");
                            body.append("                this.${cloneMethodName}((${entityType}) srcUnion.${asMethodName}(), tgtItem);");
                            body.append("                target.${addMethodName}(key, tgtItem);");
                        }
                    }
                } else if (nestedType.isList()) {
                    String unionValueClassFQN = getUnionTypeFQN(typeName + "UnionValueImpl");
                    JavaClassSource unionValueClass = getState().getJavaIndex().lookupClass(unionValueClassFQN);
                    if (unionValueClass != null) {
                        clonerClassSource.addImport(unionValueClass);
                        clonerClassSource.addImport(java.util.ArrayList.class);
                        body.addContext("unionValueClassName", unionValueClass.getName());
                        body.append("                target.${addMethodName}(key, new ${unionValueClassName}(new java.util.ArrayList<>(srcUnion.${asMethodName}())));");
                    }
                } else {
                    warn("UNION MAP property '" + property.getName() + "' nested type not cloned (unsupported): " + nestedType);
                }

                body.append("            }");
            });

            body.append("        });");
            body.append("    }");
            body.append("}");
        }

        private String determineValueType(PropertyType type) {
            if (type.isPrimitiveType()) {
                Class<?> _class = primitiveTypeToClass(type);
                if (_class != null) {
                    clonerClassSource.addImport(_class);
                    return _class.getSimpleName();
                }
            }
            if (type.isList()) {
                PropertyType listType = type.getNested().iterator().next();
                if (listType.isPrimitiveType()) {
                    Class<?> _class = primitiveTypeToClass(listType);
                    if (_class != null) {
                        clonerClassSource.addImport(_class);
                        return "List<" + _class.getSimpleName() + ">";
                    }
                }
            }
            if (type.isMap()) {
                PropertyType mapType = type.getNested().iterator().next();
                if (mapType.isPrimitiveType()) {
                    Class<?> _class = primitiveTypeToClass(mapType);
                    if (_class != null) {
                        clonerClassSource.addImport(_class);
                        return "Map<String, " + _class.getSimpleName() + ">";
                    }
                }
            }
            return "Object";
        }
    }
}
