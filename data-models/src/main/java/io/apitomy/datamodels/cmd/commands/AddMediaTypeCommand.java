package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.OpenApiRequestBody;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Header;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Parameter;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Response;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to add a media type to any node that supports content (request body,
 * response, header, parameter).
 * @author eric.wittmann@gmail.com
 */
public class AddMediaTypeCommand extends AbstractCommand {

    public NodePath _parentPath;
    public String _mediaTypeName;

    public boolean _created;

    public AddMediaTypeCommand() {
    }

    public AddMediaTypeCommand(Node parent, String mediaTypeName) {
        this._parentPath = NodePathUtil.createNodePath(parent);
        this._mediaTypeName = mediaTypeName;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddMediaTypeCommand] Executing.");
        this._created = false;

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        MediaTypeAdder adder = new MediaTypeAdder(this._mediaTypeName);
        parent.accept(adder);
        this._created = adder.created;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddMediaTypeCommand] Reverting.");
        if (!this._created) {
            return;
        }

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        MediaTypeRemover remover = new MediaTypeRemover(this._mediaTypeName);
        parent.accept(remover);
    }

    private static class MediaTypeAdder extends CombinedVisitorAdapter {
        private final String mediaTypeName;
        public boolean created = false;

        public MediaTypeAdder(String mediaTypeName) {
            this.mediaTypeName = mediaTypeName;
        }

        @Override
        public void visitRequestBody(OpenApiRequestBody node) {
            if (node.getContent() != null && node.getContent().containsKey(this.mediaTypeName)) {
                return;
            }
            node.addContent(this.mediaTypeName, node.createMediaType());
            this.created = true;
        }

        @Override
        public void visitResponse(OpenApiResponse node) {
            OpenApi30Response response = (OpenApi30Response) node;
            if (response.getContent() != null && response.getContent().containsKey(this.mediaTypeName)) {
                return;
            }
            response.addContent(this.mediaTypeName, response.createMediaType());
            this.created = true;
        }

        @Override
        public void visitParameter(Parameter node) {
            OpenApi30Parameter param = (OpenApi30Parameter) node;
            if (param.getContent() != null && param.getContent().containsKey(this.mediaTypeName)) {
                return;
            }
            param.addContent(this.mediaTypeName, param.createMediaType());
            this.created = true;
        }

        @Override
        public void visitHeader(OpenApiHeader node) {
            OpenApi30Header header = (OpenApi30Header) node;
            if (header.getContent() != null && header.getContent().containsKey(this.mediaTypeName)) {
                return;
            }
            header.addContent(this.mediaTypeName, header.createMediaType());
            this.created = true;
        }
    }

    private static class MediaTypeRemover extends CombinedVisitorAdapter {
        private final String mediaTypeName;

        public MediaTypeRemover(String mediaTypeName) {
            this.mediaTypeName = mediaTypeName;
        }

        @Override
        public void visitRequestBody(OpenApiRequestBody node) {
            node.removeContent(this.mediaTypeName);
        }

        @Override
        public void visitResponse(OpenApiResponse node) {
            OpenApi30Response response = (OpenApi30Response) node;
            response.removeContent(this.mediaTypeName);
        }

        @Override
        public void visitParameter(Parameter node) {
            OpenApi30Parameter param = (OpenApi30Parameter) node;
            param.removeContent(this.mediaTypeName);
        }

        @Override
        public void visitHeader(OpenApiHeader node) {
            OpenApi30Header header = (OpenApi30Header) node;
            header.removeContent(this.mediaTypeName);
        }
    }

}
