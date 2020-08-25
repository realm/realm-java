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

import io.realm.RealmAsyncTask;
import io.realm.mongodb.mongo.events.BaseChangeEvent;


/**
 * The RealmEventStreamAsyncTask is a specific version of {@link RealmAsyncTask} that provides a non-blocking mechanism
 * to work with asynchronous operations carried out against MongoDB Realm that yield stream results.
 *
 * @param <T> the result type delivered by this task.
 */
public interface RealmEventStreamAsyncTask<T> extends RealmAsyncTask {
    /**
     * Provides a way to subscribe to asynchronous operations via a callback, which handles both
     * results and errors.
     *
     * @param callback the {@link App.Callback} designed to receive event results.
     * @throws IllegalStateException if the stream is already open.
     */
    void get(App.Callback<BaseChangeEvent<T>> callback) throws IllegalStateException;

    /**
     * Whether or not the stream is currently open.
     * @return true if open, false if not.
     */
    boolean isOpen();
}

