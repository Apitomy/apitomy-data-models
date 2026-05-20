package io.apitomy.umg.models.concept;

import java.util.LinkedHashMap;
import java.util.Map;

import io.apitomy.umg.beans.SpecificationVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class EntityOrTraitModel {

    private final SpecificationVersion specVersion;
    @Include
    private NamespaceModel namespace;
    @Include
    private final String name;
    private final Map<String, PropertyModel> properties = new LinkedHashMap<>();
    private boolean leaf;
    private EntityOrTraitModel parent;

    public String fullyQualifiedName() {
        return namespace.fullName() + "." + name;
    }

    public void addProperty(PropertyModel property) {
        this.properties.put(property.getName(), property);
    }

    public boolean hasProperty(String propertyName) {
        return this.properties.containsKey(propertyName);
    }

    public void removeProperty(String propertyName) {
        this.properties.remove(propertyName);
    }

}
