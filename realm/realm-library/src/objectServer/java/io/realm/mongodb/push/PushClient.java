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
package io.realm.mongodb.push;

import com.google.android.gms.tasks.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.annotations.Beta;
import io.realm.internal.common.TaskDispatcher;
import io.realm.internal.jni.OsJNIVoidResultCallback;
import io.realm.internal.network.ResultHandler;
import io.realm.mongodb.AppException;

/**
 * The PushClient allows to register/deregister for push notifications from a client app.
 */
@Beta
public class PushClient {

    private final long appNativePtr;
    private final TaskDispatcher dispatcher;

    public PushClient(final long appNativePtr, final TaskDispatcher dispatcher) {
        this.appNativePtr = appNativePtr;
        this.dispatcher = dispatcher;
    }

    /**
     * Registers the given FCM registration token with the currently logged in user's
     * device on MongoDB Realm.
     *
     * @param registrationToken the registration token to register.
     * @return A {@link Task} that completes when the registration is finished.
     */
    public Task<Void> registerDevice(String registrationToken, String serviceName) {
        return dispatcher.dispatchTask(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AtomicReference<AppException> error = new AtomicReference<>(null);
                nativeRegisterDevice(appNativePtr, serviceName, registrationToken, new OsJNIVoidResultCallback(error));
                ResultHandler.handleResult(null, error);
                return null;
            }
        });
    }

    /**
     * Deregisters the FCM registration token bound to the currently logged in user's
     * device on MongoDB Realm.
     *
     * @return A {@link Task} that completes when the deregistration is finished.
     */
    public Task<Void> deregisterDevice(String registrationToken, String serviceName) {
        return dispatcher.dispatchTask(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AtomicReference<AppException> error = new AtomicReference<>(null);
                nativeDeregisterDevice(appNativePtr, serviceName, registrationToken, new OsJNIVoidResultCallback(error));
                ResultHandler.handleResult(null, error);
                return null;
            }
        });
    }

    private static native void nativeRegisterDevice(long nativeAppPtr, String serviceName, String registrationToken, OsJNIVoidResultCallback callback);
    private static native void nativeDeregisterDevice(long nativeAppPtr, String serviceName, String registrationToken, OsJNIVoidResultCallback callback);
}
