package io.apitomy.datamodels.deref;

import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Referenceable;
import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.models.ServerVariable;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannelItem;
import io.apitomy.datamodels.models.openapi.OpenApiItems;
import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
import io.apitomy.datamodels.models.openapi.OpenApiResponses;

/**
 * Determines if a Node being dereferenced should be inlined or imported.  Certain types of
 * Nodes cannot be imported into e.g. #/components because there is no collection in the
 * components entity for them.  In other cases we inline because the Node containing the
 * $ref is itself already located in the components section of the document, so there's no
 * point in adding *another* component and pointing to it.
 *
 * @author eric.wittmann@gmail.com
 */
public class InlineOrImportVisitor extends AllReferenceableNodeVisitor {

    private boolean inline = false;

    public boolean shouldInline() {
        return inline;
    }

    @Override
    protected void visitReferenceableNode(Referenceable refNode) {
        Node node = (Node) refNode;
        IsDefinitionParentVisitor idpv = new IsDefinitionParentVisitor();
        node.parent().accept(idpv);
        // TODO also check if the node has any properties OTHER than $ref??
        this.inline = idpv.isDefinitionParent();
    }

    /*
     * The following types can only be inlined because there is no place in the source
     * document to import them.  For example, in OpenAPI there is no "pathItems" collection
     * in the #/components entity.
     */

    @Override
    public void visitItems(OpenApiItems node) {
        this.inline = true;
    }

    @Override
    public void visitPathItem(OpenApiPathItem node) {
        this.inline = true;
    }

    @Override
    public void visitResponses(OpenApiResponses node) {
        this.inline = true;
    }

    @Override
    public void visitChannelItem(AsyncApiChannelItem node) {
        this.inline = true;
    }

    @Override
    public void visitServer(Server node) {
        this.inline = true;
    }

    @Override
    public void visitServerVariable(ServerVariable node) {
        this.inline = true;
    }

}
