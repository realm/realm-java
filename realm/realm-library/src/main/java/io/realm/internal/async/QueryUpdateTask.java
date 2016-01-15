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

import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.log.RealmLog;

/**
 * Manage the update of async queries.
 */
public class QueryUpdateTask implements Runnable {
    // true if updating RealmResults, false if updating RealmObject, can't mix both
    // the builder pattern will prevent this.
    private final static int MODE_UPDATE_REALM_RESULTS = 0;
    private final static int MODE_UPDATE_REALM_OBJECT = 1;
    private final int updateMode;

    private RealmConfiguration realmConfiguration;
    private List<Builder.QueryEntry> realmResultsEntries;
    private Builder.QueryEntry realmObjectEntry;
    private WeakReference<Handler> callerHandler;
    private int message;

    private QueryUpdateTask (int mode,
                             RealmConfiguration realmConfiguration,
                             List<Builder.QueryEntry> listOfRealmResults,
                             Builder.QueryEntry realmObject,
                             WeakReference<Handler> handler,
                             int message) {
        this.updateMode = mode;
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
                    SharedGroup.IMPLICIT_TRANSACTION,
                    realmConfiguration.getDurability(),
                    realmConfiguration.getEncryptionKey());

            Result result;
            boolean updateSuccessful = false;
            if (updateMode == MODE_UPDATE_REALM_RESULTS) {
                result = Result.newRealmResultsResponse();
                AlignedQueriesParameters alignedParameters = prepareQueriesParameters();
                long[] handoverTableViewPointer = TableQuery.nativeBatchUpdateQueries(sharedGroup.getNativePointer(),
                        sharedGroup.getNativeReplicationPointer(),
                        alignedParameters.handoverQueries,
                        alignedParameters.queriesParameters,
                        alignedParameters.multiSortColumnIndices,
                        alignedParameters.multiSortOrder);
                swapPointers(result, handoverTableViewPointer);
                updateSuccessful = true;
                result.versionID = sharedGroup.getVersion();

            } else {
                result = Result.newRealmObjectResponse();
                updateSuccessful = updateRealmObjectQuery(sharedGroup, result);
                result.versionID = sharedGroup.getVersion();
            }

            Handler handler = callerHandler.get();
            if (updateSuccessful && !isTaskCancelled() && isAliveHandler(handler)) {
                handler.obtainMessage(message, result).sendToTarget();
            }

        } catch (Exception e) {
            RealmLog.e(e.getMessage(), e);

        } finally {
            if (sharedGroup != null) {
                sharedGroup.close();
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

    private boolean updateRealmObjectQuery(SharedGroup sharedGroup, Result result) {
        if (!isTaskCancelled()) {
            switch (realmObjectEntry.queryArguments.type) {
                case ArgumentsHolder.TYPE_FIND_FIRST: {
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
        public IdentityHashMap<WeakReference<RealmResults<? extends RealmObject>>, Long> updatedTableViews;
        public IdentityHashMap<WeakReference<RealmObject>, Long> updatedRow;
        public SharedGroup.VersionID versionID;

        public static Result newRealmResultsResponse() {
            Result result = new Result();
            result.updatedTableViews = new IdentityHashMap<WeakReference<RealmResults<?>>, Long>(1);
            return result;
        }

        public static Result newRealmObjectResponse() {
            Result result = new Result();
            result.updatedRow = new IdentityHashMap<WeakReference<RealmObject>, Long>(1);
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
            RealmResultsQueryStep add(WeakReference<RealmResults<? extends RealmObject>> weakReference,
                                          long handoverQueryPointer,
                                          ArgumentsHolder queryArguments);
            HandlerStep addObject(WeakReference<? extends RealmObject> weakReference,
                                  long handoverQueryPointer,
                                  ArgumentsHolder queryArguments);// can only update 1 element
        }

        public interface RealmResultsQueryStep {
            RealmResultsQueryStep add(WeakReference<RealmResults<? extends RealmObject>> weakReference,
                                          long handoverQueryPointer,
                                          ArgumentsHolder queryArguments);
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
            private List<QueryEntry> realmResultsEntries;
            private QueryEntry realmObjectEntry;
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
                                             ArgumentsHolder queryArguments) {
                if (this.realmResultsEntries == null) {
                    this.realmResultsEntries = new ArrayList<QueryEntry>(1);
                }
                this.realmResultsEntries.add(new QueryEntry(weakReference, handoverQueryPointer, queryArguments));
                return this;
            }

            @Override
            public HandlerStep addObject(WeakReference<? extends RealmObject> weakReference,
                                         long handoverQueryPointer,
                                         ArgumentsHolder queryArguments) {
                realmObjectEntry =
                        new QueryEntry(weakReference, handoverQueryPointer, queryArguments);
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
                return new QueryUpdateTask(
                        (realmResultsEntries != null) ? MODE_UPDATE_REALM_RESULTS : MODE_UPDATE_REALM_OBJECT,
                        realmConfiguration,
                        realmResultsEntries,
                        realmObjectEntry,
                        callerHandler,
                        message);
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
