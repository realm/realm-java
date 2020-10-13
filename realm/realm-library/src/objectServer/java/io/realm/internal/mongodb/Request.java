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

package io.realm.internal.mongodb;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import io.realm.RealmAsyncTask;
import io.realm.internal.RealmNotifier;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.log.RealmLog;
import io.realm.mongodb.App;
import io.realm.mongodb.ErrorCode;
import io.realm.mongodb.AppException;

// Class wrapping requests made against MongoDB Realm. Is also responsible for calling with success/error on the
// correct thread.
public abstract class Request<T> {
    @Nullable
    private final App.Callback<T> callback;
    private final RealmNotifier handler;
    private final ThreadPoolExecutor networkPoolExecutor;

    public Request(ThreadPoolExecutor networkPoolExecutor, @Nullable App.Callback<T> callback) {
        this.callback = callback;
        this.handler = new AndroidRealmNotifier(null, new AndroidCapabilities());
        this.networkPoolExecutor = networkPoolExecutor;
    }

    // Implements the request. Return the current sync user if the request succeeded. Otherwise throw an error.
    public abstract T run() throws AppException;

    // Start the request
    public RealmAsyncTask start() {
        Future<?> authenticateRequest = networkPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    postSuccess(Request.this.run());
                } catch (AppException e) {
                    postError(e);
                } catch (Throwable e) {
                    postError(new AppException(ErrorCode.UNKNOWN, "Unexpected error", e));
                }
            }
        });
        return new RealmAsyncTaskImpl(authenticateRequest, networkPoolExecutor);
    }

    private void postError(final AppException error) {
        boolean errorHandled = false;
        if (callback != null) {
            Runnable action = new Runnable() {
                @Override
                public void run() {
                    callback.onResult(App.Result.withError(error));
                }
            };
            errorHandled = handler.post(action);
        }

        if (!errorHandled) {
            RealmLog.error(error, "An error was thrown, but could not be posted: \n" + error.toString());
        }
    }

    private void postSuccess(final T result) {
        if (callback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onResult((result == null) ? App.Result.success() : App.Result.withResult(result));
                }
            });
        }
    }
}
