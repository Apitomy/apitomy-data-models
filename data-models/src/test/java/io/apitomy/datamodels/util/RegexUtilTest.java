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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
public class RegexUtilTest {

    @Test
    public void testMatches() throws Exception {
        Assert.assertTrue(RegexUtil.matches("Hello World", ".+"));
        Assert.assertTrue(RegexUtil.matches("Hello World", "Hello.World"));
        Assert.assertTrue(RegexUtil.matches("Hello World", "H(.+).W(.*)"));
        Assert.assertFalse(RegexUtil.matches("Hello World", "H(.+).w(.*)"));
    }

    @Test
    public void testFindMatches() throws Exception {
        List<String[]> matches = RegexUtil.findMatches("this-is-some-test-data", "-([a-zA-Z]+)");
        Assert.assertEquals(4, matches.size());
        Assert.assertEquals("is", matches.get(0)[1]);
        Assert.assertEquals("some", matches.get(1)[1]);
        Assert.assertEquals("test", matches.get(2)[1]);
        Assert.assertEquals("data", matches.get(3)[1]);

        matches = RegexUtil.findMatches("/path-1/bar-2/foo-33/baz-414/", "/([a-zA-Z]+)-([\\d]+)");
        Assert.assertEquals(4, matches.size());
        Assert.assertEquals("path", matches.get(0)[1]);
        Assert.assertEquals("1", matches.get(0)[2]);

        Assert.assertEquals("bar", matches.get(1)[1]);
        Assert.assertEquals("2", matches.get(1)[2]);

        Assert.assertEquals("foo", matches.get(2)[1]);
        Assert.assertEquals("33", matches.get(2)[2]);

        Assert.assertEquals("baz", matches.get(3)[1]);
        Assert.assertEquals("414", matches.get(3)[2]);
    }

    @Test
    public void testPathSegmentMatching() throws Exception {
        String SEG_MATCH_REGEX = "\\/([^{}\\/]*)(\\{([a-zA-Z_][0-9a-zA-Z_]*)\\})?";

        List<String[]> matches = RegexUtil.findMatches("/pet", SEG_MATCH_REGEX);
        Assert.assertEquals(1, matches.size());

        matches = RegexUtil.findMatches("/pet/findByStatus", SEG_MATCH_REGEX);
        Assert.assertEquals(2, matches.size());

        matches = RegexUtil.findMatches("/pet/{petId}", SEG_MATCH_REGEX);
        Assert.assertEquals(2, matches.size());

        matches = RegexUtil.findMatches("/pet/{petId}/uploadImage", SEG_MATCH_REGEX);
        Assert.assertEquals(3, matches.size());

        matches = RegexUtil.findMatches("/store/order/{orderId}", SEG_MATCH_REGEX);
        Assert.assertEquals(3, matches.size());

        matches = RegexUtil.findMatches("/designs/{designId}/codegen/projects/{projectId}/zip", SEG_MATCH_REGEX);
        Assert.assertEquals(6, matches.size());
    }

    @Test
    public void testReferenceResolverMatching() throws Exception {
        String $ref = "#/components/PetResponse";
        List<String[]> split = RegexUtil.findMatches($ref, "([^/]+)/?");
        //        split.forEach(mi -> { System.out.println(String.join(",", mi)); });
        Assert.assertEquals(3, split.size());

        Assert.assertEquals("#", split.get(0)[1]);
        Assert.assertEquals("components", split.get(1)[1]);
        Assert.assertEquals("PetResponse", split.get(2)[1]);
    }

}
