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

package io.realm.mongodb;

import io.realm.RealmAsyncTask;

/**
 * The RealmResultTask is a specific version of {@link RealmAsyncTask} that provides a mechanism
 * to work with asynchronous operations carried out against MongoDB Realm that yield a result.
 * <p>
 * This class offers both blocking ({@code get}) and non-blocking ({@code getAsync}) method calls.
 *
 * @param <T> the result type delivered by this task.
 */
public interface RealmResultTask<T> extends RealmAsyncTask {

    /**
     * Blocks the thread on which the call is made until the result of the operation arrives.
     *
     * @return the result of the operation executed by this task.
     */
    T get();

    /**
     * Provides a way to subscribe to asynchronous operations via a callback, which handles both
     * results and errors.
     *
     * @param callback the {@link App.Callback} designed to receive results.
     * @throws IllegalStateException if called from a thread without a {@link android.os.Looper} or
     *                               from an {@link android.app.IntentService} thread.
     */
    void getAsync(App.Callback<T> callback);
}

