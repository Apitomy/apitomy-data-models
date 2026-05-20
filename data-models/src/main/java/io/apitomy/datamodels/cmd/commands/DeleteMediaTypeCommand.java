package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Parameter;
import io.apitomy.datamodels.models.openapi.OpenApiHeader;
import io.apitomy.datamodels.models.openapi.OpenApiMediaType;
import io.apitomy.datamodels.models.openapi.OpenApiRequestBody;
import io.apitomy.datamodels.models.openapi.OpenApiResponse;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xHeader;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xMediaType;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xParameter;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xResponse;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;

/**
 * A command used to delete a single mediaType from an operation.
 * @author eric.wittmann@gmail.com
 */
public class DeleteMediaTypeCommand extends AbstractCommand {

    public String _mediaTypeName;
    public NodePath _mediaTypePath;
    public NodePath _parentPath;

    public ObjectNode _oldMediaType;
    public int _oldMediaTypeIndex;

    public DeleteMediaTypeCommand() {
    }

    public DeleteMediaTypeCommand(OpenApiMediaType mediaType) {
        this._mediaTypeName = mediaType.mapPropertyName();
        this._mediaTypePath = NodePathUtil.createNodePath(mediaType);
        this._parentPath = NodePathUtil.createNodePath(mediaType.parent());
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[DeleteMediaTypeCommand] Executing.");
        this._oldMediaType = null;

        OpenApiMediaType mediaType = (OpenApiMediaType) NodePathUtil.resolveNodePath(this._mediaTypePath, document);
        if (this.isNullOrUndefined(mediaType)) {
            return;
        }

        this._oldMediaType = Library.writeNode(mediaType);

        MediaTypeRemover viz = new MediaTypeRemover(this._mediaTypeName);
        mediaType.parent().accept(viz);
        this._oldMediaTypeIndex = viz.index;
    }
    
    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[DeleteMediaTypeCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldMediaType)) {
            return;
        }

        Node parent = NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        MediaTypeRestorer viz = new MediaTypeRestorer(this._mediaTypeName, this._oldMediaType, this._oldMediaTypeIndex);
        parent.accept(viz);
    }

    public static class MediaTypeRemover extends CombinedVisitorAdapter {
        private String mediaTypeName;
        public int index = -1;

        public MediaTypeRemover(String mediaTypeName) {
            this.mediaTypeName = mediaTypeName;
        }

        @Override
        public void visitParameter(Parameter node) {
            OpenApi3xParameter param = (OpenApi3xParameter) node;
            index = AbstractCommand.indexOf(param.getContent().keySet(), this.mediaTypeName);
            param.removeContent(this.mediaTypeName);
        }

        @Override
        public void visitRequestBody(OpenApiRequestBody node) {
            index = AbstractCommand.indexOf(node.getContent().keySet(), this.mediaTypeName);
            node.removeContent(this.mediaTypeName);
        }

        @Override
        public void visitResponse(OpenApiResponse node) {
            OpenApi3xResponse response = (OpenApi3xResponse) node;
            index = AbstractCommand.indexOf(response.getContent().keySet(), this.mediaTypeName);
            response.removeContent(this.mediaTypeName);
        }

        @Override
        public void visitHeader(OpenApiHeader node) {
            OpenApi3xHeader header = (OpenApi3xHeader) node;
            index = AbstractCommand.indexOf(header.getContent().keySet(), this.mediaTypeName);
            header.removeContent(this.mediaTypeName);
        }
    }

    public static class MediaTypeRestorer extends CombinedVisitorAdapter {
        private final String mediaTypeName;
        private final ObjectNode oldMediaType;
        private final int oldMediaTypeIndex;

        public MediaTypeRestorer(String mediaTypeName, ObjectNode oldMediaType, int oldMediaTypeIndex) {
            this.mediaTypeName = mediaTypeName;
            this.oldMediaType = oldMediaType;
            this.oldMediaTypeIndex = oldMediaTypeIndex;
        }

        @Override
        public void visitParameter(Parameter node) {
            OpenApi3xParameter param = (OpenApi3xParameter) node;
            OpenApi3xMediaType mediaType = param.createMediaType();
            Library.readNode(this.oldMediaType, mediaType);
            if (this.oldMediaTypeIndex > -1) {
                param.insertContent(this.mediaTypeName, mediaType, this.oldMediaTypeIndex);
            } else {
                param.addContent(this.mediaTypeName, mediaType);
            }
        }

        @Override
        public void visitRequestBody(OpenApiRequestBody node) {
            OpenApiMediaType mediaType = node.createMediaType();
            Library.readNode(this.oldMediaType, mediaType);
            if (this.oldMediaTypeIndex > -1) {
                node.insertContent(this.mediaTypeName, mediaType, this.oldMediaTypeIndex);
            } else {
                node.addContent(this.mediaTypeName, mediaType);
            }
        }

        @Override
        public void visitResponse(OpenApiResponse node) {
            OpenApi3xResponse response = (OpenApi3xResponse) node;
            OpenApi3xMediaType mediaType = response.createMediaType();
            Library.readNode(this.oldMediaType, mediaType);
            if (this.oldMediaTypeIndex > -1) {
                response.insertContent(this.mediaTypeName, mediaType, this.oldMediaTypeIndex);
            } else {
                response.addContent(this.mediaTypeName, mediaType);
            }
        }

        @Override
        public void visitHeader(OpenApiHeader node) {
            OpenApi3xHeader header = (OpenApi3xHeader) node;
            OpenApi3xMediaType mediaType = header.createMediaType();
            Library.readNode(this.oldMediaType, mediaType);
            if (this.oldMediaTypeIndex > -1) {
                header.insertContent(this.mediaTypeName, mediaType, this.oldMediaTypeIndex);
            } else {
                header.addContent(this.mediaTypeName, mediaType);
            }
        }
    }

}
