package io.apitomy.umg.base;

public abstract class RootCapableImpl extends NodeImpl implements RootCapable {

    private final ModelType _modelType;

    public RootCapableImpl(ModelType modelType) {
        this._modelType = modelType;
    }

    @Override
    public boolean isRoot() {
        return this._modelType != null;
    }

    @Override
    public RootCapable root() {
        return this;
    }

    @Override
    public ModelType modelType() {
        return this._modelType;
    }

}
