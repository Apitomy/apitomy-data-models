package io.test.synthetic.union;

import io.test.synthetic.Any;
import io.test.synthetic.Visitable;

public interface Union extends Any, Visitable {

	public Object unionValue();

	public boolean isEntity();

	public boolean isEntityList();

	public boolean isEntityMap();

}
