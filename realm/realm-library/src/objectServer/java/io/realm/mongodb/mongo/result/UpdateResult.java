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

package io.realm.mongodb.mongo.result;

import javax.annotation.Nullable;

import org.bson.BsonValue;

import io.realm.annotations.Beta;

/**
 * The result of an update operation.
 */
@Beta
public class UpdateResult {

    private final long matchedCount;
    private final long modifiedCount;
    private final BsonValue upsertedId;

    /**
     * Constructs a result.
     *
     * @param matchedCount  the number of documents matched by the query.
     * @param modifiedCount the number of documents modified.
     * @param upsertedId    the _id of the inserted document if the replace resulted in an inserted
     *                      document, otherwise null.
     */
    public UpdateResult(
            final long matchedCount,
            final long modifiedCount,
            final BsonValue upsertedId
    ) {
        this.matchedCount = matchedCount;
        this.modifiedCount = modifiedCount;
        this.upsertedId = upsertedId;
    }

    /**
     * Returns the number of documents matched by the query.
     *
     * @return the number of documents matched.
     */
    public long getMatchedCount() {
        return matchedCount;
    }

    /**
     * Returns the number of documents modified.
     *
     * @return the number of documents modified.
     */
    public long getModifiedCount() {
        return modifiedCount;
    }

    /**
     * If the replace resulted in an inserted document, gets the _id of the inserted document,
     * otherwise null.
     *
     * @return if the replace resulted in an inserted document, the _id of the inserted document,
     * otherwise null.
     */
    @Nullable
    public BsonValue getUpsertedId() {
        return upsertedId;
    }
}
