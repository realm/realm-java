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

import org.bson.BsonArray;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.ObjectServerError;
import io.realm.internal.NativeObject;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.network.ResultHandler;
import io.realm.mongodb.mongo.options.CountOptions;
import io.realm.mongodb.mongo.options.FindOneAndModifyOptions;
import io.realm.mongodb.mongo.options.FindOptions;
import io.realm.mongodb.mongo.options.InsertManyResult;
import io.realm.mongodb.mongo.options.UpdateOptions;
import io.realm.mongodb.mongo.result.DeleteResult;
import io.realm.mongodb.mongo.result.InsertOneResult;
import io.realm.mongodb.mongo.result.UpdateResult;

public class OsMongoCollection<DocumentT> implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final Class<DocumentT> documentClass;
    private final CodecRegistry codecRegistry;

    OsMongoCollection(final long nativeCollectionPtr, final Class<DocumentT> documentClass, final CodecRegistry codecRegistry) {
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

    public Long count(@Nullable final Bson filter, @Nullable final CountOptions options) {
        AtomicReference<Long> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<Long> callback = new OsJNIResultCallback<Long>(success, error) {
            @Override
            protected Long mapSuccess(Object result) {
                return (Long) result;
            }
        };

        // no filter means count all
        String filterString = (filter == null) ?
                JniBsonProtocol.encode(new Document(), codecRegistry) :
                JniBsonProtocol.encode(filter, codecRegistry);
        int limit = (options == null) ? 0 : options.getLimit();

        nativeCount(nativePtr, filterString, limit, callback);

        return ResultHandler.handleResult(success, error);
    }

    public Collection<DocumentT> find() {
        return find(new Document());
    }

    public Collection<DocumentT> find(final FindOptions options) {
        return find(new Document(), options);
    }

    public <ResultT> Collection<ResultT> find(final Class<ResultT> resultClass) {
        return find(new Document(), resultClass);
    }

    public <ResultT> Collection<ResultT> find(final Class<ResultT> resultClass, final FindOptions options) {
        return find(new Document(), resultClass, options);
    }

    public Collection<DocumentT> find(final Bson filter) {
        return find(filter, documentClass);
    }

    public Collection<DocumentT> find(final Bson filter, final FindOptions options) {
        return find(filter, documentClass, options);
    }

    public <ResultT> Collection<ResultT> find(final Bson filter, final Class<ResultT> resultClass) {
        return find(filter, resultClass, null);
    }

    // FIXME: fix find implementation - ignore this code for code review
    public <ResultT> Collection<ResultT> find(final Bson filter,
                                              final Class<ResultT> resultClass,
                                              @Nullable final FindOptions options) {
        AtomicReference<Collection<ResultT>> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<Collection<ResultT>> callback = new OsJNIResultCallback<Collection<ResultT>>(success, error) {
            @Override
            @SuppressWarnings("unchecked")
            protected Collection<ResultT> mapSuccess(Object result) {
                return JniBsonProtocol.decode((String) result, Collection.class, codecRegistry);
            }
        };

        String filterString = JniBsonProtocol.encode(filter, codecRegistry);

        if (options == null) {
            nativeFind(nativePtr, filterString, callback);
        } else {
            String projectionString = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
            String sortString = JniBsonProtocol.encode(options.getSort(), codecRegistry);

            nativeFindWithOptions(nativePtr, filterString, projectionString, sortString, options.getLimit(), callback);
        }

        return ResultHandler.handleResult(success, error);
    }

    public DocumentT findOne() {
        return findOne(new Document());
    }

    public <ResultT> ResultT findOne(final Class<ResultT> resultClass) {
        return findOne(null, resultClass);
    }

    public DocumentT findOne(final Bson filter) {
        return findOne(filter, documentClass);
    }

    public <ResultT> ResultT findOne(final @Nullable Bson filter, final Class<ResultT> resultClass) {
        return findOneInternal(filter, null, resultClass);
    }

    public DocumentT findOne(@Nullable final Bson filter, final FindOptions options) {
        return findOne(filter, options, documentClass);
    }

    public <ResultT> ResultT findOne(@Nullable final Bson filter,
                                     final FindOptions options,
                                     final Class<ResultT> resultClass) {
        return findOneInternal(filter, options, resultClass);
    }

    public InsertOneResult insertOne(final DocumentT document) {
        AtomicReference<InsertOneResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<InsertOneResult> callback = new OsJNIResultCallback<InsertOneResult>(success, error) {
            @Override
            protected InsertOneResult mapSuccess(Object result) {
                BsonValue bsonObjectId = new BsonObjectId((ObjectId) result);
                return new InsertOneResult(bsonObjectId);
            }
        };

        String encodedDocument = JniBsonProtocol.encode(document, codecRegistry);
        nativeInsertOne(nativePtr, encodedDocument, callback);
        return ResultHandler.handleResult(success, error);
    }

    public InsertManyResult insertMany(final List<? extends DocumentT> documents) {
        AtomicReference<InsertManyResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<InsertManyResult> callback = new OsJNIResultCallback<InsertManyResult>(success, error) {
            @Override
            protected InsertManyResult mapSuccess(Object result) {
                Object[] objects = (Object[]) result;
                Map<Long, BsonValue> insertedIdsMap = new HashMap<>();
                for (int i = 0; i < objects.length; i++) {
                    ObjectId objectId = (ObjectId) objects[i];
                    BsonValue bsonObjectId = new BsonObjectId(objectId);
                    insertedIdsMap.put((long) i, bsonObjectId);
                }
                return new InsertManyResult(insertedIdsMap);
            }
        };

        String encodedDocumentArray = JniBsonProtocol.encode(documents, codecRegistry);
        nativeInsertMany(nativePtr, encodedDocumentArray, callback);
        return ResultHandler.handleResult(success, error);
    }

    public DeleteResult deleteOne(final Bson filter) {
        return deleteInternal(DeleteType.ONE, filter);
    }

    public DeleteResult deleteMany(final Bson filter) {
        return deleteInternal(DeleteType.MANY, filter);
    }

    public UpdateResult updateOne(final Bson filter, final Bson update) {
        return updateOne(filter, update, null);
    }

    public UpdateResult updateOne(final Bson filter,
                                  final Bson update,
                                  @Nullable final UpdateOptions options) {
        return updateInternal(UpdateType.ONE, filter, update, options);
    }

    public UpdateResult updateMany(final Bson filter, final Bson update) {
        return updateMany(filter, update, null);
    }

    public UpdateResult updateMany(final Bson filter,
                                   final Bson update,
                                   @Nullable final UpdateOptions options) {
        return updateInternal(UpdateType.MANY, filter, update, options);
    }

    public DocumentT findOneAndUpdate(final Bson filter, final Bson update) {
        return findOneAndUpdate(filter, update, documentClass);
    }

    public <ResultT> ResultT findOneAndUpdate(final Bson filter,
                                              final Bson update,
                                              final Class<ResultT> resultClass) {
        return findOneAndInternal(FindOneAndType.UPDATE, filter, update, null, resultClass);
    }

    public DocumentT findOneAndUpdate(final Bson filter,
                                      final Bson update,
                                      final FindOneAndModifyOptions options) {
        return findOneAndUpdate(filter, update, options, documentClass);
    }

    public <ResultT> ResultT findOneAndUpdate(final Bson filter,
                                              final Bson update,
                                              final FindOneAndModifyOptions options,
                                              final Class<ResultT> resultClass) {
        return findOneAndInternal(FindOneAndType.UPDATE, filter, update, options, resultClass);
    }

    public DocumentT findOneAndReplace(final Bson filter, final Bson replacement) {
        return findOneAndReplace(filter, replacement, documentClass);
    }

    public <ResultT> ResultT findOneAndReplace(final Bson filter,
                                               final Bson update,
                                               final Class<ResultT> resultClass) {
        return findOneAndInternal(FindOneAndType.REPLACE, filter, update, null, resultClass);
    }

    public DocumentT findOneAndReplace(final Bson filter,
                                       final Bson update,
                                       final FindOneAndModifyOptions options) {
        return findOneAndReplace(filter, update, options, documentClass);
    }

    public <ResultT> ResultT findOneAndReplace(final Bson filter,
                                               final Bson update,
                                               final FindOneAndModifyOptions options,
                                               final Class<ResultT> resultClass) {
        return findOneAndInternal(FindOneAndType.REPLACE, filter, update, options, resultClass);
    }

    public DocumentT findOneAndDelete(final Bson filter) {
        return findOneAndDelete(filter, documentClass);
    }

    public <ResultT> ResultT findOneAndDelete(final Bson filter,
                                              final Class<ResultT> resultClass) {
        return findOneAndDeleteInternal(filter, null, resultClass);
    }

    public DocumentT findOneAndDelete(final Bson filter,
                                      final FindOneAndModifyOptions options) {
        return findOneAndDeleteInternal(filter, options, documentClass);
    }

    public <ResultT> ResultT findOneAndDelete(final Bson filter,
                                      final FindOneAndModifyOptions options,
                                      final Class<ResultT> resultClass) {
        return findOneAndDeleteInternal(filter, options, resultClass);
    }

    private UpdateResult updateInternal(UpdateType type, final Bson filter, final Bson update, @Nullable final UpdateOptions options) {
        AtomicReference<UpdateResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<UpdateResult> callback = new OsJNIResultCallback<UpdateResult>(success, error) {
            @Override
            protected UpdateResult mapSuccess(Object result) {
                // FIXME: see OsMongoCollection.cpp - collection_mapper_update. There surely is a better way to do this
                BsonArray array = JniBsonProtocol.decode((String) result, BsonArray.class, codecRegistry);
                long matchedCount = array.get(0).asInt32().getValue();
                long modifiedCount = array.get(1).asInt32().getValue();

                // FIXME: this seems ugly, but Stitch allows retuning null for upsertedId
                BsonValue upsertedId = array.get(2);
                if (upsertedId instanceof BsonNull) {
                    upsertedId = null;
                }
                return new UpdateResult(matchedCount, modifiedCount, upsertedId);
            }
        };

        String jsonFilter = JniBsonProtocol.encode(filter, codecRegistry);
        String jsonUpdate = JniBsonProtocol.encode(update, codecRegistry);

        switch (type) {
            case ONE:
                if (options == null) {
                    nativeUpdateOne(nativePtr, jsonFilter, jsonUpdate, callback);
                } else {
                    nativeUpdateOneWithOptions(nativePtr, jsonFilter, jsonUpdate, options.isUpsert(), callback);
                }
                break;
            case MANY:
                if (options == null) {
                    nativeUpdateMany(nativePtr, jsonFilter, jsonUpdate, callback);
                } else {
                    nativeUpdateManyWithOptions(nativePtr, jsonFilter, jsonUpdate, options.isUpsert(), callback);
                }
                break;
        }
        return ResultHandler.handleResult(success, error);
    }

    private <ResultT> ResultT findOneInternal(@Nullable final Bson filter,
                                              @Nullable final FindOptions options,
                                              final Class<ResultT> resultClass) {
        AtomicReference<ResultT> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                return findSuccessMapper(result, resultClass);
            }
        };

        String encodedFilter = (filter == null) ?
                JniBsonProtocol.encode(new Document(), codecRegistry) :
                JniBsonProtocol.encode(filter, codecRegistry);
        if (options == null) {
            nativeFindOne(nativePtr, encodedFilter, callback);
        } else {
            String projectionString = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
            String sortString = JniBsonProtocol.encode(options.getSort(), codecRegistry);

            nativeFindOneWithOptions(nativePtr, encodedFilter, projectionString, sortString, options.getLimit(), callback);
        }

        return ResultHandler.handleResult(success, error);
    }

    private  <ResultT> ResultT findOneAndDeleteInternal(final Bson filter,
                                                        @Nullable final FindOneAndModifyOptions options,
                                                        final Class<ResultT> resultClass) {
        return findOneAndInternal(FindOneAndType.DELETE, filter, new Document(), options, resultClass);
    }

    private  <ResultT> ResultT findOneAndInternal(final FindOneAndType type,
                                                  final Bson filter,
                                                  final Bson update,
                                                  @Nullable final FindOneAndModifyOptions options,
                                                  final Class<ResultT> resultClass) {
        AtomicReference<ResultT> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                return findSuccessMapper(result, resultClass);
            }
        };

        String encodedFilter = JniBsonProtocol.encode(filter, codecRegistry);
        String encodedUpdate = JniBsonProtocol.encode(update, codecRegistry);
        String encodedProjection = null;
        String encodedSort = null;
        if (options != null) {
            encodedProjection = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
            encodedSort = JniBsonProtocol.encode(options.getSort(), codecRegistry);
        }

        switch (type) {
            case UPDATE:
                if (options == null) {
                    nativeFindOneAndUpdate(nativePtr, encodedFilter, encodedUpdate, callback);
                } else {
                    nativeFindOneAndUpdateWithOptions(nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                }
                break;
            case REPLACE:
                if (options == null) {
                    nativeFindOneAndReplace(nativePtr, encodedFilter, encodedUpdate, callback);
                } else {
                    nativeFindOneAndReplaceWithOptions(nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                }
                break;
            case DELETE:
                if (options == null) {
                    nativeFindOneAndDelete(nativePtr, encodedFilter, callback);
                } else {
                    nativeFindOneAndDeleteWithOptions(nativePtr, encodedFilter, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                }
                break;
        }

        return ResultHandler.handleResult(success, error);
    }

    private DeleteResult deleteInternal(final DeleteType type, final Bson filter) {
        AtomicReference<DeleteResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<DeleteResult> callback = new OsJNIResultCallback<DeleteResult>(success, error) {
            @Override
            protected DeleteResult mapSuccess(Object result) {
                return new DeleteResult((Long) result);
            }
        };

        String jsonDocument = JniBsonProtocol.encode(filter, codecRegistry);
        switch (type) {
            case ONE:
                nativeDeleteOne(nativePtr, jsonDocument, callback);
                break;
            case MANY:
                nativeDeleteMany(nativePtr, jsonDocument, callback);
                break;
        }
        return ResultHandler.handleResult(success, error);
    }

    private <T> T findSuccessMapper(@Nullable Object result, Class<T> resultClass) {
        if (result == null) {
            return null;
        } else {
            return JniBsonProtocol.decode((String) result, resultClass, codecRegistry);
        }
    }

    private enum UpdateType {
        ONE, MANY
    }

    private enum DeleteType {
        ONE, MANY
    }

    private enum FindOneAndType {
        UPDATE, REPLACE, DELETE
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeCount(long remoteMongoCollectionPtr,
                                           String filter,
                                           long limit,
                                           OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOne(long nativePtr,
                                             String filter,
                                             OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneWithOptions(long nativePtr,
                                                        String filter,
                                                        String projection,
                                                        String sort,
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
    private static native void nativeUpdateOne(long remoteMongoCollectionPtr,
                                               String filter,
                                               String update,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeUpdateOneWithOptions(long remoteMongoCollectionPtr,
                                                          String filter,
                                                          String update,
                                                          boolean upsert,
                                                          OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeUpdateMany(long remoteMongoCollectionPtr,
                                               String filter,
                                               String update,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeUpdateManyWithOptions(long remoteMongoCollectionPtr,
                                                          String filter,
                                                          String update,
                                                          boolean upsert,
                                                          OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndUpdate(long remoteMongoCollectionPtr,
                                                      String filter,
                                                      String update,
                                                      OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndUpdateWithOptions(long remoteMongoCollectionPtr,
                                                                 String filter,
                                                                 String update,
                                                                 String projection,
                                                                 String sort,
                                                                 boolean upsert,
                                                                 boolean returnNewDocument,
                                                                 OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndReplace(long remoteMongoCollectionPtr,
                                                       String filter,
                                                       String update,
                                                       OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndReplaceWithOptions(long remoteMongoCollectionPtr,
                                                                  String filter,
                                                                  String update,
                                                                  String projection,
                                                                  String sort,
                                                                  boolean upsert,
                                                                  boolean returnNewDocument,
                                                                  OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndDelete(long remoteMongoCollectionPtr,
                                                       String filter,
                                                       OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndDeleteWithOptions(long remoteMongoCollectionPtr,
                                                                  String filter,
                                                                  String projection,
                                                                  String sort,
                                                                  boolean upsert,
                                                                  boolean returnNewDocument,
                                                                  OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFind(long remoteMongoCollectionPtr,
                                          String filter,
                                          OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindWithOptions(long remoteMongoCollectionPtr,
                                                     String filter,
                                                     String projection,
                                                     String sort,
                                                     long limit,
                                                     OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
