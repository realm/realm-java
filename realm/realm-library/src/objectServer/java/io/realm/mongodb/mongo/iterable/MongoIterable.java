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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.internal.async.RealmResultTaskImpl;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.network.ResultHandler;
import io.realm.internal.objectstore.OsMongoCollection;
import io.realm.mongodb.AppException;
import io.realm.mongodb.RealmResultTask;

/**
 * The MongoIterable is the results from an operation, such as a {@code find()} or an
 * {@code aggregate()} query.
 * <p>
 * This class somewhat mimics the behavior of an {@link Iterable} but given its results are
 * obtained asynchronously, its values are wrapped inside a {@code Task}.
 *
 * @param <ResultT> The type to which this iterable will decode documents.
 */
public abstract class MongoIterable<ResultT> {

    protected final OsMongoCollection<?> osMongoCollection;
    protected final CodecRegistry codecRegistry;

    private final Class<ResultT> resultClass;
    private final ThreadPoolExecutor threadPoolExecutor;

    MongoIterable(final ThreadPoolExecutor threadPoolExecutor,
                  final OsMongoCollection<?> osMongoCollection,
                  final CodecRegistry codecRegistry,
                  final Class<ResultT> resultClass) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.osMongoCollection = osMongoCollection;
        this.codecRegistry = codecRegistry;
        this.resultClass = resultClass;
    }

    abstract void callNative(OsJNIResultCallback<?> callback);

    /**
     * Returns a cursor of the operation represented by this iterable.
     * <p>
     * The result is wrapped in a {@code Task} since the iterator should be capable of
     * asynchronously retrieve documents from the server.
     *
     * @return an asynchronous task with cursor of the operation represented by this iterable.
     */
    public RealmResultTask<MongoCursor<ResultT>> iterator() {
        return new RealmResultTaskImpl<>(threadPoolExecutor, new RealmResultTaskImpl.Executor<MongoCursor<ResultT>>() {
            @Nullable
            @Override
            public MongoCursor<ResultT> run() {
                return new MongoCursor<>(MongoIterable.this.getCollection().iterator());
            }
        });
    }

    /**
     * Helper to return the first item in the iterator or null.
     * <p>
     * The result is wrapped in a {@code Task} since the iterator should be capable of
     * asynchronously retrieve documents from the server.
     *
     * @return a task containing the first item or null.
     */
    public RealmResultTask<ResultT> first() {
        final AtomicReference<ResultT> success = new AtomicReference<>(null);
        final AtomicReference<AppException> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                Collection<ResultT> decodedCollection = mapCollection(result);
                Iterator<ResultT> iter = decodedCollection.iterator();
                return iter.hasNext() ? iter.next() : null;
            }
        };

        callNative(callback);

        return new RealmResultTaskImpl<>(threadPoolExecutor, new RealmResultTaskImpl.Executor<ResultT>() {
            @Nullable
            @Override
            public ResultT run() {
                return ResultHandler.handleResult(success, error);
            }
        });
    }

    private Collection<ResultT> getCollection() {
        AtomicReference<Collection<ResultT>> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
        OsJNIResultCallback<Collection<ResultT>> callback = new OsJNIResultCallback<Collection<ResultT>>(success, error) {
            @Override
            protected Collection<ResultT> mapSuccess(Object result) {
                return mapCollection(result);
            }
        };

        callNative(callback);

        return ResultHandler.handleResult(success, error);
    }

    private Collection<ResultT> mapCollection(Object result) {
        Collection<?> collection = JniBsonProtocol.decode((String) result, Collection.class, codecRegistry);
        Collection<ResultT> decodedCollection = new ArrayList<>();
        for (Object collectionElement : collection) {
            String encodedElement = JniBsonProtocol.encode(collectionElement, codecRegistry);
            decodedCollection.add(JniBsonProtocol.decode(encodedElement, resultClass, codecRegistry));
        }
        return decodedCollection;
    }
}
