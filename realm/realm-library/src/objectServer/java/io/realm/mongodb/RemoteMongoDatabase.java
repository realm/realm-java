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

import org.bson.Document;

import io.realm.internal.Util;
import io.realm.internal.objectstore.OsRemoteMongoDatabase;

/**
 * The RemoteMongoDatabase provides access to its {@link Document} {@link RemoteMongoCollection}s.
 */
public class RemoteMongoDatabase {

    private String databaseName;
    private OsRemoteMongoDatabase osRemoteMongoDatabase;

    RemoteMongoDatabase(OsRemoteMongoDatabase osRemoteMongoDatabase, String databaseName) {
        // we deliver the database name because we don't want to modify the C++ code right now,
        // although ideally it should be done there, i.e. remote_mongo_database.hpp should
        // include the public (Java) API's methods that aren't there yet.
        this.databaseName = databaseName;
        this.osRemoteMongoDatabase = osRemoteMongoDatabase;
    }

    /**
     * Gets the name of the database.
     *
     * @return the database name
     */
    public String getName() {
        return databaseName;
    }

    /**
     * Gets a collection.
     *
     * @param collectionName the name of the collection to return
     * @return the collection
     */
    public RemoteMongoCollection<Document> getCollection(final String collectionName) {
        Util.checkEmpty(collectionName, "collectionName");
        return new RemoteMongoCollection<>(osRemoteMongoDatabase.getCollection(collectionName));
    }

    /**
     * Gets a collection, with a specific default document class.
     *
     * @param collectionName the name of the collection to return
     * @param documentClass  the default class to cast any documents returned from the database into.
     * @param <DocumentT>    the type of the class to use instead of {@code Document}.
     * @return the collection
     */
    <DocumentT> RemoteMongoCollection<DocumentT> getCollection(
            final String collectionName,
            final Class<DocumentT> documentClass
    ) {
        Util.checkEmpty(collectionName, "collectionName");
        Util.checkNull(documentClass, "documentClass");
        return new RemoteMongoCollection<>(osRemoteMongoDatabase.getCollection(collectionName, documentClass));
    }
}
