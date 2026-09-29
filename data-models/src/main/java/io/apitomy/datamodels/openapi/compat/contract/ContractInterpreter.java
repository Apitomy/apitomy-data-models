package io.apitomy.datamodels.openapi.compat.contract;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.jsonschema.compat.containment.SchemaDialect;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * Dispatches to the family-specific interpreter ({@link OpenApi20Interpreter}
 * or {@link OpenApi3Interpreter}) based on the root document's own
 * {@link ModelType}, producing a single family-neutral {@link ContractDocument}
 * shape regardless of which OpenAPI version supplied it.
 */
public final class ContractInterpreter {

    private ContractInterpreter() {
    }

    /**
     * Interprets {@code documentJson} into an effective, read-only contract.
     * {@code documentJson} itself is never mutated: it is parsed from a private
     * clone, so the original remains available (and is used directly) to
     * recover information the generated model cannot represent on its own --
     * see {@link ContractInterpreterSupport#effectiveSecurity}.
     *
     * @param documentJson the raw OpenAPI 2.0, 3.0, 3.1, or 3.2 document
     * @param resourceUri  the retrieval URI recorded on every {@link io.apitomy.datamodels.jsonschema.compat.containment.SchemaView}
     *                     produced from this document's schemas
     * @throws IllegalArgumentException if {@code documentJson} is not a supported OpenAPI family/version
     */
    public static ContractDocument interpret(ObjectNode documentJson, String resourceUri) {
        if (documentJson == null) {
            throw new IllegalArgumentException("documentJson must not be null");
        }
        ObjectNode workingCopy = (ObjectNode) JsonUtil.clone(documentJson);
        RootCapable root = Library.readRoot(workingCopy);
        ModelType modelType = root.modelType();
        if (modelType == ModelType.OPENAPI20) {
            return OpenApi20Interpreter.interpret((OpenApi20Document) root, documentJson, resourceUri);
        }
        SchemaDialect dialect = dialectFor(modelType);
        if (dialect != null) {
            return OpenApi3Interpreter.interpret((OpenApi3xDocument) root, dialect, documentJson, resourceUri);
        }
        throw new IllegalArgumentException("Unsupported model type for contract interpretation: " + modelType);
    }

    private static SchemaDialect dialectFor(ModelType modelType) {
        if (modelType == ModelType.OPENAPI30) {
            return SchemaDialect.OAS30;
        }
        if (modelType == ModelType.OPENAPI31) {
            return SchemaDialect.OAS31;
        }
        if (modelType == ModelType.OPENAPI32) {
            return SchemaDialect.OAS32;
        }
        return null;
    }
}

