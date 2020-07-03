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

import javax.annotation.Nullable;

import io.realm.annotations.Beta;
import io.realm.internal.async.RealmResultTaskImpl;
import io.realm.internal.objectstore.OsPush;
import io.realm.mongodb.App;
import io.realm.mongodb.RealmResultTask;

/**
 * The Push client allows to register/deregister for push notifications from a client app.
 */
@Beta
public abstract class Push {

    private final OsPush osPush;

    protected Push(final OsPush osPush) {
        this.osPush = osPush;
    }

    /**
     * Registers the given FCM registration token with the currently logged in user's
     * device on MongoDB Realm.
     *
     * @param registrationToken The registration token to register.
     * @return a {@link RealmResultTask} that carries out the registration.
     */
    public RealmResultTask<Void> registerDevice(String registrationToken) {
        return new RealmResultTaskImpl<>(App.NETWORK_POOL_EXECUTOR, new RealmResultTaskImpl.Executor<Void>() {
            @Nullable
            @Override
            public Void run() {
                osPush.registerDevice(registrationToken);
                return null;
            }
        });
    }

    /**
     * Deregisters the FCM registration token bound to the currently logged in user's
     * device on MongoDB Realm.
     *
     * @return a {@link RealmResultTask} that carries out the deregistration.
     */
    public RealmResultTask<Void> deregisterDevice() {
        return new RealmResultTaskImpl<>(App.NETWORK_POOL_EXECUTOR, new RealmResultTaskImpl.Executor<Void>() {
            @Nullable
            @Override
            public Void run() {
                osPush.deregisterDevice();
                return null;
            }
        });
    }
}
