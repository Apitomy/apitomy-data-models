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

package io.apitomy.umg;

import io.apitomy.umg.logging.Logger;
import io.apitomy.umg.models.spec.SpecificationModel;
import io.apitomy.umg.pipe.GeneratorState;
import io.apitomy.umg.pipe.Pipeline;
import io.apitomy.umg.pipe.concept.CreateEntityModelsStage;
import io.apitomy.umg.pipe.concept.CreateImplicitUnionRulesStage;
import io.apitomy.umg.pipe.concept.CreateNamespaceModelsStage;
import io.apitomy.umg.pipe.concept.CreatePropertyComparatorStage;
import io.apitomy.umg.pipe.concept.CreatePropertyAndTypeModelsStage;
import io.apitomy.umg.pipe.concept.CreateTraitModelsStage;
import io.apitomy.umg.pipe.concept.CreateVisitorsStage;
import io.apitomy.umg.pipe.concept.ExpandPropertyOrderStage;
import io.apitomy.umg.pipe.concept.IndexSpecificationsStage;
import io.apitomy.umg.pipe.concept.NormalizeEntitiesStage;
import io.apitomy.umg.pipe.concept.NormalizePropertiesStage;
import io.apitomy.umg.pipe.concept.NormalizeTraitsStage;
import io.apitomy.umg.pipe.concept.NormalizeVisitorsStage;
import io.apitomy.umg.pipe.concept.RemoveShadedPropertyModelsStage;
import io.apitomy.umg.pipe.concept.RemoveTransparentTraitsStage;
import io.apitomy.umg.pipe.concept.ResolveVisitorEntityStage;
import io.apitomy.umg.pipe.concept.SpecificationValidationStage;
import io.apitomy.umg.pipe.java.ApplyUnionInterfacesToTypesStage;
import io.apitomy.umg.pipe.java.ConfigureInterfaceParentStage;
import io.apitomy.umg.pipe.java.ConfigureInterfaceTraitsStage;
import io.apitomy.umg.pipe.java.CreateNodeImplMethodsStage;
import io.apitomy.umg.pipe.java.CreateAllNodeVisitorStage;
import io.apitomy.umg.pipe.java.CreateClonerDispatchersStage;
import io.apitomy.umg.pipe.java.CreateClonerFactoryStage;
import io.apitomy.umg.pipe.java.CreateClonersStage;
import io.apitomy.umg.pipe.java.CreateCombinedVisitorInterfacesStage;

import io.apitomy.umg.pipe.java.CreateEntityImplementationsStage;
import io.apitomy.umg.pipe.java.CreateEntityInterfacesStage;
import io.apitomy.umg.pipe.java.CreateImplFieldsStage;
import io.apitomy.umg.pipe.java.CreateImplMethodsStage;
import io.apitomy.umg.pipe.java.CreateInterfaceMethodsStage;
import io.apitomy.umg.pipe.java.CreateModelTypeStage;
import io.apitomy.umg.pipe.java.CreateReaderDispatchersStage;
import io.apitomy.umg.pipe.java.CreateReaderFactoryStage;
import io.apitomy.umg.pipe.java.CreateReadersStage;
import io.apitomy.umg.pipe.java.CreateTestFixturesStage;
import io.apitomy.umg.pipe.java.CreateTraitInterfacesStage;
import io.apitomy.umg.pipe.java.CreateConversionTraversersStage;
import io.apitomy.umg.pipe.java.CreateConversionVisitorsStage;
import io.apitomy.umg.pipe.java.CreateDiffTraversersStage;
import io.apitomy.umg.pipe.java.CreateDiffVisitorsStage;
import io.apitomy.umg.pipe.java.CreateTraversersStage;
import io.apitomy.umg.pipe.java.CreateCollectionUnionValuesStage;
import io.apitomy.umg.pipe.java.CreatePrimitiveUnionValuesStage;
import io.apitomy.umg.pipe.java.CreateUnionInterfacesStage;
import io.apitomy.umg.pipe.java.CreateUnionMethodImplementationsStage;
import io.apitomy.umg.pipe.java.CreateVisitorAdaptersStage;
import io.apitomy.umg.pipe.java.CreateVisitorInterfacesStage;
import io.apitomy.umg.pipe.java.CreateWriterDispatchersStage;
import io.apitomy.umg.pipe.java.CreateWriterFactoryStage;
import io.apitomy.umg.pipe.java.CreateWritersStage;
import io.apitomy.umg.pipe.java.JavaWriteStage;
import io.apitomy.umg.pipe.java.LoadBaseClassesStage;
import io.apitomy.umg.pipe.java.OrganizeImportsStage;
import io.apitomy.umg.pipe.java.RemoveUnusedImportsStage;

import java.util.Collection;

/**
 * @author eric.wittmann@gmail.com
 */
public class UnifiedModelGenerator {

    private final UnifiedModelGeneratorConfig config;
    private final Collection<SpecificationModel> specifications;

    /**
     * Constructor.
     *
     * @param config
     * @param specifications
     */
    public UnifiedModelGenerator(UnifiedModelGeneratorConfig config, Collection<SpecificationModel> specifications) {
        this.config = config;
        this.specifications = specifications;
    }

    /**
     * Generates the output from the given list of specifications.
     */
    public void generate() throws Exception {
        Logger.info("Output directory: %s", config.getOutputDirectory().getAbsolutePath());

        GeneratorState state = new GeneratorState();
        state.setSpecifications(specifications);
        state.setConfig(config);
        Pipeline pipe = new Pipeline();

        // Index phase
        pipe.addStage(new IndexSpecificationsStage());
        pipe.addStage(new ExpandPropertyOrderStage());
        pipe.addStage(new SpecificationValidationStage());

        // Model creation phase
        pipe.addStage(new CreateNamespaceModelsStage());
        pipe.addStage(new CreateTraitModelsStage());
        pipe.addStage(new CreateEntityModelsStage());
        pipe.addStage(new CreatePropertyAndTypeModelsStage());
        pipe.addStage(new CreateVisitorsStage());
        pipe.addStage(new RemoveShadedPropertyModelsStage());

        // Implicit model creation phase
        pipe.addStage(new CreateImplicitUnionRulesStage());

        // Model optimization phase
        pipe.addStage(new RemoveTransparentTraitsStage());
        pipe.addStage(new NormalizeTraitsStage());
        pipe.addStage(new NormalizeEntitiesStage());
        pipe.addStage(new NormalizePropertiesStage());
        pipe.addStage(new NormalizeVisitorsStage());
        pipe.addStage(new ResolveVisitorEntityStage());
        pipe.addStage(new CreatePropertyComparatorStage());

        // Debug the models
        //pipe.addStage(new DebugStage());

        // Generate java code
        pipe.addStage(new LoadBaseClassesStage());
        pipe.addStage(new CreateModelTypeStage());

        pipe.addStage(new CreateTraitInterfacesStage());
        pipe.addStage(new CreateEntityInterfacesStage());
        pipe.addStage(new ConfigureInterfaceParentStage());
        pipe.addStage(new ConfigureInterfaceTraitsStage());
        pipe.addStage(new CreatePrimitiveUnionValuesStage());
        pipe.addStage(new CreateCollectionUnionValuesStage());
        pipe.addStage(new CreateUnionInterfacesStage());
        pipe.addStage(new ApplyUnionInterfacesToTypesStage());
        pipe.addStage(new CreateInterfaceMethodsStage());
        pipe.addStage(new CreateEntityImplementationsStage());
        pipe.addStage(new CreateImplFieldsStage());
        pipe.addStage(new CreateImplMethodsStage());
        pipe.addStage(new CreateUnionMethodImplementationsStage());

        pipe.addStage(new CreateReadersStage());
        pipe.addStage(new CreateWritersStage());
        pipe.addStage(new CreateClonersStage());
        pipe.addStage(new CreateVisitorInterfacesStage());
        pipe.addStage(new CreateNodeImplMethodsStage());
        pipe.addStage(new CreateCombinedVisitorInterfacesStage());
        pipe.addStage(new CreateVisitorAdaptersStage());
        pipe.addStage(new CreateAllNodeVisitorStage());
        pipe.addStage(new CreateReaderDispatchersStage());
        pipe.addStage(new CreateWriterDispatchersStage());
        pipe.addStage(new CreateClonerDispatchersStage());
        pipe.addStage(new CreateTraversersStage());
        pipe.addStage(new CreateDiffVisitorsStage());
        pipe.addStage(new CreateDiffTraversersStage());
        pipe.addStage(new CreateConversionVisitorsStage());
        pipe.addStage(new CreateConversionTraversersStage());
        pipe.addStage(new CreateReaderFactoryStage());
        pipe.addStage(new CreateWriterFactoryStage());
        pipe.addStage(new CreateClonerFactoryStage());

        pipe.addStage(new RemoveUnusedImportsStage());
        pipe.addStage(new OrganizeImportsStage());
        pipe.addStage(new JavaWriteStage());

        // Generate tests
        pipe.addStage(new CreateTestFixturesStage());

        pipe.run(state);
    }
}
