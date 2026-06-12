package io.apitomy.umg.models.concept.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class AbstractType implements Type {

    @EqualsAndHashCode.Include
    protected String namespace;

    @EqualsAndHashCode.Include
    protected String name;

    protected RawType rawType;

    protected Type parent;

    protected boolean leaf;

    protected boolean root;
}
