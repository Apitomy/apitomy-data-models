package io.apitomy.umg.pipe;

public class PrepareStage extends AbstractStage {
    @Override
    protected void doProcess() {
//        // Create the base packages
//        var basePackage = PackageModel.builder().build();
//        basePackage.setName("io.apitomy.datamodels");
//        getState().getModelIndex().indexPackage(basePackage);
//        getState().setBasePackage(basePackage);
//
//        var coreModelPackage = PackageModel.builder().build();
//        coreModelPackage.setName("io.apitomy.datamodels.core.models");
//        getState().getModelIndex().indexPackage(coreModelPackage);
//        //getState().setCoreModelPackage(coreModelPackage);
//
//        // Create some common base classes
//        var nodeClass = ClassModel.builder().build();
//        nodeClass.setName("Node");
//        nodeClass.setPackage(coreModelPackage);
//        nodeClass.setCore(true);
//        nodeClass.setAbstract(true);
//        coreModelPackage.getClasses().put(nodeClass.getName(), nodeClass);
//        getState().getModelIndex().indexClass(nodeClass);
//        getState().setNodeClass(nodeClass);
//
//        var extensibleNodeClass = ClassModel.builder().build();
//        extensibleNodeClass.setName("ExtensibleNode");
//        extensibleNodeClass.setPackage(coreModelPackage);
//        extensibleNodeClass.setCore(true);
//        extensibleNodeClass.setAbstract(true);
//        coreModelPackage.getClasses().put(extensibleNodeClass.getName(), extensibleNodeClass);
//        getState().getModelIndex().indexClass(extensibleNodeClass);
//        getState().setExtensibleNodeClass(extensibleNodeClass);
//
//        var documentClass = ClassModel.builder().build();
//        documentClass.setName("Document");
//        documentClass.setPackage(coreModelPackage);
//        documentClass.setAbstract(true);
//        documentClass.setCore(true);
//        coreModelPackage.getClasses().put(documentClass.getName(), documentClass);
//        getState().getModelIndex().indexClass(documentClass);
//        //getState().setDocumentClass(documentClass);
    }
}
