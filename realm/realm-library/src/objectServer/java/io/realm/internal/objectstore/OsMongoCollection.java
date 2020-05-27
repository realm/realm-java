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

    private static final int DELETE_ONE = 1;
    private static final int DELETE_MANY = 2;
    private static final int UPDATE_ONE = 3;
    private static final int UPDATE_ONE_WITH_OPTIONS = 4;
    private static final int UPDATE_MANY = 5;
    private static final int UPDATE_MANY_WITH_OPTIONS = 6;
    private static final int FIND_ONE_AND_UPDATE = 7;
    private static final int FIND_ONE_AND_UPDATE_WITH_OPTIONS = 8;
    private static final int FIND_ONE_AND_REPLACE = 9;
    private static final int FIND_ONE_AND_REPLACE_WITH_OPTIONS = 10;
    private static final int FIND_ONE_AND_DELETE = 11;
    private static final int FIND_ONE_AND_DELETE_WITH_OPTIONS = 12;
    private static final int FIND_ONE = 13;
    private static final int FIND_ONE_WITH_OPTIONS = 14;

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final Class<DocumentT> documentClass;
    private final CodecRegistry codecRegistry;
    private final String encodedEmptyDocument;

    OsMongoCollection(final long nativeCollectionPtr,
                      final Class<DocumentT> documentClass,
                      final CodecRegistry codecRegistry) {
        this.nativePtr = nativeCollectionPtr;
        this.documentClass = documentClass;
        this.codecRegistry = codecRegistry;
        this.encodedEmptyDocument = JniBsonProtocol.encode(new Document(), codecRegistry);
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
        return countInternal(new Document(), null);
    }

    public Long count(final Bson filter) {
        return countInternal(filter, null);
    }

    public Long count(final Bson filter, final CountOptions options) {
        return countInternal(filter, options);
    }

    private Long countInternal(final Bson filter, @Nullable final CountOptions options) {
        AtomicReference<Long> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<Long> callback = new OsJNIResultCallback<Long>(success, error) {
            @Override
            protected Long mapSuccess(Object result) {
                return (Long) result;
            }
        };

        String filterString = JniBsonProtocol.encode(filter, codecRegistry);
        int limit = (options == null) ? 0 : options.getLimit();

        nativeCount(nativePtr, filterString, limit, callback);

        return ResultHandler.handleResult(success, error);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public OsFindIterable<DocumentT> find() {
        return findInternal(new Document(), documentClass, null);
    }

    public OsFindIterable<DocumentT> find(final FindOptions options) {
        return findInternal(new Document(), documentClass, options);
    }

    public <ResultT> OsFindIterable<ResultT> find(final Class<ResultT> resultClass) {
        return findInternal(new Document(), resultClass, null);
    }

    public <ResultT> OsFindIterable<ResultT> find(final Class<ResultT> resultClass, final FindOptions options) {
        return findInternal(new Document(), resultClass, options);
    }

    public OsFindIterable<DocumentT> find(final Bson filter) {
        return findInternal(filter, documentClass, null);
    }

    public OsFindIterable<DocumentT> find(final Bson filter, final FindOptions options) {
        return findInternal(filter, documentClass, options);
    }

    public <ResultT> OsFindIterable<ResultT> find(final Bson filter,
                                                  final Class<ResultT> resultClass) {
        return findInternal(filter, resultClass, null);
    }

    public <ResultT> OsFindIterable<ResultT> find(final Bson filter,
                                                  final Class<ResultT> resultClass,
                                                  final FindOptions options) {
        return findInternal(filter, resultClass, options);
    }

    private <ResultT> OsFindIterable<ResultT> findInternal(final Bson filter,
                                                           final Class<ResultT> resultClass,
                                                           @Nullable final FindOptions options) {
        OsFindIterable<ResultT> osFindIterable = new OsFindIterable<>(this, codecRegistry, resultClass);
        osFindIterable.filter(filter);

        if (options != null) {
            osFindIterable.limit(options.getLimit());
            osFindIterable.projection(options.getProjection());
        }
        return osFindIterable;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public OsAggregateIterable<DocumentT> aggregate(final List<? extends Bson> pipeline) {
        return aggregate(pipeline, documentClass);
    }

    public <ResultT> OsAggregateIterable<ResultT> aggregate(final List<? extends Bson> pipeline,
                                                            final Class<ResultT> resultClass) {
        return new OsAggregateIterable<>(this, codecRegistry, resultClass, pipeline);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public DocumentT findOne() {
        return findOneInternal(FIND_ONE, new Document(), null, documentClass);
    }

    public <ResultT> ResultT findOne(final Class<ResultT> resultClass) {
        return findOneInternal(FIND_ONE, new Document(), null, resultClass);
    }

    public DocumentT findOne(final Bson filter) {
        return findOneInternal(FIND_ONE, filter, null, documentClass);
    }

    public <ResultT> ResultT findOne(final Bson filter, final Class<ResultT> resultClass) {
        return findOneInternal(FIND_ONE, filter, null, resultClass);
    }

    public DocumentT findOne(final Bson filter, final FindOptions options) {
        return findOneInternal(FIND_ONE_WITH_OPTIONS, filter, options, documentClass);
    }

    public <ResultT> ResultT findOne(final Bson filter,
                                     final FindOptions options,
                                     final Class<ResultT> resultClass) {
        return findOneInternal(FIND_ONE_WITH_OPTIONS, filter, options, resultClass);
    }

    private <ResultT> ResultT findOneInternal(final int type,
                                              final Bson filter,
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

        String encodedFilter = JniBsonProtocol.encode(filter, codecRegistry);
        String projectionString = encodedEmptyDocument;
        String sortString = encodedEmptyDocument;

        switch (type) {
            case FIND_ONE:
                nativeFindOne(FIND_ONE, nativePtr, encodedFilter, projectionString, sortString, 0, callback);
                break;
            case FIND_ONE_WITH_OPTIONS:
                if (options == null) {
                    throw new IllegalArgumentException("FindOptions must not be null.");
                }
                projectionString = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
                sortString = JniBsonProtocol.encode(options.getSort(), codecRegistry);

                nativeFindOne(FIND_ONE_WITH_OPTIONS, nativePtr, encodedFilter, projectionString, sortString, options.getLimit(), callback);
                break;
            default:
                throw new IllegalArgumentException("Invalid fineOne type: " + type);
        }

        return ResultHandler.handleResult(success, error);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public DeleteResult deleteOne(final Bson filter) {
        return deleteInternal(DELETE_ONE, filter);
    }

    public DeleteResult deleteMany(final Bson filter) {
        return deleteInternal(DELETE_MANY, filter);
    }

    private DeleteResult deleteInternal(final int type, final Bson filter) {
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
            case DELETE_ONE:
                nativeDelete(DELETE_ONE, nativePtr, jsonDocument, callback);
                break;
            case DELETE_MANY:
                nativeDelete(DELETE_MANY, nativePtr, jsonDocument, callback);
                break;
            default:
                throw new IllegalArgumentException("Invalid delete type: " + type);
        }
        return ResultHandler.handleResult(success, error);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UpdateResult updateOne(final Bson filter, final Bson update) {
        return updateInternal(UPDATE_ONE, filter, update, null);
    }

    public UpdateResult updateOne(final Bson filter,
                                  final Bson update,
                                  @Nullable final UpdateOptions options) {
        return updateInternal(UPDATE_ONE_WITH_OPTIONS, filter, update, options);
    }

    public UpdateResult updateMany(final Bson filter, final Bson update) {
        return updateInternal(UPDATE_MANY, filter, update, null);
    }

    public UpdateResult updateMany(final Bson filter,
                                   final Bson update,
                                   @Nullable final UpdateOptions options) {
        return updateInternal(UPDATE_MANY_WITH_OPTIONS, filter, update, options);
    }

    private UpdateResult updateInternal(final int type,
                                        final Bson filter,
                                        final Bson update,
                                        @Nullable final UpdateOptions options) {
        AtomicReference<UpdateResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<UpdateResult> callback = new OsJNIResultCallback<UpdateResult>(success, error) {
            @Override
            protected UpdateResult mapSuccess(Object result) {
                BsonArray array = JniBsonProtocol.decode((String) result, BsonArray.class, codecRegistry);
                long matchedCount = array.get(0).asInt32().getValue();
                long modifiedCount = array.get(1).asInt32().getValue();
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
            case UPDATE_ONE:
            case UPDATE_MANY:
                nativeUpdate(type, nativePtr, jsonFilter, jsonUpdate, false, callback);
                break;
            case UPDATE_ONE_WITH_OPTIONS:
            case UPDATE_MANY_WITH_OPTIONS:
                if (options == null) {
                    throw new IllegalArgumentException("UpdateOptions must not be null.");
                }
                nativeUpdate(type, nativePtr, jsonFilter, jsonUpdate, options.isUpsert(), callback);
                break;
            default:
                throw new IllegalArgumentException("Invalid update type: " + type);
        }
        return ResultHandler.handleResult(success, error);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public DocumentT findOneAndUpdate(final Bson filter, final Bson update) {
        return findOneAndModify(FIND_ONE_AND_UPDATE, filter, update, null, documentClass);
    }

    public <ResultT> ResultT findOneAndUpdate(final Bson filter,
                                              final Bson update,
                                              final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_UPDATE, filter, update, null, resultClass);
    }

    public DocumentT findOneAndUpdate(final Bson filter,
                                      final Bson update,
                                      final FindOneAndModifyOptions options) {
        return findOneAndModify(FIND_ONE_AND_UPDATE_WITH_OPTIONS, filter, update, options, documentClass);
    }

    public <ResultT> ResultT findOneAndUpdate(final Bson filter,
                                              final Bson update,
                                              final FindOneAndModifyOptions options,
                                              final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_UPDATE, filter, update, options, resultClass);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public DocumentT findOneAndReplace(final Bson filter, final Bson replacement) {
        return findOneAndModify(FIND_ONE_AND_REPLACE, filter, replacement, null, documentClass);
    }

    public <ResultT> ResultT findOneAndReplace(final Bson filter,
                                               final Bson replacement,
                                               final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_REPLACE, filter, replacement, null, resultClass);
    }

    public DocumentT findOneAndReplace(final Bson filter,
                                       final Bson replacement,
                                       final FindOneAndModifyOptions options) {
        return findOneAndModify(FIND_ONE_AND_REPLACE_WITH_OPTIONS, filter, replacement, options, documentClass);
    }

    public <ResultT> ResultT findOneAndReplace(final Bson filter,
                                               final Bson replacement,
                                               final FindOneAndModifyOptions options,
                                               final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_REPLACE, filter, replacement, options, resultClass);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public DocumentT findOneAndDelete(final Bson filter) {
        return findOneAndModify(FIND_ONE_AND_DELETE, filter, null, null, documentClass);
    }

    public <ResultT> ResultT findOneAndDelete(final Bson filter,
                                              final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_DELETE, filter, null, null, resultClass);
    }

    public DocumentT findOneAndDelete(final Bson filter,
                                      final FindOneAndModifyOptions options) {
        return findOneAndModify(FIND_ONE_AND_DELETE_WITH_OPTIONS, filter, null, options, documentClass);
    }

    public <ResultT> ResultT findOneAndDelete(final Bson filter,
                                              final FindOneAndModifyOptions options,
                                              final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_DELETE_WITH_OPTIONS, filter, null, options, resultClass);
    }

    private <ResultT> ResultT findOneAndModify(final int type,
                                               final Bson filter,
                                               @Nullable final Bson update,
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

        final String encodedFilter = JniBsonProtocol.encode(filter, codecRegistry);
        String encodedUpdate = update != null ? JniBsonProtocol.encode(update, codecRegistry) : encodedEmptyDocument;
        String encodedProjection = encodedEmptyDocument;
        String encodedSort = encodedEmptyDocument;
        if (options != null) {
            if (options.getProjection() != null) {
                encodedProjection = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
            }
            if (options.getSort() != null) {
                encodedSort = JniBsonProtocol.encode(options.getSort(), codecRegistry);
            }
        }

        switch (type) {
            case FIND_ONE_AND_UPDATE:
                nativeFindOneAndUpdate(type, nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, false, false, callback);
                break;
            case FIND_ONE_AND_UPDATE_WITH_OPTIONS:
                if (options == null) {
                    throw new IllegalArgumentException("FindOneAndModifyOptions must not be null");
                }
                nativeFindOneAndUpdate(type, nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                break;
            case FIND_ONE_AND_REPLACE:
                nativeFindOneAndReplace(type, nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, false, false,callback);
                break;
            case FIND_ONE_AND_REPLACE_WITH_OPTIONS:
                if (options == null) {
                    throw new IllegalArgumentException("FindOneAndModifyOptions must not be null");
                }
                nativeFindOneAndReplace(type, nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                break;
            case FIND_ONE_AND_DELETE:
                nativeFindOneAndDelete(type, nativePtr, encodedFilter, encodedProjection, encodedSort, false, false, callback);
                break;
            case FIND_ONE_AND_DELETE_WITH_OPTIONS:
                if (options == null) {
                    throw new IllegalArgumentException("FindOneAndModifyOptions must not be null");
                }
                nativeFindOneAndDelete(type, nativePtr, encodedFilter, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                break;
            default:
                throw new IllegalArgumentException("Invalid modify type: " + type);
        }

        return ResultHandler.handleResult(success, error);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private <T> T findSuccessMapper(@Nullable Object result, Class<T> resultClass) {
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
    private static native void nativeFindOne(int findOneType,
                                             long nativePtr,
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
    private static native void nativeDelete(int deleteType,
                                            long remoteMongoCollectionPtr,
                                            String document,
                                            OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeUpdate(int updateType,
                                            long remoteMongoCollectionPtr,
                                            String filter,
                                            String update,
                                            boolean upsert,
                                            OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndUpdate(int findOneAndUpdateType,
                                                      long remoteMongoCollectionPtr,
                                                      String filter,
                                                      String update,
                                                      String projection,
                                                      String sort,
                                                      boolean upsert,
                                                      boolean returnNewDocument,
                                                      OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndReplace(int findOneAndReplaceType,
                                                       long remoteMongoCollectionPtr,
                                                       String filter,
                                                       String update,
                                                       String projection,
                                                       String sort,
                                                       boolean upsert,
                                                       boolean returnNewDocument,
                                                       OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeFindOneAndDelete(int findOneAndDeleteType,
                                                      long remoteMongoCollectionPtr,
                                                      String filter,
                                                      String projection,
                                                      String sort,
                                                      boolean upsert,
                                                      boolean returnNewDocument,
                                                      OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
