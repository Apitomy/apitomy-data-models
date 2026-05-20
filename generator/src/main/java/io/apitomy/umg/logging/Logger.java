/*
 * Copyright 2020 JBoss Inc
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

package io.apitomy.umg.logging;

/**
 * @author eric.wittmann@gmail.com
 */
public class Logger {

    public static final void info(String message, Object ...args) {
        String formattedMsg = String.format(message, args);
        System.out.println("INFO:  " + formattedMsg);
    }

    public static final void debug(String message, Object ...args) {
        String formattedMsg = String.format(message, args);
        System.out.println("DEBUG: " + formattedMsg);
    }

    public static final void warn(String message, Object ...args) {
        String formattedMsg = String.format(message, args);
        System.err.println("WARN:  " + formattedMsg);
    }

    public static final void error(String message, Object ...args) {
        String formattedMsg = String.format(message, args);
        System.err.println("ERROR: " + formattedMsg);
    }

    public static final void error(Throwable t) {
        t.printStackTrace(System.err);
    }

}
