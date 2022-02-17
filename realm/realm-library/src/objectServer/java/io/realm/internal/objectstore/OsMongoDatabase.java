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

package io.realm.internal.objectstore;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.concurrent.ThreadPoolExecutor;

import io.realm.internal.NativeObject;
import io.realm.internal.network.StreamNetworkTransport;
import io.realm.mongodb.mongo.MongoNamespace;

public class OsMongoDatabase implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final String serviceName;
    private final CodecRegistry codecRegistry;
    private final StreamNetworkTransport streamNetworkTransport;

    OsMongoDatabase(final long nativeDatabasePtr,
                    final String serviceName,
                    final CodecRegistry codecRegistry,
                    final StreamNetworkTransport streamNetworkTransport) {
        this.nativePtr = nativeDatabasePtr;
        this.serviceName = serviceName;
        this.codecRegistry = codecRegistry;
        this.streamNetworkTransport = streamNetworkTransport;
    }

    public OsMongoCollection<Document> getCollection(final String collectionName, final MongoNamespace namespace) {
        return getCollection(collectionName, namespace, Document.class);
    }

    public <DocumentT> OsMongoCollection<DocumentT> getCollection(final String collectionName,
                                                                  final MongoNamespace namespace,
                                                                  final Class<DocumentT> documentClass) {
        long nativeCollectionPtr = nativeGetCollection(nativePtr, collectionName);
        return new OsMongoCollection<>(nativeCollectionPtr, namespace, serviceName, documentClass, codecRegistry, streamNetworkTransport);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeGetCollection(long nativeDatabasePtr, String collectionName);
    private static native long nativeGetFinalizerMethodPtr();
}
