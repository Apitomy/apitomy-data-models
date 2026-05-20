package io.apitomy.datamodels.deref;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;

public abstract class ReferencedNodeImporter extends CombinedVisitorAdapter {

    private final Document doc;
    private final Node nodeWithUnresolvedRef;
    private final String ref;
    private final boolean _shouldInline;

    private Node importedNode;
    private String pathToImportedNode;

    public ReferencedNodeImporter(Document doc, Node nodeWithUnresolvedRef, String ref, boolean shouldInline) {
        super();
        this.doc = doc;
        this.nodeWithUnresolvedRef = nodeWithUnresolvedRef;
        this.ref = ref;
        this._shouldInline = shouldInline;
    }

    public Node getImportedNode() {
        return this.importedNode;
    }

    public String getPathToImportedNode() {
        return this.pathToImportedNode;
    }

    protected Document getDoc() {
        return doc;
    }

    protected Node getNodeWithUnresolvedRef() {
        return this.nodeWithUnresolvedRef;
    }

    protected String getRef() {
        return ref;
    }

    public boolean shouldInline() {
        return this._shouldInline;
    }

    public String getNameHintFromRef(String defaultHint) {
        int idx = this.ref.lastIndexOf('/');
        if (idx > 0) {
            return this.ref.substring(idx + 1);
        }
        return defaultHint;
    }

    public void importNode(Node resolvedNode) {
        resolvedNode.accept(this);
    }

    /**
     * Imports a JsonNode (JSON/YAML content that is not OpenAPI/AsyncAPI).
     * Subclasses should override this method to handle JSON content appropriately.
     * The default implementation throws an UnsupportedOperationException.
     *
     * @param jsonNode the JsonNode to import
     * @param mediaType the media type of the content (may be null)
     */
    public void importJson(JsonNode jsonNode, String mediaType) {
        throw new UnsupportedOperationException("importJson is not supported by " + getClass().getName());
    }

    /**
     * Imports text content (non-JSON content such as Protocol Buffers, GraphQL, etc.).
     * Subclasses should override this method to handle text content appropriately.
     * The default implementation throws an UnsupportedOperationException.
     *
     * @param textContent the text content to import
     * @param mediaType the media type of the content (may be null)
     */
    public void importText(String textContent, String mediaType) {
        throw new UnsupportedOperationException("importText is not supported by " + getClass().getName());
    }

    public void setPathToImportedNode(Node importedNode, String pathToImportedNode) {
        this.importedNode = importedNode;
        this.pathToImportedNode = pathToImportedNode;
    }

    protected String generateNodeName(String nameHint, Collection<String> existingNames) {
        if (!existingNames.contains(nameHint)) {
            return nameHint;
        }
        int counter = 1;
        while (existingNames.contains(nameHint + counter)) {
            counter++;
        }
        return nameHint + counter;
    }

    protected Collection<String> getComponentNames(Map<String, ?> components) {
        if (components == null) {
            return new HashSet<>();
        }
        return components.keySet();
    }

    protected String getComponentName(Node nodeWithUnresolvedRef) {
        NodePath nodePath = NodePathUtil.createNodePath(getNodeWithUnresolvedRef());
        return nodePath.getLastSegment().getValue();
    }

    protected void inlineComponent(String componentType, Node node) {
        ObjectNode json = Library.writeNode(node);
        Library.readNode(json, getNodeWithUnresolvedRef());
        setPathToImportedNode(getNodeWithUnresolvedRef(), componentType, getComponentName(getNodeWithUnresolvedRef()));
    }

    protected abstract void setPathToImportedNode(Node nodeWithUnresolvedRef, String componentType, String componentName);

}
