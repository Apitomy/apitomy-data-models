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

package io.apitomy.datamodels.validation.rules.invalid.value;

import java.util.List;

import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.OpenApiDiscriminator;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
import io.apitomy.datamodels.refs.ReferenceUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that a discriminator is only used in a schema that utilizes oneOf, anyOf, or allOf
 * composition, or is referenced as a base schema by other schemas via allOf, oneOf, or anyOf.
 *
 * @author eric.wittmann@gmail.com
 */
public class OasUnexpectedUsageOfDiscriminatorRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasUnexpectedUsageOfDiscriminatorRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitDiscriminator(io.apitomy.datamodels.models.openapi.OpenApiDiscriminator)
     */
    @Override
    public void visitDiscriminator(OpenApiDiscriminator node) {
        Schema schema = (Schema) node.parent();
        boolean valid = schemaHasComposition(schema) || isReferencedAsBaseSchema(schema);
        this.reportIfInvalid(valid, node, "discriminator", map());
    }

    /**
     * Checks whether the given schema directly uses oneOf, anyOf, or allOf composition keywords.
     * @param schema the schema to check
     * @return true if the schema has at least one composition keyword
     */
    private boolean schemaHasComposition(Schema schema) {
        if (hasValue(schema.getAllOf())) {
            return true;
        }
        if (ModelTypeUtil.isOpenApi30Model(schema)) {
            OpenApi30Schema schema30 = (OpenApi30Schema) schema;
            return hasValue(schema30.getOneOf()) || hasValue(schema30.getAnyOf());
        } else if (ModelTypeUtil.isOpenApi31Model(schema)) {
            OpenApi31Schema schema31 = (OpenApi31Schema) schema;
            return hasValue(schema31.getOneOf()) || hasValue(schema31.getAnyOf());
        }
        return false;
    }

    /**
     * Checks whether the given schema is referenced by any other schema's allOf, oneOf, or anyOf,
     * which would make it a valid "base schema" for a discriminator. This traverses the entire
     * document tree using the Visitor pattern.
     * @param schema the schema to check
     * @return true if the schema is referenced as a base schema
     */
    private boolean isReferencedAsBaseSchema(Schema schema) {
        BaseSchemaReferenceVisitor visitor = new BaseSchemaReferenceVisitor(schema);
        VisitorUtil.visitTree((Node) schema.root(), visitor, TraverserDirection.down);
        return visitor.isReferenced();
    }

    /**
     * A visitor that traverses the document looking for schemas whose allOf, oneOf, or anyOf
     * entries contain a $ref that resolves to the target schema.
     */
    private static class BaseSchemaReferenceVisitor extends CombinedVisitorAdapter {

        private final Schema targetSchema;
        private boolean referenced = false;

        public BaseSchemaReferenceVisitor(Schema targetSchema) {
            this.targetSchema = targetSchema;
        }

        public boolean isReferenced() {
            return referenced;
        }

        @Override
        public void visitSchema(Schema node) {
            if (referenced) {
                return;
            }
            // Check allOf (available on base Schema interface)
            referenced = checkCompositionListForRef(node.getAllOf(), node);
            if (referenced) {
                return;
            }
            // Check oneOf and anyOf (version-specific)
            if (ModelTypeUtil.isOpenApi30Model(node)) {
                OpenApi30Schema schema30 = (OpenApi30Schema) node;
                referenced = checkCompositionListForRef(schema30.getOneOf(), node);
                if (!referenced) {
                    referenced = checkCompositionListForRef(schema30.getAnyOf(), node);
                }
            } else if (ModelTypeUtil.isOpenApi31Model(node)) {
                OpenApi31Schema schema31 = (OpenApi31Schema) node;
                referenced = checkCompositionListForRef(schema31.getOneOf(), node);
                if (!referenced) {
                    referenced = checkCompositionListForRef(schema31.getAnyOf(), node);
                }
            }
        }

        /**
         * Checks whether any schema in the given composition list has a $ref that resolves
         * to the target schema.
         * @param schemas the list of schemas to check
         * @param fromNode the node context for resolving $ref
         * @return true if any schema in the list references the target schema
         */
        private boolean checkCompositionListForRef(List<? extends Schema> schemas, Node fromNode) {
            if (schemas == null) {
                return false;
            }
            for (Schema childSchema : schemas) {
                if (childSchema instanceof Referenceable) {
                    String ref = ((Referenceable) childSchema).get$ref();
                    if (ref != null) {
                        Node resolved = ReferenceUtil.resolveRef(ref, fromNode);
                        if (resolved == targetSchema) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

}
