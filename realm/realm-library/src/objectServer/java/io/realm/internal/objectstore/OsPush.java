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

import io.realm.internal.NativeObject;
import io.realm.internal.jni.OsJNIVoidResultCallback;
import io.realm.internal.network.ResultHandler;
import io.realm.mongodb.AppException;

public class OsPush implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;

    public OsPush(final long appNativePtr) {
        this.nativePtr = appNativePtr;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public void registerDevice(String registrationToken, String serviceName) {
        AtomicReference<AppException> error = new AtomicReference<>(null);
        nativeRegisterDevice(nativePtr, serviceName, registrationToken, new OsJNIVoidResultCallback(error));
        ResultHandler.handleResult(null, error);
    }

    public void deregisterDevice(String registrationToken, String serviceName) {
        AtomicReference<AppException> error = new AtomicReference<>(null);
        nativeDeregisterDevice(nativePtr, serviceName, registrationToken, new OsJNIVoidResultCallback(error));
        ResultHandler.handleResult(null, error);
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeRegisterDevice(long nativeAppPtr, String serviceName, String registrationToken, OsJNIVoidResultCallback callback);
    private static native void nativeDeregisterDevice(long nativeAppPtr, String serviceName, String registrationToken, OsJNIVoidResultCallback callback);
}
