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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.ObjectServerError;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.network.ResultHandler;

public abstract class OsMongoIterable<ResultT> {

    final OsMongoCollection osMongoCollection;
    final CodecRegistry codecRegistry;

    private final Class<ResultT> resultClass;

    OsMongoIterable(final OsMongoCollection osMongoCollection,
                    final CodecRegistry codecRegistry,
                    final Class<ResultT> resultClass) {
        this.osMongoCollection = osMongoCollection;
        this.codecRegistry = codecRegistry;
        this.resultClass = resultClass;
    }

    abstract void callNative(final OsJNIResultCallback callback);

    public Collection<ResultT> getCollection() {
        AtomicReference<Collection<ResultT>> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<Collection<ResultT>> callback = new OsJNIResultCallback<Collection<ResultT>>(success, error) {
            @Override
            protected Collection<ResultT> mapSuccess(Object result) {
                return mapCollection(result);
            }
        };

        callNative(callback);

        return ResultHandler.handleResult(success, error);
    }

    public ResultT first() {
        AtomicReference<ResultT> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<ResultT> callback = new OsJNIResultCallback<ResultT>(success, error) {
            @Override
            protected ResultT mapSuccess(Object result) {
                Collection<ResultT> decodedCollection = mapCollection(result);
                Iterator<ResultT> iter = decodedCollection.iterator();
                return iter.hasNext() ? iter.next() : null;
            }
        };

        callNative(callback);

        return ResultHandler.handleResult(success, error);
    }

    private Collection<ResultT> mapCollection(Object result) {
        Collection<?> collection = JniBsonProtocol.decode((String) result, Collection.class, codecRegistry);
        Collection<ResultT> decodedCollection = new ArrayList<>();
        for (Object collectionElement: collection) {
            String encodedElement = JniBsonProtocol.encode(collectionElement, codecRegistry);
            decodedCollection.add(JniBsonProtocol.decode(encodedElement, resultClass, codecRegistry));
        }
        return decodedCollection;
    }
}
