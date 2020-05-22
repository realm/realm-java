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

package io.realm.mongodb.mongo.iterable;

import com.google.android.gms.tasks.Task;

import java.util.Collection;

import io.realm.internal.common.TaskDispatcher;

/**
 * The MongoIterable is the results from an operation, such as a query.
 *
 * @param <ResultT> The type that this iterable will decode documents to.
 */
public abstract class MongoIterable<ResultT> {

    private final TaskDispatcher dispatcher;

    MongoIterable(final TaskDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Gets the collection from the Object Store.
     * @return collection with the results from the Object Store.
     */
    abstract Collection<ResultT> getCollection();

    /**
     * Gets the first entry in the result collection
     * @return first entry in the collection.
     */
    abstract ResultT getFirst();

    /**
     * Returns a cursor of the operation represented by this iterable.
     *
     * @return a cursor of the operation represented by this iterable.
     */
    public Task<MongoCursor<ResultT>> iterator() {
        return dispatcher.dispatchTask(() ->
                new MongoCursor<>(getCollection().iterator(), dispatcher)
        );
    }

    /**
     * Helper to return the first item in the iterator or null.
     *
     * @return a task containing the first item or null.
     */
    public Task<ResultT> first() {
        return dispatcher.dispatchTask(this::getFirst);
    }
}
