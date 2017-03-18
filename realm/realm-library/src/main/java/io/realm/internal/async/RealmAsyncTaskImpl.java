/*
 * Copyright 2016 Realm Inc.
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

package io.realm.internal.async;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import io.realm.RealmAsyncTask;


public final class RealmAsyncTaskImpl implements RealmAsyncTask {
    private final Future<?> pendingTask;
    private final ThreadPoolExecutor service;
    private volatile boolean isCancelled = false;

    public RealmAsyncTaskImpl(Future<?> pendingTask, ThreadPoolExecutor service) {
        this.pendingTask = pendingTask;
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        pendingTask.cancel(true);
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
        service.getQueue().remove(pendingTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
}
