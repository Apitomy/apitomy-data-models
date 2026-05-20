package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.ExternalDocumentation;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiExternalDocumentation;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command to set external documentation on a node (Document, Tag, Operation, or Schema).
 * @author eric.wittmann@gmail.com
 */
public class SetExternalDocsCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _newUrl;
    public String _newDescription;

    public ObjectNode _oldExternalDocs;

    public SetExternalDocsCommand() {
    }

    /**
     * Constructor.
     * @param parent the parent node (Document, Tag, Operation, or Schema)
     * @param url the external documentation URL
     * @param description an optional description
     */
    public SetExternalDocsCommand(Node parent, String url, String description) {
        this._parentPath = NodePathUtil.createNodePath(parent);
        this._newUrl = url;
        this._newDescription = description;
    }

    @Override
    public void execute(Document document) {
        LoggerUtil.info("[SetExternalDocsCommand] Executing.");
        this._oldExternalDocs = null;

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        // Read existing externalDocs for undo
        ExternalDocsReader reader = new ExternalDocsReader();
        parent.accept(reader);
        if (NodeUtil.isDefined(reader.externalDocs)) {
            this._oldExternalDocs = Library.writeNode(reader.externalDocs);
        }

        // Create new externalDocs and set properties
        ExternalDocsCreator creator = new ExternalDocsCreator();
        parent.accept(creator);
        creator.externalDocs.setUrl(this._newUrl);
        creator.externalDocs.setDescription(this._newDescription);
    }

    @Override
    public void undo(Document document) {
        LoggerUtil.info("[SetExternalDocsCommand] Reverting.");

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        if (NodeUtil.isDefined(this._oldExternalDocs)) {
            // Restore old externalDocs
            ExternalDocsCreator creator = new ExternalDocsCreator();
            parent.accept(creator);
            Library.readNode(this._oldExternalDocs, creator.externalDocs);
        } else {
            // Remove externalDocs (none existed before)
            ExternalDocsSetter setter = new ExternalDocsSetter(null);
            parent.accept(setter);
        }
    }

    /**
     * Visitor that reads the existing externalDocs from a parent node.
     */
    private static class ExternalDocsReader extends CombinedVisitorAdapter {
        ExternalDocumentation externalDocs;

        @Override
        public void visitSchema(Schema node) {
            externalDocs = ((OpenApiSchema) node).getExternalDocs();
        }

        @Override
        public void visitDocument(Document node) {
            externalDocs = ((OpenApiDocument) node).getExternalDocs();
        }

        @Override
        public void visitOperation(Operation node) {
            externalDocs = node.getExternalDocs();
        }

        @Override
        public void visitTag(Tag node) {
            externalDocs = node.getExternalDocs();
        }
    }

    /**
     * Visitor that creates a new externalDocs child on a parent node.
     */
    private static class ExternalDocsCreator extends CombinedVisitorAdapter {
        ExternalDocumentation externalDocs;

        @Override
        public void visitSchema(Schema node) {
            OpenApiSchema oaiSchema = (OpenApiSchema) node;
            externalDocs = oaiSchema.createExternalDocumentation();
            oaiSchema.setExternalDocs((OpenApiExternalDocumentation) externalDocs);
        }

        @Override
        public void visitDocument(Document node) {
            externalDocs = ((OpenApiDocument) node).createExternalDocumentation();
            ((OpenApiDocument) node).setExternalDocs((OpenApiExternalDocumentation) externalDocs);
        }

        @Override
        public void visitOperation(Operation node) {
            externalDocs = node.createExternalDocumentation();
            node.setExternalDocs(externalDocs);
        }

        @Override
        public void visitTag(Tag node) {
            externalDocs = node.createExternalDocumentation();
            node.setExternalDocs(externalDocs);
        }
    }

    /**
     * Visitor that sets externalDocs to a given value (used for setting to null during undo).
     */
    private static class ExternalDocsSetter extends CombinedVisitorAdapter {
        private final ExternalDocumentation value;

        ExternalDocsSetter(ExternalDocumentation value) {
            this.value = value;
        }

        @Override
        public void visitSchema(Schema node) {
            ((OpenApiSchema) node).setExternalDocs((OpenApiExternalDocumentation) value);
        }

        @Override
        public void visitDocument(Document node) {
            ((OpenApiDocument) node).setExternalDocs((OpenApiExternalDocumentation) value);
        }

        @Override
        public void visitOperation(Operation node) {
            node.setExternalDocs(value);
        }

        @Override
        public void visitTag(Tag node) {
            node.setExternalDocs(value);
        }
    }
}
