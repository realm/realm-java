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
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.internal.NativeObject;
import io.realm.internal.Util;
import io.realm.internal.events.NetworkEventStream;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.network.ResultHandler;
import io.realm.internal.network.StreamNetworkTransport;
import io.realm.internal.objectserver.EventStream;
import io.realm.mongodb.App;
import io.realm.mongodb.AppException;
import io.realm.mongodb.mongo.MongoNamespace;
import io.realm.mongodb.mongo.iterable.AggregateIterable;
import io.realm.mongodb.mongo.iterable.FindIterable;
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
    private static final int WATCH = 15;
    private static final int WATCH_IDS = 16;
    private static final int WATCH_WITH_FILTER= 17;


    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final Class<DocumentT> documentClass;
    private final CodecRegistry codecRegistry;
    private final String encodedEmptyDocument;
    private final ThreadPoolExecutor threadPoolExecutor = App.NETWORK_POOL_EXECUTOR;
    private final String serviceName;
    private final MongoNamespace namespace;
    private final StreamNetworkTransport streamNetworkTransport;

    OsMongoCollection(final long nativeCollectionPtr,
                      final MongoNamespace namespace,
                      final String serviceName,
                      final Class<DocumentT> documentClass,
                      final CodecRegistry codecRegistry,
                      final StreamNetworkTransport streamNetworkTransport) {
        this.nativePtr = nativeCollectionPtr;
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.documentClass = documentClass;
        this.codecRegistry = codecRegistry;
        this.encodedEmptyDocument = JniBsonProtocol.encode(new Document(), codecRegistry);
        this.streamNetworkTransport = streamNetworkTransport;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public Class<DocumentT> getDocumentClass() {
        return documentClass;
    }

    public CodecRegistry getCodecRegistry() {
        return codecRegistry;
    }

    public <NewDocumentT> OsMongoCollection<NewDocumentT> withDocumentClass(
            final Class<NewDocumentT> clazz) {
        return new OsMongoCollection<>(nativePtr, namespace, serviceName, clazz, codecRegistry, streamNetworkTransport);
    }

    public OsMongoCollection<DocumentT> withCodecRegistry(final CodecRegistry codecRegistry) {
        return new OsMongoCollection<>(nativePtr, namespace, serviceName, documentClass, codecRegistry, streamNetworkTransport);
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
        AtomicReference<AppException> error = new AtomicReference<>(null);
        OsJNIResultCallback<Long> callback = new OsJNIResultCallback<Long>(success, error) {
            @Override
            protected Long mapSuccess(Object result) {
                return (Long) result;
            }
        };

        final String filterString = JniBsonProtocol.encode(filter, codecRegistry);
        final int limit = (options == null) ? 0 : options.getLimit();

        nativeCount(nativePtr, filterString, limit, callback);

        return ResultHandler.handleResult(success, error);
    }

    public FindIterable<DocumentT> find() {
        return findInternal(new Document(), documentClass, null);
    }

    public FindIterable<DocumentT> find(final FindOptions options) {
        return findInternal(new Document(), documentClass, options);
    }

    public <ResultT> FindIterable<ResultT> find(final Class<ResultT> resultClass) {
        return findInternal(new Document(), resultClass, null);
    }

    public <ResultT> FindIterable<ResultT> find(final Class<ResultT> resultClass, final FindOptions options) {
        return findInternal(new Document(), resultClass, options);
    }

    public FindIterable<DocumentT> find(final Bson filter) {
        return findInternal(filter, documentClass, null);
    }

    public FindIterable<DocumentT> find(final Bson filter, final FindOptions options) {
        return findInternal(filter, documentClass, options);
    }

    public <ResultT> FindIterable<ResultT> find(final Bson filter,
                                                final Class<ResultT> resultClass) {
        return findInternal(filter, resultClass, null);
    }

    public <ResultT> FindIterable<ResultT> find(final Bson filter,
                                                final Class<ResultT> resultClass,
                                                final FindOptions options) {
        return findInternal(filter, resultClass, options);
    }

    private <ResultT> FindIterable<ResultT> findInternal(final Bson filter,
                                                         final Class<ResultT> resultClass,
                                                         @Nullable final FindOptions options) {
        FindIterable<ResultT> findIterable =
                new FindIterable<>(threadPoolExecutor, this, codecRegistry, resultClass);
        findIterable.filter(filter);

        if (options != null) {
            findIterable.limit(options.getLimit());
            findIterable.projection(options.getProjection());
        }
        return findIterable;
    }

    public AggregateIterable<DocumentT> aggregate(final List<? extends Bson> pipeline) {
        return aggregate(pipeline, documentClass);
    }

    public <ResultT> AggregateIterable<ResultT> aggregate(final List<? extends Bson> pipeline,
                                                          final Class<ResultT> resultClass) {
        return new AggregateIterable<>(threadPoolExecutor, this, codecRegistry, resultClass, pipeline);
    }

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
        AtomicReference<AppException> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                return findSuccessMapper(result, resultClass);
            }
        };

        final String encodedFilter = JniBsonProtocol.encode(filter, codecRegistry);

        // default to empty docs or update if needed
        String projectionString = encodedEmptyDocument;
        String sortString = encodedEmptyDocument;

        switch (type) {
            case FIND_ONE:
                nativeFindOne(FIND_ONE, nativePtr, encodedFilter, projectionString, sortString, 0, callback);
                break;
            case FIND_ONE_WITH_OPTIONS:
                Util.checkNull(options, "options");
                projectionString = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
                sortString = JniBsonProtocol.encode(options.getSort(), codecRegistry);

                nativeFindOne(FIND_ONE_WITH_OPTIONS, nativePtr, encodedFilter, projectionString, sortString, options.getLimit(), callback);
                break;
            default:
                throw new IllegalArgumentException("Invalid fineOne type: " + type);
        }

        return ResultHandler.handleResult(success, error);
    }

    public InsertOneResult insertOne(final DocumentT document) {
        AtomicReference<InsertOneResult> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
        OsJNIResultCallback<InsertOneResult> callback = new OsJNIResultCallback<InsertOneResult>(success, error) {
            @Override
            protected InsertOneResult mapSuccess(Object result) {
                BsonValue bsonObjectId = new BsonObjectId((ObjectId) result);
                return new InsertOneResult(bsonObjectId);
            }
        };

        final String encodedDocument = JniBsonProtocol.encode(document, codecRegistry);
        nativeInsertOne(nativePtr, encodedDocument, callback);
        return ResultHandler.handleResult(success, error);
    }

    public InsertManyResult insertMany(final List<? extends DocumentT> documents) {
        AtomicReference<InsertManyResult> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
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

        final String encodedDocumentArray = JniBsonProtocol.encode(documents, codecRegistry);
        nativeInsertMany(nativePtr, encodedDocumentArray, callback);
        return ResultHandler.handleResult(success, error);
    }

    public DeleteResult deleteOne(final Bson filter) {
        return deleteInternal(DELETE_ONE, filter);
    }

    public DeleteResult deleteMany(final Bson filter) {
        return deleteInternal(DELETE_MANY, filter);
    }

    private DeleteResult deleteInternal(final int type, final Bson filter) {
        AtomicReference<DeleteResult> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
        OsJNIResultCallback<DeleteResult> callback = new OsJNIResultCallback<DeleteResult>(success, error) {
            @Override
            protected DeleteResult mapSuccess(Object result) {
                return new DeleteResult((Long) result);
            }
        };

        final String jsonDocument = JniBsonProtocol.encode(filter, codecRegistry);
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

    public UpdateResult updateOne(final Bson filter, final Bson update) {
        return updateInternal(UPDATE_ONE, filter, update, null);
    }

    public UpdateResult updateOne(final Bson filter,
                                  final Bson update,
                                  final UpdateOptions options) {
        return updateInternal(UPDATE_ONE_WITH_OPTIONS, filter, update, options);
    }

    public UpdateResult updateMany(final Bson filter, final Bson update) {
        return updateInternal(UPDATE_MANY, filter, update, null);
    }

    public UpdateResult updateMany(final Bson filter,
                                   final Bson update,
                                   final UpdateOptions options) {
        return updateInternal(UPDATE_MANY_WITH_OPTIONS, filter, update, options);
    }

    private UpdateResult updateInternal(final int type,
                                        final Bson filter,
                                        final Bson update,
                                        @Nullable final UpdateOptions options) {
        AtomicReference<UpdateResult> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
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

        final String jsonFilter = JniBsonProtocol.encode(filter, codecRegistry);
        final String jsonUpdate = JniBsonProtocol.encode(update, codecRegistry);

        switch (type) {
            case UPDATE_ONE:
            case UPDATE_MANY:
                nativeUpdate(type, nativePtr, jsonFilter, jsonUpdate, false, callback);
                break;
            case UPDATE_ONE_WITH_OPTIONS:
            case UPDATE_MANY_WITH_OPTIONS:
                Util.checkNull(options, "options");
                nativeUpdate(type, nativePtr, jsonFilter, jsonUpdate, options.isUpsert(), callback);
                break;
            default:
                throw new IllegalArgumentException("Invalid update type: " + type);
        }
        return ResultHandler.handleResult(success, error);
    }

    public DocumentT findOneAndUpdate(final Bson filter, final Bson update) {
        return findOneAndUpdate(filter, update, documentClass);
    }

    public <ResultT> ResultT findOneAndUpdate(final Bson filter,
                                              final Bson update,
                                              final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_UPDATE, filter, update, null, resultClass);
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
        return findOneAndModify(FIND_ONE_AND_UPDATE_WITH_OPTIONS, filter, update, options, resultClass);
    }

    public DocumentT findOneAndReplace(final Bson filter, final Bson replacement) {
        return findOneAndReplace(filter, replacement, documentClass);
    }

    public <ResultT> ResultT findOneAndReplace(final Bson filter,
                                               final Bson replacement,
                                               final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_REPLACE, filter, replacement, null, resultClass);
    }

    public DocumentT findOneAndReplace(final Bson filter,
                                       final Bson replacement,
                                       final FindOneAndModifyOptions options) {
        return findOneAndReplace(filter, replacement, options, documentClass);
    }

    public <ResultT> ResultT findOneAndReplace(final Bson filter,
                                               final Bson replacement,
                                               final FindOneAndModifyOptions options,
                                               final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_REPLACE_WITH_OPTIONS, filter, replacement, options, resultClass);
    }

    public DocumentT findOneAndDelete(final Bson filter) {
        return findOneAndDelete(filter, documentClass);
    }

    public <ResultT> ResultT findOneAndDelete(final Bson filter,
                                              final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_DELETE, filter, new Document(), null, resultClass);
    }

    public DocumentT findOneAndDelete(final Bson filter,
                                      final FindOneAndModifyOptions options) {
        return findOneAndDelete(filter, options, documentClass);
    }

    public <ResultT> ResultT findOneAndDelete(final Bson filter,
                                              final FindOneAndModifyOptions options,
                                              final Class<ResultT> resultClass) {
        return findOneAndModify(FIND_ONE_AND_DELETE_WITH_OPTIONS, filter, new Document(), options, resultClass);
    }

    public String getServiceName() {
        return serviceName;
    }

    private <ResultT> ResultT findOneAndModify(final int type,
                                               final Bson filter,
                                               final Bson update,
                                               @Nullable final FindOneAndModifyOptions options,
                                               final Class<ResultT> resultClass) {
        AtomicReference<ResultT> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                return findSuccessMapper(result, resultClass);
            }
        };

        final String encodedFilter = JniBsonProtocol.encode(filter, codecRegistry);
        final String encodedUpdate = JniBsonProtocol.encode(update, codecRegistry);

        // default to empty docs or update if needed
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
                Util.checkNull(options, "options");
                nativeFindOneAndUpdate(type, nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                break;
            case FIND_ONE_AND_REPLACE:
                nativeFindOneAndReplace(type, nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, false, false, callback);
                break;
            case FIND_ONE_AND_REPLACE_WITH_OPTIONS:
                Util.checkNull(options, "options");
                nativeFindOneAndReplace(type, nativePtr, encodedFilter, encodedUpdate, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                break;
            case FIND_ONE_AND_DELETE:
                nativeFindOneAndDelete(type, nativePtr, encodedFilter, encodedProjection, encodedSort, false, false, callback);
                break;
            case FIND_ONE_AND_DELETE_WITH_OPTIONS:
                Util.checkNull(options, "options");
                nativeFindOneAndDelete(type, nativePtr, encodedFilter, encodedProjection, encodedSort, options.isUpsert(), options.isReturnNewDocument(), callback);
                break;
            default:
                throw new IllegalArgumentException("Invalid modify type: " + type);
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

    private EventStream<DocumentT> watchInternal(int type, @Nullable List<?> ids, @Nullable BsonDocument matchFilter) throws IOException {
        List<Document> args = new ArrayList<>();

        Document watchArgs = new Document("database", namespace.getDatabaseName());
        watchArgs.put("collection", namespace.getCollectionName());

        switch (type) {
            case WATCH:
                break;
            case WATCH_IDS:
                watchArgs.put("ids", ids);
                break;
            case WATCH_WITH_FILTER:
                watchArgs.put("filter", matchFilter);
                break;
            default:
                throw new IllegalArgumentException("Invalid watch type: " + type);
        }

        args.add(watchArgs);

        String encodedArguments = JniBsonProtocol.encode(args, codecRegistry);

        OsJavaNetworkTransport.Request request = streamNetworkTransport.makeStreamingRequest("watch", encodedArguments, serviceName);
        OsJavaNetworkTransport.Response response = streamNetworkTransport.sendRequest(request);

        return new NetworkEventStream<>(response, codecRegistry, documentClass);
    }

    public EventStream<DocumentT> watch() throws IOException {
        return watchInternal(WATCH, null, null);
    }

    public EventStream<DocumentT> watch(final List<?> ids) throws IOException {
        return watchInternal(WATCH_IDS, ids, null);
    }

    public EventStream<DocumentT> watchWithFilter(Document matchFilter) throws IOException {
        return watchInternal(WATCH_WITH_FILTER, null, matchFilter.toBsonDocument(getDocumentClass(), getCodecRegistry()));
    }

    public EventStream<DocumentT> watchWithFilter(BsonDocument matchFilter) throws IOException {
        return watchInternal(WATCH_WITH_FILTER, null, matchFilter);
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
