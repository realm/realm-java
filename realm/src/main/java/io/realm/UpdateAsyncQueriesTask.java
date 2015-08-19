package io.realm;


import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import io.realm.internal.SharedGroup;

/**
 * Created by Nabil on 05/08/15.
 */
public class UpdateAsyncQueriesTask implements Runnable {
    private final RealmConfiguration realmConfiguration;
    private final WeakReference<Handler> callerThread;

    private final List<Entry> queries;
    // need to position the background Realm at the exact version as the caller thread
    // in order to import correctly the exported queries
    private final SharedGroup.VersionID callerThreadSharedGroupVersion;


    public UpdateAsyncQueriesTask(Handler callerThread, RealmConfiguration realmConfiguration, List<Entry> queries, SharedGroup.VersionID callerThreadSharedGroupVersion) {
        this.callerThread = new WeakReference<Handler>(callerThread);
        this.realmConfiguration = realmConfiguration;
        this.queries = queries;
        this.callerThreadSharedGroupVersion = callerThreadSharedGroupVersion;
    }

    @Override
    public void run() {
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfiguration);
            // TODO position the realm at the caller Thread version
            //realm.sharedGroup. [End_read|begin_readAT(version_ID)] if it crash
            HashMap<Integer, Long> updatedQueries = new HashMap<Integer, Long>(queries.size());

            for (Entry entry : queries) {
                if (!isTaskCancelled()) {
                    // TODO: call findAll with the right mode
                    switch (entry.query.arguments.type) {
                        case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL:
                            // need to import the query into the latest
                            //findAll
                            break;
                        case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL_SORTED:
                            //findAllSorted
                            break;
                        case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL_MULTI_SORTED:
                            //findAllMultiSorted
                            break;
                        default:
                            throw new IllegalArgumentException("Query mode " + entry.query.arguments + " not supported");
                    }

                    // update the pointer with the new exported tableview
                    // realm accessed from another thread (maybe try the weak reference)

                    updatedQueries.put(entry.realmResults, 0xCAFEBABEL);
                }
            }

            // TODO: should group result & send one call to Handler
            if(!isTaskCancelled()) {
                Handler handler = callerThread.get();
                if (handler != null) {
                    Result result = new Result(updatedQueries, realm.sharedGroup.getVersion());
                    Message message = handler.obtainMessage(Realm.REALM_UPDATE_ASYNC_QUERIES, result);
                    handler.sendMessage(message);
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private boolean isTaskCancelled () {
        // no point continuing if the caller thread was stopped or this thread was interrupted
        return Thread.currentThread().isInterrupted() || callerThread.isEnqueued();
    }

    static class Result {
        public final HashMap<Integer, Long> updatedTableViews;

        // communicate the version used to update the queries
        // the caller thread need to advance to this version (may not be the latest version yet)
        // in order to import without (handover mismatch) the exported tableView
        public final SharedGroup.VersionID versionID;

        private Result(HashMap<Integer, Long> updatedTableViews, SharedGroup.VersionID versionID) {
            this.updatedTableViews = updatedTableViews;
            this.versionID = versionID;
        }
    }

    // replace with maps as we need to query
    static class Entry {
        // need a way to identify which RealmResult we're dealing with
        // using the RealmResults instance as a key is not safe, since
        // Map need to calculate the hashCode, involving iterating over
        // RealmObject on a different thread which is not permitted.
        // to overcome this limitation we use the identity hash of the instance
        // to identify uniquely the RealmResults we just updated
        final Integer realmResults;
        final UpdateQuery query;

        Entry(Integer realmResults, UpdateQuery query) {
            this.realmResults = realmResults;
            this.query = query;
        }
    }

    static class UpdateQuery {
        final long handoverQueryPtr;
        final RealmQuery.ArgumentsHolder arguments;

        UpdateQuery(long handoverQueryPtr, RealmQuery.ArgumentsHolder arguments) {
            this.handoverQueryPtr = handoverQueryPtr;
            this.arguments = arguments;
        }
    }
}
