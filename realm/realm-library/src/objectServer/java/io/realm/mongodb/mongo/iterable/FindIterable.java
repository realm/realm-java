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

package io.realm.mongodb.mongo.iterable;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsMongoCollection;
import io.realm.mongodb.mongo.options.FindOptions;

/**
 * Specific iterable for {@link io.realm.mongodb.mongo.MongoCollection#find()} operations.
 *
 * @param <ResultT> The type to which this iterable will decode documents.
 */
public class FindIterable<ResultT> extends MongoIterable<ResultT> {

    private static final int FIND = 1;
    private static final int FIND_WITH_OPTIONS = 2;

    private final FindOptions options;
    private final String encodedEmptyDocument;

    private Bson filter;

    public FindIterable(final ThreadPoolExecutor threadPoolExecutor,
                        final OsMongoCollection<?> osMongoCollection,
                        final CodecRegistry codecRegistry,
                        final Class<ResultT> resultClass) {
        super(threadPoolExecutor, osMongoCollection, codecRegistry, resultClass);
        this.options = new FindOptions();
        this.filter = new Document();
        this.encodedEmptyDocument = JniBsonProtocol.encode(new Document(), codecRegistry);
    }

    @Override
    void callNative(final OsJNIResultCallback<?> callback) {
        String filterString = JniBsonProtocol.encode(filter, codecRegistry);
        String projectionString = encodedEmptyDocument;
        String sortString = encodedEmptyDocument;

        if (options == null) {
            nativeFind(FIND, osMongoCollection.getNativePtr(), filterString, projectionString, sortString, 0, callback);
        } else {
            projectionString = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
            sortString = JniBsonProtocol.encode(options.getSort(), codecRegistry);

            nativeFind(FIND_WITH_OPTIONS, osMongoCollection.getNativePtr(), filterString, projectionString, sortString, options.getLimit(), callback);
        }
    }

    /**
     * Sets the query filter to apply to the query.
     *
     * @param filter the filter, which may be null.
     * @return this
     */
    public FindIterable<ResultT> filter(@Nullable final Bson filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets the limit to apply.
     *
     * @param limit the limit, which may be 0
     * @return this
     */
    public FindIterable<ResultT> limit(int limit) {
        this.options.limit(limit);
        return this;
    }

    /**
     * Sets a document describing the fields to return for all matching documents.
     *
     * @param projection the project document, which may be null.
     * @return this
     */
    public FindIterable<ResultT> projection(@Nullable final Bson projection) {
        this.options.projection(projection);
        return this;
    }

    /**
     * Sets the sort criteria to apply to the query.
     *
     * @param sort the sort criteria, which may be null.
     * @return this
     */
    public FindIterable<ResultT> sort(@Nullable final Bson sort) {
        this.options.sort(sort);
        return this;
    }

    private static native void nativeFind(int findType,
                                          long remoteMongoCollectionPtr,
                                          String filter,
                                          String projection,
                                          String sort,
                                          long limit,
                                          OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
