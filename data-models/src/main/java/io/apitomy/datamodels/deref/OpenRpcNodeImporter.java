package io.apitomy.datamodels.deref;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Example;
import io.apitomy.datamodels.models.Link;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.openrpc.OpenRpcComponents;
import io.apitomy.datamodels.models.openrpc.OpenRpcContentDescriptor;
import io.apitomy.datamodels.models.openrpc.OpenRpcDocument;
import io.apitomy.datamodels.models.openrpc.OpenRpcError;
import io.apitomy.datamodels.models.openrpc.OpenRpcExamplePairing;
import io.apitomy.datamodels.models.openrpc.OpenRpcLink;
import io.apitomy.datamodels.models.openrpc.OpenRpcSchema;
import io.apitomy.datamodels.models.openrpc.OpenRpcTag;

/**
 * Imports an external Node into an OpenRPC document. Used during dereferencing to internalize
 * an external reference. This importer figures out what kind of thing is being internalized
 * so it can be put in the right place.
 */
public class OpenRpcNodeImporter extends ReferencedNodeImporter {

    public OpenRpcNodeImporter(Document doc, Node nodeWithUnresolvedRef, String ref, boolean shouldInline) {
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
            OpenRpcComponents components = ensureOpenRpcComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedSchema"), getComponentNames(components.getSchemas()));
            components.addSchema(name, (OpenRpcSchema) node);
            node.attach(components);
            setPathToImportedNode(node, componentType, name);
        }
    }

    @Override
    public void visitContentDescriptor(OpenRpcContentDescriptor node) {
        String componentType = "contentDescriptors";
        if (shouldInline()) {
            inlineComponent(componentType, (Node) node);
        } else {
            OpenRpcComponents components = ensureOpenRpcComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedContentDescriptor"), getComponentNames(components.getContentDescriptors()));
            components.addContentDescriptor(name, node);
            ((Node) node).attach(components);
            setPathToImportedNode((Node) node, componentType, name);
        }
    }

    @Override
    public void visitExample(Example node) {
        String componentType = "examples";
        if (shouldInline()) {
            inlineComponent(componentType, (Node) node);
        } else {
            OpenRpcComponents components = ensureOpenRpcComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedExample"), getComponentNames(components.getExamples()));
            components.addExample(name, (io.apitomy.datamodels.models.openrpc.OpenRpcExample) node);
            ((Node) node).attach(components);
            setPathToImportedNode((Node) node, componentType, name);
        }
    }

    @Override
    public void visitLink(Link node) {
        String componentType = "links";
        if (shouldInline()) {
            inlineComponent(componentType, (Node) node);
        } else {
            OpenRpcComponents components = ensureOpenRpcComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedLink"), getComponentNames(components.getLinks()));
            components.addLink(name, (OpenRpcLink) node);
            ((Node) node).attach(components);
            setPathToImportedNode((Node) node, componentType, name);
        }
    }

    @Override
    public void visitError(OpenRpcError node) {
        String componentType = "errors";
        if (shouldInline()) {
            inlineComponent(componentType, (Node) node);
        } else {
            OpenRpcComponents components = ensureOpenRpcComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedError"), getComponentNames(components.getErrors()));
            components.addError(name, node);
            ((Node) node).attach(components);
            setPathToImportedNode((Node) node, componentType, name);
        }
    }

    @Override
    public void visitExamplePairing(OpenRpcExamplePairing node) {
        String componentType = "examplePairings";
        if (shouldInline()) {
            inlineComponent(componentType, (Node) node);
        } else {
            OpenRpcComponents components = ensureOpenRpcComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedExamplePairing"), getComponentNames(components.getExamplePairings()));
            components.addExamplePairing(name, node);
            ((Node) node).attach(components);
            setPathToImportedNode((Node) node, componentType, name);
        }
    }

    @Override
    public void visitTag(Tag node) {
        String componentType = "tags";
        if (shouldInline()) {
            inlineComponent(componentType, (Node) node);
        } else {
            OpenRpcComponents components = ensureOpenRpcComponents();
            String name = generateNodeName(getNameHintFromRef("ImportedTag"), getComponentNames(components.getTags()));
            components.addTag(name, (OpenRpcTag) node);
            ((Node) node).attach(components);
            setPathToImportedNode((Node) node, componentType, name);
        }
    }

    private OpenRpcComponents ensureOpenRpcComponents() {
        OpenRpcDocument doc = (OpenRpcDocument) getDoc();
        if (doc.getComponents() == null) {
            doc.setComponents(doc.createComponents());
        }
        return doc.getComponents();
    }

}
