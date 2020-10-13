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

import org.bson.BsonValue;

import io.realm.annotations.Beta;

/**
 * The result of an insert one operation.
 */
@Beta
public class InsertOneResult {

    private final BsonValue insertedId;

    /**
     * Constructs a result.
     *
     * @param insertedId the _id of the inserted document.
     */
    public InsertOneResult(final BsonValue insertedId) {
        this.insertedId = insertedId;
    }

    /**
     * Returns the _id of the inserted document.
     *
     * @return the _id of the inserted document.
     */
    public BsonValue getInsertedId() {
        return insertedId;
    }
}
