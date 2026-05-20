/*
 * Copyright 2020 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apitomy.umg.main;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import io.apitomy.umg.UnifiedModelGenerator;
import io.apitomy.umg.UnifiedModelGeneratorConfig;
import io.apitomy.umg.io.SpecificationLoader;
import io.apitomy.umg.logging.Logger;
import io.apitomy.umg.models.spec.SpecificationModel;

/**
 * @author eric.wittmann@gmail.com
 */
public class Main {

    /**
     * Main entry point. Runs the unified model generator.
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Logger.info("=== START ===");
        //        generate("org.example.smoke", "../maven-plugin-tests/src/it/smoke-test/src/main/resources/smoke-spec.yaml");
        //        generate("org.example.union", "../maven-plugin-tests/src/it/union-test/src/main/resources/union-spec.yaml");
        //        generate("org.example.io", "../maven-plugin-tests/src/it/io-test/src/main/resources/io-spec.yaml");
        //        generate("org.example.multi", "../maven-plugin-tests/src/it/multi-spec-test/src/main/resources/multi-spec.yaml");
        //        generate("org.example.multil",
        //                "../maven-plugin-tests/src/it/multi-level-spec-test/src/main/resources/first-spec.yaml",
        //                "../maven-plugin-tests/src/it/multi-level-spec-test/src/main/resources/second-spec.yaml");
        //        generate("org.example.pt", "../maven-plugin-tests/src/it/parent-trait-test/src/main/resources/parent-trait-spec.yaml");
        //        generate("io.apitomy.datamodels.models", "../maven-plugin-tests/src/it/openapi-test/src/main/resources/openapi.yaml");
        //        generate("io.apitomy.datamodels.models", "../maven-plugin-tests/src/it/asyncapi-test/src/main/resources/asyncapi.yaml");
        generate("io.apitomy.datamodels.models",
                "../maven-plugin-tests/src/it/all-test/src/main/resources/openapi.yaml",
                "../maven-plugin-tests/src/it/all-test/src/main/resources/asyncapi.yaml");
        Logger.info("=== END ===");
    }

    public static void generate(String rootNamespace, String ... specPaths) throws Exception {
        Logger.info("Starting Apitomy Unified Model Generator");
        // Clean the output directory
        File outputDir = new File("target/from-main/" + rootNamespace);
        File testOutputDir = new File("target/from-main-test/" + rootNamespace);
        if (outputDir.isDirectory()) {
            FileUtils.deleteDirectory(outputDir);
            outputDir.mkdirs();
        }
        if (testOutputDir.isDirectory()) {
            FileUtils.deleteDirectory(testOutputDir);
            testOutputDir.mkdirs();
        }

        // Set up config
        UnifiedModelGeneratorConfig config = UnifiedModelGeneratorConfig.builder()
                .rootNamespace(rootNamespace)
                .outputDirectory(outputDir)
                .testOutputDirectory(testOutputDir).build();
        // Load the specs
        List<SpecificationModel> specs = loadSpecs(specPaths);
        // Create a unified model generator
        UnifiedModelGenerator generator = new UnifiedModelGenerator(config, specs);
        // Generate the source code into the target output directory.
        generator.generate();
        Logger.info("Model generated successfully!");
    }

    /**
     * Loads all the specification files (as resources).
     */
    private static List<SpecificationModel> loadSpecs(String ... specPaths) {
        Logger.info("Loading specifications.");
        List<SpecificationModel> loadedSpecs = new LinkedList<>();
        for (String specPath : specPaths) {
            if (specPath.startsWith("specs")) {
                loadedSpecs.add(SpecificationLoader.loadSpec(specPath, Main.class.getClassLoader()));
            } else {
                loadedSpecs.add(SpecificationLoader.loadSpec(specPath));
            }
        }
        return loadedSpecs;
    }

}
