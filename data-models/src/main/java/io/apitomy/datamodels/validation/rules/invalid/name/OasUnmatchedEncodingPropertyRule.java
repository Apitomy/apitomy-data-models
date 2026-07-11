/*
 * Copyright 2019 Red Hat
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

package io.apitomy.datamodels.validation.rules.invalid.name;

import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.OpenApiEncoding;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30MediaType;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Unmatched Encoding Property Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasUnmatchedEncodingPropertyRule extends OasInvalidPropertyNameRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnmatchedEncodingPropertyRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Returns true if the given schema has a property defined with the given name.
     * @param {Oas30Schema} schema
     * @param {string} propertyName
     * @return {boolean}
     */
    private boolean isValidSchemaProperty(Schema schema, String propertyName) {
        if (isNullOrUndefined(schema) && isNullOrUndefined(schema.getProperties())) {
            return false;
        }
        return !isNullOrUndefined(schema.getProperties().get(propertyName));
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitEncoding(io.apitomy.datamodels.models.openapi.OpenApiEncoding)
     */
    @Override
    public void visitEncoding(OpenApiEncoding node) {
        String name = getEncodingName(node);
        Schema schema = ((OpenApi30MediaType) (node.parent())).getSchema();

        this.reportIfInvalid(isValidSchemaProperty(schema, name), node, name, map("name", name));
    }

}
