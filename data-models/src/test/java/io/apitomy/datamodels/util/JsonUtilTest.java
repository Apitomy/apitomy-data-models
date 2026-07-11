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

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.datamodels.models.util.JsonUtil;

/**
 * @author eric.wittmann@gmail.com
 */
public class JsonUtilTest {

    private static final String INPUT = "{\"total_itemized_taxes\": {" +
            "  \"excise_taxes\": [" +
            "    {" +
            "      \"name\": \"Tax 1\"," +
            "      \"rate\": 0.15," +
            "      \"collectable\": 1500," +
            "      \"tax_basis\": 10000," +
            "      \"enum\": [ 10, 100, 1000, 100000 ]," +
            "      \"jurisdiction_type\": \"state\"," +
            "      \"customer_reimbursable\": true" +
            "    }," +
            "    {" +
            "      \"name\": \"Tax 1\"," +
            "      \"rate\": 0.15," +
            "      \"collectable\": 1500," +
            "      \"tax_basis\": 10000," +
            "      \"enum\": [ 10, 100, 1000, 100000 ]," +
            "      \"jurisdiction_type\": \"state\"," +
            "      \"customer_reimbursable\": true" +
            "    }" +
            "  ]" +
            "}}";

    private static final String EXPECTED = "{\n" +
            "    \"total_itemized_taxes\": {\n" +
            "        \"excise_taxes\": [\n" +
            "            {\n" +
            "                \"name\": \"Tax 1\",\n" +
            "                \"rate\": 0.15,\n" +
            "                \"collectable\": 1500,\n" +
            "                \"tax_basis\": 10000,\n" +
            "                \"enum\": [\n" +
            "                    10,\n" +
            "                    100,\n" +
            "                    1000,\n" +
            "                    100000\n" +
            "                ],\n" +
            "                \"jurisdiction_type\": \"state\",\n" +
            "                \"customer_reimbursable\": true\n" +
            "            },\n" +
            "            {\n" +
            "                \"name\": \"Tax 1\",\n" +
            "                \"rate\": 0.15,\n" +
            "                \"collectable\": 1500,\n" +
            "                \"tax_basis\": 10000,\n" +
            "                \"enum\": [\n" +
            "                    10,\n" +
            "                    100,\n" +
            "                    1000,\n" +
            "                    100000\n" +
            "                ],\n" +
            "                \"jurisdiction_type\": \"state\",\n" +
            "                \"customer_reimbursable\": true\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    @Test
    public void testStringify() throws Exception {
        JsonNode data = JsonUtil.parseJSON(INPUT);
        String actual = JsonUtil.stringify(data);
        //System.out.println(printed);
        Assert.assertEquals(EXPECTED, actual);
    }

}
