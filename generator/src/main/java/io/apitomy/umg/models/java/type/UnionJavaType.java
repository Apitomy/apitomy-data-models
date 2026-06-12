package io.apitomy.umg.models.java.type;

import io.apitomy.umg.models.concept.type.Type;
import io.apitomy.umg.models.concept.type.UnionType;
import org.jboss.forge.roaster.model.source.Importer;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import java.util.List;

/**
 * Java type for union types: boolean|Widget → BooleanWidgetUnion.
 */
public class UnionJavaType implements JavaType {

    private final UnionType unionType;
    private final String unionName;
    private final String unionFQN;
    private final List<JavaType> variantJavaTypes;

    public UnionJavaType(UnionType unionType, String unionName, String unionFQN,
                         List<JavaType> variantJavaTypes) {
        this.unionType = unionType;
        this.unionName = unionName;
        this.unionFQN = unionFQN;
        this.variantJavaTypes = variantJavaTypes;
    }

    @Override
    public Type getConceptType() {
        return unionType;
    }

    @Override
    public String toJavaTypeString() {
        return unionName;
    }

    @Override
    public void addImportsTo(Importer<?> importer) {
        importer.addImport(unionFQN);
    }

    @Override
    public String getSimpleName() {
        return unionName;
    }

    public String getUnionFQN() {
        return unionFQN;
    }

    public List<JavaType> getVariantJavaTypes() {
        return variantJavaTypes;
    }
}
