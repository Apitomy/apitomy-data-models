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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a path to a node within the data model.  Any node in a data model can be
 * represented by its path.  The node path can be used to identify and locate a single node
 * in the document tree.
 * @author eric.wittmann@gmail.com
 */
public class NodePath {

    // Example 1: /foo/bar/baz
    // Example 2: /foo/bars[2]/baz
    // Example 3: /foo/bars[example]/baz

    private final List<NodePathSegment> segments = new LinkedList<>();

    public NodePath() {
    }

    public void append(NodePathSegment segment) {
        this.segments.add(segment);
    }

    public void prepend(NodePathSegment segment) {
        this.segments.add(0, segment);
    }

    /**
     * Removes and returns the first segment from the path.
     *
     * @return the first segment, or null if the path is empty
     */
    public NodePathSegment removeFirst() {
        if (segments.size() > 0) {
            return segments.remove(0);
        }
        return null;
    }

    /**
     * Removes and returns the last segment from the path.
     *
     * @return the last segment, or null if the path is empty
     */
    public NodePathSegment removeLast() {
        if (segments.size() > 0) {
            return segments.remove(segments.size() - 1);
        }
        return null;
    }

    public List<NodePathSegment> getSegments() {
        return this.segments;
    }

    /**
     * Returns the first segment in the path without removing it.
     *
     * @return the first segment, or null if the path is empty
     */
    public NodePathSegment getFirstSegment() {
        if (segments.size() > 0) {
            return segments.get(0);
        }
        return null;
    }

    public NodePathSegment getLastSegment() {
        if (segments.size() > 0) {
            return segments.get(segments.size() - 1);
        }
        return null;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean escaped) {
        StringBuilder builder = new StringBuilder();
        if (this.segments.size() > 0) {
            this.segments.forEach(segment -> {
                builder.append(segment.toString(escaped));
            });
        } else {
            builder.append("/");
        }
        return builder.toString();
    }

    public List<String> toSegments() {
        List<String> rval = new ArrayList<>();
        for (NodePathSegment segment : segments) {
            rval.add(segment.toString());
        }
        return rval;
    }

    private static final int SCAN_TYPE_PATH = 0;
    private static final int SCAN_TYPE_INDEX = 1;

    /**
     * Parses a stringified node path and returns an instance.  Returns null if
     * the path cannot be parsed.
     * @param path
     */
    public static NodePath parse(String path) {
        NodePath rval = null;
        if ("/".equals(path)) {
            rval = new NodePath();
        } else if (path != null && path.indexOf("/") == 0) {
            rval = new NodePath();

            int currentScanType = SCAN_TYPE_PATH;
            int currentIdx = 1;
            while (currentIdx < path.length()) {
                int segStart = currentIdx;
                int segEnd;
                if (currentScanType == SCAN_TYPE_PATH) {
                    int nextPathSep = path.indexOf("/", segStart);
                    int nextBrace = path.indexOf("[", segStart);
                    if (nextPathSep == -1) { nextPathSep = path.length(); }
                    if (nextBrace == -1) { nextBrace = path.length(); }
                    if (nextPathSep <= nextBrace) {
                        segEnd = nextPathSep;
                    } else {
                        segEnd = nextBrace;
                    }
                } else {
                    int nextCloseBrace = path.indexOf("]", segStart);
                    if (nextCloseBrace == -1) { nextCloseBrace = path.length(); }
                    segEnd = nextCloseBrace + 1;
                }

                String seg = path.substring(segStart, segEnd);
                NodePathSegment segment = NodePathSegment.parse(seg);
                rval.append(segment);

                // Default next values.
                currentScanType = SCAN_TYPE_PATH;
                currentIdx = segEnd + 1;

                // Find real next values.
                if (segEnd >= path.length()) {
                    // No further scanning - end of input.
                } else if (path.charAt(segEnd) == '/') {
                    currentScanType = SCAN_TYPE_PATH;
                    currentIdx = segEnd + 1;
                } else if (path.charAt(segEnd) == '[') {
                    currentScanType = SCAN_TYPE_INDEX;
                    currentIdx = segEnd;
                } else if (path.charAt(segEnd) == ']') {
                    if (path.charAt(segEnd+1) == '[') {
                        currentScanType = SCAN_TYPE_INDEX;
                        currentIdx = segEnd + 1;
                    } else if (path.charAt(segEnd+1) == '/') {
                        currentScanType = SCAN_TYPE_PATH;
                        currentIdx = segEnd + 1;
                    }
                }
            }
        }
        return rval;
    }

}
