/*
 * Copyright 2022 Red Hat
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

import io.apitomy.datamodels.models.Node;

/**
 * Used to resolve references ($ref or operationRef) to a JSON object.  Users of the
 * apitomy-data-models library can provide zero or more custom implementations of
 * this interface in order to provide a way to resolve reference URI formats that
 * are not supported by default. Internal references are supported by default.
 *
 * When providing custom reference resolvers, those custom resolvers will be used
 * before any default/built-in resolvers.
 *
 * @author eric.wittmann@gmail.com
 */
public interface IReferenceResolver {

    /**
     * Resolves a reference to a {@link ResolvedReference}.
     *
     * <p>The resolver can return three different types of content:</p>
     * <ul>
     *   <li><b>Node</b>: Use {@link ResolvedReference#fromNode(Node)} when the resolved content is
     *       an OpenAPI or AsyncAPI document/component that has been parsed into the data model.</li>
     *   <li><b>JsonNode</b>: Use {@link ResolvedReference#fromJson(com.fasterxml.jackson.databind.JsonNode)}
     *       or {@link ResolvedReference#fromJson(com.fasterxml.jackson.databind.JsonNode, String)} when the
     *       resolved content is JSON or YAML that is not OpenAPI/AsyncAPI (e.g., JSON Schema, Avro JSON format).
     *       Include the media type when known.</li>
     *   <li><b>Text</b>: Use {@link ResolvedReference#fromText(String, String)} when the resolved content
     *       is non-JSON text such as Protocol Buffers (.proto), GraphQL schemas, Avro text format, etc.
     *       The media type should be provided (e.g., "application/vnd.google.protobuf;version=3").</li>
     * </ul>
     *
     * <p>Example for a Protocol Buffers .proto file reference:</p>
     * <pre>
     * String protoContent = "syntax = \"proto3\";\n\nmessage UserEvent {\n  string userId = 1;\n}\n";
     * return ResolvedReference.fromText(protoContent, "application/vnd.google.protobuf;version=3");
     * </pre>
     *
     * <p>Example for a JSON Schema reference:</p>
     * <pre>
     * JsonNode jsonSchema = // ... parse JSON ...
     * return ResolvedReference.fromJson(jsonSchema, "application/schema+json");
     * </pre>
     *
     * <p>Example for an OpenAPI component reference:</p>
     * <pre>
     * Node node = // ... parse and convert to Node ...
     * return ResolvedReference.fromNode(node);
     * </pre>
     *
     * @param reference the reference URI to resolve (e.g., "http://example.com/schema.proto")
     * @param from the node containing the reference
     * @return a ResolvedReference containing the resolved content, or null if the resolver cannot resolve the reference
     */
    public ResolvedReference resolveRef(String reference, Node from);

}
