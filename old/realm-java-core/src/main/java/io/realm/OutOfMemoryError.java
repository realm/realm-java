/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;


/**
 * Can be thrown when realm runs out of memory.
 * A JVM that catches this will be able to cleanup, e.g. release other resources
 * to avoid also running out of memory.
 *
 */
@SuppressWarnings("serial")
public class OutOfMemoryError extends Error {

    public OutOfMemoryError() {
        super();
    }

    public OutOfMemoryError(String message) {
        super(message);
    }

    public OutOfMemoryError(String message, Throwable cause) {
        super(message, cause);
    }

    public OutOfMemoryError(Throwable cause) {
        super(cause);
    }
}
