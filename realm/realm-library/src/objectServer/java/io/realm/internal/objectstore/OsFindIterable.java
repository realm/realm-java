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
import org.bson.conversions.Bson;

import javax.annotation.Nullable;

import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.mongodb.mongo.options.FindOptions;

public class OsFindIterable<ResultT> extends OsMongoIterable<ResultT> {

    private static final int FIND = 1;
    private static final int FIND_WITH_OPTIONS = 2;

    private final FindOptions options;
    private final String encodedEmptyDocument;
    private Bson filter;

    OsFindIterable(final OsMongoCollection osMongoCollection,
                   final CodecRegistry codecRegistry,
                   final Class<ResultT> resultClass,
                   final Bson filter) {
        super(osMongoCollection, codecRegistry, resultClass);
        this.filter = filter;
        this.options = new FindOptions();
        this.encodedEmptyDocument = JniBsonProtocol.encode(new Document(), codecRegistry);
    }

    @Override
    void callNative(OsJNIResultCallback callback) {
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

    public void filter(@Nullable final Bson filter) {
        this.filter = filter;
    }

    public void limit(int limit) {
        this.options.limit(limit);
    }

    public void projection(@Nullable final Bson projection) {
        this.options.projection(projection);
    }

    public void sort(@Nullable final Bson sort) {
        this.options.sort(sort);
    }

    private static native void nativeFind(int findType,
                                          long remoteMongoCollectionPtr,
                                          String filter,
                                          String projection,
                                          String sort,
                                          long limit,
                                          OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
