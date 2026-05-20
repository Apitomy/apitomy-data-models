package io.apitomy.datamodels.transform;

import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.ExternalDocumentation;
import io.apitomy.datamodels.models.Operation;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiExternalDocumentation;
import io.apitomy.datamodels.models.openapi.OpenApiSchema;
import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;

public class ExternalDocsCreator extends CombinedVisitorAdapter {

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
