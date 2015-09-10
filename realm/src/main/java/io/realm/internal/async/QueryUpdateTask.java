package io.realm.internal.async;

import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.log.RealmLog;

/**
 * Created by Nabil on 02/09/15.
 */
public class QueryUpdateTask implements Runnable {
    // true if updating RealmResults, false if updating RealmObject, can't mix both
    // the builder pattern will prevent this.
    private boolean isUpdatingRealmResults;
    private RealmConfiguration realmConfiguration;
    private List<Builder.QueryEntry<RealmResults<?>>> realmResultsEntries;
    private Builder.QueryEntry<RealmObject> realmObjectEntry;
    private WeakReference<Handler> callerHandler;
    private int message;

    private QueryUpdateTask (boolean isUpdatingRealmResults,
                             RealmConfiguration realmConfiguration,
                             List<Builder.QueryEntry<RealmResults<?>>> listOfRealmResults,
                             Builder.QueryEntry<RealmObject> realmObject,
                             WeakReference<Handler> handler,
                             int message) {
        this.isUpdatingRealmResults = isUpdatingRealmResults;
        this.realmConfiguration = realmConfiguration;
        this.realmResultsEntries = listOfRealmResults;
        this.realmObjectEntry = realmObject;
        this.callerHandler = handler;
        this.message = message;
    }

    public static Builder.RealmConfigurationStep newBuilder() {
        return new Builder.Steps();
    }

    @Override
    public void run() {
        SharedGroup sharedGroup = null;
        try {
            sharedGroup = new SharedGroup(realmConfiguration.getPath(),
                    true, realmConfiguration.getDurability(),
                    realmConfiguration.getEncryptionKey());

            Result result;
            boolean updateSuccessful = false;
            if (isUpdatingRealmResults) {
                result = Result.newRealmResults();
                updateSuccessful = updateRealmResultsQueries(sharedGroup, result);
                result.versionID = sharedGroup.getVersion();

            } else {
                result = Result.newRealmObject();
                updateSuccessful = updateRealmObjectQuery(sharedGroup, result);
                result.versionID = sharedGroup.getVersion();
            }

            Handler handler = callerHandler.get();
            if (updateSuccessful && !isTaskCancelled() && isAliveHandler(handler)) {
                handler.obtainMessage(message, result).sendToTarget();
            }

        } catch (Exception e) {
            RealmLog.e(e.getMessage());
            e.fillInStackTrace();

        } finally {
            if (sharedGroup != null) {
                sharedGroup.close();
            }
        }
    }

    private boolean updateRealmResultsQueries(SharedGroup sharedGroup, Result result) {
        for (Builder.QueryEntry<RealmResults<?>>  queryEntry : realmResultsEntries) {
            if (!isTaskCancelled()) {
                switch (queryEntry.queryArguments.type) {
                    case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL: {
                        long handoverTableViewPointer = TableQuery.nativeFindAllWithHandover
                                (sharedGroup.getNativePointer(),
                                        sharedGroup.getNativeReplicationPointer(),
                                        queryEntry.handoverQueryPointer, 0, Table.INFINITE, Table.INFINITE);
                        result.updatedTableViews.put(queryEntry.element, handoverTableViewPointer);
                        // invalidate the handover query pointer, in case this task is cancelled
                        // we will not try to close/delete a consumed pointer
                        queryEntry.handoverQueryPointer = 0L;
                        break;
                    }
                    case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL_SORTED: {
                        // TODO calling statically the native method willnot trigger executeDelayedDisposal
                        //      make the java method static
                        long handoverTableViewPointer = TableQuery.nativeFindAllSortedWithHandover(
                                sharedGroup.getNativePointer(),
                                sharedGroup.getNativeReplicationPointer(),
                                queryEntry.handoverQueryPointer,
                                0, Table.INFINITE, Table.INFINITE,
                                queryEntry.queryArguments.columnIndex,
                                queryEntry.queryArguments.ascending);

                        result.updatedTableViews.put(queryEntry.element, handoverTableViewPointer);
                        queryEntry.handoverQueryPointer = 0L;
                        break;
                    }
                    case RealmQuery.ArgumentsHolder.TYPE_FIND_ALL_MULTI_SORTED:
                        long handoverTableViewPointer = TableQuery.nativeFindAllMultiSortedWithHandover(
                                sharedGroup.getNativePointer(),
                                sharedGroup.getNativeReplicationPointer(),
                                queryEntry.handoverQueryPointer,
                                0, Table.INFINITE, Table.INFINITE,
                                queryEntry.queryArguments.columnIndices,
                                queryEntry.queryArguments.ascendings);

                        result.updatedTableViews.put(queryEntry.element, handoverTableViewPointer);
                        queryEntry.handoverQueryPointer = 0L;
                        break;
                    default:
                        throw new IllegalArgumentException("Query mode " + queryEntry.queryArguments.type + " not supported");
                }
            } else {
                for (Long handoverQueryPointer : result.updatedTableViews.values()) {
                    if (handoverQueryPointer != 0) {
                        TableQuery.nativeCloseQueryHandover(handoverQueryPointer);
                    }
                }
                return false;
            }
        }
        return true;
    }

    private boolean updateRealmObjectQuery(SharedGroup sharedGroup, Result result) {
        if (!isTaskCancelled()) {
            switch (realmObjectEntry.queryArguments.type) {
                case RealmQuery.ArgumentsHolder.TYPE_FIND_FIRST: {
                    long handoverRowPointer = TableQuery.
                            nativeFindWithHandover(sharedGroup.getNativePointer(),
                                    sharedGroup.getNativeReplicationPointer(),
                                    realmObjectEntry.handoverQueryPointer, 0);
                    result.updatedRow.put(realmObjectEntry.element, handoverRowPointer);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Query mode " + realmObjectEntry.queryArguments.type + " not supported");
            }
        } else {
            TableQuery.nativeCloseQueryHandover(realmObjectEntry.handoverQueryPointer);
            return false;
        }
        return true;
    }

    private boolean isTaskCancelled() {
        // no point continuing if the caller thread was stopped or this thread was interrupted
        return Thread.currentThread().isInterrupted();
    }

    private boolean isAliveHandler(Handler handler) {
        return handler != null && handler.getLooper().getThread().isAlive();
    }

    // result of the async query
    public static class Result {
        public IdentityHashMap<WeakReference<RealmResults<?>>, Long> updatedTableViews;
        public IdentityHashMap<WeakReference<RealmObject>, Long> updatedRow;
        //TODO maybe inline parameters or updateRow

        // communicate the version used to update the queries
        // the caller thread need to advance to this version (may not be the latest version yet)
        // in order to import without (handover mismatch) the exported tableView
        public SharedGroup.VersionID versionID;

        public static Result newRealmResults () {
            Result result = new Result();
            result.updatedTableViews = new IdentityHashMap<WeakReference<RealmResults<?>>, Long>(1);
            return result;
        }

        public static Result newRealmObject () {
            Result result = new Result();
            result.updatedRow = new IdentityHashMap<WeakReference<RealmObject>, Long>(1);
            return result;
        }
    }

    // builder pattern

    /**
     * Example of call
     * QueryUpdateTask task = QueryUpdateTask.newBuilder()
     .realmConfiguration(null, null)
     .add(null, 0, null)
     .add(null, 0, null)
     .sendToHandler(null, 0)
     .build();

     QueryUpdateTask task2 = QueryUpdateTask.newBuilder()
     .realmConfiguration(null, null)
     .addObject(null, 0, null)
     .sendToHandler(null, 0)
     .build();
     */
    public static class Builder {
        public interface RealmConfigurationStep {
            UpdateQueryStep realmConfiguration (RealmConfiguration realmConfiguration);
        }

        public interface UpdateQueryStep {
            RealmResultsQueryStep add(WeakReference<RealmResults<?>> weakReference,
                                          long handoverQueryPointer,
                                          RealmQuery.ArgumentsHolder queryArguments);
            HandlerStep addObject(WeakReference<RealmObject> weakReference,
                                  long handoverQueryPointer,
                                  RealmQuery.ArgumentsHolder queryArguments);// can only update 1 element
        }

        public interface RealmResultsQueryStep {
            RealmResultsQueryStep add(WeakReference<RealmResults<?>> weakReference,
                                          long handoverQueryPointer,
                                          RealmQuery.ArgumentsHolder queryArguments);
            BuilderStep sendToHandler(Handler handler, int message);
        }

        public interface HandlerStep {
            BuilderStep sendToHandler (Handler handler, int message);
        }

        public interface BuilderStep {
            QueryUpdateTask build();
        }

        private static class Steps implements RealmConfigurationStep, UpdateQueryStep, RealmResultsQueryStep, HandlerStep, BuilderStep {
            private RealmConfiguration realmConfiguration;
            private List<QueryEntry<RealmResults<?>>> realmResultsEntries;
            private QueryEntry<RealmObject> realmObjectEntry;
            private WeakReference<Handler> callerHandler;
            private int message;

            @Override
            public UpdateQueryStep realmConfiguration(RealmConfiguration realmConfiguration) {
                this.realmConfiguration = realmConfiguration;
                return this;
            }

            @Override
            public RealmResultsQueryStep add(WeakReference<RealmResults<?>> weakReference,
                                             long handoverQueryPointer,
                                             RealmQuery.ArgumentsHolder queryArguments) {
                if (this.realmResultsEntries == null) {
                    this.realmResultsEntries = new ArrayList<QueryEntry<RealmResults<?>>>(1);
                }
                this.realmResultsEntries.add(
                        new QueryEntry<RealmResults<?>>(weakReference, handoverQueryPointer, queryArguments));
                return this;
            }

            @Override
            public HandlerStep addObject(WeakReference<RealmObject> weakReference,
                                         long handoverQueryPointer,
                                         RealmQuery.ArgumentsHolder queryArguments) {
                realmObjectEntry =
                        new QueryEntry<RealmObject>(weakReference, handoverQueryPointer, queryArguments);
                return this;
            }

            @Override
            public BuilderStep sendToHandler(Handler handler, int message) {
                this.callerHandler = new WeakReference<Handler>(handler);
                this.message = message;
                return this;
            }

            @Override
            public QueryUpdateTask build() {
                return new QueryUpdateTask(realmResultsEntries != null,
                        realmConfiguration,
                        realmResultsEntries,
                        realmObjectEntry,
                        callerHandler,
                        message);
            }
        }

        private static class QueryEntry<T> {
            final WeakReference<T> element;
            long handoverQueryPointer;
            final RealmQuery.ArgumentsHolder queryArguments;

            private QueryEntry(WeakReference<T> element, long handoverQueryPointer, RealmQuery.ArgumentsHolder queryArguments) {
                this.element = element;
                this.handoverQueryPointer = handoverQueryPointer;
                this.queryArguments = queryArguments;
            }
        }
    }
}
