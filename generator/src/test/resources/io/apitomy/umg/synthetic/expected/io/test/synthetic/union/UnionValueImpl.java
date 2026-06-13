package io.test.synthetic.union;

import io.test.synthetic.ModelType;
import io.test.synthetic.visitors.Visitor;

/**
 * Base class for all union value implementations.
 * 
 * @author eric.wittmann@gmail.com
 */
public abstract class UnionValueImpl<T> implements UnionValue<T>, Union {

	private T value;
	private ModelType _modelType;

	public UnionValueImpl() {
	}

	public UnionValueImpl(T value) {
		this.value = value;
	}

	public UnionValueImpl(T value, ModelType modelType) {
		this.value = value;
		this._modelType = modelType;
	}

	public boolean isRoot() {
		return this._modelType != null;
	}

	public ModelType modelType() {
		return this._modelType;
	}

	@Override
	public Object unionValue() {
		return value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public boolean isList() {
		return false;
	}

	@Override
	public boolean isMap() {
		return false;
	}

	@Override
	public boolean isNode() {
		return false;
	}

	@Override
	public boolean isEntity() {
		return false;
	}

	@Override
	public boolean isEntityList() {
		return false;
	}

	@Override
	public boolean isEntityMap() {
		return false;
	}

	@Override
	public void accept(Visitor visitor) {
	}

}
