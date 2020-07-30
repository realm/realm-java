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

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.annotations.NonNull;
import io.realm.internal.Util;
import io.realm.internal.objectserver.EventStream;
import io.realm.mongodb.App;
import io.realm.mongodb.AppException;
import io.realm.mongodb.ErrorCode;
import io.realm.mongodb.RealmEventStreamTask;
import io.realm.mongodb.mongo.events.BaseChangeEvent;

public class RealmEventStreamTaskImpl<T> implements RealmEventStreamTask<T> {
    private final String name;
    private final Executor<T> executor;
    volatile EventStream<T> eventStream;
    volatile boolean isCancelled;
    final ReentrantLock lock;

    public RealmEventStreamTaskImpl(final String name, final Executor<T> executor) {
        Util.checkNull(executor, "executor");
        Util.checkNull(executor, "documentClass");
        Util.checkNull(executor, "codecRegistry");

        this.lock = new ReentrantLock();
        this.executor = executor;
        this.name = name;
    }

    synchronized EventStream<T> getEventStream() throws IOException {
        if (eventStream == null) {
            eventStream = executor.run();
        }

        return this.eventStream;
    }

    @Override
    public BaseChangeEvent<T> getNext() throws IOException {
        if (lock.tryLock()) {
            try {
                eventStream = getEventStream();
                return eventStream.getNextEvent();
            } finally {
                lock.unlock();
            }
        }

        throw new RuntimeException("Resource already open");
    }

    @Override
    public void getAsync(App.Callback<BaseChangeEvent<T>> callback) {
        Util.checkNull(callback, "callback");

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (lock.tryLock()) {
                    try {
                        eventStream = getEventStream();

                        while (true) {
                            BaseChangeEvent<T> nextEvent = eventStream.getNextEvent();

                            callback.onResult(App.Result.withResult(nextEvent));
                        }
                    } catch (IllegalStateException exception) {
                        callback.onResult(App.Result.withError(new AppException(ErrorCode.FUNCTION_EXECUTION_ERROR, exception)));
                    } catch (IOException | RuntimeException exception) {
                        callback.onResult(App.Result.withError(new AppException(ErrorCode.NETWORK_IO_EXCEPTION, exception)));
                    } finally {
                        lock.unlock();
                    }
                } else {
                    callback.onResult(App.Result.withError(new AppException(ErrorCode.RUNTIME_EXCEPTION, new RuntimeException("Resource already open"))));
                }
            }
        }, String.format("RealmStreamTask|%s", name))
                .start();
    }

    @Override
    public boolean isOpen() {
        return (eventStream != null) && eventStream.isOpen();
    }

    @Override
    public void cancel() {
        if (eventStream != null) {
            isCancelled = true;
            try {
                eventStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    public abstract static class Executor<T> {

        /**
         * Executes the code block.
         *
         * @return the result yielded by the task.
         */
        @NonNull
        public abstract EventStream<T> run() throws IOException;
    }
}
