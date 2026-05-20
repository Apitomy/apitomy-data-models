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

import java.io.PrintStream;

/**
 * Compatibility class used to perform logging.  Logging is handled differently in Java vs. TS/JS.
 * A LoggerCompat.ts file exists and implements the TS/JS version of this class.
 * @author eric.wittmann@gmail.com
 */
public class LoggerUtil {

    private static void output(String prefix, PrintStream stream, String message, Object ...args) {
        stream.print("|");
        stream.print(prefix);
        stream.print("| ");
        stream.print(formatMessage(message, args));
        stream.println("");
    }
    private static String formatMessage(String message, Object[] args) {
        boolean done = false;
        String rval = message;
        int argidx = 0;
        while (!done) {
            int idx = rval.indexOf("%");
            if (idx == -1) {
                break;
            }
            rval = rval.substring(0, idx) + String.valueOf(args[argidx++]) + rval.substring(idx + 2);
        }
        return rval;
    }

    public static void info(String message, Object ...args) {
        LoggerUtil.output("INFO", System.out, message, args);
    }

    public static void warn(String message, Object ...args) {
        LoggerUtil.output("WARN", System.err, message, args);
    }

    public static void debug(String message, Object ...args) {
        LoggerUtil.output("DEBUG", System.out, message, args);
    }

}
