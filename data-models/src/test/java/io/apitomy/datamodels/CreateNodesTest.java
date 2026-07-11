package io.apitomy.datamodels;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.Schema;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import org.junit.Assert;
import org.junit.Test;

public class CreateNodesTest {

    @Test
    public void test() {
        OpenApi30Document doc = (OpenApi30Document) Library.createDocument(ModelType.OPENAPI30);
        doc.setInfo(doc.createInfo());
        doc.setComponents(doc.createComponents());
        doc.getComponents().addSchema("fooType", doc.getComponents().createSchema());
        doc.setPaths(doc.createPaths());
        doc.getPaths().addItem("/widgets", doc.getPaths().createPathItem());
        Schema fooTypeSchema = doc.getComponents().getSchemas().get("fooType");
        fooTypeSchema.addAllOf(fooTypeSchema.createSchema());
        fooTypeSchema.addAllOf(fooTypeSchema.createSchema());
        fooTypeSchema.getAllOf().get(0).setDescription("allOf Schema #1");
        fooTypeSchema.getAllOf().get(1).setDescription("allOf Schema #2");

        NodePath infoNP = createNodePath(doc.getInfo());
        NodePath pathNP = createNodePath(doc.getPaths().getItem("/widgets"));
        NodePath schemaNP = createNodePath(doc.getComponents().getSchemas().get("fooType"));
        NodePath allOfNP = createNodePath(doc.getComponents().getSchemas().get("fooType").getAllOf().get(0));

        Assert.assertEquals("/info", infoNP.toString());
        Assert.assertEquals("/components/schemas[fooType]", schemaNP.toString());
        Assert.assertEquals("/paths[/widgets]", pathNP.toString());
        Assert.assertEquals("/components/schemas[fooType]/allOf[0]", allOfNP.toString());

        Assert.assertNotNull(NodePathUtil.resolveNodePath(infoNP, doc));
        Assert.assertNotNull(NodePathUtil.resolveNodePath(schemaNP, doc));
        Assert.assertNotNull(NodePathUtil.resolveNodePath(pathNP, doc));

        Assert.assertEquals("/widgets", doc.getPaths().getItem("/widgets").mapPropertyName());
        Assert.assertEquals("fooType", doc.getComponents().getSchemas().get("fooType").mapPropertyName());
    }

    public static NodePath createNodePath(Node node) {
        return NodePathUtil.createNodePath(node);
    }

}
