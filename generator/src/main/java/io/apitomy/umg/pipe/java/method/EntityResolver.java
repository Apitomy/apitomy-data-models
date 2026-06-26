package io.apitomy.umg.pipe.java.method;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;

/**
 * Centralises the repeated "namespace + entity lookup + null-check + warn"
 * pattern used across reader, writer, and cloner property blocks.
 */
public final class EntityResolver {

    private EntityResolver() {
    }

    /**
     * Result of resolving a property's entity type. Contains the concept-level
     * {@link EntityModel} and the corresponding {@link JavaInterfaceSource}.
     */
    public record ResolvedEntity(EntityModel entityModel, JavaInterfaceSource javaInterface) {
    }

    /**
     * Resolves the entity type referenced by a property.
     * <ol>
     *   <li>Builds the fully-qualified entity name from the owning entity's namespace
     *       and the property's resolved type name.</li>
     *   <li>Looks the entity up in the {@link io.apitomy.umg.models.concept.ConceptIndex}.</li>
     *   <li>Looks the Java interface up in the {@link io.apitomy.umg.pipe.java.index.JavaIndex}.</li>
     * </ol>
     *
     * @param prop          the property whose type to resolve
     * @param ownerEntity   the entity that owns the property (used for namespace and warning messages)
     * @param ctx           code-generation context for lookups and warnings
     * @param warnPrefix    prefix for warning messages (e.g. "LIST", "MAP", "STAR", "REGEX", or empty)
     * @return the resolved entity and Java interface, or {@code null} if resolution failed
     */
    public static ResolvedEntity resolveEntityInterface(PropertyModelWithOrigin prop,
            EntityModel ownerEntity, CodeGenContext ctx, String warnPrefix) {
        return resolveEntityInterface(prop.getProperty(), prop.getProperty().getResolvedType().getName(),
                ownerEntity, ctx, warnPrefix);
    }

    /**
     * Resolves an entity type by its simple name within the owning entity's namespace.
     * This variant is useful for list/map value types where the type name comes from
     * the inner type rather than the property's direct resolved type.
     *
     * @param property      the property (for warning messages)
     * @param entityTypeName the simple entity type name to resolve
     * @param ownerEntity   the entity that owns the property (used for namespace and warning messages)
     * @param ctx           code-generation context for lookups and warnings
     * @param warnPrefix    prefix for warning messages (e.g. "LIST", "MAP", or empty)
     * @return the resolved entity and Java interface, or {@code null} if resolution failed
     */
    public static ResolvedEntity resolveEntityInterface(PropertyModel property, String entityTypeName,
            EntityModel ownerEntity, CodeGenContext ctx, String warnPrefix) {
        var prefix = warnPrefix.isEmpty() ? "" : warnPrefix + " ";
        var fqEntityName = ownerEntity.getNamespace().fullName() + "." + entityTypeName;
        var entityModel = ctx.getConceptIndex().lookupEntity(fqEntityName);
        if (entityModel == null) {
            ctx.warn(prefix + "Property entity type not found for property: '" + property.getName()
                    + "' of entity: " + ownerEntity.fullyQualifiedName());
            ctx.warn("       property type: " + property.getResolvedType());
            return null;
        }
        var javaInterface = ctx.getJavaIndex().lookupInterface(ctx.getJavaEntityInterfaceFQN(entityModel));
        if (javaInterface == null) {
            ctx.warn(prefix + "Entity property '" + property.getName()
                    + "' not resolved (unsupported) for entity: " + ownerEntity.fullyQualifiedName());
            ctx.warn("       property type is entity but not found in JAVA index: " + property.getResolvedType());
            return null;
        }
        return new ResolvedEntity(entityModel, javaInterface);
    }
}
