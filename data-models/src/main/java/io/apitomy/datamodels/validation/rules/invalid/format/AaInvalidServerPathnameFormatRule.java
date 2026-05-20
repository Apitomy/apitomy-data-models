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
import io.apitomy.datamodels.models.asyncapi.v3x.AsyncApi3xServer;
import io.apitomy.datamodels.util.ModelTypeUtil;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;

/**
 * Rule: SRV-007
 * Validates that a Server's pathname property in AsyncAPI 3.x begins with '/' if provided.
 *
 * @author eric.wittmann@gmail.com
 */
public class AaInvalidServerPathnameFormatRule extends ValidationRule {

    /**
     * Constructor.
     *
     * @param ruleInfo
     */
    public AaInvalidServerPathnameFormatRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Returns true only if the given pathname is valid.
     *
     * @param pathname
     * @return {boolean}
     */
    public static boolean isValidPathname(String pathname) {
        // Pathname must begin with '/' if provided
        return pathname.startsWith("/");
    }

    @Override
    public void visitServer(Server node) {
        if (ModelTypeUtil.isAsyncApi3Model(node)) {
            String pathname = ((AsyncApi3xServer) node).getPathname();
            if (hasValue(pathname)) {
                this.reportIfInvalid(isValidPathname(pathname), node, "pathname", map());
            }
        }
    }

}
