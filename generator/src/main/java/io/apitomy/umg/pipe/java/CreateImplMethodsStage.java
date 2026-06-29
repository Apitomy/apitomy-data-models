package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.MappedNodeMethods;

/**
 * Creates methods on all entity implementation classes.  This follows the same algorithm
 * as {@link CreateInterfaceMethodsStage} except it rolls up all of the methods from the
 * entire entity and trait hierarchies so that the Impl class can implement them.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateImplMethodsStage extends AbstractCreateMethodsStage {

    @Override
    protected void doProcess() {
        initContext();
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            createEntityImplMethods(entity);
        });
    }

    /**
     * Creates implementations of all methods needed for an implementation of the given
     * entity model.
     * @param entity
     */
    private void createEntityImplMethods(EntityModel entity) {
        JavaClassSource javaEntity = lookupJavaEntityImpl(entity);

        Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entity);
        allProperties.forEach(property -> {
            createPropertyMethods(javaEntity, property);
        });
    }

    /**
     * When an entity has a "*" property, that means the entity is a wrapper around a map
     * of values of a particular type.  In this case, the entity interface needs to extend
     * the "MappedNode" interface.
     * @param javaEntity
     * @param propertyWithOrigin
     */
    @Override
    protected void createMappedNodeMethods(JavaSource<?> javaEntity, PropertyModelWithOrigin propertyWithOrigin) {
        new MappedNodeMethods(propertyWithOrigin.getProperty(), propertyWithOrigin, getCtx()).writeTo(javaEntity);
    }

}
