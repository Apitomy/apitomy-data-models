package io.apitomy.datamodels.validation.rules.invalid.value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.apitomy.datamodels.models.Tag;
import io.apitomy.datamodels.models.openapi.OpenApiDocument;
import io.apitomy.datamodels.models.openapi.OpenApiTag;
import io.apitomy.datamodels.models.openapi.v3x.v32.OpenApi32Tag;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Validates that no circular references exist between parent and child tags.
 */
public class OasTagParentCircularRefRule extends AbstractInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasTagParentCircularRefRule(ValidationRuleMetaData ruleInfo) {
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
        if (!hasValue(tags)) {
            return;
        }

        // Build a map of tag name -> parent name
        Map<String, String> parentMap = new HashMap<>();
        for (OpenApiTag t : tags) {
            if (t instanceof OpenApi32Tag) {
                OpenApi32Tag t32 = (OpenApi32Tag) t;
                if (hasValue(t32.getParent())) {
                    parentMap.put(t.getName(), t32.getParent());
                }
            }
        }

        // Walk the parent chain from this tag, looking for a cycle
        Set<String> visited = new HashSet<>();
        String current = tag.getName();
        while (current != null && parentMap.containsKey(current)) {
            if (!visited.add(current)) {
                // Cycle detected
                this.report(node, "parent", map("tagName", tag.getName()));
                return;
            }
            current = parentMap.get(current);
        }
    }

}
