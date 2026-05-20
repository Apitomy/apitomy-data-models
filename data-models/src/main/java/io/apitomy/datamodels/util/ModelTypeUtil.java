package io.apitomy.datamodels.util;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Node;

public class ModelTypeUtil {

    public static ModelType fromString(String type) {
        return ModelType.valueOf(type);
    }

    public static String getVersion(ModelType type) {
        switch (type) {
            case ASYNCAPI20:
                return "2.0.0";
            case ASYNCAPI21:
                return "2.1.0";
            case ASYNCAPI22:
                return "2.2.0";
            case ASYNCAPI23:
                return "2.3.0";
            case ASYNCAPI24:
                return "2.4.0";
            case ASYNCAPI25:
                return "2.5.0";
            case ASYNCAPI26:
                return "2.6.0";
            case ASYNCAPI30:
                return "3.0.0";
            case ASYNCAPI31:
                return "3.1.0";
            case JSDRAFT4:
                return "http://json-schema.org/draft-04/schema#";
            case JSDRAFT6:
                return "http://json-schema.org/draft-06/schema#";
            case JSDRAFT7:
                return "http://json-schema.org/draft-07/schema#";
            case JS201909:
                return "https://json-schema.org/draft/2019-09/schema";
            case JS202012:
                return "https://json-schema.org/draft/2020-12/schema";
            case OPENAPI20:
                return "2.0";
            case OPENAPI30:
                return "3.0.3";
            case OPENAPI31:
                return "3.1.0";
            case OPENAPI32:
                return "3.2.0";
            case OPENRPC13:
                return "1.3.2";
            case OPENRPC14:
                return "1.4.0";
        }
        throw new RuntimeException("Unsupported model type: " + type.name());
    }

    /**
     * Returns the JSON property name used to store the specification version
     * for the given model type (e.g. "openapi", "asyncapi", "swagger", "openrpc").
     * @param type the model type
     * @return the version property name
     */
    public static String getVersionPropertyName(ModelType type) {
        switch (type) {
            case OPENAPI20:
                return "swagger";
            case OPENAPI30:
            case OPENAPI31:
            case OPENAPI32:
                return "openapi";
            case ASYNCAPI20:
            case ASYNCAPI21:
            case ASYNCAPI22:
            case ASYNCAPI23:
            case ASYNCAPI24:
            case ASYNCAPI25:
            case ASYNCAPI26:
            case ASYNCAPI30:
            case ASYNCAPI31:
                return "asyncapi";
            case OPENRPC13:
            case OPENRPC14:
                return "openrpc";
            case JSDRAFT4:
            case JSDRAFT6:
            case JSDRAFT7:
            case JS201909:
            case JS202012:
                return "$schema";
        }
        throw new RuntimeException("Unsupported model type: " + type.name());
    }

    public static boolean isOpenApiModel(Node node) {
        switch (node.root().modelType()) {
            case OPENAPI20:
            case OPENAPI30:
            case OPENAPI31:
            case OPENAPI32:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAsyncApiModel(Node node) {
        switch (node.root().modelType()) {
            case ASYNCAPI20:
            case ASYNCAPI21:
            case ASYNCAPI22:
            case ASYNCAPI23:
            case ASYNCAPI24:
            case ASYNCAPI25:
            case ASYNCAPI26:
            case ASYNCAPI30:
            case ASYNCAPI31:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAsyncApi2Model(Node node) {
        switch (node.root().modelType()) {
            case ASYNCAPI20:
            case ASYNCAPI21:
            case ASYNCAPI22:
            case ASYNCAPI23:
            case ASYNCAPI24:
            case ASYNCAPI25:
            case ASYNCAPI26:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAsyncApi3Model(Node node) {
        switch (node.root().modelType()) {
            case ASYNCAPI30:
            case ASYNCAPI31:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAsyncApi30Model(Node node) {
        switch (node.root().modelType()) {
            case ASYNCAPI30:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAsyncApi31Model(Node node) {
        switch (node.root().modelType()) {
            case ASYNCAPI31:
                return true;
            default:
                return false;
        }
    }

    public static boolean isOpenApi3Model(Node node) {
        switch (node.root().modelType()) {
            case OPENAPI30:
            case OPENAPI31:
            case OPENAPI32:
                return true;
            default:
                return false;
        }
    }

    public static boolean isOpenApi2Model(Node node) {
        switch (node.root().modelType()) {
            case OPENAPI20:
                return true;
            default:
                return false;
        }
    }

    public static boolean isOpenApi30Model(Node node) {
        switch (node.root().modelType()) {
            case OPENAPI30:
                return true;
            default:
                return false;
        }
    }

    public static boolean isOpenApi31Model(Node node) {
        switch (node.root().modelType()) {
            case OPENAPI31:
                return true;
            default:
                return false;
        }
    }

    public static boolean isOpenApi32Model(Node node) {
        switch (node.root().modelType()) {
            case OPENAPI32:
                return true;
            default:
                return false;
        }
    }

    public static boolean isJsonSchemaModel(Node node) {
        switch (node.root().modelType()) {
            case JSDRAFT4:
            case JSDRAFT6:
            case JSDRAFT7:
            case JS201909:
            case JS202012:
                return true;
            default:
                return false;
        }
    }

    public static boolean isOpenRpcModel(Node node) {
        switch (node.root().modelType()) {
            case OPENRPC13:
            case OPENRPC14:
                return true;
            default:
                return false;
        }
    }

}
