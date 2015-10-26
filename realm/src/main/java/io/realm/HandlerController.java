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
 *
 */

package io.realm;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import io.realm.internal.SharedGroup;
import io.realm.internal.async.QueryUpdateTask;
import io.realm.internal.log.RealmLog;

/**
 * Centralise all Handler callbacks, including updating async queries and refreshing the Realm.
 */
public class HandlerController implements Handler.Callback {
    static final int REALM_CHANGED = 14930352; // Hopefully it won't clash with other message IDs.
    static final int REALM_UPDATE_ASYNC_QUERIES = 24157817;
    static final int REALM_COMPLETED_ASYNC_QUERY = 39088169;
    static final int REALM_COMPLETED_ASYNC_FIND_FIRST = 63245986;
    static final int REALM_ASYNC_BACKGROUND_EXCEPTION = 102334155;
    final BaseRealm realm;
    // pending update of async queries
    private Future updateAsyncQueriesTask;

    final ReferenceQueue<RealmResults<? extends RealmObject>> referenceQueue = new ReferenceQueue<RealmResults<? extends RealmObject>>();
    // keep a WeakReference list to RealmResults obtained asynchronously in order to update them
    // RealmQuery is not WeakReferenced to prevent it from being GC'd. RealmQuery should be
    // cleaned if RealmResults is cleaned. we need to keep RealmQuery because it contains the query
    // pointer (to handover for each update) + all the arguments necessary to rerun the query:
    // sorting orders, soring columns, type (findAll, findFirst, findAllSorted etc.)
    final Map<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<? extends RealmObject>> asyncRealmResults =
            new IdentityHashMap<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<? extends RealmObject>>();

    final Map<WeakReference<RealmObject>, RealmQuery<? extends RealmObject>> asyncRealmObjects =
            new IdentityHashMap<WeakReference<RealmObject>, RealmQuery<? extends RealmObject>>();

    public HandlerController(BaseRealm realm) {
        this.realm = realm;
    }

    private void updateAsyncQueries () {
        if (updateAsyncQueriesTask != null && !updateAsyncQueriesTask.isDone()) {
            // try to cancel any pending update since we're submitting a new one anyway
            updateAsyncQueriesTask.cancel(true);
            Realm.asyncQueryExecutor.getQueue().remove(updateAsyncQueriesTask);
            RealmLog.d("REALM_CHANGED realm:" + HandlerController.this + " cancelling pending REALM_UPDATE_ASYNC_QUERIES updates");
        }
        RealmLog.d("REALM_CHANGED realm:"+ HandlerController.this + " updating async queries, total: " + asyncRealmResults.size());
        // prepare a QueryUpdateTask to current async queries in this thread
        QueryUpdateTask.Builder.UpdateQueryStep updateQueryStep = QueryUpdateTask.newBuilder()
                .realmConfiguration(realm.getConfiguration());
        QueryUpdateTask.Builder.RealmResultsQueryStep realmResultsQueryStep = null;

        // we iterate over non GC'd async RealmResults then add them to the list to be updated (in a batch)
        Iterator<Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<?>>> iterator = asyncRealmResults.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<?>> entry = iterator.next();
            WeakReference<RealmResults<? extends RealmObject>> weakReference = entry.getKey();
            RealmResults<? extends RealmObject> realmResults = weakReference.get();
            if (realmResults == null) {
                // GC'd instance remove from the list
                iterator.remove();

            } else {
                realmResultsQueryStep = updateQueryStep.add(weakReference,
                        entry.getValue().handoverQueryPointer(),
                        entry.getValue().getArgument());
            }

            // Note: we're passing an WeakRef of a RealmResults to another thread
            //       this is safe as long as we don't invoke any of the RealmResults methods.
            //       we're just using it as a Key in an IdentityHashMap (i.e doesn't call
            //       AbstractList's hashCode, that require accessing objects from another thread)
            //
            //       watch out when you debug, as you're IDE try to evaluate RealmResults
            //       which break the Thread confinement constraints.
        }
        if (realmResultsQueryStep != null) {
            QueryUpdateTask queryUpdateTask = realmResultsQueryStep
                    .sendToHandler(realm.handler, REALM_UPDATE_ASYNC_QUERIES)
                    .build();
            updateAsyncQueriesTask = Realm.asyncQueryExecutor.submit(queryUpdateTask);
        }
    }

    private void completedAsyncQueryUpdate(QueryUpdateTask.Result result) {
        Set<WeakReference<RealmResults<? extends RealmObject>>> updatedTableViewsKeys = result.updatedTableViews.keySet();
        if (updatedTableViewsKeys.size() > 0) {
            WeakReference<RealmResults<? extends RealmObject>> weakRealmResults = updatedTableViewsKeys.iterator().next();

            RealmResults<? extends RealmObject> realmResults = weakRealmResults.get();
            if (realmResults == null) {
                asyncRealmResults.remove(weakRealmResults);
                RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] realm:"+ HandlerController.this + " RealmResults GC'd ignore results");

            } else {
                SharedGroup.VersionID callerVersionID = realm.sharedGroupManager.getVersion();
                int compare = callerVersionID.compareTo(result.versionID);
                if (compare == 0) {
                    // if the RealmResults is empty (has not completed yet) then use the value
                    // otherwise a task (grouped update) has already updated this RealmResults
                    if (!realmResults.isLoaded()) {
                        RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] , realm:"+ HandlerController.this + " same versions, using results (RealmResults is not loaded)");
                        // swap pointer
                        realmResults.swapTableViewPointer(result.updatedTableViews.get(weakRealmResults));
                        // notify callbacks
                        realmResults.notifyChangeListeners();
                    } else {
                        RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] , realm:"+ HandlerController.this + " ignoring result the RealmResults (is already loaded)");
                    }

                } else if (compare > 0) {
                    // we have two use cases:
                    // 1- this RealmResults is not empty, this means that after we started the async
                    //    query, we received a REALM_CHANGE that triggered an update of all async queries
                    //    including the last async submitted, so no need to use the provided TableView pointer
                    //    (or the user forced the sync behaviour .load())
                    // 2- This RealmResults is still empty but this caller thread is advanced than the worker thread
                    //    this could happen if the current thread advanced the shared_group (via a write or refresh)
                    //    this means that we need to rerun the query against a newer worker thread.

                    if (!realmResults.isLoaded()) { // UC2
                        // UC covered by this test: RealmAsyncQueryTests#testFindAllAsyncRetry
                        RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY " + weakRealmResults + "] , realm:"+ HandlerController.this + " caller is more advanced & RealmResults is not loaded, rerunning the query against the latest version");

                        RealmQuery<?> query = asyncRealmResults.get(weakRealmResults);
                        QueryUpdateTask queryUpdateTask = QueryUpdateTask.newBuilder()
                                .realmConfiguration(realm.getConfiguration())
                                .add(weakRealmResults,
                                        query.handoverQueryPointer(),
                                        query.getArgument())
                                .sendToHandler(realm.handler, REALM_COMPLETED_ASYNC_QUERY)
                                .build();

                        Realm.asyncQueryExecutor.submit(queryUpdateTask);

                    } else {
                        // UC covered by this test: RealmAsyncQueryTests#testFindAllCallerIsAdvanced
                        RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] , realm:"+ HandlerController.this + " caller is more advanced & RealmResults is loaded ignore the outdated result");
                    }

                } else {
                    // the caller thread is behind the worker thread,
                    // no need to rerun the query, since we're going to receive the update signal
                    // & batch update all async queries including this one
                    // UC covered by this test: RealmAsyncQueryTests#testFindAllCallerThreadBehind
                    RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] , realm:"+ HandlerController.this + " caller thread behind worker thread, ignore results (a batch update will update everything including this query)");
                }
            }
        }
    }

    private void completedAsyncQueriesUpdate(QueryUpdateTask.Result result) {
        SharedGroup.VersionID callerVersionID = realm.sharedGroupManager.getVersion();
        int compare = callerVersionID.compareTo(result.versionID);
        if (compare > 0) {
            RealmLog.d("REALM_UPDATE_ASYNC_QUERIES realm:" + HandlerController.this + " caller is more advanced, rerun updates");
            // The caller is more advance than the updated queries ==>
            // need to refresh them again (if there is still queries)
            realm.handler.sendEmptyMessage(REALM_CHANGED);

        } else {
            // We're behind or on the same version as the worker thread

            // only advance if we're behind
            if (compare != 0) {
                // no need to remove old pointers from TableView, since they're
                // imperative TV, they will not rerun if the SharedGroup advance

                // UC covered by this test: RealmAsyncQueryTests#testFindAllCallerThreadBehind
                RealmLog.d("REALM_UPDATE_ASYNC_QUERIES realm:"+ HandlerController.this + " caller is behind  advance_read");
                // refresh the Realm to the version provided by the worker thread
                // (advanceRead to the latest version may cause a version mismatch error) preventing us
                // from importing correctly the handover table view
                realm.sharedGroupManager.advanceRead(result.versionID);
            }

            ArrayList<RealmResults<? extends RealmObject>> callbacksToNotify = new ArrayList<RealmResults<? extends RealmObject>>(result.updatedTableViews.size());
            // use updated TableViews pointers for the existing async RealmResults
            for (Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, Long> query : result.updatedTableViews.entrySet()) {
                WeakReference<RealmResults<? extends RealmObject>> weakRealmResults = query.getKey();
                RealmResults<? extends RealmObject> realmResults = weakRealmResults.get();
                if (realmResults == null) {
                    // don't update GC'd instance
                    asyncRealmResults.remove(weakRealmResults);

                } else {
                    // it's dangerous to notify the callback about new results before updating
                    // the pointers, because the callback may use another RealmResults not updated yet
                    // this is why we defer the notification until we're done updating all pointers

                    // TODO find a way to only notify callbacks if the underlying data changed compared
                    //      to the existing value(s) for this RealmResults (use a hashCode?)
                    callbacksToNotify.add(realmResults);

                    RealmLog.d("REALM_UPDATE_ASYNC_QUERIES realm:"+ HandlerController.this + " updating RealmResults " + weakRealmResults);
                    // update the instance with the new pointer
                    realmResults.swapTableViewPointer(query.getValue());
                }
            }

            for (RealmResults<? extends RealmObject> query : callbacksToNotify) {
                query.notifyChangeListeners();
            }

            // notify listeners only when we advanced
            if (compare != 0) {
                realm.sendNotifications();
            }

            updateAsyncQueriesTask = null;
        }
    }

    private void completedAsyncFindFirst(QueryUpdateTask.Result result) {
        Set<WeakReference<? extends RealmObject>> updatedRowKey = result.updatedRow.keySet();
        if (updatedRowKey.size() > 0) {
            WeakReference<? extends RealmObject> realmObjectWeakReference = updatedRowKey.iterator().next();
            RealmObject realmObject = realmObjectWeakReference.get();

            if (realmObject != null) {
                SharedGroup.VersionID callerVersionID = realm.sharedGroupManager.getVersion();
                int compare = callerVersionID.compareTo(result.versionID);
                // we always query on the same version
                // only two use cases could happen 1. we're on the same version or 2. the caller has advanced in the meanwhile
                if (compare == 0) { //same version import the handover
                    realmObject.onCompleted(result.updatedRow.get(realmObjectWeakReference));
                    asyncRealmObjects.remove(realmObjectWeakReference);

                } else if (compare > 0) {
                    // the caller has advanced we need to
                    // retry against the current version of the caller
                    RealmQuery<?> realmQuery = asyncRealmObjects.get(realmObjectWeakReference);

                    QueryUpdateTask queryUpdateTask = QueryUpdateTask.newBuilder()
                            .realmConfiguration(realm.getConfiguration())
                            .addObject(realmObjectWeakReference,
                                    realmQuery.handoverQueryPointer(),
                                    realmQuery.getArgument())
                            .sendToHandler(realm.handler, REALM_COMPLETED_ASYNC_FIND_FIRST)
                            .build();

                    Realm.asyncQueryExecutor.submit(queryUpdateTask);
                } else {
                    // should not happen, since the the background thread position itself against the provided version
                    // and the caller thread can only go forward (advance_read)
                    throw new IllegalStateException("Caller thread behind the worker thread");
                }
            } // else: element GC'd in the meanwhile
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        // Due to how a ConcurrentHashMap iterator is created we cannot be sure that other threads are
        // aware when this threads handler is removed before they send messages to it. We don't wish to synchronize
        // access to the handlers as they are the prime mean of notifying about updates. Instead we make sure
        // that if a message does slip though (however unlikely), it will not try to update a SharedGroup that no
        // longer exists. `sharedGroupManager` will only be null if a Realm is really closed.
        if (realm.sharedGroupManager != null) {
            switch (message.what) {
                case REALM_CHANGED: {
                    if (threadContainsAsyncQueries()) {
                        updateAsyncQueries();

                    } else {
                        RealmLog.d("REALM_CHANGED realm:"+ HandlerController.this + " no async queries, advance_read");
                        realm.sharedGroupManager.advanceRead();
                        realm.sendNotifications();
                    }
                    break;
                }
                case REALM_COMPLETED_ASYNC_QUERY: {
                    // one async query has completed
                    QueryUpdateTask.Result result = (QueryUpdateTask.Result) message.obj;
                    completedAsyncQueryUpdate(result);
                    break;
                }
                case REALM_UPDATE_ASYNC_QUERIES: {
                    // this is called once the background thread completed the update of the async queries
                    QueryUpdateTask.Result result = (QueryUpdateTask.Result) message.obj;
                    completedAsyncQueriesUpdate(result);
                    break;
                }
                case REALM_COMPLETED_ASYNC_FIND_FIRST: {
                    QueryUpdateTask.Result result = (QueryUpdateTask.Result) message.obj;
                    completedAsyncFindFirst(result);
                    break;
                }
                case REALM_ASYNC_BACKGROUND_EXCEPTION: {
                    // Don't fail silently in the background in case of Core exception
                    throw (Error) message.obj;
                }
            }
        }
        return true;
    }

    /**
     * This will prevent advanceReading from accidentally advancing the thread and potentially re-run the queries in this thread.
     * @return {@code true} if there is at least one (non GC'd) instance of {@link RealmResults} {@code false} otherwise
     */
    private boolean threadContainsAsyncQueries () {
        deleteWeakReferences();
        boolean isEmpty = true;
        Iterator<Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<?>>> iterator = asyncRealmResults.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<?>> next = iterator.next();
            if (next.getKey().get() == null) {
                // clean the GC'ed instances
                // we could've avoided this if we had a 'WeakIdentityHashmap' data structure. miss Guava :(
                iterator.remove();
            } else {
                isEmpty = false;
            }
        }

        return !isEmpty;
    }

    private void deleteWeakReferences() {
        // From the AOSP FinalizationTest:
        // https://android.googlesource.com/platform/libcore/+/master/support/src/test/java/libcore/
        // java/lang/ref/FinalizationTester.java
        // System.gc() does not garbage collect every time. Runtime.gc() is
        // more likely to perform a gc.
        Runtime.getRuntime().gc();
        Reference<? extends RealmResults<? extends RealmObject>> weakReference;
        while ((weakReference = referenceQueue.poll()) != null ) { // Does not wait for a reference to become available.
            asyncRealmResults.remove(weakReference);
        }
    }
}
