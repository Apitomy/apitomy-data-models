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

package io.apitomy.datamodels.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compatibility class used to bridge the gap between Java and JavaScript with respect to working with
 * regular expressions.
 * @author eric.wittmann@gmail.com
 */
public class RegexUtil {

    /**
     * Returns true if the given value matches the given regular expression.
     * @param value
     * @param regex
     */
    public static boolean matches(String value, String regex) {
        // TODO: compile every regular expression and cache the Pattern instance
        return Pattern.matches(regex, value);
    }

    /**
     * Finds all strings within the given input that match the given regular expression.  Returns
     * all matches, with each match represented as a list of the groups specified in the regex.
     * @param value
     * @param regex
     */
    public static List<String[]> findMatches(String value, String regex) {
        List<String[]> rval = new ArrayList<>();

        // TODO: compile every regular expression and cache the Pattern instance
        Pattern segMatchEx = Pattern.compile(regex);
        Matcher match = segMatchEx.matcher(value);
        while (match.find()) {
            String[] mi = new String[match.groupCount() + 1];
            for (int gidx = 0; gidx <= match.groupCount(); gidx++) {
                mi[gidx] = match.group(gidx);
            }
            rval.add(mi);
        }
        return rval;
    }

}
