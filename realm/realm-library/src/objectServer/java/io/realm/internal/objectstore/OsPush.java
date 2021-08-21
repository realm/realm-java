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
import io.realm.internal.network.NetworkRequest;
import io.realm.internal.network.VoidNetworkRequest;

public class OsPush implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    private final long nativePtr;
    private final OsSyncUser osSyncUser;
    private final String serviceName;

    public OsPush(final OsApp osApp, final OsSyncUser osSyncUser, final String serviceName) {
        this.nativePtr = nativeCreate(osApp.getNativePtr(), serviceName);
        this.osSyncUser = osSyncUser;
        this.serviceName = serviceName;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public void registerDevice(String registrationToken) {
        new VoidNetworkRequest() {
            @Override
            protected void execute(NetworkRequest<Void> callback) {
                nativeRegisterDevice(nativePtr, osSyncUser.getNativePtr(), serviceName, registrationToken, callback);
            }
        }.run();
    }

    public void deregisterDevice() {
        new VoidNetworkRequest() {
            @Override
            protected void execute(NetworkRequest<Void> callback) {
                nativeDeregisterDevice(nativePtr, osSyncUser.getNativePtr(), serviceName, callback);
            }
        }.run();
    }

    private static native long nativeCreate(long nativeAppPtr, String serviceName);
    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeRegisterDevice(long nativePtr, long nativeUserPtr, String serviceName, String registrationToken, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
    private static native void nativeDeregisterDevice(long nativePtr, long nativeUserPtr, String serviceName, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
