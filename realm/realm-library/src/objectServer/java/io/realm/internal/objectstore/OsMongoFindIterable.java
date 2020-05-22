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

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;

import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.mongodb.mongo.options.FindOptions;

public class OsMongoFindIterable<ResultT> extends OsMongoIterable<ResultT> {

    @Nullable
    private final FindOptions options;
    private final Bson filter;

    OsMongoFindIterable(final OsMongoCollection osMongoCollection,
                        final CodecRegistry codecRegistry,
                        final Class<ResultT> resultClass,
                        final Bson filter,
                        @Nullable final FindOptions options) {
        super(osMongoCollection, codecRegistry, resultClass);
        this.filter = filter;
        this.options = options;
    }

    @Override
    void callNative(OsJNIResultCallback callback) {
        String filterString = JniBsonProtocol.encode(filter, codecRegistry);

        if (options == null) {
            nativeFind(osMongoCollection.getNativePtr(), filterString, callback);
        } else {
            String projectionString = JniBsonProtocol.encode(options.getProjection(), codecRegistry);
            String sortString = JniBsonProtocol.encode(options.getSort(), codecRegistry);

            nativeFindWithOptions(osMongoCollection.getNativePtr(), filterString, projectionString, sortString, options.getLimit(), callback);
        }
    }

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
