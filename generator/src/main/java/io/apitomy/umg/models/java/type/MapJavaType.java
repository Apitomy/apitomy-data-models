package io.apitomy.umg.models.java.type;

import io.apitomy.umg.models.concept.type.MapType;
import io.apitomy.umg.models.concept.type.Type;
import org.jboss.forge.roaster.model.source.Importer;

import java.util.Map;

/**
 * Java type for map types: {Widget} → Map&lt;String, Widget&gt;, {string} → Map&lt;String, String&gt;.
 * Keys are always String.
 */
public class MapJavaType implements JavaType {

    private final MapType mapType;
    private final JavaType valueJavaType;

    public MapJavaType(MapType mapType, JavaType valueJavaType) {
        this.mapType = mapType;
        this.valueJavaType = valueJavaType;
    }

    @Override
    public Type getConceptType() {
        return mapType;
    }

    @Override
    public String toJavaTypeString() {
        return "Map<String, " + valueJavaType.toJavaTypeString() + ">";
    }

    @Override
    public void addImportsTo(Importer<?> importer) {
        importer.addImport(Map.class);
        valueJavaType.addImportsTo(importer);
    }

    @Override
    public String getSimpleName() {
        return valueJavaType.getSimpleName() + "Map";
    }

    public JavaType getValueJavaType() {
        return valueJavaType;
    }
}
