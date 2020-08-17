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
import io.realm.mongodb.AppException;
import io.realm.mongodb.RealmEventStreamTask;
import io.realm.mongodb.mongo.events.BaseChangeEvent;

public class RealmEventStreamTaskImpl<T> implements RealmEventStreamTask<T> {
    private final String name;
    private final Executor<T> executor;
    private volatile EventStream<T> eventStream;
    private volatile boolean isCancelled;

    public RealmEventStreamTaskImpl(final String name, final Executor<T> executor) {
        Util.checkNull(executor, "name");
        Util.checkNull(executor, "executor");

        this.executor = executor;
        this.name = name;
    }

    private EventStream<T> getEventStream() throws IOException {
        if (eventStream == null) {
            eventStream = executor.run();
        }

        return this.eventStream;
    }

    @Override
    public synchronized BaseChangeEvent<T> getNext() throws AppException, IOException {
        eventStream = getEventStream();
        return eventStream.getNextEvent();
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
