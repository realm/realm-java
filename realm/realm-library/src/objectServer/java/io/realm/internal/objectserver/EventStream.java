/*
 * Copyright 2020 Realm Inc.
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

package io.realm.internal.objectserver;

import java.io.IOException;

import io.realm.mongodb.AppException;
import io.realm.mongodb.mongo.events.BaseChangeEvent;

public interface EventStream<T> {
    /**
     * Fetch the next event from a given stream.
     *
     * @return the next event
     * @throws IOException any io exception that could occur
     */
    BaseChangeEvent<T> getNextEvent() throws AppException, IOException;

    /**
     * Closes the current stream.
     *
     * @throws IOException can throw exception if internal buffer not closed properly
     */
    void close();

    /**
     * Indicates whether or not the change stream is currently open.
     *
     * @return True if the underlying change stream is open.
     */
    boolean isOpen();
}
