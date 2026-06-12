package io.apitomy.umg.models.java.type;

import io.apitomy.umg.models.concept.type.PrimitiveType;
import io.apitomy.umg.models.concept.type.Type;
import org.jboss.forge.roaster.model.source.Importer;

/**
 * Java type for primitives: string → String, boolean → Boolean, number → Number, etc.
 */
public class PrimitiveJavaType implements JavaType {

    private final PrimitiveType primitiveType;

    public PrimitiveJavaType(PrimitiveType primitiveType) {
        this.primitiveType = primitiveType;
    }

    @Override
    public Type getConceptType() {
        return primitiveType;
    }

    @Override
    public String toJavaTypeString() {
        return primitiveType.getJavaClass().getSimpleName();
    }

    @Override
    public void addImportsTo(Importer<?> importer) {
        var javaClass = primitiveType.getJavaClass();
        if (!javaClass.getPackageName().equals("java.lang")) {
            importer.addImport(javaClass);
        }
    }

    @Override
    public String getSimpleName() {
        return primitiveType.getJavaClass().getSimpleName();
    }

    public Class<?> getJavaClass() {
        return primitiveType.getJavaClass();
    }
}
