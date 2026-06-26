package io.apitomy.umg.pipe.java;

import io.apitomy.umg.models.concept.type.UnionVariantComparator;
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
import io.apitomy.umg.models.concept.type.Type;

import java.util.Comparator;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.cloner.CloneEntityPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneListPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.ClonePrimitivePropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneUnionPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneUnionListPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneUnionMapPropertyBlock;
import io.apitomy.umg.index.concept.ConceptIndex;
import io.apitomy.umg.index.java.JavaIndex;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Creates the deep-copy cloner classes. There is a bespoke cloner for each specification
 * version. Each cloner can clone any node in the tree by walking the source node and
 * copying property values directly into a new target node -- avoiding JSON serialization.
 */
public class CreateClonersStage extends AbstractJavaStage implements CodeGenContext {

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

    // --- CodeGenContext implementation (only methods not inherited from AbstractJavaStage) ---

    @Override
    public ConceptIndex getConceptIndex() {
        return getState().getConceptIndex();
    }

    @Override
    public JavaIndex getJavaIndex() {
        return getState().getJavaIndex();
    }

    @Override
    public void warn(String message) {
        super.warn(message);
    }

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
            new ClonePrimitivePropertyBlock(propertyWithOrigin, CreateClonersStage.this).appendTo(body);
        }

        private void handleEntityProperty(BodyBuilder body) {
            new CloneEntityPropertyBlock(propertyWithOrigin, entityModel, clonerClassSource, CreateClonersStage.this).appendTo(body);
        }

        private void handleStarProperty(BodyBuilder body) {
            PropertyModel property = propertyWithOrigin.getProperty();
            if (isEntity(property)) {
                String entityTypeName = entityModel.getNamespace().fullName() + "." + property.getResolvedType().getName();
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
                String entityTypeName = entityModel.getNamespace().fullName() + "." + property.getResolvedType().getName();
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
                body.addContext("valueType", determineValueType(property.getResolvedType()));

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
            new CloneListPropertyBlock(propertyWithOrigin, entityModel, clonerClassSource, CreateClonersStage.this).appendTo(body);
        }

        private void handleMapProperty(BodyBuilder body) {
            new CloneMapPropertyBlock(propertyWithOrigin, entityModel, clonerClassSource, CreateClonersStage.this).appendTo(body);
        }

        private void handleUnionProperty(BodyBuilder body) {
            new CloneUnionPropertyBlock(propertyWithOrigin, entityModel, clonerClassSource, CreateClonersStage.this).appendTo(body);
        }

        private void handleUnionListProperty(BodyBuilder body) {
            new CloneUnionListPropertyBlock(propertyWithOrigin, entityModel, clonerClassSource, CreateClonersStage.this).appendTo(body);
        }

        private void handleUnionMapProperty(BodyBuilder body) {
            new CloneUnionMapPropertyBlock(propertyWithOrigin, entityModel, clonerClassSource, CreateClonersStage.this).appendTo(body);
        }

        private String determineValueType(Type type) {
            if (type.isPrimitiveType()) {
                Class<?> _class = primitiveTypeToClass(type);
                if (_class != null) {
                    clonerClassSource.addImport(_class);
                    return _class.getSimpleName();
                }
            }
            if (type.isListType()) {
                Type listValueType = ((io.apitomy.umg.models.concept.type.ListType) type).getValueType();
                if (listValueType.isPrimitiveType()) {
                    Class<?> _class = primitiveTypeToClass(listValueType);
                    if (_class != null) {
                        clonerClassSource.addImport(_class);
                        return "List<" + _class.getSimpleName() + ">";
                    }
                }
            }
            if (type.isMapType()) {
                Type mapValueType = ((io.apitomy.umg.models.concept.type.MapType) type).getValueType();
                if (mapValueType.isPrimitiveType()) {
                    Class<?> _class = primitiveTypeToClass(mapValueType);
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
