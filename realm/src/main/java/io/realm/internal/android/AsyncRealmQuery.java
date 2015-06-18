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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.Future;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.async.RetryPolicy;
import io.realm.internal.async.RetryPolicyFactory;

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
    private final Realm.QueryCallback<E> callback;

    private int from;
    private int to;
    private String fieldName;
    private EventHandler eventHandler;
    private RealmQueryAdapter<E> realmQueryAdapter;
    private Future<?> pendingQuery;

    // Creating a worker Realm for each query is expensive, we cache one per worker thread
    //FIXME use ThreadPoolExecutor#afterExecute to cleanup/close Realm once the Executors shutdown
    private final ThreadLocal<Realm> bgRealmWorker = new ThreadLocal<Realm>();

    private final RetryPolicy retryPolicy;
    //Allow the test to override the default retry policy using reflection
    //FIXME move those as instance attributes if we want to expose the retry policy to the user
    private static int RETRY_POLICY_MODE = RetryPolicy.MODE_INDEFINITELY;
    private static int MAX_NUMBER_RETRIES_POLICY = 0;

    /**
     * Create an {@code AsyncRealmQuery} instance.
     *
     * @param realm    The realm to query within.
     * @param clazz    The class to query.
     * @param callback invoked on the thread {@link Realm#findAsync(Class, Realm.QueryCallback)} was called from, to post results.
     */
    public AsyncRealmQuery(Realm realm, Class<E> clazz, Realm.QueryCallback<E> callback) {
        this.callerRealm = realm;
        this.callback = callback;
        this.clazz = clazz;
        this.retryPolicy = RetryPolicyFactory.get(RETRY_POLICY_MODE, MAX_NUMBER_RETRIES_POLICY);

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
        //This call needs to be done on the caller's thread, since SG()->get_version_of_current_transaction is not thread safe
        final long[] callerSharedGroupVersion = callerRealm.getSharedGroupVersion();

        pendingQuery = asyncQueryExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (!Thread.currentThread().isInterrupted()) {
                    try {
                        Realm bgRealm = createOrRetrieveCachedWorkerRealm(realmConfiguration);
                        // Position the SharedGroup to the same version as the caller thread
                        // the call to SharedGroup->get_version_of_current_transaction is not thread safe
                        // it needs to happen on the caller's thread.
                        bgRealm.setSharedGroupAtVersion(callerSharedGroupVersion);
                        //TODO This will probably be replace by a kind of 'QueryBuilder'
                        //     that holds all the operations (predicates/filters) then
                        //     replay them here in this background thread. The current implementation
                        //     call Core for each step, we want to limit the overhead by sending one
                        //     single call to Core with all the parameters.

                        long tableViewPtr = new RealmQueryAdapter<>(bgRealm, clazz)
                                .between(fieldName, from, to)
                                .findAll(bgRealm.getSharedGroupPointer());

                        if (IS_DEBUG && NB_ADVANCE_READ_SIMULATION-- > 0) {
                            // notify caller thread that we're about to post result to Handler
                            // (this is interesting in Unit Testing, as we can advance read to simulate
                            // a mismatch between the query result, and the current version of the Realm
                            eventHandler.sendEmptyMessage(EventHandler.MSG_ADVANCE_READ);
                        }

                        // send results to the caller thread's callback
                        if (!pendingQuery.isCancelled()) {
                            Bundle bundle = new Bundle(2);
                            bundle.putLong(EventHandler.TABLE_VIEW_POINTER_ARG, tableViewPtr);
                            bundle.putLong(EventHandler.CALLER_SHARED_GROUP_POINTER_ARG, callerSharedGroupNativePtr);

                            Message msg = eventHandler.obtainMessage(EventHandler.MSG_SUCCESS);
                            msg.setData(bundle);
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

    // If the cached Realm share the same configuration as the caller then use it
    // instead of creating a new one (Opening a SharedGroup is expensive)
    private Realm createOrRetrieveCachedWorkerRealm (final RealmConfiguration callerConfiguration) {
        Realm worker = bgRealmWorker.get();
        if (null == worker || !worker.getConfiguration().equals(callerConfiguration)) {
            if (null != worker) {
                //This cached Realm instance can't be used with the provided configuration
                worker.close();
            }

            worker = Realm.getInstance(callerConfiguration);
            bgRealmWorker.set(worker);
        }

        // Clear cached pointers to Table (since we may position the Realm to transaction
        // where those pointers are no longer valid)
        worker.resetTableCache();

        return worker;
    }

    private class EventHandler extends Handler {
        public final static String TABLE_VIEW_POINTER_ARG = "tvPtr";
        public final static String CALLER_SHARED_GROUP_POINTER_ARG = "callerSgPtr";

        private static final int MSG_SUCCESS = 1;
        private static final int MSG_ERROR = MSG_SUCCESS + 1;
        // This is only used for testing scenario when we want to simulate a change in the
        // caller Realm before delivering the result, thus this will trigger 'Handover failed due to version mismatch'
        // so we can test the retry process
        private static final int MSG_ADVANCE_READ = MSG_ERROR + 1;

        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS:
                    Bundle bundle = msg.getData();
                    try {
                        // This will block the caller Thread from performing any advance_read, commit or rollback
                        // operations on its SharedGroup, until the handover is complete, this will also eliminate the risk
                        // of performing those operations while creating a TableView
                        RealmResults<E> resultList = new RealmResults<>(callerRealm,
                                realmQueryAdapter.importTableViewToRealm(bundle.getLong(TABLE_VIEW_POINTER_ARG), bundle.getLong(CALLER_SHARED_GROUP_POINTER_ARG)),
                                clazz);
                        callback.onSuccess(resultList);

                    } catch (Exception e) {//TODO use the type safe exception from Core
                        if (retryPolicy.shouldRetry()) {
                            findAll();
                        } else {
                            callback.onError(e);
                        }
                    }
                    break;

                case MSG_ERROR:
                    callback.onError((Throwable) msg.obj);
                    break;

                case MSG_ADVANCE_READ:
                    ((Realm.DebugQueryCallback<E>)callback).onBackgroundQueryCompleted(callerRealm);
                    break;
            }
        }
    }

    // *** Debug Helper *** //
    // private & final to prevent any access by error (besides reflection)
    private static boolean IS_DEBUG = false; // this is not final to prevent the compiler from inlining
    private static int NB_ADVANCE_READ_SIMULATION = 1;

}
