package io.apitomy.datamodels.deref;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Example;
import io.apitomy.datamodels.models.Link;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.SecurityScheme;
import io.apitomy.datamodels.models.openapi.OpenApiCallback;
import io.apitomy.datamodels.models.openapi.OpenApiComponents;
import io.apitomy.datamodels.models.openapi.OpenApiExample;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.OpenApiLink;
import io.apitomy.datamodels.models.openapi.OpenApiParameter;
import io.apitomy.datamodels.models.openapi.OpenApiSecurityScheme;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiRequestBody;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.OpenApiResponses;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Document;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * Imports an external Node into a OpenAPI 3 document.  Used during dereferencing to internalize
 * an external reference.  This importer figures out what kind of thing is being internalized
 * so it can be put in the right place.
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApi3NodeImporter extends ReferencedNodeImporter {

    public OpenApi3NodeImporter(Document doc, Node nodeWithUnresolvedRef, String ref, boolean shouldInline) {
        super(doc, nodeWithUnresolvedRef, ref, shouldInline);
    }

    @Override
    protected void setPathToImportedNode(Node importedNode, String type, String name) {
        setPathToImportedNode(importedNode, "#/components/" + type + "/" + name);
    }

    @Override
    public void visitSchema(Schema node) {
        String componentType = "schemas";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedSchema"), getComponentNames(components.getSchemas()));
            // Cast to OpenApiSchema (OpenApi30Schema and OpenApi31Schema implement this)
            components.addSchema(name, (io.apitomy.datamodels.models.openapi.OpenApiSchema) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitCallback(OpenApiCallback node) {
        String componentType = "callbacks";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedCallback"), getComponentNames(components.getCallbacks()));
            components.addCallback(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitExample(Example node) {
        String componentType = "examples";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedExample"), getComponentNames(components.getExamples()));
            components.addExample(name, (OpenApiExample) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitHeader(OpenApiHeader node) {
        String componentType = "headers";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedHeader"), getComponentNames(components.getHeaders()));
            components.addHeader(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitLink(Link node) {
        String componentType = "links";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedLink"), getComponentNames(components.getLinks()));
            components.addLink(name, (OpenApiLink) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitParameter(Parameter node) {
        String componentType = "parameters";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedParameter"), getComponentNames(components.getParameters()));
            components.addParameter(name, (OpenApiParameter) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
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
    public void visitRequestBody(OpenApiRequestBody node) {
        String componentType = "requestBodies";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedRequestBody"), getComponentNames(components.getRequestBodies()));
            components.addRequestBody(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitResponse(OpenApiResponse node) {
        String componentType = "responses";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedResponse"), getComponentNames(components.getResponses()));
            components.addResponse(name, node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitResponses(OpenApiResponses node) {
        // Note: there is no place in #/components to store a Responses node, so inline it
        ObjectNode json = Library.writeNode(node);
        Library.readNode(json, getNodeWithUnresolvedRef());
        setPathToImportedNode(getNodeWithUnresolvedRef(), null);
    }

    @Override
    public void visitSecurityScheme(SecurityScheme node) {
        String componentType = "securitySchemes";
        if (shouldInline()) {
            inlineComponent(componentType, node);
        } else {
            OpenApiComponents components = ensureOpenApiComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedSecurityScheme"), getComponentNames(components.getSecuritySchemes()));
            components.addSecurityScheme(name, (OpenApiSecurityScheme) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    private OpenApiComponents ensureOpenApiComponents() {
        if (ModelTypeUtil.isOpenApi30Model(getDoc())) {
            OpenApi30Document doc = (OpenApi30Document) getDoc();
            if (doc.getComponents() == null) {
                doc.setComponents(doc.createComponents());
            }
            return doc.getComponents();
        }
        if (ModelTypeUtil.isOpenApi31Model(getDoc())) {
            OpenApi31Document doc = (OpenApi31Document) getDoc();
            if (doc.getComponents() == null) {
                doc.setComponents(doc.createComponents());
            }
            return doc.getComponents();
        }
        return null;
    }

}
