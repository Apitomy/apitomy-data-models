package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.asyncapi.AsyncApiSchema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Schema;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Schema;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.util.NodeUtil;

import java.util.List;

/**
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractSchemaInhCommand extends AbstractCommand {

    public static final String TYPE_NONE = "none";
    public static final String TYPE_ALL_OF = "allOf";
    public static final String TYPE_ONE_OF = "oneOf";
    public static final String TYPE_ANY_OF = "anyOf";

    /**
     * Determines the current inheritance type for the given schema.
     * @param schema
     */
    static String getInheritanceType(Schema schema) {
        if (ModelTypeUtil.isOpenApiModel(schema)) {
            if (NodeUtil.isDefined(schema.getAllOf())) {
                return TYPE_ALL_OF;
            }
            if (ModelTypeUtil.isOpenApi3Model(schema)) {
                if (NodeUtil.isDefined(NodeUtil.getNodeProperty(schema, TYPE_ANY_OF))) {
                    return TYPE_ANY_OF;
                }
                if (NodeUtil.isDefined(NodeUtil.getNodeProperty(schema, TYPE_ONE_OF))) {
                    return TYPE_ONE_OF;
                }
            }
        } else if (ModelTypeUtil.isAsyncApiModel(schema)) {
            if (NodeUtil.isDefined(((AsyncApiSchema) schema).getAllOf())) {
                return TYPE_ALL_OF;
            }
            if (NodeUtil.isDefined(((AsyncApiSchema) schema).getAnyOf())) {
                return TYPE_ANY_OF;
            }
            if (NodeUtil.isDefined(((AsyncApiSchema) schema).getOneOf())) {
                return TYPE_ONE_OF;
            }
        }

        return TYPE_NONE;
    }

    /**
     * Creates a schema of a particular type (allOf, anyOf, etc).
     * @param parentSchema
     * @param inheritanceType
     */
    protected Schema createSchema(Schema parentSchema, String inheritanceType) {
        if (NodeUtil.equals(TYPE_ALL_OF, inheritanceType)) {
            return createAllOfSchema(parentSchema);
        }
        if (NodeUtil.equals(TYPE_ANY_OF, inheritanceType)) {
            return createAnyOfSchema(parentSchema);
        }
        if (NodeUtil.equals(TYPE_ONE_OF, inheritanceType)) {
            return createOneOfSchema(parentSchema);
        }
        // TODO is it possible to get here?  if so what should we do?
        return createAllOfSchema(parentSchema);
    }

    /**
     * Copies the given list of schemas to the appropriate property on the model
     * @param schemas
     * @param targetSchema 
     * @param inheritanceType
     */
    protected void copySchemaJsTo(List<ObjectNode> schemas, Schema targetSchema, String inheritanceType) {
        if (NodeUtil.equals(TYPE_ALL_OF, inheritanceType)) {
            schemas.forEach(ser -> {
                addAllOfSchema(targetSchema, (Schema) Library.readNode(ser, createAllOfSchema(targetSchema)));
            });
        }
        if (NodeUtil.equals(TYPE_ANY_OF, inheritanceType)) {
            schemas.forEach(ser -> {
                addAnyOfSchema(targetSchema, (Schema) Library.readNode(ser, createAnyOfSchema(targetSchema)));
            });
        }
        if (NodeUtil.equals(TYPE_ONE_OF, inheritanceType)) {
            schemas.forEach(ser -> {
                addOneOfSchema(targetSchema, (Schema) Library.readNode(ser, createOneOfSchema(targetSchema)));
            });
        }        
    }
    
    private static void addAllOfSchema(Schema to, Schema from) {
        to.addAllOf(from);
    }
    
    private static Schema createAllOfSchema(Schema schema) {
        return schema.createSchema();
    }
    
    private static void addAnyOfSchema(Schema to, Schema from) {
        if (ModelTypeUtil.isOpenApi30Model(to)) {
            ((OpenApi30Schema) to).addAnyOf((OpenApi30Schema) from);
        } else if (ModelTypeUtil.isOpenApi31Model(to)) {
            ((OpenApi31Schema) to).addAnyOf((OpenApi31Schema) from);
        } else if (ModelTypeUtil.isAsyncApiModel(to)) {
            ((AsyncApiSchema) to).addAnyOf((AsyncApiSchema) from);
        }
    }
    
    private static Schema createAnyOfSchema(Schema schema) {
        return schema.createSchema();
    }
    
    private static void addOneOfSchema(Schema to, Schema from) {
        if (ModelTypeUtil.isOpenApi30Model(to)) {
            ((OpenApi30Schema) to).addOneOf((OpenApi30Schema) from);
        } else if (ModelTypeUtil.isOpenApi31Model(to)) {
            ((OpenApi31Schema) to).addOneOf((OpenApi31Schema) from);
        } else if (ModelTypeUtil.isAsyncApiModel(to)) {
            ((AsyncApiSchema) to).addOneOf((AsyncApiSchema) from);
        }
    }
    
    private static Schema createOneOfSchema(Schema schema) {
        return schema.createSchema();
    }

    protected boolean has$Ref(Node node) {
        if (node != null && node instanceof Referenceable) {
            String ref = ((Referenceable) node).get$ref();
            return !NodeUtil.isNullOrUndefined(ref);
        }
        return false;
    }

}
