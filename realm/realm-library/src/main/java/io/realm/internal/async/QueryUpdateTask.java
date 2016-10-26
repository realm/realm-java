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

package io.realm.internal.async;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.internal.RealmNotifier;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.log.RealmLog;

/**
 * Manages the update of async queries.
 */
public class QueryUpdateTask implements Runnable {

    public enum NotifyEvent {
        COMPLETE_ASYNC_RESULTS,
        COMPLETE_ASYNC_OBJECT,
        COMPLETE_UPDATE_ASYNC_QUERIES,
        THROW_BACKGROUND_EXCEPTION,
    }

    // true if updating RealmResults, false if updating RealmObject, can't mix both
    // the builder pattern will prevent this.
    private final static int MODE_UPDATE_REALM_RESULTS = 0;
    private final static int MODE_UPDATE_REALM_OBJECT = 1;
    private final int updateMode;

    private RealmConfiguration realmConfiguration;
    private List<Builder.QueryEntry> realmResultsEntries;
    private Builder.QueryEntry realmObjectEntry;
    private WeakReference<RealmNotifier> callerNotifier;
    private NotifyEvent event;

    private QueryUpdateTask (int mode,
                             RealmConfiguration realmConfiguration,
                             List<Builder.QueryEntry> listOfRealmResults,
                             Builder.QueryEntry realmObject,
                             WeakReference<RealmNotifier> notifier,
                             NotifyEvent event) {
        this.updateMode = mode;
        this.realmConfiguration = realmConfiguration;
        this.realmResultsEntries = listOfRealmResults;
        this.realmObjectEntry = realmObject;
        this.callerNotifier = notifier;
        this.event = event;
    }

    public static Builder.RealmConfigurationStep newBuilder() {
        return new Builder.Steps();
    }

    @Override
    public void run() {
        SharedRealm sharedRealm = null;
        try {
            sharedRealm = SharedRealm.getInstance(realmConfiguration);

            Result result;
            boolean updateSuccessful;
            if (updateMode == MODE_UPDATE_REALM_RESULTS) {
                result = Result.newRealmResultsResponse();
                AlignedQueriesParameters alignedParameters = prepareQueriesParameters();
                long[] handoverTableViewPointer = TableQuery.batchUpdateQueries(sharedRealm,
                        alignedParameters.handoverQueries,
                        alignedParameters.queriesParameters,
                        alignedParameters.multiSortColumnIndices,
                        alignedParameters.multiSortOrder);
                swapPointers(result, handoverTableViewPointer);
                updateSuccessful = true;
                result.versionID = sharedRealm.getVersionID();

            } else {
                result = Result.newRealmObjectResponse();
                updateSuccessful = updateRealmObjectQuery(sharedRealm, result);
                result.versionID = sharedRealm.getVersionID();
            }

            RealmNotifier notifier = callerNotifier.get();
            if (updateSuccessful && !isTaskCancelled() && notifier != null) {
                switch (event) {
                    case COMPLETE_ASYNC_RESULTS:
                        notifier.completeAsyncResults(result);
                        break;
                    case COMPLETE_ASYNC_OBJECT:
                        notifier.completeAsyncObject(result);
                        break;
                    case COMPLETE_UPDATE_ASYNC_QUERIES:
                        notifier.completeUpdateAsyncQueries(result);
                        break;
                    default:
                        throw new IllegalStateException(String.format("%s is not handled here.", event));
                }
            }

        } catch (BadVersionException e) {
            // In some rare race conditions, this can happen. In that case, just ignore the error.
            RealmLog.debug("Query update task could not complete due to a BadVersionException. " +
                    "Retry is scheduled by a REALM_CHANGED event.");

        } catch (Throwable e) {
            RealmLog.error(e);
            RealmNotifier notifier = callerNotifier.get();
            if (notifier!= null) {
                notifier.throwBackgroundException(e);
            }

        } finally {
            if (sharedRealm != null) {
                sharedRealm.close();
            }
        }
    }

    private AlignedQueriesParameters prepareQueriesParameters() {
        long[] handoverQueries = new long[realmResultsEntries.size()];
        long[][] queriesParameters = new long[realmResultsEntries.size()][6];
        long[][] multiSortColumnIndices = new long[realmResultsEntries.size()][];
        boolean[][] multiSortOrder = new boolean[realmResultsEntries.size()][];

        int i = 0;
        for (Builder.QueryEntry  queryEntry : realmResultsEntries) {
            switch (queryEntry.queryArguments.type) {
                case ArgumentsHolder.TYPE_FIND_ALL: {
                    handoverQueries[i] = queryEntry.handoverQueryPointer;
                    queriesParameters[i][0] = ArgumentsHolder.TYPE_FIND_ALL;
                    queriesParameters[i][1] = 0;
                    queriesParameters[i][2] = Table.INFINITE;
                    queriesParameters[i][3] = Table.INFINITE;
                    break;
                }
                case ArgumentsHolder.TYPE_DISTINCT: {
                    handoverQueries[i] = queryEntry.handoverQueryPointer;
                    queriesParameters[i][0] = ArgumentsHolder.TYPE_DISTINCT;
                    queriesParameters[i][1] = queryEntry.queryArguments.columnIndex;
                    break;
                }
                case ArgumentsHolder.TYPE_FIND_ALL_SORTED: {
                    handoverQueries[i] = queryEntry.handoverQueryPointer;
                    queriesParameters[i][0] = ArgumentsHolder.TYPE_FIND_ALL_SORTED;
                    queriesParameters[i][1] = 0;
                    queriesParameters[i][2] = Table.INFINITE;
                    queriesParameters[i][3] = Table.INFINITE;
                    queriesParameters[i][4] = queryEntry.queryArguments.columnIndex;
                    queriesParameters[i][5] = (queryEntry.queryArguments.sortOrder.getValue()) ? 1 : 0;
                    break;
                }
                case ArgumentsHolder.TYPE_FIND_ALL_MULTI_SORTED:
                    handoverQueries[i] = queryEntry.handoverQueryPointer;
                    queriesParameters[i][0] = ArgumentsHolder.TYPE_FIND_ALL_MULTI_SORTED;
                    queriesParameters[i][1] = 0;
                    queriesParameters[i][2] = Table.INFINITE;
                    queriesParameters[i][3] = Table.INFINITE;
                    multiSortColumnIndices[i] = queryEntry.queryArguments.columnIndices;
                    multiSortOrder[i] = TableQuery.getNativeSortOrderValues(queryEntry.queryArguments.sortOrders);
                    break;
                default:
                    throw new IllegalArgumentException("Query mode " + queryEntry.queryArguments.type + " not supported");
            }
            i++;
        }
        AlignedQueriesParameters alignedParameters = new AlignedQueriesParameters();

        alignedParameters.handoverQueries = handoverQueries;
        alignedParameters.multiSortColumnIndices = multiSortColumnIndices;
        alignedParameters.multiSortOrder = multiSortOrder;
        alignedParameters.queriesParameters = queriesParameters;

        return alignedParameters;
    }

    private void swapPointers(Result result, long[] handoverTableViewPointer) {
        int i = 0;
        for (Builder.QueryEntry  queryEntry : realmResultsEntries) {
            result.updatedTableViews.put(queryEntry.element, handoverTableViewPointer[i++]);
        }
    }

    private boolean updateRealmObjectQuery(SharedRealm sharedRealm, Result result) {
        if (!isTaskCancelled()) {
            switch (realmObjectEntry.queryArguments.type) {
                case ArgumentsHolder.TYPE_FIND_FIRST: {
                    long handoverRowPointer = TableQuery.findWithHandover(sharedRealm,
                                    realmObjectEntry.handoverQueryPointer);
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

    // result of the async query
    public static class Result {
        public IdentityHashMap<WeakReference<RealmResults<? extends RealmModel>>, Long> updatedTableViews;
        public IdentityHashMap<WeakReference<RealmObjectProxy>, Long> updatedRow;
        public SharedRealm.VersionID versionID;

        public static Result newRealmResultsResponse() {
            Result result = new Result();
            result.updatedTableViews = new IdentityHashMap<WeakReference<RealmResults<? extends RealmModel>>, Long>(1);
            return result;
        }

        public static Result newRealmObjectResponse() {
            Result result = new Result();
            result.updatedRow = new IdentityHashMap<WeakReference<RealmObjectProxy>, Long>(1);
            return result;
        }
    }
    private static class AlignedQueriesParameters {
        long[] handoverQueries;
        long[][] queriesParameters;
        long[][] multiSortColumnIndices;
        boolean[][] multiSortOrder;
    }
    /*
      This uses the step builder pattern to guide the caller throughout the creation of the instance
      http://rdafbn.blogspot.ie/2012/07/step-builder-pattern_28.html
      Example of call:
      QueryUpdateTask task = QueryUpdateTask.newBuilder()
         .realmConfiguration(null, null)
         .add(null, 0, null)
         .add(null, 0, null)
         .sendToNotifier(null, 0)
         .build();

     QueryUpdateTask task2 = QueryUpdateTask.newBuilder()
         .realmConfiguration(null, null)
         .addObject(null, 0, null)
         .sendToNotifier(null, 0)
         .build();
     */
    public static class Builder {
        public interface RealmConfigurationStep {
            UpdateQueryStep realmConfiguration (RealmConfiguration realmConfiguration);
        }

        public interface UpdateQueryStep {
            RealmResultsQueryStep add(WeakReference<RealmResults<? extends RealmModel>> weakReference,
                                          long handoverQueryPointer,
                                          ArgumentsHolder queryArguments);
            HandlerStep addObject(WeakReference<? extends RealmModel> weakReference,
                                  long handoverQueryPointer,
                                  ArgumentsHolder queryArguments);// can only update 1 element
        }

        public interface RealmResultsQueryStep {
            RealmResultsQueryStep add(WeakReference<RealmResults<? extends RealmModel>> weakReference,
                                          long handoverQueryPointer,
                                          ArgumentsHolder queryArguments);
            BuilderStep sendToNotifier(RealmNotifier notifier, NotifyEvent event);
        }

        public interface HandlerStep {
            BuilderStep sendToNotifier(RealmNotifier notifier, NotifyEvent event);
        }

        public interface BuilderStep {
            QueryUpdateTask build();
        }

        private static class Steps implements RealmConfigurationStep, UpdateQueryStep, RealmResultsQueryStep, HandlerStep, BuilderStep {
            private RealmConfiguration realmConfiguration;
            private List<QueryEntry> realmResultsEntries;
            private QueryEntry realmObjectEntry;
            private WeakReference<RealmNotifier> callerNotifier;
            private NotifyEvent event;

            @Override
            public UpdateQueryStep realmConfiguration(RealmConfiguration realmConfiguration) {
                this.realmConfiguration = realmConfiguration;
                return this;
            }

            @Override
            public RealmResultsQueryStep add(WeakReference<RealmResults<?>> weakReference,
                                             long handoverQueryPointer,
                                             ArgumentsHolder queryArguments) {
                if (this.realmResultsEntries == null) {
                    this.realmResultsEntries = new ArrayList<QueryEntry>(1);
                }
                this.realmResultsEntries.add(new QueryEntry(weakReference, handoverQueryPointer, queryArguments));
                return this;
            }

            @Override
            public HandlerStep addObject(WeakReference<? extends RealmModel> weakReference,
                                         long handoverQueryPointer,
                                         ArgumentsHolder queryArguments) {
                realmObjectEntry =
                        new QueryEntry(weakReference, handoverQueryPointer, queryArguments);
                return this;
            }

            @Override
            public BuilderStep sendToNotifier(RealmNotifier notifier, NotifyEvent event) {
                this.callerNotifier = new WeakReference<RealmNotifier>(notifier);
                this.event = event;
                return this;
            }

            @Override
            public QueryUpdateTask build() {
                return new QueryUpdateTask(
                        (realmResultsEntries != null) ? MODE_UPDATE_REALM_RESULTS : MODE_UPDATE_REALM_OBJECT,
                        realmConfiguration,
                        realmResultsEntries,
                        realmObjectEntry,
                        callerNotifier,
                        event);
            }
        }

        private static class QueryEntry {
            final WeakReference element;
            long handoverQueryPointer;
            final ArgumentsHolder queryArguments;

            private QueryEntry(WeakReference element, long handoverQueryPointer, ArgumentsHolder queryArguments) {
                this.element = element;
                this.handoverQueryPointer = handoverQueryPointer;
                this.queryArguments = queryArguments;
            }
        }
    }
}
