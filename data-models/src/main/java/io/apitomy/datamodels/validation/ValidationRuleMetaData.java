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

package io.apitomy.datamodels.validation;

import io.apitomy.datamodels.models.ModelType;

/**
 * @author eric.wittmann@gmail.com
 */
public class ValidationRuleMetaData {

    public String code;
    public String name;
    public String type;
    public String entity;
    public ModelType[] versions;
    public boolean specMandated;
    public String messageTemplate;
    public Class<?> ruleClass;

    /**
     * Constructor.
     */
    public ValidationRuleMetaData() {
    }

    /**
     * Constructor.
     * @param code
     * @param name
     * @param type
     * @param entity
     * @param versions
     * @param specMandated
     * @param messageTemplate
     * @param ruleClass
     */
    public ValidationRuleMetaData(String code, String name, String type, String entity, ModelType[] versions,
            boolean specMandated, String messageTemplate, Class<?> ruleClass) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.entity = entity;
        this.versions = versions;
        this.specMandated = specMandated;
        this.messageTemplate = messageTemplate;
        this.ruleClass = ruleClass;
    }

    /**
     * Returns true if this rule applies to documents of the given type.
     */
    public boolean appliesTo(ModelType type) {
        for (ModelType version : this.versions) {
            if (type == version) {
                return true;
            }
        }
        return false;
    }

}
