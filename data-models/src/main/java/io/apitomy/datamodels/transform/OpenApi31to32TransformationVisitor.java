/*
 * Copyright 2024 Red Hat
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

package io.apitomy.datamodels.transform;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.visitors.OpenApi31Visitor;
import io.apitomy.datamodels.models.openapi.v3x.v31.visitors.OpenApi31VisitorAdapter;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32Document;

/**
 * A visitor used to transform an OpenAPI 3.1.x document into an OpenAPI 3.2.x document.
 *
 * <p>Since OpenAPI 3.2 is fully backward compatible with 3.1 (all changes are additive),
 * the transformation simply clones the document with the version bumped to 3.2.0.
 * No structural changes are required.</p>
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApi31to32TransformationVisitor extends OpenApi31VisitorAdapter implements OpenApi31Visitor {

    private OpenApi32Document doc32;

    /**
     * Constructor.
     * @param source the OpenAPI 3.1 document to transform
     */
    public OpenApi31to32TransformationVisitor(OpenApi31Document source) {
        doc32 = (OpenApi32Document) Library.cloneDocument(source, rawJson -> {
            rawJson.put("openapi", "3.2.0");
            return rawJson;
        });
    }

    /**
     * Retrieves the final result.
     * @return the transformed OpenAPI 3.2 document
     */
    public OpenApi32Document getResult() {
        return this.doc32;
    }

}
