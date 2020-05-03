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

package io.realm.mongodb;

import io.realm.RealmUser;
import io.realm.internal.Util;
import io.realm.internal.objectstore.OsRemoteMongoClient;

/**
 * The remote MongoClient used for working with data in MongoDB remotely via Realm.
 */
public class RemoteMongoClient {

    private OsRemoteMongoClient osRemoteMongoClient;

    public RemoteMongoClient(RealmUser realmUser, String serviceName) {
        Util.checkEmpty(serviceName, "serviceName");
        osRemoteMongoClient = new OsRemoteMongoClient(realmUser, serviceName);
    }

    /**
     * Gets a {@link RemoteMongoDatabase} instance for the given database name.
     *
     * @param databaseName the name of the database to retrieve
     * @return a {@code RemoteMongoDatabase} representing the specified database
     */
    public RemoteMongoDatabase getDatabase(final String databaseName) {
        Util.checkEmpty(databaseName, "databaseName");
        return new RemoteMongoDatabase(osRemoteMongoClient.getRemoteDatabase(databaseName), databaseName);
    }
}
