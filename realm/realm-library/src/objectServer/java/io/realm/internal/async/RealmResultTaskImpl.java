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

package io.realm.internal.async;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import io.realm.internal.RealmNotifier;
import io.realm.internal.Util;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.log.RealmLog;
import io.realm.mongodb.App;
import io.realm.mongodb.AppException;
import io.realm.mongodb.ErrorCode;
import io.realm.mongodb.RealmResultTask;

/**
 * Implementation of RealmResultTask used internally by MongoDB Realm APIs. Implementation is
 * separate from the interface so that we can hide the constructor from end users.
 *
 * @param <T> the result type delivered by this task.
 */
public class RealmResultTaskImpl<T> implements RealmResultTask<T> {

    private Future<?> pendingTask;
    private volatile boolean isCancelled = false;
    private final ThreadPoolExecutor service;
    private Executor<T> executor;

    /**
     * Constructor for RealmResultTaskImpl.
     *
     * @param service  pool thread service on which the task will be executed.
     * @param executor the code block executed by the task.
     */
    public RealmResultTaskImpl(ThreadPoolExecutor service, Executor<T> executor) {
        Util.checkNull(service, "service");
        this.service = service;
        Util.checkNull(executor, "executor");
        this.executor = executor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        if (pendingTask != null) {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public T get() {
        return executor.run();
    }

    @Override
    public void getAsync(App.Callback<T> callback) {
        Util.checkNull(callback, "callback");
        Util.checkLooperThread("RealmResultTaskImpl can only run on looper threads.");

        RealmNotifier handler = new AndroidRealmNotifier(null, new AndroidCapabilities());

        pendingTask = service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    postSuccess(handler, executor.run(), callback);
                } catch (AppException e) {
                    postError(handler, e, callback);
                } catch (Throwable e) {
                    postError(handler, new AppException(ErrorCode.UNKNOWN, "Unexpected error", e), callback);
                }
            }
        });
    }

    private void postError(RealmNotifier handler,
                           final AppException error,
                           App.Callback<T> callback) {
        boolean errorHandled;
        Runnable action = new Runnable() {
            @Override
            public void run() {
                if (!isCancelled) {
                    callback.onResult(App.Result.withError(error));
                }
            }
        };
        errorHandled = handler.post(action);

        if (!errorHandled) {
            RealmLog.error(error, "An error was thrown, but could not be posted: \n" + error.toString());
        }
    }

    private void postSuccess(RealmNotifier handler,
                             @Nullable final T result,
                             App.Callback<T> callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isCancelled) {
                    callback.onResult((result == null) ? App.Result.success() : App.Result.withResult(result));
                }
            }
        });
    }

    /**
     * The Executor class represent the portion of code the RealmResultTaskImpl will execute.
     *
     * @param <T> the result type delivered by the task.
     */
    public abstract static class Executor<T> {

        /**
         * Executes the code block.
         *
         * @return the result yielded by the task.
         */
        @Nullable
        public abstract T run();
    }
}
