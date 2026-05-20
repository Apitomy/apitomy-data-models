package io.apitomy.umg;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedModelGeneratorConfig {

    private String rootNamespace;
    private File outputDirectory;
    private File testOutputDirectory;
    private boolean generateTestFixtures;

}
