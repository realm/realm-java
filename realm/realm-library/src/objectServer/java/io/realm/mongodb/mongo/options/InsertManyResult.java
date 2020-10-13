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

import java.util.Map;

import org.bson.BsonValue;

import io.realm.annotations.Beta;

/**
 * The result of an insert many operation.
 */
@Beta
public class InsertManyResult {

    private final Map<Long, BsonValue> insertedIds;

    /**
     * Constructs a result.
     *
     * @param insertedIds the _ids of the inserted documents arranged by the index of the document
     *                    from the operation and its corresponding id.
     */
    public InsertManyResult(final Map<Long, BsonValue> insertedIds) {
        this.insertedIds = insertedIds;
    }

    /**
     * Returns the _ids of the inserted documents arranged by the index of the document from the
     * operation and its corresponding id.
     *
     * @return the _ids of the inserted documents arranged by the index of the document from the
     * operation and its corresponding id.
     */
    public Map<Long, BsonValue> getInsertedIds() {
        return insertedIds;
    }
}
