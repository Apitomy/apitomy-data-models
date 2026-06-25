package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaClassSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.index.java.JavaIndex;
import io.apitomy.umg.index.concept.ConceptIndex;

/**
 * Provides the stage-level lookups that method classes need for resolving FQNs and
 * looking up Java entities. This decouples method classes from the stage itself.
 */
public interface ImplMethodContext {

    String getFieldName(PropertyModel property);

    String getNodeEntityClassFQN();

    String getParentPropertyTypeEnumFQN();

    String getUnionValueInterfaceFQN();

    String getUnionTypeFQN(String name);

    String getDataModelUtilFQCN();

    String getJavaEntityClassFQN(EntityModel entity);

    JavaClassSource lookupJavaEntityImpl(String fqn);

    JavaClassSource lookupJavaEntityImpl(EntityModel entity);

    ConceptIndex getConceptIndex();

    JavaIndex getJavaIndex();

    void error(String message);

}
