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

package io.realm.mongodb.mongo;

import org.bson.codecs.configuration.CodecRegistry;

import io.realm.mongodb.User;
import io.realm.internal.Util;
import io.realm.internal.objectstore.OsMongoClient;

/**
 * The remote MongoClient used for working with data in MongoDB remotely via Realm.
 */
public class MongoClient {

    private OsMongoClient osMongoClient;
    private CodecRegistry codecRegistry;

    public MongoClient(final User user, final String serviceName, final CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
        Util.checkEmpty(serviceName, "serviceName");
        osMongoClient = new OsMongoClient(user, serviceName);
    }

    /**
     * Gets a {@link MongoDatabase} instance for the given database name.
     *
     * @param databaseName the name of the database to retrieve
     * @return a {@code RemoteMongoDatabase} representing the specified database
     */
    public MongoDatabase getDatabase(final String databaseName) {
        Util.checkEmpty(databaseName, "databaseName");
        return new MongoDatabase(osMongoClient.getRemoteDatabase(databaseName, codecRegistry), databaseName);
    }
}
