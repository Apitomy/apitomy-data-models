package io.test.synthetic;

import io.test.synthetic.union.Union;

public interface SchemaOrBoolean extends RootCapable, Union {

	public boolean isBoolean();

	public Boolean asBoolean();

	public boolean isSchema();

	public SynSchema asSchema();
}