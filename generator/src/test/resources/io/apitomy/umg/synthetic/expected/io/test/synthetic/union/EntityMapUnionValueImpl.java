package io.test.synthetic.union;

import io.test.synthetic.Node;
import java.util.Map;

public class EntityMapUnionValueImpl<T extends Node> extends MapUnionValueImpl<T> implements EntityMapUnionValue<T> {

	public EntityMapUnionValueImpl() {
		super();
	}

	public EntityMapUnionValueImpl(Map<String, T> value) {
		super(value);
	}

	@Override
	public boolean isEntityMap() {
		return true;
	}

}
