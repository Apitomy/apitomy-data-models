package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.PropertyType;

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
            PropertyType mappedType = PropertyType.builder()
                    .nested(Collections.singleton(property.getType()))
                    .map(true)
                    .build();
            property = PropertyModel.builder().name("_items").type(mappedType).build();
            isStarProperty = true;
        } else if (isRegexProperty(property) && (isEntity(property) || isPrimitive(property))) {
            if (property.getCollection() == null) {
                error("Regex property defined without a collection name: " + javaEntityImpl.getCanonicalName() + "::" + property);
                return;
            }
            PropertyType collectionPropertyType = PropertyType.builder()
                    .nested(Collections.singleton(property.getType()))
                    .map(true)
                    .build();
            property = PropertyModel.builder().name(property.getCollection()).type(collectionPropertyType).build();
        }

        String fieldName = getFieldName(property);
        String fieldType = "String";

        if (fieldName == null) {
            warn("Could not figure out field name for property: " + property);
            return;
        }

        if (isUnion(property)) {
            UnionPropertyType upt = new UnionPropertyType(property.getType());
            upt.addImportsTo(javaEntityImpl);
            fieldType = upt.getName();
        } else {
            JavaType jt = new JavaType(property.getType(), propertyWithOrigin.getOrigin().getNamespace().fullName());
            jt.addImportsTo(javaEntityImpl);
            fieldType = jt.toJavaTypeString();
        }

        FieldSource<JavaClassSource> field = javaEntityImpl.addField().setPrivate().setType(fieldType).setName(fieldName);
        if (isStarProperty) {
            javaEntityImpl.addImport(LinkedHashMap.class);
            field.setLiteralInitializer("new LinkedHashMap<>()");
        }
    }
}
