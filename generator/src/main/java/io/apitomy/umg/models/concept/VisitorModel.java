package io.apitomy.umg.models.concept;

import java.util.Collection;
import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VisitorModel {

    @Include
    private NamespaceModel namespace;
    private VisitorModel parent;
    private final Collection<VisitorModel> children = new HashSet<>();
    private final Collection<EntityModel> entities = new HashSet<>();

    @Override
    public String toString() {
        return "Visitor <" + namespace.fullName() + ">";
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void addEntity(EntityModel entity) {
        this.entities.add(entity);
    }

    public void removeEntity(String entityName) {
        EntityModel entity = this.findEntity(entityName);
        if (entity != null) {
            this.entities.remove(entity);
        }
    }

    public boolean containsEntity(String entityName) {
        return findEntity(entityName) != null;
    }

    public EntityModel findEntity(String entityName) {
        for (EntityModel entityModel : entities) {
            if (entityModel.getName().equals(entityName)) {
                return entityModel;
            }
        }
        return null;
    }
}
