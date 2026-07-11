package io.apitomy.datamodels.deref;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.TraverserDirection;
import io.apitomy.datamodels.VisitorUtil;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.visitors.Visitor;
import io.apitomy.datamodels.refs.IReferenceResolver;
import io.apitomy.datamodels.refs.Reference;
import io.apitomy.datamodels.refs.ReferenceContext;
import io.apitomy.datamodels.refs.ResolvedReference;
import io.apitomy.datamodels.util.ModelTypeUtil;

public class Dereferencer {

    private IReferenceResolver resolver;
    private boolean strict;

    public Dereferencer(IReferenceResolver resolver, boolean strict) {
        this.resolver = resolver;
        this.strict = strict;
    }

    /**
     * Performs the dereferencing logic on the source document.  Returns a new document that
     * contains no external references (all external references are internalized).
     */
    public Document dereference(final Document source) {
        Document doc = Library.cloneDocument(source);
        Map<Reference, String> resolvedNodes = new HashMap<>();

        // Mark all internal references as already "resolved"
        InternalRefResolverVisitor irrv = new InternalRefResolverVisitor();
        VisitorUtil.visitTree(doc, irrv, TraverserDirection.down);

        int unresolveableRefCount = 0;
        boolean isDone = false;
        do {
            // Find all nodes in the document with $refs that need to be resolved.
            UnresolvedReferenceNodeVisitor visitor = new UnresolvedReferenceNodeVisitor();
            VisitorUtil.visitTree(doc, visitor, TraverserDirection.down);
            List<Node> nodesWithUnresolvedRefs = visitor.getNodesWithUnresolvedRefs();

            // If none were found, we're done.  Otherwise, resolve all unresolved refs!
            if (nodesWithUnresolvedRefs.isEmpty()) {
                isDone = true;
            } else {
                // Resolve all external refs.
                for (Node nodeWithUnresolvedRef : nodesWithUnresolvedRefs) {
                    if (!resolveNodeWithRef(doc, resolvedNodes, nodeWithUnresolvedRef)) {
                        unresolveableRefCount++;
                    }
                }
            }
        } while (!isDone);

        // Fail if strict mode is enabled and at least one ref was not resolved.
        if (this.strict && unresolveableRefCount > 0) {
            throw new RuntimeException("Could not resolve at least one external reference.");
        }

        return doc;
    }

    /**
     * Resolves external references by loading the external source and pulling out a Node
     * from the resulting model.  That Node is then imported into the original source
     * document in the appropriate place (e.g. within "components" for OpenAPI and AsyncAPI
     * documents).  Once that is done, the $ref is updated to point to the new, imported
     * component.
     *
     * For AsyncAPI 3.0 documents, non-JSON-Schema formats (such as Avro) are automatically
     * wrapped in Multi-Format Schema Objects with the appropriate schemaFormat property.
     *
     * Returns true if the ref was successfully resolved.
     *
     * @param doc the document being dereferenced
     * @param resolvedNodes resolved node map tracking already-resolved references
     * @param nodeWithUnresolvedRef the node containing the unresolved reference
     * @return true if the reference was successfully resolved, false otherwise
     */
    private boolean resolveNodeWithRef(Document doc, Map<Reference, String> resolvedNodes, Node nodeWithUnresolvedRef) {
        String ref = ((Referenceable) nodeWithUnresolvedRef).get$ref();
        Reference reference = new Reference(ref);

        // What is the context of this reference?  The context is the chain of references that got us
        // to this point.  It could be that A.json has a $ref to B.json which has a $ref to C.json, etc.
        // The context allows us to know that chain of references so that we can canonicalize the value
        // of this $ref into a string that can be resolved by the resolver.
        ReferenceContext refContext = (ReferenceContext) nodeWithUnresolvedRef.getNodeAttribute(DereferenceConstants.KEY_REFERENCE_CONTEXT);
        if (refContext == null) {
            refContext = new ReferenceContext();
        }
        Reference canonicalReference = refContext.canonicalizeRef(reference);

        boolean inlined = false;
        String newRefValue = null;

        if (resolvedNodes.containsKey(canonicalReference)) {
            newRefValue = resolvedNodes.get(canonicalReference);
        } else {
            // Resolve the reference.
            ResolvedReference resolvedRef = resolver.resolveRef(canonicalReference.getRef(), nodeWithUnresolvedRef);

            // Cannot be resolved
            if (resolvedRef == null) {
                nodeWithUnresolvedRef.setNodeAttribute(DereferenceConstants.KEY_RESOLUTION, DereferenceConstants.VALUE_UNRESOLVEABLE);
                return false;
            }

            // Reference was resolved, insert it into the source document (eg. into the #/components section of the source doc)
            inlined = shouldInlineNode(nodeWithUnresolvedRef);
            ReferencedNodeImporter importer = createImporter(doc, nodeWithUnresolvedRef, ref, inlined);

            // Import the resolved reference based on its type
            try {
                if (resolvedRef.isNode()) {
                    importer.importNode(resolvedRef.asNode());
                } else if (resolvedRef.isJson()) {
                    importer.importJson(resolvedRef.asJson(), resolvedRef.getMediaType());
                } else if (resolvedRef.isText()) {
                    importer.importText(resolvedRef.asText(), resolvedRef.getMediaType());
                }
            } catch (UnsupportedOperationException e) {
                // This importer doesn't support this content type (e.g., OpenAPI trying to import Protobuf)
                // Mark as unresolveable and continue
                nodeWithUnresolvedRef.setNodeAttribute(DereferenceConstants.KEY_RESOLUTION, DereferenceConstants.VALUE_UNRESOLVEABLE);
                return false;
            }

            Node importedNode = importer.getImportedNode();
            newRefValue = importer.getPathToImportedNode();

            // Set appropriate context information on any Referenceable nodes in the resolved node.  This
            // will allow us to handle importing nodes that have additional references to even more nodes
            // (either internal or external).
            ReferenceContext resolvedNodeRefContext = refContext.append(reference);
            Visitor scv = new SetContextVisitor(resolvedNodeRefContext);
            VisitorUtil.visitTree(importedNode, scv, TraverserDirection.down);

            // Cache the resolved reference in the resolved node map
            if (newRefValue != null) {
                resolvedNodes.put(canonicalReference, newRefValue);
            }
        }

        // We have a new value for the $ref (unless we inlined the imported node).
        if (inlined) {
            ((Referenceable) nodeWithUnresolvedRef).set$ref(null);
        } else {
            ((Referenceable) nodeWithUnresolvedRef).set$ref(newRefValue);
        }

        // Resolution complete, make sure to mark it as resolved.
        nodeWithUnresolvedRef.setNodeAttribute(DereferenceConstants.KEY_RESOLUTION, DereferenceConstants.VALUE_RESOLVED);

        return true;
    }

    private ReferencedNodeImporter createImporter(Document doc, Node nodeWithUnresolvedRef, String ref, boolean shouldInline) {
        if (ModelTypeUtil.isAsyncApi3Model(doc)) {
            return new AsyncApi3NodeImporter(doc, nodeWithUnresolvedRef, ref, shouldInline);
        } else if (ModelTypeUtil.isAsyncApi2Model(doc)) {
            return new AsyncApi2NodeImporter(doc, nodeWithUnresolvedRef, ref, shouldInline);
        } else if (ModelTypeUtil.isOpenApi2Model(doc)) {
            return new OpenApi2NodeImporter(doc, nodeWithUnresolvedRef, ref, shouldInline);
        } else if (ModelTypeUtil.isOpenApi3Model(doc)) {
            return new OpenApi3NodeImporter(doc, nodeWithUnresolvedRef, ref, shouldInline);
        } else if (ModelTypeUtil.isOpenRpcModel(doc)) {
            return new OpenRpcNodeImporter(doc, nodeWithUnresolvedRef, ref, shouldInline);
        }
        throw new RuntimeException("Unsupported model type: " + doc.root().modelType());
    }

    /**
     * Returns true if the node being imported should be inlined or false if the node should
     * be imported.  An imported node should be inlined if:
     *
     *  - the node with the $ref is a definition with no properties other than the $ref.
     *  - the type of node being imported cannot be put in #/components
     *
     * @param nodeWithUnresolvedRef
     */
    private boolean shouldInlineNode(Node nodeWithUnresolvedRef) {
        InlineOrImportVisitor viz = new InlineOrImportVisitor();
        nodeWithUnresolvedRef.accept(viz);
        return viz.shouldInline();
    }

}
