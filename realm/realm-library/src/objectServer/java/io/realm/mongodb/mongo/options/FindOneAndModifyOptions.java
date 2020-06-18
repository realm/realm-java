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
 * The options to apply to a findOneAndUpdate, findOneAndReplace, or findOneAndDelete operation
 * (also commonly referred to as findOneAndModify operations).
 */
@Beta
public class FindOneAndModifyOptions {

    private Bson projection;
    private Bson sort;
    private boolean upsert;
    private boolean returnNewDocument;

    public FindOneAndModifyOptions() {
        this.projection = new Document();
        this.sort = new Document();
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
    public FindOneAndModifyOptions projection(@Nullable final Bson projection) {
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
    public FindOneAndModifyOptions sort(@Nullable final Bson sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Returns true if a new document should be inserted if there are no matches to the query filter.
     * The default is false.
     * Note: Only findOneAndUpdate and findOneAndReplace take this option
     *
     * @return true if a new document should be inserted if there are no matches to the query filter
     */
    public boolean isUpsert() {
        return upsert;
    }

    /**
     * Set to true if a new document should be inserted if there are no matches to the query filter.
     *
     * @param upsert true if a new document should be inserted if there are no matches to the query
     *               filter.
     * @return this
     */
    public FindOneAndModifyOptions upsert(final boolean upsert) {
        this.upsert = upsert;
        return this;
    }

    /**
     * Returns true if the findOneAndModify operation should return the new document.
     * The default is false
     * Note: Only findOneAndUpdate and findOneAndReplace take this options
     * findOneAndDelete will always return the old document
     *
     * @return true if findOneAndModify operation should return the new document
     */
    public boolean isReturnNewDocument() {
        return returnNewDocument;
    }

    /**
     * Set to true if findOneAndModify operations should return the new updated document.
     * Set to false / leave blank to have these operation return the document before the update.
     * Note: Only findOneAndUpdate and findOneAndReplace take this options
     * findOneAndDelete will always return the old document
     *
     * @param returnNewDocument true if findOneAndModify operations should return the updated document
     * @return this
     */
    public FindOneAndModifyOptions returnNewDocument(final boolean returnNewDocument) {
        this.returnNewDocument = returnNewDocument;
        return this;
    }

    @Override
    public String toString() {
        return "RemoteFindOneAndModifyOptions{"
                + "projection=" + projection
                + ", sort=" + sort
                + ", upsert=" + upsert
                + ", returnNewDocument=" + returnNewDocument
                + "}";
    }
}
