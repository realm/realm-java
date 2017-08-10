/*
 * Copyright 2017 Realm Inc.
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
 * Wrapper around a Realm reference
 * TODO: Very detailed explanation about what this class is and does, especially that Query Results might change
 * when moved across threads.
 */
public class ThreadSafeReference<E extends ThreadConfined> {

    /**
     * Creates a thread safe reference for thread confined object. This makes it possible to transfer the object
     * to another thread and access it there.
     *
     * @param object
     * @param <E>
     * @return
     */
    public static <E extends ThreadConfined> ThreadSafeReference<E> create(E object) {
        //noinspection ConstantConditions
        if (object == null) {
            throw new IllegalArgumentException("Non-null object required.");
        }
    }
}
