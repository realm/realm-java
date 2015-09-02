package io.realm;


import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.WeakHashMap;

import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;

/**
 * Created by Nabil on 05/08/15.
 */
public class UpdateAsyncQueriesTask implements Runnable {
    private final RealmConfiguration realmConfiguration;
    private final WeakReference<Handler> callerThread;
    private final int notifySignal;

    private final List<Entry> queries;
    // need to position the background Realm at the exact version as the caller thread
    // in order to import correctly the exported queries
    private final SharedGroup.VersionID callerThreadSharedGroupVersion;


    public UpdateAsyncQueriesTask(Handler callerThread, RealmConfiguration realmConfiguration, int notifySignal, List<Entry> queries, SharedGroup.VersionID callerThreadSharedGroupVersion) {
        this.notifySignal = notifySignal;
        this.callerThread = new WeakReference<Handler>(callerThread);
        this.realmConfiguration = realmConfiguration;
        this.queries = queries;
        this.callerThreadSharedGroupVersion = callerThreadSharedGroupVersion;
    }

    @Override
    public void run() {
//        Realm realm = null;
        SharedGroup sharedGroup = null;
        try {
//            realm = Realm.getInstance(realmConfiguration);
            sharedGroup = new SharedGroup(realmConfiguration.getPath(),
                    true, realmConfiguration.getDurability(),
                    realmConfiguration.getEncryptionKey());

            // TODO position the realm at the caller Thread version
            //realm.sharedGroup. [End_read|begin_readAT(version_ID)] if it crash
            IdentityHashMap<WeakReference<RealmResults<?>>, Long> updatedQueries = new IdentityHashMap<WeakReference<RealmResults<?>>, Long>(queries.size());
            //TODO remove duplicate
            IdentityHashMap<WeakReference<RealmObject>, Long> updatedObject = new IdentityHashMap<WeakReference<RealmObject>, Long>(queries.size());

            //TODO optimisation batch those update in JNI level (avoid ending_read & advance_read for each  query)
            for (Entry entry : queries) {
                if (!isTaskCancelled()) {
                    // TODO: call findAll with the right mode
                    switch (entry.query.arguments.type) {
                        case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL:
                            long tvPointer = TableQuery.nativeFindAllWithHandover(sharedGroup.getNativePointer(),
                                    sharedGroup.getNativeReplicationPointer(),
                                    entry.query.handoverQueryPtr, 0, Table.INFINITE, Table.INFINITE);
                            updatedQueries.put(entry.realmResults, tvPointer);
                            //findAll
                            break;
                        case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL_SORTED:
                            //findAllSorted
                            break;
                        case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL_MULTI_SORTED:
                            //findAllMultiSorted
                            break;
                        case RealmQuery.ArgumentsHolder.TYPE_FIND_FIRST:
                            //findfirst
                            long rowPointer = TableQuery.nativeFindWithHandover(sharedGroup.getNativePointer(),
                                    entry.query.handoverQueryPtr, 0);
                            updatedObject.put(entry.realmObject, rowPointer);
                            break;
                        default:
                            throw new IllegalArgumentException("Query mode " + entry.query.arguments + " not supported");
                    }

                    // update the pointer with the new exported tableview
                    // realm accessed from another thread (maybe try the weak reference)


                }
            }

            // TODO: should group result & send one call to Handler
            if(!isTaskCancelled()) {
                Handler handler = callerThread.get();
                if (handler != null && handler.getLooper().getThread().isAlive()) {
                    Result result = null;
                    if (!updatedQueries.isEmpty()) {
                        result = Result.newRealmResults(updatedQueries, sharedGroup.getVersion());
                    } else {
                        result = Result.newRealmObject(updatedObject, sharedGroup.getVersion());
                    }

                    Message message = handler.obtainMessage(notifySignal, result);
                    handler.sendMessage(message);
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();

        } finally {
//            if (realm != null) {
//                realm.close();
//            }
            if (sharedGroup != null) {
                sharedGroup.close();
            }
        }
    }

    private boolean isTaskCancelled () {
        // no point continuing if the caller thread was stopped or this thread was interrupted
        return Thread.currentThread().isInterrupted() || callerThread.isEnqueued();
    }

    static class Result {
        public IdentityHashMap<WeakReference<RealmResults<?>>, Long> updatedTableViews;
        public IdentityHashMap<WeakReference<RealmObject>, Long> updatedRow;


        // communicate the version used to update the queries
        // the caller thread need to advance to this version (may not be the latest version yet)
        // in order to import without (handover mismatch) the exported tableView
        public SharedGroup.VersionID versionID;

        public static Result newRealmResults (IdentityHashMap<WeakReference<RealmResults<?>>, Long> updatedTableViews, SharedGroup.VersionID versionID) {
            Result result = new Result();
            result.updatedTableViews = updatedTableViews;
            result.versionID = versionID;
            result.updatedRow = null;

            return result;
        }

        public static Result newRealmObject (IdentityHashMap<WeakReference<RealmObject>, Long> updatedRow, SharedGroup.VersionID versionID) {
            Result result = new Result();
            result.updatedTableViews = null;
            result.versionID = versionID;
            result.updatedRow = updatedRow;

            return result;
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
        WeakReference<RealmResults<?>> realmResults;
        WeakReference<RealmObject> realmObject;
        UpdateQuery query;

        public static Entry newRealmResultsEntry (WeakReference<RealmResults<?>> realmResults, UpdateQuery query) {
            Entry entry = new Entry();
            entry.realmResults = realmResults;
            entry.query = query;
            entry.realmObject = null;
            return entry;
        }

        public static Entry newRealmObjectEntry (WeakReference<RealmObject> realmObject, UpdateQuery query) {
            Entry entry = new Entry();
            entry.realmResults = null;
            entry.query = query;
            entry.realmObject = realmObject;
            return entry;
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
