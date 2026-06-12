package io.apitomy.umg.models.java.type;

import io.apitomy.umg.models.concept.type.ListType;
import io.apitomy.umg.models.concept.type.Type;
import org.jboss.forge.roaster.model.source.Importer;

import java.util.List;

/**
 * Java type for list types: [Widget] → List&lt;Widget&gt;, [string] → List&lt;String&gt;.
 */
public class ListJavaType implements JavaType {

    private final ListType listType;
    private final JavaType valueJavaType;

    public ListJavaType(ListType listType, JavaType valueJavaType) {
        this.listType = listType;
        this.valueJavaType = valueJavaType;
    }

    @Override
    public Type getConceptType() {
        return listType;
    }

    @Override
    public String toJavaTypeString() {
        return "List<" + valueJavaType.toJavaTypeString() + ">";
    }

    @Override
    public void addImportsTo(Importer<?> importer) {
        importer.addImport(List.class);
        valueJavaType.addImportsTo(importer);
    }

    @Override
    public String getSimpleName() {
        return valueJavaType.getSimpleName() + "List";
    }

    public JavaType getValueJavaType() {
        return valueJavaType;
    }
}
