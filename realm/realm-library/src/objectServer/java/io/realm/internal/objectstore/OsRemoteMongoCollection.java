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

import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.ObjectServerError;
import io.realm.internal.NativeObject;
import io.realm.internal.ResultHandler;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.mongodb.remote.RemoteCountOptions;
import io.realm.mongodb.remote.RemoteDeleteResult;
import io.realm.mongodb.remote.RemoteInsertManyResult;
import io.realm.mongodb.remote.RemoteInsertOneResult;

public class OsRemoteMongoCollection<DocumentT> implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final Class<DocumentT> documentClass;
    private final CodecRegistry codecRegistry;

    OsRemoteMongoCollection(final long nativeCollectionPtr, final Class<DocumentT> documentClass, final CodecRegistry codecRegistry) {
        this.nativePtr = nativeCollectionPtr;
        this.documentClass = documentClass;
        this.codecRegistry = codecRegistry;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public Long count() {
        return count(null);
    }

    public Long count(@Nullable final Bson filter) {
        return count(filter, null);
    }

    public Long count(@Nullable final Bson filter, @Nullable final RemoteCountOptions options) {
        AtomicReference<Long> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<Long> callback = new OsJNIResultCallback<Long>(success, error) {
            @Override
            protected Long mapSuccess(Object result) {
                return (long) result;
            }
        };

        // FIXME: add support for POJOs - default to empty bson for now
        String filterString = filter == null ?
                JniBsonProtocol.encode(new BsonDocument()) :
                JniBsonProtocol.encode(filter.toBsonDocument(documentClass, codecRegistry));
        int limit = options == null ? 0 : options.getLimit();

        nativeCount(nativePtr, filterString, limit, callback);

        return ResultHandler.handleResult(success, error);
    }

    public RemoteInsertOneResult insertOne(final DocumentT document) {
        AtomicReference<RemoteInsertOneResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<RemoteInsertOneResult> callback = new OsJNIResultCallback<RemoteInsertOneResult>(success, error) {
            @Override
            protected RemoteInsertOneResult mapSuccess(Object result) {
                BsonValue bsonObjectId = new BsonObjectId((ObjectId) result);
                return new RemoteInsertOneResult(bsonObjectId);
            }
        };

        // FIXME: add support for POJOs - default to empty bson for now
        String jsonDocument;
        if (document instanceof Document) {
            BsonDocument bsonDocument = ((Document) document).toBsonDocument(documentClass, codecRegistry);
            jsonDocument = JniBsonProtocol.encode(bsonDocument);
        } else {
            jsonDocument = JniBsonProtocol.encode(new BsonDocument());
        }

        nativeInsertOne(nativePtr, jsonDocument, callback);

        return ResultHandler.handleResult(success, error);
    }

    public RemoteInsertManyResult insertMany(final List<? extends DocumentT> documents) {
        AtomicReference<RemoteInsertManyResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<RemoteInsertManyResult> callback = new OsJNIResultCallback<RemoteInsertManyResult>(success, error) {
            @Override
            protected RemoteInsertManyResult mapSuccess(Object result) {
                Object[] objects = (Object[]) result;
                Map<Long, BsonValue> insertedIdsMap = new HashMap<>();
                for (int i = 0; i < objects.length; i++) {
                    ObjectId objectId = (ObjectId) objects[i];
                    BsonValue bsonObjectId = new BsonObjectId(objectId);
                    insertedIdsMap.put((long) i, bsonObjectId);
                }
                return new RemoteInsertManyResult(insertedIdsMap);
            }
        };

        // FIXME: add support for POJOs - default to empty bson for now
        String[] documentArray = new String[documents.size()];
        if (documents.get(0) instanceof Document) {
            List<Document> documentList = (List<Document>) documents;
            for (int i = 0; i < documentList.size(); i++) {
                Document document = documentList.get(i);
                BsonDocument bsonDocument = document.toBsonDocument(documentClass, codecRegistry);
                documentArray[i] = JniBsonProtocol.encode(bsonDocument);
            }
        } else {
            for (int i = 0; i < documents.size(); i++) {
                documentArray[i] = JniBsonProtocol.encode(new BsonDocument());
            }
        }

        nativeInsertMany(nativePtr, callback, documentArray);

        return ResultHandler.handleResult(success, error);
    }

    public RemoteDeleteResult deleteOne(final Bson filter) {
        AtomicReference<RemoteDeleteResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<RemoteDeleteResult> callback = new OsJNIResultCallback<RemoteDeleteResult>(success, error) {
            @Override
            protected RemoteDeleteResult mapSuccess(Object result) {
                return new RemoteDeleteResult((Long) result);
            }
        };

        String jsonDocument = JniBsonProtocol.encode(filter.toBsonDocument(documentClass, codecRegistry));

        nativeDeleteOne(nativePtr, jsonDocument, callback);

        return ResultHandler.handleResult(success, error);
    }

    public RemoteDeleteResult deleteMany(final Bson filter) {
        AtomicReference<RemoteDeleteResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<RemoteDeleteResult> callback = new OsJNIResultCallback<RemoteDeleteResult>(success, error) {
            @Override
            protected RemoteDeleteResult mapSuccess(Object result) {
                return new RemoteDeleteResult((Long) result);
            }
        };

        String jsonDocument = JniBsonProtocol.encode(filter.toBsonDocument(documentClass, codecRegistry));

        nativeDeleteMany(nativePtr, jsonDocument, callback);

        return ResultHandler.handleResult(success, error);
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeCount(long remoteMongoCollectionPtr,
                                           String filter,
                                           long limit,
                                           OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeInsertOne(long remoteMongoCollectionPtr,
                                               String document,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeInsertMany(long remoteMongoCollectionPtr,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback,
                                               String... documents);
    private static native void nativeDeleteOne(long remoteMongoCollectionPtr,
                                               String document,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeDeleteMany(long remoteMongoCollectionPtr,
                                               String document,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
