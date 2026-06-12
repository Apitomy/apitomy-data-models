package io.test.synthetic.union;

public interface BooleanUnionValue
		extends
			PrimitiveUnionValue<Boolean>,
			SchemaOrBoolean,
			BooleanSchemaUnion,
			BooleanSchemaSchemaListUnion {

}
