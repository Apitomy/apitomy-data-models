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

package io.apitomy.datamodels;

/**
 * Base exception for all errors thrown by the data-models library.
 * Extends {@link RuntimeException} to maintain backward compatibility.
 */
public class DataModelsException extends RuntimeException {

    /**
     * @param message the detail message
     */
    public DataModelsException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause the underlying cause
     */
    public DataModelsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the underlying cause
     */
    public DataModelsException(Throwable cause) {
        super(cause);
    }
}
