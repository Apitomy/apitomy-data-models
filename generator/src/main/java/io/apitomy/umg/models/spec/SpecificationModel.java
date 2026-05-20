package io.apitomy.umg.models.spec;

import java.util.LinkedList;
import java.util.List;

import io.apitomy.umg.beans.Specification;
import io.apitomy.umg.beans.SpecificationVersion;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SpecificationModel {

    public static SpecificationModel from(Specification spec) {
        return SpecificationModel.builder()
                .name(spec.getName())
                .prefix(spec.getPrefix())
                .namespace(spec.getNamespace())
                .build();
    }

    @Include
    private String name;
    private String prefix;
    private String namespace;
    private final List<SpecificationVersion> versions = new LinkedList<>();

}
