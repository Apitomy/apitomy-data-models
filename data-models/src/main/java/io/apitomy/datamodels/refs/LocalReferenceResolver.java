/*
 * Copyright 2019 Red Hat
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

import java.util.List;

import io.apitomy.datamodels.models.MappedNode;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.util.NodeUtil;
import io.apitomy.datamodels.util.RegexUtil;

/**
 * A class to help with resolving references.  Handles recursion with loop detection.
 * @author eric.wittmann@gmail.com
 */
public class LocalReferenceResolver implements IReferenceResolver {

    @SuppressWarnings("rawtypes")
    @Override
    public ResolvedReference resolveRef(String $ref, Node from) {
        // Only handle internal $refs
        if ($ref == null || $ref.indexOf("#/") != 0) {
            return null;
        }

        // TODO support escaped chars in JSON refs
        // TODO implement a proper reference resolver including external file resolution: https://github.com/EricWittmann/oai-ts-core/issues/8
        List<String[]> split = RegexUtil.findMatches($ref, "([^/]+)/?");
        Object cnode = null;
        for (String[] mi : split) {
            String seg = mi[1];
            if (NodeUtil.equals(seg, "#")) {
                cnode = from.root();
            } else if (cnode != null) {
                if (cnode instanceof MappedNode) {
                    cnode = ((MappedNode) cnode).getItem(seg);
                } else {
                    cnode = NodeUtil.getProperty(cnode, seg);
                }
            }
        }

        if (cnode instanceof Node) {
            return ResolvedReference.fromNode((Node) cnode);
        } else {
            return null;
        }

        //        try {
        //            // Not found?  Return null.
        //            if (cnode == null) {
        //                return null;
        //            }
        //
        //            // If cnode itself has a $ref, then keep looking!
        //            String another$ref = (String) NodeUtil.getProperty(cnode, Constants.PROP_$REF);
        //            if (another$ref != null) {
        //                return ReferenceUtil.resolveRef(another$ref, from);
        //            } else {
        //                return (Node) cnode;
        //            }
        //        } catch (Throwable t) {
        //            return null;
        //        }
    }
}
