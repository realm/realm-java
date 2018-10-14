/*
 * Copyright 2015 Realm Inc.
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

package io.realm;

/**
 * Represents a pending asynchronous Realm task, like asynchronous transactions.
 * <p>
 * Users are responsible for maintaining a reference to {@code RealmAsyncTask} in order to call {@link #cancel()} in
 * case of a configuration change for example (to avoid memory leak, as the transaction will post the result to the
 * caller's thread callback).
 */
public interface RealmAsyncTask {

    /**
     * Attempts to cancel execution of this transaction (if it hasn't already completed or previously cancelled).
     */
    void cancel();

    /**
     * Checks whether an attempt to cancel the transaction was performed.
     *
     * @return {@code true} if {@link #cancel()} has already been called, {@code false} otherwise.
     */
    boolean isCancelled();
}

