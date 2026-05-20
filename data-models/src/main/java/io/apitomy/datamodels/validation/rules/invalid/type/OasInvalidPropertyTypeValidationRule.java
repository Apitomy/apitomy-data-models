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

package io.apitomy.datamodels.validation.rules.invalid.type;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Schema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.models.union.StringStringListUnion;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Property Type validation rule.  This rule is responsible
 * for reporting whenever the **type** and **items** of a property fails to conform to the required
 * format defined by the specification
 * @author eric.wittmann@gmail.com
 */
public abstract class OasInvalidPropertyTypeValidationRule extends ValidationRule {

    protected static final String[] ALLOWED_TYPES = {"string", "number", "integer", "boolean", "array", "object"};
    protected static final String[] ALLOWED_TYPES31 = {"string", "number", "integer", "boolean", "array", "object", "null"};

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidPropertyTypeValidationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    static List<String> toList(String type) {
        return type != null ? Collections.singletonList(type) : Collections.emptyList();
    }

    static List<String> toList(StringStringListUnion types) {
        if (types != null) {
            return types.isString() ? Collections.singletonList(types.asString()) : types.asStringList();
        }
        return Collections.emptyList();
    }

    public static void getTypes(Schema node, BiConsumer<List<String>, String[]> handler) {
        final String[] allowedTypes;
        final List<String> types;

        if (node instanceof OpenApi31Schema) {
            allowedTypes = ALLOWED_TYPES31;
            types = toList(((OpenApi31Schema) node).getType());
        } else if (node instanceof OpenApi30Schema) {
            allowedTypes = ALLOWED_TYPES;
            types = toList(((OpenApi30Schema) node).getType());
        } else if (node instanceof OpenApi20Schema) {
            allowedTypes = ALLOWED_TYPES;
            types = toList(((OpenApi20Schema) node).getType());
        } else {
            allowedTypes = null;
            types = Collections.emptyList();
        }

        handler.accept(types, allowedTypes);
    }
}
