package io.test.synthetic;

import io.test.synthetic.union.Union;
import java.util.List;

public interface BooleanSchemaSchemaListUnion extends Union {

	public boolean isBoolean();

	public Boolean asBoolean();

	public boolean isSchema();

	public SynSchema asSchema();

	public boolean isSchemaList();

	public List<SynSchema> asSchemaList();
}