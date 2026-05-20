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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
public class ValidationRuleTest extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public ValidationRuleTest() {
        // Note - this extends ValidationRule so we can easily unit test some of its protected methods.
        super(null);
    }

    @Test
    public void testIsValidUrl() throws Exception {
        Assert.assertTrue(isValidUrl("http://www.google.com"));
        Assert.assertTrue(isValidUrl("https://github.com/Apitomy/apitomy-studio/blob/master/back-end/hub-api/src/main/java/io/apitomy/hub/api/rest/IDesignsResource.java"));
        Assert.assertTrue(isValidUrl("https://www.pcworld.com/article/3394338/pixel-3a-and-3a-xl-vs-pixel-3-and-3-xl-specs-features-camera-price.html"));

        Assert.assertFalse(isValidUrl("www.google.com"));
        Assert.assertFalse(isValidUrl("first.last@gmail.com"));
    }

    @Test
    public void testIsValidEmail() throws Exception {
        Assert.assertTrue(isValidEmailAddress("first.last@gmail.com"));
        Assert.assertTrue(isValidEmailAddress("first.last+postfix@gmail.com"));
    }
    
}
