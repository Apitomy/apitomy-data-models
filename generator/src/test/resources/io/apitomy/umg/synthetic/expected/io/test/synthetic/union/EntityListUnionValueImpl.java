package io.test.synthetic.union;

import io.test.synthetic.Any;
import java.util.List;

public class EntityListUnionValueImpl<T extends Any> extends ListUnionValueImpl<T> implements EntityListUnionValue<T> {

	public EntityListUnionValueImpl() {
		super();
	}

	public EntityListUnionValueImpl(List<T> value) {
		super(value);
	}

	@Override
	public boolean isEntityList() {
		return true;
	}

}
