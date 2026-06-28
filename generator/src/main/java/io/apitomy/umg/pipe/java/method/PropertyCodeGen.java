package io.apitomy.umg.pipe.java.method;

import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;

/**
 * Bundles property-level data and code-generation context needed by code blocks.
 * <p>
 * This wrapper replaces the repeated {@code (PropertyModelWithOrigin, EntityModel, CodeGenContext)}
 * parameter triple, reducing code-block constructors from 3-4 parameters to 1-2.
 * <p>
 * The stage-specific {@code JavaClassSource} (reader/writer/cloner) is <em>not</em>
 * included here because it varies per stage; code blocks receive it as a separate parameter.
 */
public class PropertyCodeGen {

    private final PropertyModelWithOrigin propertyWithOrigin;
    private final EntityModel owningEntity;
    private final CodeGenContext ctx;

    /**
     * Creates a PropertyCodeGen for a property that belongs to an entity.
     *
     * @param propertyWithOrigin the property with its origin entity
     * @param owningEntity       the entity that owns this property
     * @param ctx                code-generation context for lookups and naming
     */
    public PropertyCodeGen(PropertyModelWithOrigin propertyWithOrigin, EntityModel owningEntity,
            CodeGenContext ctx) {
        this.propertyWithOrigin = propertyWithOrigin;
        this.owningEntity = owningEntity;
        this.ctx = ctx;
    }

    /**
     * Creates a PropertyCodeGen without an owning entity. Used for blocks that
     * don't need entity resolution (e.g. primitive blocks, union blocks that
     * resolve types via the property's namespace).
     *
     * @param propertyWithOrigin the property with its origin entity
     * @param ctx                code-generation context for lookups and naming
     */
    public PropertyCodeGen(PropertyModelWithOrigin propertyWithOrigin, CodeGenContext ctx) {
        this(propertyWithOrigin, null, ctx);
    }

    // --- Accessors ---

    public PropertyModelWithOrigin getPropertyWithOrigin() {
        return propertyWithOrigin;
    }

    public PropertyModel getProperty() {
        return propertyWithOrigin.getProperty();
    }

    public EntityModel getOwningEntity() {
        return owningEntity;
    }

    public CodeGenContext getCtx() {
        return ctx;
    }

    public String getFieldName() {
        return ctx.getFieldName(getProperty());
    }
}
