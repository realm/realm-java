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
import io.realm.internal.network.ResultHandler;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.mongodb.remote.RemoteCountOptions;
import io.realm.mongodb.remote.RemoteDeleteResult;
import io.realm.mongodb.remote.RemoteFindOptions;
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
                return (Long) result;
            }
        };

        // no filter means count all
        String filterString = filter == null ?
                JniBsonProtocol.encode(new Document(), codecRegistry) :
                JniBsonProtocol.encode(filter, codecRegistry);
        int limit = options == null ? 0 : options.getLimit();

        nativeCount(nativePtr, filterString, limit, callback);

        return ResultHandler.handleResult(success, error);
    }

    public DocumentT findOne() {
        return findOne(new Document());
    }

    public <ResultT> ResultT findOne(final Class<ResultT> resultClass) {
        AtomicReference<ResultT> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                return findOneSuccessMapper(result, resultClass);
            }
        };

        nativeFindOne(nativePtr, JniBsonProtocol.encode(new Document(), codecRegistry), callback);

        return ResultHandler.handleResult(success, error);
    }

    public DocumentT findOne(final Bson filter) {
        AtomicReference<DocumentT> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<DocumentT> callback = new OsJNIResultCallback<DocumentT>(success, error) {
            @Override
            protected DocumentT mapSuccess(Object result) {
                return findOneSuccessMapper(result, documentClass);
            }
        };

        String encodedFilter = JniBsonProtocol.encode(filter, codecRegistry);
        nativeFindOne(nativePtr, encodedFilter, callback);

        return ResultHandler.handleResult(success, error);
    }

    public <ResultT> ResultT findOne(final @Nullable Bson filter, final Class<ResultT> resultClass) {
        AtomicReference<ResultT> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                return findOneSuccessMapper(result, resultClass);
            }
        };

        String encodedFilter = filter == null ?
                JniBsonProtocol.encode(new Document(), codecRegistry) :
                JniBsonProtocol.encode(filter, codecRegistry);
        nativeFindOne(nativePtr, encodedFilter, callback);

        return ResultHandler.handleResult(success, error);
    }

    public DocumentT findOne(@Nullable final Bson filter, final RemoteFindOptions options) {
        AtomicReference<DocumentT> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<DocumentT> callback = new OsJNIResultCallback<DocumentT>(success, error) {
            @Override
            protected DocumentT mapSuccess(Object result) {
                return findOneSuccessMapper(result, documentClass);
            }
        };

        String encodedFilter = filter == null ?
                JniBsonProtocol.encode(new Document(), codecRegistry) :
                JniBsonProtocol.encode(filter, codecRegistry);
        String projectionString = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
        String sortString = JniBsonProtocol.encode(options.getSort(), codecRegistry);
        nativeFindOneWithOptions(nativePtr, encodedFilter, projectionString, sortString, options.getLimit(), callback);

        return ResultHandler.handleResult(success, error);
    }

    public <ResultT> ResultT findOne(
            final Bson filter,
            final RemoteFindOptions options,
            final Class<ResultT> resultClass) {
        AtomicReference<ResultT> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                return findOneSuccessMapper(result, resultClass);
            }
        };

        String encodedFilter = filter == null ?
                JniBsonProtocol.encode(new Document(), codecRegistry) :
                JniBsonProtocol.encode(filter, codecRegistry);
        String projectionString = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
        String sortString = JniBsonProtocol.encode(options.getSort(), codecRegistry);
        nativeFindOneWithOptions(nativePtr, encodedFilter, projectionString, sortString, options.getLimit(), callback);

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

        String encodedDocument = JniBsonProtocol.encode(document, codecRegistry);
        nativeInsertOne(nativePtr, encodedDocument, callback);
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

        String encodedDocumentArray = JniBsonProtocol.encode(documents, codecRegistry);
        nativeInsertMany(nativePtr, encodedDocumentArray, callback);
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

        String jsonDocument = JniBsonProtocol.encode(filter, codecRegistry);
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

        String jsonDocument = JniBsonProtocol.encode(filter, codecRegistry);
        nativeDeleteMany(nativePtr, jsonDocument, callback);
        return ResultHandler.handleResult(success, error);
    }

    private <T> T findOneSuccessMapper(@Nullable Object result, Class<T> resultClass) {
        if (result == null) {
            return null;
        } else {
            return JniBsonProtocol.decode((String) result, resultClass, codecRegistry);
        }
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeCount(long remoteMongoCollectionPtr,
                                           String filter,
                                           long limit,
                                           OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOne(long nativePtr,
                                             String filterString,
                                             OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneWithOptions(long nativePtr,
                                                        String filterString,
                                                        String projectionString,
                                                        String sortString,
                                                        long limit,
                                                        OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeInsertOne(long remoteMongoCollectionPtr,
                                               String document,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeInsertMany(long remoteMongoCollectionPtr,
                                                String documents,
                                                OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeDeleteOne(long remoteMongoCollectionPtr,
                                               String document,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeDeleteMany(long remoteMongoCollectionPtr,
                                                String document,
                                                OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
