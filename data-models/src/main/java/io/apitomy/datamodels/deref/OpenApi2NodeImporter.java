package io.apitomy.datamodels.deref;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.OpenApiItems;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Definitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Items;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Parameter;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20ParameterDefinitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Response;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20ResponseDefinitions;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Schema;

public class OpenApi2NodeImporter extends ReferencedNodeImporter {

    public OpenApi2NodeImporter(Document doc, Node nodeWithUnresolvedRef, String ref, boolean shouldInline) {
        super(doc, nodeWithUnresolvedRef, ref, shouldInline);
    }

    @Override
    public void visitItems(OpenApiItems node) {
        // Note: there is no place in #/components to store items, so we will inline them.
        ObjectNode json = Library.writeNode(node);
        Library.readNode(json, getNodeWithUnresolvedRef());
        setPathToImportedNode(getNodeWithUnresolvedRef(), null);
    }

    @Override
    public void visitParameter(Parameter node) {
        String collection = "parameters";
        if (shouldInline()) {
            inlineDefinition(collection, node);
        } else {
            OpenApi20Document doc = (OpenApi20Document) getDoc();
            OpenApi20ParameterDefinitions params = (OpenApi20ParameterDefinitions) doc.getParameters();
            if (params == null) {
                params = (OpenApi20ParameterDefinitions) doc.createParameterDefinitions();
                doc.setParameters(params);
            }
            String name = generateNodeName(getNameHintFromRef("ImportedParameter"), params.getItemNames());
            params.addItem(name, (OpenApi20Parameter) node);
            node.attach(params);
            setPathToImportedNode(node, collection, name);
        }
    }

    @Override
    public void visitPathItem(OpenApiPathItem node) {
        // Note: there is no place in #/components to store path items, so they must be inlined.
        ObjectNode json = Library.writeNode(node);
        Library.readNode(json, getNodeWithUnresolvedRef());
        setPathToImportedNode(getNodeWithUnresolvedRef(), null);
    }

    @Override
    public void visitResponse(OpenApiResponse node) {
        String collection = "responses";
        if (shouldInline()) {
            inlineDefinition(collection, node);
        } else {
            OpenApi20Document doc = (OpenApi20Document) getDoc();
            OpenApi20ResponseDefinitions responses = (OpenApi20ResponseDefinitions) doc.getResponses();
            if (responses == null) {
                responses = (OpenApi20ResponseDefinitions) doc.createResponseDefinitions();
                doc.setResponses(responses);
            }
            String name = generateNodeName(getNameHintFromRef("ImportedResponse"), responses.getItemNames());
            responses.addItem(name, (OpenApi20Response) node);
            node.attach(responses);
            setPathToImportedNode(node, collection, name);
        }
    }

    @Override
    public void visitSchema(Schema node) {
        String collection = "definitions";
        if (shouldInline()) {
            inlineDefinition(collection, node);
        } else {
            OpenApi20Document doc = (OpenApi20Document) getDoc();
            OpenApi20Definitions definitions = (OpenApi20Definitions) doc.getDefinitions();
            if (definitions == null) {
                definitions = (OpenApi20Definitions) doc.createDefinitions();
                doc.setDefinitions(definitions);
            }
            String name = generateNodeName(getNameHintFromRef("ImportedSchema"), definitions.getItemNames());
            definitions.addItem(name, (OpenApi20Schema) node);
            node.attach(definitions);
            setPathToImportedNode(node, collection, name);
        }
    }

    @Override
    protected void setPathToImportedNode(Node importedNode, String collection, String name) {
        setPathToImportedNode(importedNode, "#/" + collection + "/" + name);
    }

    private void inlineDefinition(String collection, Node node) {
        inlineComponent(collection, node);
    }

}
