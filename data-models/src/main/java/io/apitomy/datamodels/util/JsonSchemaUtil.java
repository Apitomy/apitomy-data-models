package io.apitomy.datamodels.util;

import java.util.ArrayList;
import java.util.List;

import io.apitomy.datamodels.models.SchemaOrBoolean;
import io.apitomy.datamodels.models.Schema;

/**
 * Utility methods for working with values typed as the {@link SchemaOrBoolean} union (boolean|Schema),
 * as introduced by JSON Schema 2020-12 keywords used by OpenAPI 3.1 and 3.2.  Note that every
 * {@link Schema} implementation is already structurally a {@link SchemaOrBoolean} (since {@code Schema}
 * extends {@code SchemaOrBoolean}), so a plain {@code Schema} instance can always be passed wherever a
 * {@code SchemaOrBoolean} is expected without any additional wrapping.
 * @author eric.wittmann@gmail.com
 */
public class JsonSchemaUtil {

    /**
     * Unwraps a SchemaOrBoolean union value to a {@link Schema}, returning null if the value is null
     * or is a boolean schema.
     * @param value a Schema or a SchemaOrBoolean union value
     */
    public static Schema asSchema(Object value) {
        if (value == null) {
            return null;
        }
        SchemaOrBoolean union = (SchemaOrBoolean) value;
        return union.isSchema() ? union.asSchema() : null;
    }

    /**
     * Converts a list of SchemaOrBoolean union values (or plain Schema values) into a list of Schema,
     * skipping any boolean schema entries.
     * @param values a list of Schema or SchemaOrBoolean union values
     */
    public static List<Schema> toSchemaList(List<?> values) {
        List<Schema> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (int i = 0; i < values.size(); i++) {
            Schema schema = asSchema(values.get(i));
            if (schema != null) {
                result.add(schema);
            }
        }
        return result;
    }

}
