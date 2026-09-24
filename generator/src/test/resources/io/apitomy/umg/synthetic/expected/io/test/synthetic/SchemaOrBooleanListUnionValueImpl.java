package io.test.synthetic;

import io.test.synthetic.union.EntityListUnionValueImpl;
import java.util.List;

public class SchemaOrBooleanListUnionValueImpl extends EntityListUnionValueImpl<SchemaOrBoolean>
		implements
			SchemaOrBooleanListUnionValue {

	public SchemaOrBooleanListUnionValueImpl() {
		super();
	}

	public SchemaOrBooleanListUnionValueImpl(List<SchemaOrBoolean> value) {
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
	public boolean isSchemaOrBooleanList() {
		return true;
	}

	@Override
	public List<SchemaOrBoolean> asSchemaOrBooleanList() {
		return getValue();
	}
}