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

import org.bson.conversions.Bson;

import java.util.Collection;

import javax.annotation.Nullable;

import io.realm.internal.common.TaskDispatcher;
import io.realm.internal.objectstore.OsFindIterable;
import io.realm.mongodb.mongo.MongoCollection;

/**
 * Specific iterable for {@link MongoCollection#find()} operations.
 *
 * @param <ResultT> The type to which this iterable will decode documents.
 */
public class FindIterable<ResultT> extends MongoIterable<ResultT> {

    private final OsFindIterable<ResultT> osFindIterable;

    public FindIterable(final TaskDispatcher dispatcher,
                        final OsFindIterable<ResultT> osFindIterable) {
        super(dispatcher);
        this.osFindIterable = osFindIterable;
    }

    @Override
    Collection<ResultT> getCollection() {
        return osFindIterable.getCollection();
    }

    @Override
    ResultT getFirst() {
        return osFindIterable.first();
    }

    /**
     * Sets the query filter to apply to the query.
     *
     * @param filter the filter, which may be null.
     * @return this
     */
    public FindIterable<ResultT> filter(@Nullable final Bson filter) {
        osFindIterable.filter(filter);
        return this;
    }

    /**
     * Sets the limit to apply.
     *
     * @param limit the limit, which may be 0
     * @return this
     */
    public FindIterable<ResultT> limit(final int limit) {
        osFindIterable.limit(limit);
        return this;
    }

    /**
     * Sets a document describing the fields to return for all matching documents.
     *
     * @param projection the project document, which may be null.
     * @return this
     */
    public FindIterable<ResultT> projection(@Nullable final Bson projection) {
        osFindIterable.projection(projection);
        return this;
    }

    /**
     * Sets the sort criteria to apply to the query.
     *
     * @param sort the sort criteria, which may be null.
     * @return this
     */
    public FindIterable<ResultT> sort(@Nullable final Bson sort) {
        osFindIterable.sort(sort);
        return this;
    }
}
