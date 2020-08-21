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

package io.realm.mongodb;

import java.io.IOException;

import io.realm.RealmAsyncTask;
import io.realm.mongodb.mongo.events.BaseChangeEvent;


/**
 * The RealmEventStreamTask is a specific version of {@link RealmAsyncTask} that provides a blocking mechanism
 * to work with asynchronous operations carried out against MongoDB Realm that yield stream results.
 *
 * @param <T> the result type delivered by this task.
 */
public interface RealmEventStreamTask<T> extends RealmAsyncTask {

    /**
     * Blocks the thread on which the call is made until the result of the operation arrives.
     *
     * @return the next event in the stream.
     * @throws AppException if the server raises an error
     * @throws IOException if something is wrong with the input stream
     */
    BaseChangeEvent<T> getNext() throws AppException, IOException;

    /**
     * Whether or not the stream is currently open.
     *
     * @return true if open, false if not.
     */
    boolean isOpen();
}

