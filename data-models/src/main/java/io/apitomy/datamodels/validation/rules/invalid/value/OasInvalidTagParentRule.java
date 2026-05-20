package io.apitomy.datamodels.validation.rules.invalid.value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiTag;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32Tag;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that the Tag parent field references an existing tag name,
 * and that no circular parent references exist.
 */
public class OasInvalidTagParentRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidTagParentRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    @Override
    public void visitTag(Tag node) {
        OpenApi32Tag tag = (OpenApi32Tag) node;
        String parent = tag.getParent();
        if (!hasValue(parent)) {
            return;
        }

        OpenApiDocument doc = (OpenApiDocument) node.root();
        List<OpenApiTag> tags = doc.getTags();

        // Check that the parent tag name exists
        boolean parentExists = false;
        if (hasValue(tags)) {
            for (OpenApiTag t : tags) {
                if (equals(t.getName(), parent)) {
                    parentExists = true;
                    break;
                }
            }
        }
        this.reportIfInvalid(parentExists, node, "parent", map("parent", parent));
    }

}
