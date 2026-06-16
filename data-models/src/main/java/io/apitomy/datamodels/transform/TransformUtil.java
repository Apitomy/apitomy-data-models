package io.apitomy.datamodels.transform;

import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Document;

/**
 * Utility methods used by the document transformation layer.
 */
public final class TransformUtil {

    private TransformUtil() {
    }

    /**
     * Clones the given document by serializing it to a JSON object, applying the given
     * transformer function to the raw JSON, and then re-parsing the result.  This is used
     * for cross-version transformations where the document type changes during the clone
     * (e.g. bumping the spec version field before re-parsing as the new version).
     * @param source the document to clone
     * @param transformer a function to modify the raw JSON before re-parsing
     * @return a new document parsed from the transformed JSON
     */
    public static Document cloneAndTransform(Document source, UnaryOperator<ObjectNode> transformer) {
        ObjectNode jsObj = transformer.apply(Library.writeNode(source));
        return Library.readDocument(jsObj);
    }

}
