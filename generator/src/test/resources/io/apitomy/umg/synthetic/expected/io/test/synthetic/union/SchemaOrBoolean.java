package io.test.synthetic.union;

import io.test.synthetic.RootCapable;
import io.test.synthetic.SynSchema;

public interface SchemaOrBoolean extends RootCapable, Union {

	public boolean isBoolean();

	public Boolean asBoolean();

	public boolean isSchema();

	public SynSchema asSchema();
}