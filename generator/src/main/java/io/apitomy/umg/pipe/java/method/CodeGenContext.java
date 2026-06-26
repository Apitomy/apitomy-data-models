package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.java.type.JavaTypeFactory;
import io.apitomy.umg.index.concept.ConceptIndex;
import io.apitomy.umg.index.java.JavaIndex;

/**
 * Provides stage-level lookups that extracted code block classes need
 * for resolving FQNs, looking up Java entities, and computing method names.
 * This decouples code blocks from the concrete stage classes.
 */
public interface CodeGenContext {

    ConceptIndex getConceptIndex();

    JavaIndex getJavaIndex();

    JavaTypeFactory getJavaTypeFactory();

    String getJavaEntityInterfaceFQN(EntityModel entity);

    String getJavaEntityClassFQN(EntityModel entity);

    String getNodeEntityClassFQN();

    String getUnionTypeFQN(String name);

    JavaInterfaceSource resolveJavaEntityType(NamespaceModel namespace, PropertyModel property);

    JavaInterfaceSource resolveJavaEntityType(NamespaceModel namespace, Type type);

    JavaInterfaceSource resolveJavaEntity(EntityModel entity);

    JavaInterfaceSource resolveCommonJavaEntity(EntityModel entity);

    JavaClassSource lookupJavaEntityImpl(String fqn);

    Class<?> primitiveTypeToClass(Type type);

    String getterMethodName(PropertyModel property);

    String setterMethodName(PropertyModel property);

    String createMethodName(EntityModel entity);

    String addMethodName(String singularName);

    String singularize(String name);

    String readMethodName(EntityModel entity);

    void warn(String message);
}
