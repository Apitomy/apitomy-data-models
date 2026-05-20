/*
 * Copyright 2020 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apitomy.datamodels.refs;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.Library;
import io.apitomy.datamodels.models.Node;

/**
 * Reference resolver used in the {@link DereferenceTest}.
 * @author eric.wittmann@gmail.com
 */
public class DereferenceTestReferenceResolver implements IReferenceResolver {

    public static Map<String, JsonNode> refs = new HashMap<>();

    @Override
    public ResolvedReference resolveRef(String reference, Node from) {
        if (refs.containsKey(reference)) {
            JsonNode jsonNode = refs.get(reference);
            if (jsonNode.isObject() && jsonNode.has("type") && jsonNode.get("type").isTextual() && jsonNode.get("type").asText().equals("record")) {
                return ResolvedReference.fromJson(jsonNode, "application/vnd.apache.avro+json");
            } else if (jsonNode.isObject()) {
                // Object content - convert to Node
                Node node = toModel((ObjectNode) jsonNode, from);
                return ResolvedReference.fromNode(node);
            } else if (jsonNode.isTextual()) {
                // Text content - return as text with appropriate media type
                String textContent = jsonNode.asText();
                String mediaType = detectMediaType(reference, textContent);
                return ResolvedReference.fromText(textContent, mediaType);
            }
        }
        return null;
    }

    /**
     * Detects the media type of content based on the reference URL and content.
     *
     * @param reference the reference URL
     * @param textContent the text content
     * @return the detected media type
     */
    private String detectMediaType(String reference, String textContent) {
        // Detect based on file extension in reference
        if (reference.endsWith(".proto")) {
            return "application/vnd.google.protobuf;version=3";
        } else if (reference.endsWith(".avsc")) {
            return "application/vnd.apache.avro+json;version=1.12.0";
        } else if (reference.endsWith(".graphql") || reference.endsWith(".gql")) {
            return "application/graphql";
        }

        // Fallback to plain text
        return "text/plain";
    }

    private Node toModel(ObjectNode jsonNode, Node from) {
        Node rval = from.emptyClone();
        rval.attach(from.parent());
        return Library.readNode(jsonNode, rval);
    }

}
