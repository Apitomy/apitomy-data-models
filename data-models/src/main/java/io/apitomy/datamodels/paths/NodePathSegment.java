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

package io.apitomy.datamodels.paths;

/**
 * Represents a single segment of a node path.
 * @author eric.wittmann@gmail.com
 */
public class NodePathSegment {

    private final String value;
    private final boolean index;

    /**
     * Constructor.
     * @param value
     * @param index
     */
    public NodePathSegment(String value, boolean index) {
        this.value = value;
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public boolean isIndex() {
        return index;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean escaped) {
        StringBuilder builder = new StringBuilder();
        String _value = escaped ? escapePathSegmentValue(value, !isIndex()) : value;
        if (this.isIndex()) {
            builder.append('[');
            builder.append(_value);
            builder.append(']');
        } else {
            builder.append('/');
            builder.append(_value);
        }
        return builder.toString();
    }

    /**
     * Parse a single segment of a node path from a String.
     * @param segment
     */
    public static NodePathSegment parse(String segment) {
        if (segment == null) {
            return new NodePathSegment(null, false);
        }
        boolean isIndex = false;
        if (segment.indexOf("[") == 0
                && segment.indexOf("]") == (segment.length() - 1)) {
            segment = segment.substring(1, segment.length() - 1);
            isIndex = true;
        }
        segment = unescapePathSegmentValue(segment);
        return new NodePathSegment(segment, isIndex);
    }

    /**
     * When a path is represented as a string,
     * we are using three characters with special meaning
     * to encode it's structure.
     * '/' to separate path segments, and '[' with ']' to denote a segment for an "indexed" node.
     * Since these characters can also appear in the segment values themselves (in their non-special meaning),
     * they have to be escaped.
     * The following rules are used, inspired by <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>:
     * - The escape character is '~', preceding a number which determines which special character is encoded.
     * - '~1' = '/'
     * - '~2' = '['
     * - '~3' = ']'
     * The escape character is itself encoded as '~0'.
     *
     * @param escapeSlash In the "indexed" segment, '/' can be used unescaped.
     */
    private static String escapePathSegmentValue(String rawValue, boolean escapeSlash) {
        // order is important
        String res = rawValue.replace("~", "~0");
        if (escapeSlash) {
            res = res.replace("/", "~1");
        }
        res = res.replace("[", "~2");
        res = res.replace("]", "~3");
        return res;
    }

    /**
     * @see io.apitomy.datamodels.core.models.NodePath.NodePathSegment#escapePathSegmentValue(String, boolean)
     */
    private static String unescapePathSegmentValue(String escapedValue) {
        // order is important
        String res = escapedValue.replace("~3", "]");
        res = res.replace("~2", "[");
        res = res.replace("~1", "/");
        res = res.replace("~0", "~");
        return res;
    }

}
