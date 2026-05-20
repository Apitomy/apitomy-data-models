package io.apitomy.datamodels.refs;

import com.fasterxml.jackson.databind.JsonNode;
import io.apitomy.datamodels.models.Node;

/**
 * Represents a resolved reference that can contain three different types of content:
 * <ul>
 *   <li><b>NODE</b>: An {@link io.apitomy.datamodels.models.Node} representing an OpenAPI or AsyncAPI document/component</li>
 *   <li><b>JSON</b>: A {@link com.fasterxml.jackson.databind.JsonNode} representing JSON/YAML content that isn't OpenAPI/AsyncAPI</li>
 *   <li><b>TEXT</b>: A String representing non-JSON content such as Protocol Buffers (.proto), GraphQL, Avro text schemas, etc.</li>
 * </ul>
 *
 * <p>This class is used as the return type for {@link IReferenceResolver#resolveRef(String, Node)} to support
 * dereferencing external schemas in formats other than OpenAPI/AsyncAPI, particularly for AsyncAPI 3.0
 * Multi-Format Schema Objects.</p>
 *
 * @author ewittman
 */
public class ResolvedReference {

    /**
     * Enumeration of the types of content that can be contained in a {@link ResolvedReference}.
     */
    public enum ResolvedReferenceType {
        /**
         * Content is an {@link io.apitomy.datamodels.models.Node} (OpenAPI or AsyncAPI document/component).
         */
        NODE,

        /**
         * Content is a {@link com.fasterxml.jackson.databind.JsonNode} (JSON/YAML that isn't OpenAPI/AsyncAPI).
         */
        JSON,

        /**
         * Content is a String (non-JSON content like Protocol Buffers, GraphQL, Avro text, etc.).
         */
        TEXT
    }

    private final ResolvedReferenceType referenceType;
    private final Object content;
    private final String mediaType;

    /**
     * Creates a ResolvedReference containing a Node.
     *
     * @param node the Node content
     * @return a ResolvedReference of type NODE
     */
    public static ResolvedReference fromNode(Node node) {
        return new ResolvedReference(ResolvedReferenceType.NODE, node, null);
    }

    /**
     * Creates a ResolvedReference containing a JsonNode with application/json media type.
     *
     * @param jsonNode the JsonNode content
     * @return a ResolvedReference of type JSON
     */
    public static ResolvedReference fromJson(JsonNode jsonNode) {
        return new ResolvedReference(ResolvedReferenceType.JSON, jsonNode, "application/json");
    }

    /**
     * Creates a ResolvedReference containing a JsonNode with a specific media type.
     *
     * @param jsonNode the JsonNode content
     * @param mediaType the media type of the content (e.g., "application/vnd.apache.avro+json;version=1.12.0")
     * @return a ResolvedReference of type JSON
     */
    public static ResolvedReference fromJson(JsonNode jsonNode, String mediaType) {
        return new ResolvedReference(ResolvedReferenceType.JSON, jsonNode, mediaType);
    }

    /**
     * Creates a ResolvedReference containing text content with a specific media type.
     *
     * @param text the text content
     * @param mediaType the media type of the content (e.g., "application/vnd.google.protobuf;version=3")
     * @return a ResolvedReference of type TEXT
     */
    public static ResolvedReference fromText(String text, String mediaType) {
        return new ResolvedReference(ResolvedReferenceType.TEXT, text, mediaType);
    }

    /**
     * Private constructor.
     *
     * @param referenceType the type of content
     * @param content the actual content
     * @param mediaType the media type (may be null for NODE type)
     */
    private ResolvedReference(ResolvedReferenceType referenceType, Object content, String mediaType) {
        this.referenceType = referenceType;
        this.content = content;
        this.mediaType = mediaType;
    }

    /**
     * Gets the type of content in this reference.
     *
     * @return the reference type
     */
    public ResolvedReferenceType getReferenceType() {
        return referenceType;
    }

    /**
     * Gets the media type of the content, if available.
     *
     * @return the media type, or null if not available
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Checks if this reference contains a Node.
     *
     * @return true if the content is a Node
     */
    public boolean isNode() {
        return referenceType == ResolvedReferenceType.NODE;
    }

    /**
     * Checks if this reference contains a JsonNode.
     *
     * @return true if the content is a JsonNode
     */
    public boolean isJson() {
        return referenceType == ResolvedReferenceType.JSON;
    }

    /**
     * Checks if this reference contains text.
     *
     * @return true if the content is text
     */
    public boolean isText() {
        return referenceType == ResolvedReferenceType.TEXT;
    }

    /**
     * Gets the content as a Node.
     *
     * @return the Node content
     * @throws ClassCastException if the content is not a Node
     */
    public Node asNode() {
        if (referenceType != ResolvedReferenceType.NODE) {
            throw new ClassCastException("Content is not a Node, it is " + referenceType);
        }
        return (Node) content;
    }

    /**
     * Gets the content as a JsonNode.
     *
     * @return the JsonNode content
     * @throws ClassCastException if the content is not a JsonNode
     */
    public JsonNode asJson() {
        if (referenceType != ResolvedReferenceType.JSON) {
            throw new ClassCastException("Content is not a JsonNode, it is " + referenceType);
        }
        return (JsonNode) content;
    }

    /**
     * Gets the content as text.
     *
     * @return the text content
     * @throws ClassCastException if the content is not text
     */
    public String asText() {
        if (referenceType != ResolvedReferenceType.TEXT) {
            throw new ClassCastException("Content is not text, it is " + referenceType);
        }
        return (String) content;
    }
}
