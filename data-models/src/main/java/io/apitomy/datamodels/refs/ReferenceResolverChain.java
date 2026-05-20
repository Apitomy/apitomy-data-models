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

import java.util.LinkedList;
import java.util.List;

import io.apitomy.datamodels.models.Node;

/**
 * The chain of reference resolvers.
 *
 * @author eric.wittmann@gmail.com
 */
public class ReferenceResolverChain implements IReferenceResolver {

    private static final ReferenceResolverChain instance = new ReferenceResolverChain();
    public static ReferenceResolverChain getInstance() {
        return instance;
    }

    private final List<IReferenceResolver> resolvers = new LinkedList<>();

    /**
     * Constructor.
     */
    public ReferenceResolverChain() {
        addResolver(new LocalReferenceResolver());
    }

    /**
     * @return the resolvers
     */
    public List<IReferenceResolver> getResolvers() {
        return resolvers;
    }

    /**
     * @param resolver
     */
    public void addResolver(IReferenceResolver resolver) {
        resolvers.add(0, resolver);
    }

    /**
     * @param resolver
     */
    public void removeResolver(IReferenceResolver resolver) {
        resolvers.remove(resolver);
    }

    @Override
    public ResolvedReference resolveRef(String reference, Node from) {
        for (IReferenceResolver resolver : resolvers) {
            ResolvedReference resolvedRef = resolver.resolveRef(reference, from);
            if (resolvedRef != null) {
                return resolvedRef;
            }
        }
        return null;
    }
}
