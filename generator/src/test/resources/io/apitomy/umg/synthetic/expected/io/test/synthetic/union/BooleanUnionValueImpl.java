package io.test.synthetic.union;

import io.test.synthetic.ModelType;
import io.test.synthetic.SynSchema;
import java.util.List;

public class BooleanUnionValueImpl extends PrimitiveUnionValueImpl<Boolean> implements BooleanUnionValue {

	public BooleanUnionValueImpl(Boolean value) {
		super(value);
	}

	public BooleanUnionValueImpl(Boolean value, ModelType modelType) {
		super(value, modelType);
	}

	@Override
	public boolean isBoolean() {
		return true;
	}

	@Override
	public Boolean asBoolean() {
		return getValue();
	}

	@Override
	public boolean isSchema() {
		return false;
	}

	@Override
	public SynSchema asSchema() {
		throw new ClassCastException();
	}

	@Override
	public boolean isSchemaList() {
		return false;
	}

	@Override
	public List<SynSchema> asSchemaList() {
		throw new ClassCastException();
	}
}