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

import io.realm.internal.NativeObject;

public class OsRemoteMongoCollection implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;

    OsRemoteMongoCollection(long nativeCollectionPtr) {
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

    public void count(String filter) {
        // TODO: move callbacks outside RealmApp
//        nativeCount(nativePtr,
//                new OsJavaNetworkTransport.NetworkTransportJNIResultCallback() {
//                    @Override
//                    public void onSuccess(Object result) {
//                        super.onSuccess(result);
//                    }
//
//                    @Override
//                    public void onError(String nativeErrorCategory, int nativeErrorCode, String errorMessage) {
//                        super.onError(nativeErrorCategory, nativeErrorCode, errorMessage);
//                    }
//                },
//                filter);
    }

    private static native long nativeCreate();
    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeCount(long remoteMongoCollectionPtr,
                                           OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback,
                                           String filter);
}
