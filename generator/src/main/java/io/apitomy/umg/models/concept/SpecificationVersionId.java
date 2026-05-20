package io.apitomy.umg.models.concept;

import io.apitomy.umg.beans.SpecificationVersion;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SpecificationVersionId {

    private String namespace;

    public static SpecificationVersionId create(SpecificationVersion specVersion) {
        return SpecificationVersionId.builder().namespace(specVersion.getNamespace()).build();
    }

    public static SpecificationVersionId create(String namespace) {
        return SpecificationVersionId.builder().namespace(namespace).build();
    }

}
