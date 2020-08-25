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

import io.realm.internal.Util;
import io.realm.internal.objectserver.EventStream;
import io.realm.mongodb.App;
import io.realm.mongodb.AppException;
import io.realm.mongodb.ErrorCode;
import io.realm.mongodb.RealmEventStreamAsyncTask;
import io.realm.mongodb.mongo.events.BaseChangeEvent;

public class RealmEventStreamAsyncTaskImpl<T> implements RealmEventStreamAsyncTask<T> {
    private final String name;
    private final Executor<T> executor;
    private volatile EventStream<T> eventStream;
    private volatile boolean isCancelled;
    private Thread thread;

    public RealmEventStreamAsyncTaskImpl(final String name, final Executor<T> executor) {
        Util.checkNull(executor, "name");
        Util.checkNull(executor, "executor");

        this.executor = executor;
        this.name = name;
    }

    @Override
    public synchronized void get(App.Callback<BaseChangeEvent<T>> callback) throws IllegalStateException {
        Util.checkNull(callback, "callback");

        if (thread != null) {
            throw new IllegalStateException("Resource already open");
        } else {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        eventStream = executor.run();

                        while (true) {
                            BaseChangeEvent<T> nextEvent = eventStream.getNextEvent();
                            callback.onResult(App.Result.withResult(nextEvent));
                        }
                    } catch (AppException exception) {
                        callback.onResult(App.Result.withError(exception));
                    } catch (IOException exception) {
                        AppException appException = new AppException(ErrorCode.NETWORK_IO_EXCEPTION, exception);
                        callback.onResult(App.Result.withError(appException));
                    }
                }
            }, String.format("RealmStreamTask|%s", name));

            thread.start();
        }
    }

    @Override
    public boolean isOpen() {
        return (eventStream != null) && eventStream.isOpen();
    }

    @Override
    public void cancel() {
        if (eventStream != null) {
            isCancelled = true;
            eventStream.close();
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
        public abstract EventStream<T> run() throws IOException;
    }
}
