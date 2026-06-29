package io.apitomy.umg.pipe.java;

import java.util.Collection;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.CodeGenContext;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;

/**
 * Shared orchestration for the reader, writer, and cloner stages.
 *
 * <p>Template method pattern: subclasses provide naming, imports, method
 * creation, and property-block factories; this class owns the spec-version
 * iteration, Roaster class creation, entity loop, and property-type dispatch.
 */
public abstract class AbstractIOStage extends AbstractJavaStage {

    protected CodeGenContext ctx;

    @Override
    protected void doProcess() {
        ctx = new CodeGenContext(
                getState().getConceptIndex(),
                getState().getJavaIndex(),
                getJavaTypeFactory(),
                getState().getConfig().getRootNamespace(),
                getState().getSpecIndex(),
                getClass().getSimpleName());
        getState().getSpecIndex().getAllSpecificationVersions()
                .forEach(this::processSpecVersion);
    }

    private void processSpecVersion(SpecificationVersion specVersion) {
        JavaClassSource classSource = Roaster.create(JavaClassSource.class)
                .setPackage(getPackageName(specVersion))
                .setName(getClassName(specVersion))
                .setPublic();

        addImports(classSource);
        addInterfaceImplementation(classSource);

        specVersion.getEntities().forEach(entity -> {
            EntityModel entityModel = getState().getConceptIndex()
                    .lookupEntity(specVersion.getNamespace() + "." + entity.getName());
            if (entityModel == null) {
                warn("Entity model not found for entity: " + entity);
            } else {
                createEntityMethod(specVersion, classSource, entityModel);
            }
        });

        afterEntityMethods(specVersion, classSource);

        getState().getJavaIndex().index(classSource);
    }

    // ---- Shared property-type dispatch ----

    protected void dispatchProperty(BodyBuilder body, PropertyModelWithOrigin propertyWithOrigin,
            EntityModel entityModel, JavaClassSource classSource) {
        PropertyModel property = propertyWithOrigin.getProperty();
        PropertyCodeGen prop = new PropertyCodeGen(propertyWithOrigin, entityModel, ctx);

        if (isStarProperty(property)) {
            createPropertyBlock(PropertyBlockKind.STAR, prop, classSource).appendTo(body);
        } else if (isRegexProperty(property)) {
            createPropertyBlock(PropertyBlockKind.REGEX, prop, classSource).appendTo(body);
        } else if (!dispatchByResolvedType(body, property, prop, classSource)) {
            warn("Entity property '" + property.getName()
                    + "' not handled (unsupported) for entity: " + entityModel.fullyQualifiedName());
        }
    }

    private boolean dispatchByResolvedType(BodyBuilder body, PropertyModel property,
            PropertyCodeGen prop, JavaClassSource classSource) {
        PropertyBlockKind kind = PropertyBlockKind.fromResolvedType(property.getResolvedType());
        if (kind == null) return false;
        createPropertyBlock(kind, prop, classSource).appendTo(body);
        return true;
    }

    // ---- Abstract methods (must override) ----

    protected abstract String getPackageName(SpecificationVersion specVersion);

    protected abstract String getClassName(SpecificationVersion specVersion);

    protected abstract void addImports(JavaClassSource classSource);

    protected abstract void createEntityMethod(SpecificationVersion specVersion,
            JavaClassSource classSource, EntityModel entityModel);

    protected abstract CodeBlock createPropertyBlock(PropertyBlockKind kind,
            PropertyCodeGen prop, JavaClassSource classSource);

    // ---- Hooks (default no-op) ----

    protected void addInterfaceImplementation(JavaClassSource classSource) {}

    protected void afterEntityMethods(SpecificationVersion specVersion, JavaClassSource classSource) {}

    // ---- Property block kinds ----

    protected enum PropertyBlockKind {
        STAR, REGEX, UNION, UNION_LIST, UNION_MAP, ENTITY, PRIMITIVE, LIST, MAP;

        /**
         * Maps a resolved type to its PropertyBlockKind.
         * Returns null if the type is null or not recognized.
         */
        public static PropertyBlockKind fromResolvedType(Type resolvedType) {
            if (resolvedType == null) return null;
            if (resolvedType.isUnionType())     return UNION;
            if (resolvedType.isUnionListType())  return UNION_LIST;
            if (resolvedType.isUnionMapType())   return UNION_MAP;
            if (resolvedType.isEntityType())     return ENTITY;
            if (resolvedType.isPrimitiveType())  return PRIMITIVE;
            if (resolvedType.isListType())       return LIST;
            if (resolvedType.isMapType())        return MAP;
            return null;
        }
    }
}
