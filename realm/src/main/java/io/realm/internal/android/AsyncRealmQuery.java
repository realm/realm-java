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

package io.realm.internal.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.Future;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.TableOrView;
import io.realm.internal.TableView;

import static io.realm.Realm.asyncQueryExecutor;

/**
 * A helper class to help make handling asynchronous {@link RealmQuery} easier.
 *
 * @param <E> type of the object which is to be queried for
 */
// Behaviour may change, user should not subclass
public final class AsyncRealmQuery<E extends RealmObject> {
    private final Realm callerRealm;
    private final Class<E> clazz;
    private final Realm.QueryCallback<RealmResults<E>> callback;

    private int from;
    private int to;
    private String fieldName;
    private EventHandler eventHandler;
    private Future<?> pendingQuery;

    /**
     * Create an {@code AsyncRealmQuery} instance.
     *
     * @param realm    The realm to query within.
     * @param clazz    The class to query.
     * @param callback invoked on the thread {@link Realm#findAsync(Class, Realm.QueryCallback)} was called from, to post results.
     */
    public AsyncRealmQuery(Realm realm, Class<E> clazz, Realm.QueryCallback<RealmResults<E>> callback) {
        this.callerRealm = realm;
        this.callback = callback;
        this.clazz = clazz;
    }

    /**
     * Between condition
     *
     * @param fieldName The field to compare
     * @param from      Lowest value (inclusive)
     * @param to        Highest value (inclusive)
     * @return current instance of {@code AsyncRealmQuery} for method chaining
     */
    public AsyncRealmQuery<E> between(String fieldName, int from, int to) {
        this.from = from;
        this.to = to;
        this.fieldName = fieldName;
        return this;
    }

    /**
     * Find all objects that fulfill the query conditions.
     * Results will be posted to the callback instance {@link Realm.QueryCallback} asynchronously
     * If no objects match the condition, a list with zero objects is returned.
     *
     * @see io.realm.RealmResults
     */
    public RealmQuery.AsyncRequest findAll() {
        // will use the Looper of the caller thread to post the result
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            eventHandler = new EventHandler(looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            eventHandler = new EventHandler(looper);
        } else {
            eventHandler = null;//FIXME define strategy/behaviour for regular JVM call
        }

        // We need a pointer to the caller Realm, to be able to handover the result to it
        final long callerSharedGroupNativePtr = callerRealm.getSharedGroupPointer();

        // We need to use the same configuration to open a background SharedGroup (i.e Realm)
        // to perform the query
        final RealmConfiguration realmConfiguration = callerRealm.getConfiguration();

        pendingQuery = asyncQueryExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (!Thread.currentThread().isInterrupted()) {
                    try {
                        Realm bgRealm = Realm.getInstance(realmConfiguration);

                        //TODO This will probably be replace by a kind of 'QueryBuilder'
                        //     that holds all the operations (predicates/filters) then
                        //     replay them here in this background thread. The current implementation
                        //     call Core for each step, we want to limit the overhead by sending one
                        //     single call to Core with all the parameters.

                        TableView tv = new RealmQueryAdapter<E>(bgRealm, clazz)
                                .between(fieldName, from, to)
                                .findAll(callerSharedGroupNativePtr, bgRealm.getSharedGroupPointer());

                        bgRealm.close();

                        // send results to the caller thread's callback
                        if (!pendingQuery.isCancelled()) {
                            Message msg = eventHandler.obtainMessage(EventHandler.MSG_SUCCESS, tv);
                            eventHandler.sendMessage(msg);
                        }

                    } catch (Exception e) {
                        if (!pendingQuery.isCancelled()) {
                            Message msg = eventHandler.obtainMessage(EventHandler.MSG_ERROR, e);
                            eventHandler.sendMessage(msg);
                        }
                    }
                }
            }
        });

        return new RealmQuery.AsyncRequest(pendingQuery);
    }

    private class EventHandler extends Handler {
        private static final int MSG_SUCCESS = 1;
        private static final int MSG_ERROR = MSG_SUCCESS + 1;

        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS:
                    RealmResults<E> resultList = new RealmResults<E>(callerRealm, (TableOrView) msg.obj, clazz);
                    callback.onSuccess(resultList);
                    break;
                case MSG_ERROR:
                    callback.onError((Throwable) msg.obj);
                    break;
            }
        }
    }
}
