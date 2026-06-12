package io.test.synthetic.union;

import java.util.List;

public class StringListUnionValueImpl extends ListUnionValueImpl<String> implements StringListUnionValue {

	public StringListUnionValueImpl() {
		super();
	}

	public StringListUnionValueImpl(List<String> value) {
		super(value);
	}

}
