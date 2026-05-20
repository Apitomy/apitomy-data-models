/*
 * Copyright 2023 Red Hat
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

import java.util.List;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v30.visitors.OpenApi30Visitor;
import io.apitomy.datamodels.models.openapi.v3x.v30.visitors.OpenApi30VisitorAdapter;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.models.union.StringListUnionValueImpl;
import io.apitomy.datamodels.models.union.StringUnionValueImpl;
import io.apitomy.datamodels.paths.NodePathUtil;

/**
 * A visitor used to transform an OpenAPI 3.0.x document into an OpenAPI 3.1.x document.
 */
public class OpenApi30to31TransformationVisitor extends OpenApi30VisitorAdapter implements OpenApi30Visitor {

    private OpenApi31Document doc31;

    public OpenApi30to31TransformationVisitor(OpenApi30Document source) {
        doc31 = (OpenApi31Document) Library.cloneDocument(source, rawJson -> {
            rawJson.put("openapi", "3.1.0");
            return rawJson;
        });
    }

    /**
     * Retrieves the final result.
     */
    public OpenApi31Document getResult() {
        return this.doc31;
    }

    @Override
    public void visitSchema(Schema node) {
        mapSchema((OpenApi30Schema) node, findMatchingNode(node));
    }

    private OpenApi31Schema findMatchingNode(Node node) {
        return (OpenApi31Schema) NodePathUtil.resolveNodePath(NodePathUtil.createNodePath(node), doc31);
    }

    private void mapSchema(OpenApi30Schema from, OpenApi31Schema schema31) {
        schema31.getExtraPropertyNames().forEach(schema31::removeExtraProperty);

        if (from.getType() == null) {
            schema31.setType(new StringUnionValueImpl(from.getType()));
        } else if (Boolean.TRUE.equals(from.isNullable())) {
            schema31.setType(new StringListUnionValueImpl(List.of(from.getType(), "null")));
        } else {
            schema31.setType(new StringUnionValueImpl(from.getType()));
        }

        if (Boolean.TRUE.equals(from.isExclusiveMaximum())) {
            schema31.setExclusiveMaximum(from.getMaximum());
            schema31.setMaximum(null);
        } else {
            schema31.setMaximum(from.getMaximum());
        }

        if (Boolean.TRUE.equals(from.isExclusiveMinimum())) {
            schema31.setExclusiveMinimum(from.getMinimum());
            schema31.setMinimum(null);
        } else {
            schema31.setMinimum(from.getMinimum());
        }
    }
}
