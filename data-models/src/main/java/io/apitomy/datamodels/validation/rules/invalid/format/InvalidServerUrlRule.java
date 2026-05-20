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

package io.apitomy.datamodels.validation.rules.invalid.format;

import io.apitomy.datamodels.models.Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v20.AsyncApi20Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v21.AsyncApi21Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v22.AsyncApi22Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v23.AsyncApi23Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v24.AsyncApi24Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v25.AsyncApi25Server;
import io.apitomy.datamodels.models.asyncapi.v2x.v26.AsyncApi26Server;
import io.apitomy.datamodels.models.openapi.OpenApiServer;
import io.apitomy.datamodels.models.openrpc.OpenRpcServer;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Server URL Rule
 * @author eric.wittmann@gmail.com
 */
public class InvalidServerUrlRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public InvalidServerUrlRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter#visitServer(io.apitomy.datamodels.models.Server)
     */
    @Override
    public void visitServer(Server node) {
        String url = getUrl(node);
        if (hasValue(url)) {
            this.reportIfInvalid(isValidUrlTemplate(url), node, "url", map());
        }
    }

    private String getUrl(Server node) {
        if (ModelTypeUtil.isOpenApiModel(node)) {
            return ((OpenApiServer) node).getUrl();
        }
        if (ModelTypeUtil.isAsyncApi2Model(node)) {
            if (node instanceof AsyncApi20Server) {
                return ((AsyncApi20Server) node).getUrl();
            }
            if (node instanceof AsyncApi21Server) {
                return ((AsyncApi21Server) node).getUrl();
            }
            if (node instanceof AsyncApi22Server) {
                return ((AsyncApi22Server) node).getUrl();
            }
            if (node instanceof AsyncApi23Server) {
                return ((AsyncApi23Server) node).getUrl();
            }
            if (node instanceof AsyncApi24Server) {
                return ((AsyncApi24Server) node).getUrl();
            }
            if (node instanceof AsyncApi25Server) {
                return ((AsyncApi25Server) node).getUrl();
            }
            if (node instanceof AsyncApi26Server) {
                return ((AsyncApi26Server) node).getUrl();
            }
        }
        if (ModelTypeUtil.isOpenRpcModel(node)) {
            return ((OpenRpcServer) node).getUrl();
        }
        return null;
    }

}
