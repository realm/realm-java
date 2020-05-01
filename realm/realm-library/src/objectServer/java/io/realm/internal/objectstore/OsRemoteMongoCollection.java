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

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.ObjectServerError;
import io.realm.internal.NativeObject;
import io.realm.internal.ResultHandler;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.mongodb.remote.RemoteCountOptions;

public class OsRemoteMongoCollection implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;

    public OsRemoteMongoCollection(long nativeCollectionPtr) {
        this.nativePtr = nativeCollectionPtr;
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

    public Long count(@Nullable String filter) {
        return count(filter, null);
    }

    public Long count(@Nullable String filter, @Nullable RemoteCountOptions options) {
        AtomicReference<Long> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<Long> callback = new OsJNIResultCallback<Long>(success, error) {
            @Override
            protected Long mapSuccess(Object result) {
                return (long) result;
            }
        };

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

    private final static String JSON = "{\"breed\":\"king charles\"}";

    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeCount(long remoteMongoCollectionPtr,
                                           String filter,
                                           long limit,
                                           OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
