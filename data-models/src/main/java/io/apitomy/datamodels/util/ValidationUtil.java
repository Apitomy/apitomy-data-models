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

package io.apitomy.datamodels.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.validation.ValidationRule;
import io.apitomy.datamodels.validation.ValidationRuleMetaData;
import io.apitomy.datamodels.validation.ValidationVisitor;

/**
 * A compatibility layer containing methods used when performing validation.  Any code that must
 * be different in Java vs. TS/JS *and* related to validation should go in here.  There is a
 * ValidationUtil.ts file that contains the TS/JS specific implementation of this class.
 * @author eric.wittmann@gmail.com
 */
public class ValidationUtil {

    public static final ValidationVisitor createValidationVisitorForNode(Node node) {
        return new ValidationVisitor(node.root().modelType());
    }

    public static final ValidationVisitor createValidationVisitor(ModelType type) {
        return new ValidationVisitor(type);
    }

    public static ValidationRule instantiate(ValidationRuleMetaData ruleInfo) {
        try {
            Constructor<?> constructor = ruleInfo.ruleClass.getConstructor(ValidationRuleMetaData.class);
            return (ValidationRule) constructor.newInstance(ruleInfo);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Joins a list of strings into a single string with a given delimiter.
     * @param delim
     * @param values
     */
    public static String join(String delim, List<String> values) {
        return String.join(delim, values);
    }

    /**
     * Joins a list of strings into a single string with a given delimiter.
     * @param delim
     * @param values
     */
    public static String joinArray(String delim, String[] values) {
        return join(delim, Arrays.asList(values));
    }

}
