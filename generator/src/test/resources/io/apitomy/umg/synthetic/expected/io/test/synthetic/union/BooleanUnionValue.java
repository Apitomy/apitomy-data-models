package io.test.synthetic.union;

import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaSchemaOrBooleanListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.SchemaOrBoolean;

public interface BooleanUnionValue
		extends
			PrimitiveUnionValue<Boolean>,
			SchemaOrBoolean,
			BooleanSchemaSchemaOrBooleanListUnion,
			BooleanSchemaSchemaListUnion,
			BooleanSchemaUnion {
}