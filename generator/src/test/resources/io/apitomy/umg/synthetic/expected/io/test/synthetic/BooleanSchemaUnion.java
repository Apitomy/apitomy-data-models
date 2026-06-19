package io.test.synthetic;

import io.test.synthetic.union.Union;

public interface BooleanSchemaUnion extends Union {

	public boolean isBoolean();

	public Boolean asBoolean();

	public boolean isSchema();

	public SynSchema asSchema();
}