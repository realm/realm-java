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

import io.realm.annotations.Beta;
import io.realm.internal.common.TaskDispatcher;
import io.realm.internal.objectstore.OsPush;

/**
 * The Push client allows to register/deregister for push notifications from a client app.
 */
@Beta
public abstract class Push {

    private final TaskDispatcher dispatcher;
    private final OsPush osPush;

    public Push(final OsPush osPush, TaskDispatcher dispatcher) {
        this.osPush = osPush;
        this.dispatcher = dispatcher;
    }

    // TODO: Task vs RealmAsyncTask - https://github.com/realm/realm-java/issues/6914
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
            public Void call() {
                osPush.registerDevice(registrationToken, serviceName);
                return null;
            }
        });
    }

    // TODO: Task vs RealmAsyncTask - https://github.com/realm/realm-java/issues/6914
    /**
     * Deregisters the FCM registration token bound to the currently logged in user's
     * device on MongoDB Realm.
     *
     * @return A {@link Task} that completes when the deregistration is finished.
     */
    public Task<Void> deregisterDevice(String registrationToken, String serviceName) {
        return dispatcher.dispatchTask(new Callable<Void>() {
            @Override
            public Void call() {
                osPush.deregisterDevice(registrationToken, serviceName);
                return null;
            }
        });
    }
}
