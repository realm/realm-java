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

package io.realm.mongodb.mongo.options;

import javax.annotation.Nullable;

import org.bson.Document;
import org.bson.conversions.Bson;

import io.realm.annotations.Beta;

/**
 * The options to apply to a find operation (also commonly referred to as a query).
 */
@Beta
public class FindOptions {

    private int limit;
    private Bson projection;
    private Bson sort;

    /**
     * Construct a new instance.
     */
    public FindOptions() {
        this.projection = new Document();
        this.sort = new Document();
    }

    /**
     * Gets the limit to apply.  The default is null.
     *
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the limit to apply.
     *
     * @param limit the limit, which may be null
     * @return this
     */
    public FindOptions limit(final int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Gets a document describing the fields to return for all matching documents.
     *
     * @return the project document, which may be null
     */
    @Nullable
    public Bson getProjection() {
        return projection;
    }

    /**
     * Sets a document describing the fields to return for all matching documents.
     *
     * @param projection the project document, which may be null.
     * @return this
     */
    public FindOptions projection(@Nullable final Bson projection) {
        this.projection = projection;
        return this;
    }

    /**
     * Gets the sort criteria to apply to the query. The default is null, which means that the
     * documents will be returned in an undefined order.
     *
     * @return a document describing the sort criteria
     */
    @Nullable
    public Bson getSort() {
        return sort;
    }

    /**
     * Sets the sort criteria to apply to the query.
     *
     * @param sort the sort criteria, which may be null.
     * @return this
     */
    public FindOptions sort(@Nullable final Bson sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public String toString() {
        return "RemoteFindOptions{"
                + "limit=" + limit
                + ", projection=" + projection
                + ", sort=" + sort
                + "}";
    }
}
