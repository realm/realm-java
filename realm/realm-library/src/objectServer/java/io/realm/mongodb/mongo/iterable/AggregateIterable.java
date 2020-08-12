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

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsMongoCollection;

/**
 * Specific iterable for {@link io.realm.mongodb.mongo.MongoCollection#aggregate(List)} operations.
 *
 * @param <ResultT> The type to which this iterable will decode documents.
 */
public class AggregateIterable<ResultT> extends MongoIterable<ResultT> {

    private List<? extends Bson> pipeline;

    public AggregateIterable(final ThreadPoolExecutor threadPoolExecutor,
                             final OsMongoCollection<?> osMongoCollection,
                             final CodecRegistry codecRegistry,
                             final Class<ResultT> resultClass,
                             final List<? extends Bson> pipeline) {
        super(threadPoolExecutor, osMongoCollection, codecRegistry, resultClass);
        this.pipeline = pipeline;
    }

    @Override
    void callNative(final OsJNIResultCallback<?> callback) {
        String pipelineString = JniBsonProtocol.encode(pipeline, codecRegistry);
        nativeAggregate(osMongoCollection.getNativePtr(), pipelineString, callback);
    }

    private static native void nativeAggregate(long remoteMongoCollectionPtr,
                                               String pipeline,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
