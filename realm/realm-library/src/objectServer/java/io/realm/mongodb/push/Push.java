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

import io.realm.RealmAsyncTask;
import io.realm.annotations.Beta;
import io.realm.internal.Util;
import io.realm.internal.mongodb.Request;
import io.realm.internal.objectstore.OsPush;
import io.realm.mongodb.App;
import io.realm.mongodb.AppException;

/**
 * The Push client allows to register/deregister for push notifications from a client app.
 */
@Beta
public abstract class Push {

    private final OsPush osPush;

    public Push(final OsPush osPush) {
        this.osPush = osPush;
    }

    /**
     * Registers the given FCM registration token with the currently logged in user's
     * device on MongoDB Realm.
     *
     * @param registrationToken The registration token to register.
     */
    public void registerDevice(String registrationToken) {
        osPush.registerDevice(registrationToken);
    }

    /**
     * Registers the given FCM registration token with the currently logged in user's
     * device on MongoDB Realm.
     *
     * @param registrationToken The registration token to register.
     * @param callback          The callback used when the device has been registered or the call
     *                          failed - it will always happen on the same thread as this method was
     *                          called on.
     */
    public RealmAsyncTask registerDeviceAsync(String registrationToken,
                                              App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous registering a device is only possible from looper threads.");
        return new Request<Void>(App.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                osPush.registerDevice(registrationToken);
                return null;
            }
        }.start();
    }

    /**
     * Deregisters the FCM registration token bound to the currently logged in user's
     * device on MongoDB Realm.
     */
    public void deregisterDevice() {
        osPush.deregisterDevice();
    }

    /**
     * Deregisters the FCM registration token bound to the currently logged in user's
     * device on MongoDB Realm.
     *
     * @param callback The callback used when the device has been registered or the call
     *                 failed - it will always happen on the same thread as this method was
     *                 called on.
     */
    public RealmAsyncTask deregisterDeviceAsync(App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous deregistering a device is only possible from looper threads.");
        return new Request<Void>(App.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                osPush.deregisterDevice();
                return null;
            }
        }.start();
    }
}
