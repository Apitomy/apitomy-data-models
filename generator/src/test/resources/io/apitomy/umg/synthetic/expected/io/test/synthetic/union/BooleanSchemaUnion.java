package io.test.synthetic.union;

import io.test.synthetic.SynSchema;

public interface BooleanSchemaUnion extends Union {

	public boolean isBoolean();

	public Boolean asBoolean();

	public boolean isSchema();

	public SynSchema asSchema();
}