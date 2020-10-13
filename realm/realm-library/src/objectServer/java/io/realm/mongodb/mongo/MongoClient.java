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

import io.realm.annotations.Beta;
import io.realm.internal.Util;
import io.realm.internal.objectstore.OsMongoClient;

/**
 * The remote MongoClient used for working with data in MongoDB remotely via Realm.
 */
@Beta
public abstract class MongoClient {

    private final OsMongoClient osMongoClient;
    private final CodecRegistry codecRegistry;

    protected MongoClient(final OsMongoClient osMongoClient,
                          final CodecRegistry codecRegistry) {
        this.osMongoClient = osMongoClient;
        this.codecRegistry = codecRegistry;
    }

    /**
     * Gets a {@link MongoDatabase} instance for the given database name.
     *
     * @param databaseName the name of the database to retrieve
     * @return a {@code RemoteMongoDatabase} representing the specified database
     */
    public MongoDatabase getDatabase(final String databaseName) {
        Util.checkEmpty(databaseName, "databaseName");
        return new MongoDatabase(osMongoClient.getDatabase(databaseName, codecRegistry),
                databaseName);
    }

    /**
     * Returns the service name for this client.
     *
     * @return the service name.
     */
    public String getServiceName() {
        return osMongoClient.getServiceName();
    }
}
