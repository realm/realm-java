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

import org.bson.BsonValue;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.ObjectServerError;
import io.realm.internal.NativeObject;
import io.realm.internal.ResultHandler;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.mongodb.remote.RemoteCountOptions;
import io.realm.mongodb.remote.RemoteInsertOneResult;

public class OsRemoteMongoCollection<DocumentT> implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final Class<DocumentT> documentClass;

    public OsRemoteMongoCollection(final long nativeCollectionPtr, final Class<DocumentT> documentClass) {
        this.nativePtr = nativeCollectionPtr;
        this.documentClass = documentClass;
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

    public Long count() {
        return count(null);
    }

    public Long count(@Nullable final String filter) {
        return count(filter, null);
    }

    public Long count(@Nullable final String filter, @Nullable final RemoteCountOptions options) {
        AtomicReference<Long> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<Long> callback = new OsJNIResultCallback<Long>(success, error) {
            @Override
            protected Long mapSuccess(Object result) {
                return (long) result;
            }
        };

        // FIXME: change all filters/documents to BSON when the OS part is ready
        if (filter == null && options == null) {
            nativeCount(nativePtr, JSON, 0, callback);
        } else if (filter == null) {
            nativeCount(nativePtr, JSON, options.getLimit(), callback);
        } else if (options == null) {
            nativeCount(nativePtr, filter, 0, callback);
        } else {
            nativeCount(nativePtr, filter, options.getLimit(), callback);
        }

        return ResultHandler.handleResult(success, error);
    }

    public RemoteInsertOneResult insertOne(final DocumentT document) {
        AtomicReference<RemoteInsertOneResult> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<RemoteInsertOneResult> callback = new OsJNIResultCallback<RemoteInsertOneResult>(success, error) {
            @Override
            protected RemoteInsertOneResult mapSuccess(Object result) {
                BsonValue insertedId = (BsonValue) result;
                return new RemoteInsertOneResult(insertedId);
            }
        };

        // FIXME: change all filters/documents to BSON when the OS part is ready
        nativeInsertOne(nativePtr, document.toString(), callback);

        return ResultHandler.handleResult(success, error);
    }

    private final static String JSON = "{\"breed\":\"king charles\"}";

    // FIXME: change all filters/documents to BSON when the OS part is ready
    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeCount(long remoteMongoCollectionPtr,
                                           String filter,
                                           long limit,
                                           OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeInsertOne(long remoteMongoCollectionPtr,
                                               String filter,
                                               OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
