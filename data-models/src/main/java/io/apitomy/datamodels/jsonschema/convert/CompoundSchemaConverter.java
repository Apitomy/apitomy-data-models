package io.apitomy.datamodels.jsonschema.convert;

import io.apitomy.datamodels.models.Any;
import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.jsonschema.JsonSchema;
import io.apitomy.datamodels.models.jsonschema.draft.draft4.visitors.JD4ToJCConversionTraverser;
import io.apitomy.datamodels.models.jsonschema.draft.draft6.visitors.JD6ToJCConversionTraverser;
import io.apitomy.datamodels.models.jsonschema.draft.draft7.visitors.JD7ToJCConversionTraverser;
import io.apitomy.datamodels.models.jsonschema.modern.v201909.visitors.JM201909ToJCConversionTraverser;
import io.apitomy.datamodels.models.jsonschema.modern.v202012.visitors.JM202012ToJCConversionTraverser;

/**
 * Converts any JSON Schema version to the compound schema type.
 */
public class CompoundSchemaConverter {

    /**
     * Converts a JSON Schema of any draft version to the compound schema type.
     */
    public static JsonSchema toCompound(JsonSchema source, ModelType modelType) {
        if (source == null) return null;
        Any result;
        switch (modelType) {
            case JD4:
                result = new JD4ToJCConversionTraverser(new JD4ToCompoundConverter()).convert(source);
                break;
            case JD6:
                result = new JD6ToJCConversionTraverser(new JD6ToCompoundConverter()).convert(source);
                break;
            case JD7:
                result = new JD7ToJCConversionTraverser(new JD7ToCompoundConverter()).convert(source);
                break;
            case JM201909:
                result = new JM201909ToJCConversionTraverser(new JM201909ToCompoundConverter()).convert(source);
                break;
            case JM202012:
                result = new JM202012ToJCConversionTraverser(new JM202012ToCompoundConverter()).convert(source);
                break;
            default:
                return source;
        }
        return (JsonSchema) result;
    }
}
