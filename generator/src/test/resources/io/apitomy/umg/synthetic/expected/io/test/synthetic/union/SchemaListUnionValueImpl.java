package io.test.synthetic.union;

import io.test.synthetic.SynSchema;
import java.util.List;

public class SchemaListUnionValueImpl extends EntityListUnionValueImpl<SynSchema> implements SchemaListUnionValue {

	public SchemaListUnionValueImpl() {
		super();
	}

	public SchemaListUnionValueImpl(List<SynSchema> value) {
		super(value);
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	@Override
	public Boolean asBoolean() {
		throw new ClassCastException();
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
		return true;
	}

	@Override
	public List<SynSchema> asSchemaList() {
		return getValue();
	}
}