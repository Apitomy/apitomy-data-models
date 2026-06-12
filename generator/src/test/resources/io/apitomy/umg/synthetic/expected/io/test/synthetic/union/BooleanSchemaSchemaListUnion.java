package io.test.synthetic.union;

import io.test.synthetic.SynSchema;
import java.util.List;

public interface BooleanSchemaSchemaListUnion extends Union {

	public boolean isBoolean();

	public Boolean asBoolean();

	public boolean isSchema();

	public SynSchema asSchema();

	public boolean isSchemaList();

	public List<SynSchema> asSchemaList();
}