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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The Mongo Cursor class.
 * <p>
 * An application should ensure that a cursor is closed in all circumstances, e.g. using a
 * try-with-resources statement.
 *
 * @param <ResultT> The type of documents the cursor contains
 */
public class MongoCursor<ResultT> implements Iterator<ResultT> {

    private final Iterator<ResultT> iterator;

    MongoCursor(Iterator<ResultT> iterator) {
        this.iterator = iterator;
    }

    /**
     * Returns whether or not there is a next document to retrieve with {@code next()}.
     *
     * @return A {@link Task} containing whether or not there is a next document to
     * retrieve with {@code next()}.
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next document.
     *
     * @return A {@link Task} containing the next document if available or a failed task with
     * a {@link NoSuchElementException } exception.
     */
    @Override
    public ResultT next() {
        return iterator.next();
    }

    /**
     * A special {@code next()} case that returns the next document if available or null.
     *
     * @return A {@link Task} containing the next document if available or null.
     */
    public ResultT tryNext() {
        if (!iterator.hasNext()) {
            return null;
        }
        return iterator.next();
    }
}
