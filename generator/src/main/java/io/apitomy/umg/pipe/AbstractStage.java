package io.apitomy.umg.pipe;

import org.modeshape.common.text.Inflector;

import io.apitomy.umg.logging.Logger;
import io.apitomy.umg.models.concept.PropertyModel;
import lombok.Getter;

/**
 * Base class for all pipeline stages.
 */
public abstract class AbstractStage implements Stage {

    private static final Inflector inflector = new Inflector();

    @Getter
    private GeneratorState state;

    @Override
    public final void process(GeneratorState state) {
        this.state = state;
        debug("Executing stage.");
        this.doProcess();
    }

    /**
     * Perform the logic of the stage.
     */
    protected abstract void doProcess();

    protected boolean isStarProperty(PropertyModel property) {
        return "*".equals(property.getName());
    }

    protected boolean isRegexProperty(PropertyModel property) {
        return property.getName().startsWith("/");
    }

    protected boolean isEntityList(PropertyModel property) {
        return property.getResolvedType().isListType()
                && ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType().isEntityType();
    }

    protected boolean isEntityMap(PropertyModel property) {
        return property.getResolvedType().isMapType()
                && ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType().isEntityType();
    }

    protected boolean isEntity(PropertyModel property) {
        return property.getResolvedType().isEntityType();
    }

    protected boolean isUnion(PropertyModel property) {
        return property.getResolvedType().isUnionType();
    }

    protected boolean isPrimitive(PropertyModel property) {
        return property.getResolvedType().isPrimitiveType();
    }

    protected boolean isPrimitiveList(PropertyModel property) {
        return property.getResolvedType().isListType()
                && ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType().isPrimitiveType();
    }

    protected boolean isPrimitiveMap(PropertyModel property) {
        return property.getResolvedType().isMapType()
                && ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType().isPrimitiveType();
    }

    protected boolean isUnionList(PropertyModel property) {
        return property.getResolvedType().isListType()
                && ((io.apitomy.umg.models.concept.type.ListType) property.getResolvedType()).getValueType().isUnionType();
    }

    protected boolean isUnionMap(PropertyModel property) {
        return property.getResolvedType().isMapType()
                && ((io.apitomy.umg.models.concept.type.MapType) property.getResolvedType()).getValueType().isUnionType();
    }

    public String singularize(String name) {
        return inflector.singularize(name);
    }

    protected String extractRegex(String propertyName) {
        return propertyName.substring(1, propertyName.length() - 1);
    }

    protected void info(String message, Object ...args) {
        Logger.info("[" + getClass().getSimpleName() + "] " + message, args);
    }

    protected void warn(String message, Object ...args) {
        Logger.warn("[" + getClass().getSimpleName() + "] " + message, args);
    }

    protected void debug(String message, Object ...args) {
        Logger.debug("[" + getClass().getSimpleName() + "] " + message, args);
    }

    protected void error(String message, Object ...args) {
        Logger.error("[" + getClass().getSimpleName() + "] " + message, args);
    }

}
