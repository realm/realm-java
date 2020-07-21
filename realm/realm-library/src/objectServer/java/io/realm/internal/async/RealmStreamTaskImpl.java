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

import org.bson.codecs.configuration.CodecRegistry;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import io.realm.internal.Util;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.mongodb.App;
import io.realm.mongodb.RealmStreamTask;


public class RealmStreamTaskImpl<T> implements RealmStreamTask<T> {
    private final CodecRegistry codecRegistry;
    private ExecutorService service;
    private final Executor executor;
    private EventStream<T> eventStream;
    private final Class<T> documentClass;

    public RealmStreamTaskImpl(Executor executor, CodecRegistry codecRegistry, Class<T> documentClass) {
        Util.checkNull(executor, "executor");
        this.codecRegistry = codecRegistry;
        this.documentClass = documentClass;
        this.executor = executor;
    }

    private synchronized EventStream<T> getEventStream() throws IOException {
        if (eventStream == null){
            OsJavaNetworkTransport.Response response = executor.sendRequest();
            this.eventStream = new EventStream<>(response, codecRegistry, documentClass);
        }

        return this.eventStream;
    }

    @Override
    public T getNextEvent() throws IOException {
        EventStream<T> eventStream = getEventStream();
        return eventStream.getNextEvent();
    }

    @Override
    public void getEventsAsync(App.Callback<T> callback) {
        service = Executors.newSingleThreadExecutor();
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void cancel() {
        try {
            getEventStream().cancel();
        } catch (IOException ignore) {
            ignore.printStackTrace();
        }
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public abstract static class Executor {

        /**
         * Executes the code block.
         *
         * @return the result yielded by the task.
         */
        @Nullable
        public abstract OsJavaNetworkTransport.Response sendRequest() throws IOException;
    }
}
