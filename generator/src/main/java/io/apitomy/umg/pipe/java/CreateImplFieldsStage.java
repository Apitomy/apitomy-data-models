package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.MapType;

/**
 * Creates the fields for each entity implementation.  This is done by iterating over all leaf entities
 * and collecting all the properties for it.  One field is created for each property.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateImplFieldsStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            createEntityImplFields(entity);
        });
    }

    private void createEntityImplFields(EntityModel entity) {
        JavaClassSource javaEntityImpl = lookupJavaEntityImpl(entity);
        Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entity);

        allProperties.forEach(property -> {
            createEntityImplField(javaEntityImpl, property);
        });
    }

    private void createEntityImplField(JavaClassSource javaEntityImpl, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        boolean isStarProperty = false;
        if (isStarProperty(property)) {
            var collectionResolvedType = MapType.builder()
                    .namespace(property.getResolvedType().getNamespace())
                    .name("{" + property.getResolvedType().getName() + "}")
                    .valueType(property.getResolvedType())
                    .build();
            property = PropertyModel.builder().name("_items").resolvedType(collectionResolvedType).build();
            isStarProperty = true;
        } else if (isRegexProperty(property) && (isEntity(property) || isPrimitive(property))) {
            if (property.getCollection() == null) {
                error("Regex property defined without a collection name: " + javaEntityImpl.getCanonicalName() + "::" + property);
                return;
            }
            var collectionResolvedType = MapType.builder()
                    .namespace(property.getResolvedType().getNamespace())
                    .name("{" + property.getResolvedType().getName() + "}")
                    .valueType(property.getResolvedType())
                    .build();
            property = PropertyModel.builder().name(property.getCollection()).resolvedType(collectionResolvedType).build();
        }

        String fieldName = getFieldName(property);
        String fieldType = "String";

        if (fieldName == null) {
            warn("Could not figure out field name for property: " + property);
            return;
        }

        var jt = getJavaTypeFactory().createJavaType(
                property.getResolvedType(), propertyWithOrigin.getOrigin().getNamespace());
        jt.addImportsTo(javaEntityImpl);
        fieldType = jt.toJavaTypeString();

        FieldSource<JavaClassSource> field = javaEntityImpl.addField().setPrivate().setType(fieldType).setName(fieldName);
        if (isStarProperty) {
            javaEntityImpl.addImport(LinkedHashMap.class);
            field.setLiteralInitializer("new LinkedHashMap<>()");
        }
    }
}
