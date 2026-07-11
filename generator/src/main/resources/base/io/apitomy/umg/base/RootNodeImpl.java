package io.apitomy.umg.base;

public abstract class RootNodeImpl extends NodeImpl implements RootNode {

    private final ModelType _modelType;

    public RootNodeImpl(ModelType modelType) {
        this._modelType = modelType;
    }

    @Override
    public RootNode root() {
        return this;
    }

    @Override
    public ModelType modelType() {
        return this._modelType;
    }

}
