package io.apitomy.umg.models.java.type;

import io.apitomy.umg.models.concept.type.EntityType;
import io.apitomy.umg.models.concept.type.Type;
import org.jboss.forge.roaster.model.source.Importer;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

/**
 * Java type for entity references. Resolves to the entity's Java interface.
 */
public class EntityJavaType implements JavaType {

    private final EntityType entityType;
    private final JavaInterfaceSource interfaceSource;

    public EntityJavaType(EntityType entityType, JavaInterfaceSource interfaceSource) {
        this.entityType = entityType;
        this.interfaceSource = interfaceSource;
    }

    @Override
    public Type getConceptType() {
        return entityType;
    }

    @Override
    public String toJavaTypeString() {
        return interfaceSource.getName();
    }

    @Override
    public void addImportsTo(Importer<?> importer) {
        importer.addImport(interfaceSource);
    }

    @Override
    public String getSimpleName() {
        return interfaceSource.getName();
    }

    public JavaInterfaceSource getInterfaceSource() {
        return interfaceSource;
    }
}
