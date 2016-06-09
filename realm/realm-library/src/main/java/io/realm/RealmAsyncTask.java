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

import java.util.concurrent.Future;

/**
 * Represents a pending asynchronous Realm transaction.
 * <p>
 * Users are responsible for maintaining a reference to {@code RealmAsyncTask} in order to call {@link #cancel()} in
 * case of a configuration change for example (to avoid memory leak, as the transaction will post the result to the
 * caller's thread callback).
 */
public final class RealmAsyncTask {
    private final Future<?> pendingQuery;
    private volatile boolean isCancelled = false;

    RealmAsyncTask(Future<?> pendingQuery) {
        this.pendingQuery = pendingQuery;
    }

    /**
     * Attempts to cancel execution of this transaction (if it hasn't already completed or previously cancelled).
     */
    public void cancel() {
        pendingQuery.cancel(true);
        isCancelled = true;

        // From "Java Threads": By Scott Oaks & Henry Wong
        // cancelled tasks are never executed, but may
        // accumulate in work queues, which may causes a memory leak
        // if the task hold references (to an enclosing class for example)
        // we can use purge() but one caveat applies: if a second thread attempts to add
        // something to the pool (using the execute() method) at the same time the
        // first thread is attempting to purge the queue the attempt to purge
        // the queue fails and the cancelled object remain in the queue.
        // A better way to cancel objects with thread pools is to use the remove()
        Realm.asyncTaskExecutor.getQueue().remove(pendingQuery);
    }

    /**
     * Checks whether an attempt to cancel the transaction was performed.
     *
     * @return {@code true} if {@link #cancel()} has already been called, {@code false} otherwise.
     */
    public boolean isCancelled() {
        return isCancelled;
    }
}
